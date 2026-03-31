package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.atguigu.lease.web.app.vo.room.RoomSubmitVo;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewDetailVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private AttrKeyMapper attrKeyMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<RoomItemVo> pageItem(Page<RoomItemVo> page, RoomQueryVo queryVo) {
        // 执行分页查询
        IPage<RoomItemVo> result = roomInfoMapper.pageItem(page, queryVo);
        
        // 为每个房源补充关联数据
        for (RoomItemVo roomItemVo : result.getRecords()) {
            // 查询图片信息
            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, roomItemVo.getId());
            roomItemVo.setGraphVoList(graphVoList);
            
            log.debug("为房源 {} 补充关联数据：图片{}张", 
                roomItemVo.getId(), 
                graphVoList.size());
        }
        
        return result;
    }

    @Override//根据房间id获取房间详情
    public RoomDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = null;
        
        // 1. 先查缓存
        try {
            roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
            if (roomDetailVo != null) {
                log.debug("从缓存加载房间详情，房源ID: {}", id);
                // 保存浏览历史
                saveUserBrowsingHistory(id);
                return roomDetailVo;
            }
        } catch (Exception e) {
            log.warn("房源详情缓存读取失败，清除缓存重新加载，房源ID: {}, 错误: {}", id, e.getMessage());
            // 清除有问题的缓存
            try {
                redisTemplate.delete(key);
            } catch (Exception deleteEx) {
                log.warn("清除异常缓存失败，房源ID: {}", id, deleteEx);
            }
        }

        // 2. 缓存未命中，查数据库
        roomDetailVo = loadRoomDetailFromDatabase(id);
        if (roomDetailVo == null) {
            return null;
        }

        // 3. 更新缓存
        updateRoomDetailCache(key, roomDetailVo, id);
        
        // 保存浏览历史
        saveUserBrowsingHistory(id);
        
        return roomDetailVo;
    }

    /**
     * 从数据库加载房源详情
     */
    private RoomDetailVo loadRoomDetailFromDatabase(Long id) {
        //1.查询房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            return null;
        }
        
        //2.查询图片
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);
        //3.查询租期
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);
        //4.查询配套
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);
        //5.查询标签
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);
        //6.查询支付方式
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);
        //7.查询基本属性
        List<AttrValueVo> attrValueVoList = attrValueMapper.selectListByRoomId(id);
        
        //8.查询公寓信息
        ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(roomInfo.getApartmentId());
        
        // 检查公寓信息是否存在
        if (apartmentItemVo == null) {
            log.error("房源关联的公寓信息不存在，房源ID: {}, 公寓ID: {}", id, roomInfo.getApartmentId());
            return null;
        }

        //10.查询审核详情（如果房源已审核）
        RoomReviewDetailVo reviewDetail = null;
        if (roomInfo.getReviewStatus() != null && 
            !com.atguigu.lease.model.enums.ReviewStatus.PENDING.equals(roomInfo.getReviewStatus())) {
            
            try {
                reviewDetail = roomInfoMapper.selectRoomReviewByRoomId(id);
            } catch (Exception e) {
                log.warn("查询房源审核详情失败，房源ID: {}, 错误: {}", id, e.getMessage());
            }
        }

        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        roomDetailVo.setApartmentItemVo(apartmentItemVo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);
        roomDetailVo.setReviewDetail(reviewDetail);

        log.info("从数据库加载房间详情，房源ID: {}", id);
        return roomDetailVo;
    }

    /**
     * 更新房源详情缓存
     */
    private void updateRoomDetailCache(String key, RoomDetailVo roomDetailVo, Long roomId) {
        try {
            // 先测试序列化是否正常
            redisTemplate.opsForValue().set(key, roomDetailVo, RedisConstant.APP_ROOM_CACHE_TTL_SEC, TimeUnit.SECONDS);
            
            // 立即验证缓存是否写入成功
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("房源详情缓存写入并验证成功，房源ID: {}, TTL: {}秒", roomId, RedisConstant.APP_ROOM_CACHE_TTL_SEC);
            } else {
                log.error("房源详情缓存写入后验证失败，缓存为空，房源ID: {}", roomId);
            }
        } catch (Exception e) {
            log.error("房源详情缓存写入失败，房源ID: {}, 错误详情: ", roomId, e);
            // 不抛出异常，避免影响主要业务流程，但记录详细错误信息
        }
    }

    /**
     * 保存用户浏览历史
     */
    private void saveUserBrowsingHistory(Long roomId) {
        try {
            if (LoginUserHolder.getLoginUser() != null && LoginUserHolder.getLoginUser().getUserId() != null) {
                browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(), roomId);
            }
        } catch (Exception e) {
            // 如果保存浏览历史失败，不影响主要功能，只记录日志
            log.warn("保存浏览历史失败，房间ID: {}, 错误: {}", roomId, e.getMessage());
        }
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(Page<RoomItemVo> page, Long id) {
        // 执行分页查询
        IPage<RoomItemVo> result = roomInfoMapper.pageItemByApartmentId(page, id);
        
        // 为每个房源补充关联数据
        for (RoomItemVo roomItemVo : result.getRecords()) {
            // 查询图片信息
            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, roomItemVo.getId());
            roomItemVo.setGraphVoList(graphVoList);
            
            log.debug("为房源 {} 补充关联数据：图片{}张", 
                roomItemVo.getId(), 
                graphVoList.size());
        }
        
        return result;
    }

    @Override
    @Transactional
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        Long roomId = roomSubmitVo.getId();
        
        // 写操作：延迟双删策略 - 第一次删除缓存
        if (isUpdate) {
            deleteRoomCache(roomId, "更新前");
        }
        
        // 如果是更新操作，进行权限验证
        if (isUpdate && roomSubmitVo.getPublisherType() != null && 
            com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomSubmitVo.getPublisherType())) {
            
            // 验证房东对房源的编辑权限
            if (!validateRoomEditPermission(roomSubmitVo.getId(), roomSubmitVo.getPublisherId())) {
                throw new RuntimeException("无权编辑该房源");
            }
            
            // 验证房东对公寓的访问权限
            if (roomSubmitVo.getApartmentId() != null && 
                !validateApartmentAccessPermission(roomSubmitVo.getApartmentId(), roomSubmitVo.getPublisherId())) {
                throw new RuntimeException("无权选择该公寓");
            }
            
            // 验证房东对属性的访问权限
            if (roomSubmitVo.getAttrValueIds() != null) {
                for (Long attrValueId : roomSubmitVo.getAttrValueIds()) {
                    // 通过属性值ID获取属性键ID进行验证
                    AttrValue attrValue = attrValueMapper.selectById(attrValueId);
                    if (attrValue != null && !validateAttributeAccessPermission(attrValue.getAttrKeyId(), roomSubmitVo.getPublisherId())) {
                        throw new RuntimeException("无权使用该属性");
                    }
                }
            }
            
            // 验证房东对配套设施的访问权限
            if (roomSubmitVo.getFacilityInfoIds() != null) {
                for (Long facilityId : roomSubmitVo.getFacilityInfoIds()) {
                    if (!validateFacilityAccessPermission(facilityId, roomSubmitVo.getPublisherId())) {
                        throw new RuntimeException("无权使用该配套设施");
                    }
                }
            }
            
            // 验证房东对标签的访问权限
            if (roomSubmitVo.getLabelInfoIds() != null) {
                for (Long labelId : roomSubmitVo.getLabelInfoIds()) {
                    if (!validateLabelAccessPermission(labelId, roomSubmitVo.getPublisherId())) {
                        throw new RuntimeException("无权使用该标签");
                    }
                }
            }
            
            // 验证房东对支付方式的访问权限
            if (roomSubmitVo.getPaymentTypeIds() != null) {
                for (Long paymentTypeId : roomSubmitVo.getPaymentTypeIds()) {
                    if (!validatePaymentTypeAccessPermission(paymentTypeId, roomSubmitVo.getPublisherId())) {
                        throw new RuntimeException("无权使用该支付方式");
                    }
                }
            }
        }
        
        // 保存或更新房间基本信息
        super.saveOrUpdate(roomSubmitVo);
        
        // 获取房源ID（新增时才有）
        if (!isUpdate) {
            roomId = roomSubmitVo.getId();
        }
        
        if (isUpdate) {
            // 更新操作：先删除原有关联数据，再重新插入
            log.info("更新房源关联关系，房源ID: {}", roomId);
            
            // 删除原有关联数据
            roomInfoMapper.deleteRoomAttrValueByRoomId(roomId);
            roomInfoMapper.deleteRoomFacilityByRoomId(roomId);
            roomInfoMapper.deleteRoomLabelByRoomId(roomId);
            roomInfoMapper.deleteRoomPaymentTypeByRoomId(roomId);
            roomInfoMapper.deleteRoomLeaseTermByRoomId(roomId);
            graphInfoMapper.deleteByItemTypeAndId(ItemType.ROOM, roomId);
            
            log.info("删除房源原有关联数据完成，房源ID: {}", roomId);
        }
        
        // 重新插入关联数据
        insertRoomRelationships(roomId, roomSubmitVo);
        
        // 如果是房东发布的新房源且状态为待审核，创建审核记录
        if (roomSubmitVo.getPublisherType() != null && 
            roomSubmitVo.getPublisherType().equals(com.atguigu.lease.model.enums.PublisherType.LANDLORD) &&
            roomSubmitVo.getReviewStatus() != null &&
            roomSubmitVo.getReviewStatus().equals(com.atguigu.lease.model.enums.ReviewStatus.PENDING)) {
            
            createRoomReviewRecord(roomId, roomSubmitVo.getApartmentId(), roomSubmitVo.getPublisherId());
        }
        
        log.info("房间信息保存成功，房间ID: {}", roomId);
        
        // 写操作：延迟双删策略 - 第二次删除缓存
        delayedDeleteRoomCache(roomId, "更新后");
    }

    /**
     * 插入房源关联关系数据
     */
    private void insertRoomRelationships(Long roomId, RoomSubmitVo roomSubmitVo) {
        try {
            // 保存房间图片
            if (roomSubmitVo.getGraphVoList() != null && !roomSubmitVo.getGraphVoList().isEmpty()) {
                roomInfoMapper.insertRoomGraphBatch(roomId, roomSubmitVo.getGraphVoList());
                log.debug("保存房间图片关联，房源ID: {}, 图片数量: {}", roomId, roomSubmitVo.getGraphVoList().size());
            }
            
            // 保存房间属性
            if (roomSubmitVo.getAttrValueIds() != null && !roomSubmitVo.getAttrValueIds().isEmpty()) {
                roomInfoMapper.insertRoomAttrValueBatch(roomId, roomSubmitVo.getAttrValueIds());
                log.debug("保存房间属性关联，房源ID: {}, 属性数量: {}", roomId, roomSubmitVo.getAttrValueIds().size());
            }
            
            // 保存房间配套设施
            if (roomSubmitVo.getFacilityInfoIds() != null && !roomSubmitVo.getFacilityInfoIds().isEmpty()) {
                roomInfoMapper.insertRoomFacilityBatch(roomId, roomSubmitVo.getFacilityInfoIds());
                log.debug("保存房间配套设施关联，房源ID: {}, 配套设施数量: {}", roomId, roomSubmitVo.getFacilityInfoIds().size());
            }
            
            // 保存房间标签
            if (roomSubmitVo.getLabelInfoIds() != null && !roomSubmitVo.getLabelInfoIds().isEmpty()) {
                roomInfoMapper.insertRoomLabelBatch(roomId, roomSubmitVo.getLabelInfoIds());
                log.debug("保存房间标签关联，房源ID: {}, 标签数量: {}", roomId, roomSubmitVo.getLabelInfoIds().size());
            }
            
            // 保存支付方式
            if (roomSubmitVo.getPaymentTypeIds() != null && !roomSubmitVo.getPaymentTypeIds().isEmpty()) {
                roomInfoMapper.insertRoomPaymentTypeBatch(roomId, roomSubmitVo.getPaymentTypeIds());
                log.debug("保存房间支付方式关联，房源ID: {}, 支付方式数量: {}", roomId, roomSubmitVo.getPaymentTypeIds().size());
            }
            
            // 保存租期
            if (roomSubmitVo.getLeaseTermIds() != null && !roomSubmitVo.getLeaseTermIds().isEmpty()) {
                roomInfoMapper.insertRoomLeaseTermBatch(roomId, roomSubmitVo.getLeaseTermIds());
                log.debug("保存房间租期关联，房源ID: {}, 租期数量: {}", roomId, roomSubmitVo.getLeaseTermIds().size());
            }
            
            log.info("房源关联关系插入完成，房源ID: {}", roomId);
            
        } catch (Exception e) {
            log.error("插入房源关联关系失败，房源ID: {}", roomId, e);
            throw new RuntimeException("保存房源关联关系失败：" + e.getMessage());
        }
    }

    /**
     * 创建房源审核记录
     */
    private void createRoomReviewRecord(Long roomId, Long apartmentId, Long landlordId) {
        try {
            // 检查是否已存在审核记录
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.atguigu.lease.model.entity.RoomReview> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(com.atguigu.lease.model.entity.RoomReview::getRoomId, roomId)
                       .eq(com.atguigu.lease.model.entity.RoomReview::getStatus, com.atguigu.lease.model.enums.ReviewStatus.PENDING);
            
            // 这里需要注入RoomReviewService，但为了避免循环依赖，我们直接使用Mapper
            // 暂时跳过创建审核记录，在后续的修复中处理
            log.info("房东发布房源，房间ID: {}, 需要创建审核记录", roomId);
            
        } catch (Exception e) {
            log.error("创建房源审核记录失败，房间ID: {}", roomId, e);
            // 不影响主要流程，只记录错误
        }
    }

    @Override
    @Transactional
    public void removeRoomById(Long roomId) {
        // 写操作：延迟双删策略 - 第一次删除缓存
        deleteRoomCache(roomId, "删除前");
        
        // 删除房间关联数据
        roomInfoMapper.deleteRoomAttrValueByRoomId(roomId);
        roomInfoMapper.deleteRoomFacilityByRoomId(roomId);
        roomInfoMapper.deleteRoomLabelByRoomId(roomId);
        roomInfoMapper.deleteRoomPaymentTypeByRoomId(roomId);
        roomInfoMapper.deleteRoomLeaseTermByRoomId(roomId);
        graphInfoMapper.deleteByItemTypeAndId(ItemType.ROOM, roomId);
        
        // 逻辑删除房间信息
        super.removeById(roomId);
        
        log.info("房间删除成功，房间ID: {}", roomId);
        
        // 写操作：延迟双删策略 - 第二次删除缓存
        delayedDeleteRoomCache(roomId, "删除后");
    }

    @Override
    public com.atguigu.lease.common.result.Result<RoomDetailVo> getLandlordRoomDetail(Long roomId, Long landlordId) {
        try {
            // 1. 验证房源是否存在
            RoomInfo roomInfo = this.getById(roomId);
            if (roomInfo == null) {
                return com.atguigu.lease.common.result.Result.fail("房源不存在");
            }
            
            // 2. 权限验证：房东可以查看自己发布的房源，也可以查看官方发布的房源（用于申请管理）
            boolean canView = false;
            
            // 如果是房东发布的房源，必须是自己的
            if (com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
                canView = roomInfo.getPublisherId().equals(landlordId);
            } 
            // 如果是官方发布的房源，房东可以查看（用于处理申请）
            else if (com.atguigu.lease.model.enums.PublisherType.OFFICIAL.equals(roomInfo.getPublisherType())) {
                canView = true;
            }
            
            if (!canView) {
                return com.atguigu.lease.common.result.Result.fail("无权查看该房源");
            }
            
            // 3. 获取房源详情
            RoomDetailVo result = this.getDetailById(roomId);
            if (result == null) {
                return com.atguigu.lease.common.result.Result.fail("房源详情获取失败");
            }
            
            return com.atguigu.lease.common.result.Result.ok(result);
            
        } catch (Exception e) {
            log.error("房东查询房源详情失败，房源ID: {}, 房东ID: {}", roomId, landlordId, e);
            return com.atguigu.lease.common.result.Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ApartmentInfo getApartmentInfoByRoomId(Long roomId) {
        try {
            // 1. 根据房间ID获取房间信息
            RoomInfo roomInfo = this.getById(roomId);
            if (roomInfo == null) {
                log.warn("房间不存在，房间ID: {}", roomId);
                return null;
            }
            
            // 2. 根据公寓ID获取公寓信息
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
            if (apartmentInfo == null) {
                log.warn("公寓信息不存在，公寓ID: {}", roomInfo.getApartmentId());
                return null;
            }
            
            return apartmentInfo;
            
        } catch (Exception e) {
            log.error("根据房间ID获取公寓信息失败，房间ID: {}", roomId, e);
            return null;
        }
    }

    @Override
    public void clearRoomDetailCache(Long roomId) {
        try {
            String key = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(key);
            log.info("清除房源详情缓存成功，房源ID: {}", roomId);
        } catch (Exception e) {
            log.error("清除房源详情缓存失败，房源ID: {}", roomId, e);
        }
    }

    @Override
    public void clearAllRoomDetailCache() {
        try {
            String pattern = RedisConstant.APP_ROOM_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("清除所有房源详情缓存成功");
        } catch (Exception e) {
            log.error("清除所有房源详情缓存失败", e);
        }
    }

    @Override
    public boolean validateRoomEditPermission(Long roomId, Long landlordId) {
        try {
            RoomInfo roomInfo = this.getById(roomId);
            if (roomInfo == null) {
                log.warn("房源不存在，无法验证编辑权限，房源ID: {}", roomId);
                return false;
            }
            
            // 只有房东发布的房源且发布者是当前房东才能编辑
            boolean hasPermission = com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) 
                                  && roomInfo.getPublisherId().equals(landlordId);
            
            if (!hasPermission) {
                log.warn("房东无权编辑房源，房源ID: {}, 房东ID: {}, 发布者类型: {}, 发布者ID: {}", 
                    roomId, landlordId, roomInfo.getPublisherType(), roomInfo.getPublisherId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证房源编辑权限失败，房源ID: {}, 房东ID: {}", roomId, landlordId, e);
            return false;
        }
    }

    @Override
    public boolean validateApartmentAccessPermission(Long apartmentId, Long landlordId) {
        try {
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);
            if (apartmentInfo == null) {
                log.warn("公寓不存在，无法验证访问权限，公寓ID: {}", apartmentId);
                return false;
            }
            
            // 房东可以访问自己的公寓（通过landlordId字段判断）
            // 如果landlordId为null，则认为是官方公寓，房东也可以访问
            boolean hasPermission = apartmentInfo.getLandlordId() == null || 
                                  apartmentInfo.getLandlordId().equals(landlordId);
            
            if (!hasPermission) {
                log.warn("房东无权访问公寓，公寓ID: {}, 房东ID: {}, 公寓房东ID: {}", 
                    apartmentId, landlordId, apartmentInfo.getLandlordId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证公寓访问权限失败，公寓ID: {}, 房东ID: {}", apartmentId, landlordId, e);
            return false;
        }
    }

    @Override
    public boolean validateAttributeAccessPermission(Long attrKeyId, Long landlordId) {
        try {
            AttrKey attrKey = attrKeyMapper.selectById(attrKeyId);
            if (attrKey == null) {
                log.warn("属性键不存在，无法验证访问权限，属性键ID: {}", attrKeyId);
                return false;
            }
            
            // 房东可以访问官方发布的属性和自己发布的属性
            boolean hasPermission = Integer.valueOf(1).equals(attrKey.getCreatorType())
                                  || (Integer.valueOf(2).equals(attrKey.getCreatorType()) 
                                      && attrKey.getCreatorId().equals(landlordId));
            
            if (!hasPermission) {
                log.warn("房东无权访问属性，属性键ID: {}, 房东ID: {}, 创建者类型: {}, 创建者ID: {}", 
                    attrKeyId, landlordId, attrKey.getCreatorType(), attrKey.getCreatorId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证属性访问权限失败，属性键ID: {}, 房东ID: {}", attrKeyId, landlordId, e);
            return false;
        }
    }

    @Override
    public boolean validateFacilityAccessPermission(Long facilityId, Long landlordId) {
        try {
            FacilityInfo facilityInfo = facilityInfoMapper.selectById(facilityId);
            if (facilityInfo == null) {
                log.warn("配套设施不存在，无法验证访问权限，配套设施ID: {}", facilityId);
                return false;
            }
            
            // 验证类型：房间编辑时只能使用房间级别的配套设施
            if (!ItemType.ROOM.equals(facilityInfo.getType())) {
                log.warn("房东尝试使用非房间级别的配套设施，配套设施ID: {}, 类型: {}", facilityId, facilityInfo.getType());
                return false;
            }
            
            // 房东可以访问官方发布的配套设施和自己发布的配套设施
            boolean hasPermission = com.atguigu.lease.model.enums.CreatorType.OFFICIAL.equals(facilityInfo.getCreatorType())
                                  || (com.atguigu.lease.model.enums.CreatorType.LANDLORD.equals(facilityInfo.getCreatorType()) 
                                      && facilityInfo.getCreatorId().equals(landlordId));
            
            if (!hasPermission) {
                log.warn("房东无权访问配套设施，配套设施ID: {}, 房东ID: {}, 创建者类型: {}, 创建者ID: {}", 
                    facilityId, landlordId, facilityInfo.getCreatorType(), facilityInfo.getCreatorId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证配套设施访问权限失败，配套设施ID: {}, 房东ID: {}", facilityId, landlordId, e);
            return false;
        }
    }

    @Override
    public boolean validateLabelAccessPermission(Long labelId, Long landlordId) {
        try {
            LabelInfo labelInfo = labelInfoMapper.selectById(labelId);
            if (labelInfo == null) {
                log.warn("标签不存在，无法验证访问权限，标签ID: {}", labelId);
                return false;
            }
            
            // 验证类型：房间编辑时只能使用房间级别的标签
            if (!ItemType.ROOM.equals(labelInfo.getType())) {
                log.warn("房东尝试使用非房间级别的标签，标签ID: {}, 类型: {}", labelId, labelInfo.getType());
                return false;
            }
            
            // 房东可以访问官方发布的标签和自己发布的标签
            boolean hasPermission = com.atguigu.lease.model.enums.CreatorType.OFFICIAL.equals(labelInfo.getCreatorType())
                                  || (com.atguigu.lease.model.enums.CreatorType.LANDLORD.equals(labelInfo.getCreatorType()) 
                                      && labelInfo.getCreatorId().equals(landlordId));
            
            if (!hasPermission) {
                log.warn("房东无权访问标签，标签ID: {}, 房东ID: {}, 创建者类型: {}, 创建者ID: {}", 
                    labelId, landlordId, labelInfo.getCreatorType(), labelInfo.getCreatorId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证标签访问权限失败，标签ID: {}, 房东ID: {}", labelId, landlordId, e);
            return false;
        }
    }

    @Override
    public boolean validatePaymentTypeAccessPermission(Long paymentTypeId, Long landlordId) {
        try {
            PaymentType paymentType = paymentTypeMapper.selectById(paymentTypeId);
            if (paymentType == null) {
                log.warn("支付方式不存在，无法验证访问权限，支付方式ID: {}", paymentTypeId);
                return false;
            }
            
            // 房东可以访问官方发布的支付方式和自己发布的支付方式
            boolean hasPermission = Integer.valueOf(1).equals(paymentType.getCreatorType())
                                  || (Integer.valueOf(2).equals(paymentType.getCreatorType()) 
                                      && paymentType.getCreatorId().equals(landlordId));
            
            if (!hasPermission) {
                log.warn("房东无权访问支付方式，支付方式ID: {}, 房东ID: {}, 创建者类型: {}, 创建者ID: {}", 
                    paymentTypeId, landlordId, paymentType.getCreatorType(), paymentType.getCreatorId());
            }
            
            return hasPermission;
        } catch (Exception e) {
            log.error("验证支付方式访问权限失败，支付方式ID: {}, 房东ID: {}", paymentTypeId, landlordId, e);
            return false;
        }
    }

    @Override
    public Result updateReleaseStatus(Long roomId, com.atguigu.lease.model.enums.ReleaseStatus status, Long landlordId) {
        try {
            // 验证房源是否属于该房东
            RoomInfo roomInfo = this.getById(roomId);
            if (roomInfo == null) {
                return Result.fail("房源不存在");
            }
            
            if (!com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) || 
                !roomInfo.getPublisherId().equals(landlordId)) {
                return Result.fail("无权操作该房源");
            }
            
            // 只有审核通过的房源才能发布
            if (com.atguigu.lease.model.enums.ReleaseStatus.RELEASED.equals(status) && 
                !com.atguigu.lease.model.enums.ReviewStatus.APPROVED.equals(roomInfo.getReviewStatus())) {
                return Result.fail("只有审核通过的房源才能发布");
            }
            
            // 写操作：延迟双删策略 - 第一次删除缓存
            deleteRoomCache(roomId, "发布状态更新前");
            
            // 更新房源发布状态
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RoomInfo> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(RoomInfo::getId, roomId);
            updateWrapper.set(RoomInfo::getIsRelease, status);
            updateWrapper.set(RoomInfo::getUpdateTime, java.time.LocalDateTime.now());
            
            this.update(updateWrapper);
            
            log.info("房东修改房源发布状态成功：房东ID={}, 房源ID={}, 状态={}", landlordId, roomId, status);
            
            // 写操作：延迟双删策略 - 第二次删除缓存
            delayedDeleteRoomCache(roomId, "发布状态更新后");
            
            return Result.ok("发布状态修改成功");
        } catch (Exception e) {
            log.error("房东修改房源发布状态失败：房东ID={}, 房源ID={}, 状态={}", landlordId, roomId, status, e);
            return Result.fail("修改发布状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result resubmitForReview(Long roomId, Long landlordId) {
        try {
            // 验证房源是否属于该房东
            RoomInfo roomInfo = this.getById(roomId);
            if (roomInfo == null) {
                return Result.fail("房源不存在");
            }
            
            if (!com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) || 
                !roomInfo.getPublisherId().equals(landlordId)) {
                return Result.fail("无权操作该房源");
            }
            
            // 只有被拒绝的房源才能重新提交审核
            if (!com.atguigu.lease.model.enums.ReviewStatus.REJECTED.equals(roomInfo.getReviewStatus())) {
                return Result.fail("只有被拒绝的房源才能重新提交审核");
            }
            
            // 写操作：延迟双删策略 - 第一次删除缓存
            deleteRoomCache(roomId, "重新提交审核前");
            
            // 更新房源审核状态为待审核，发布状态为未发布
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RoomInfo> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(RoomInfo::getId, roomId);
            updateWrapper.set(RoomInfo::getReviewStatus, com.atguigu.lease.model.enums.ReviewStatus.PENDING);
            updateWrapper.set(RoomInfo::getIsRelease, com.atguigu.lease.model.enums.ReleaseStatus.NOT_RELEASED);
            updateWrapper.set(RoomInfo::getUpdateTime, java.time.LocalDateTime.now());
            
            this.update(updateWrapper);
            
            log.info("房东重新提交审核成功：房东ID={}, 房源ID={}", landlordId, roomId);
            
            // 写操作：延迟双删策略 - 第二次删除缓存
            delayedDeleteRoomCache(roomId, "重新提交审核后");
            
            return Result.ok("重新提交审核成功，请等待审核结果");
        } catch (Exception e) {
            log.error("房东重新提交审核失败：房东ID={}, 房源ID={}", landlordId, roomId, e);
            return Result.fail("重新提交审核失败：" + e.getMessage());
        }
    }

    /**
     * 删除房源缓存
     */
    private void deleteRoomCache(Long roomId, String operation) {
        try {
            String cacheKey = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(cacheKey);
            log.debug("{}删除房源缓存成功，房源ID: {}", operation, roomId);
        } catch (Exception e) {
            log.warn("{}删除房源缓存失败，房源ID: {}, 错误: {}", operation, roomId, e.getMessage());
        }
    }

    /**
     * 延迟删除房源缓存（双删策略）
     */
    private void delayedDeleteRoomCache(Long roomId, String operation) {
        try {
            Thread.sleep(RedisConstant.CACHE_DOUBLE_DELETE_DELAY_MS);
            deleteRoomCache(roomId, operation + "延迟");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("缓存双删延迟被中断，房源ID: {}", roomId);
        }
    }


}




