package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
//import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {


    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RoomReviewMapper roomReviewMapper;

    @Override//"保存或更新房间信息
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        Boolean isUpdate = roomSubmitVo.getId() != null;
        Long roomId = roomSubmitVo.getId();
        
        // 写操作：延迟双删策略 - 第一次删除缓存
        if (isUpdate) {
            deleteAdminRoomCache(roomId, "后台管理系统更新前");
        }
        
        super.saveOrUpdate(roomSubmitVo);
        
        // 获取保存后的房源ID（新增时才有）
        if (!isUpdate) {
            roomId = roomSubmitVo.getId();
        }
        
        //若为更新操作，则先删除与Room相关的各项信息列表
        if (isUpdate){
            //删除图片列表//1.删除原有graphInfoList
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getItemId, roomId);
            graphInfoService.remove(graphQueryWrapper);
            //2.删除原有roomAttrValueList
            LambdaQueryWrapper<RoomAttrValue> attrQueryMapper = new LambdaQueryWrapper<>();
            attrQueryMapper.eq(RoomAttrValue::getRoomId, roomId);
            roomAttrValueService.remove(attrQueryMapper);
            //3.删除原有roomFacilityList
            LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(RoomFacility::getRoomId, roomId);
            roomFacilityService.remove(facilityQueryWrapper);
            //4.删除原有roomLabelList
            LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(RoomLabel::getRoomId, roomId);
            roomLabelService.remove(labelQueryWrapper);
            //5.删除原有paymentTypeList
            LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
            paymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomId);
            roomPaymentTypeService.remove(paymentQueryWrapper);
            //6.删除原有leaseTermList
            LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
            termQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomId);
            roomLeaseTermService.remove(termQueryWrapper);
        }
        //1.保存新的graphInfoList
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomId);
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        //2.保存新的roomAttrValueList
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)){
            List<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = RoomAttrValue.builder()
                        .roomId(roomId)
                        .attrValueId(attrValueId).build();
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }
        //3.保存新的facilityInfoList
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
            List<RoomFacility> roomFacilityList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomId).facilityId(facilityInfoId).build();
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }
        //4.保存新的labelInfoList
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIds)) {
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomId).labelId(labelInfoId).build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }
        //5.保存新的paymentTypeList
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomId).paymentTypeId(paymentTypeId).build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }
        //6.保存新的leaseTermList
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomId).leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

        // 写操作：延迟双删策略 - 第二次删除缓存
        delayedDeleteAdminRoomCache(roomId, "后台管理系统更新后", isUpdate ? "更新" : "新增");
    }

    @Override//根据条件分页查询房间列表
    public IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageRoomItemByQuery(page, queryVo);
    }

    @Override//根据id获取房间详细信息
    public RoomDetailVo getRoomDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = null;
        
        // 1. 先查缓存
        try {
            roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
            if (roomDetailVo != null) {
                log.debug("后台管理系统从缓存加载房间详情，房源ID: {}", id);
                return roomDetailVo;
            }
        } catch (Exception e) {
            log.warn("后台管理系统房源详情缓存读取失败，清除缓存重新加载，房源ID: {}, 错误: {}", id, e.getMessage());
            try {
                redisTemplate.delete(key);
            } catch (Exception deleteEx) {
                log.warn("清除异常缓存失败，房源ID: {}", id, deleteEx);
            }
        }

        // 2. 缓存未命中，查数据库
        roomDetailVo = loadAdminRoomDetailFromDatabase(id);
        if (roomDetailVo == null) {
            return null;
        }

        // 3. 更新缓存
        updateAdminRoomDetailCache(key, roomDetailVo, id);

        return roomDetailVo;
    }

    /**
     * 从数据库加载房源详情（后台管理系统）
     */
    private RoomDetailVo loadAdminRoomDetailFromDatabase(Long id) {
        //1.查询RoomInfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            return null;
        }
        
        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
        //3.查询graphInfoList
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);
        //4.查询attrValueList
        List<AttrValueVo> attrvalueVoList= attrValueMapper.selectListByRoomId(id);

        //5.查询facilityInfoList
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);
        
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setAttrValueVoList(attrvalueVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);

        log.info("后台管理系统从数据库加载房间详情，房源ID: {}", id);
        return roomDetailVo;
    }

    /**
     * 更新房源详情缓存（后台管理系统）
     */
    private void updateAdminRoomDetailCache(String key, RoomDetailVo roomDetailVo, Long roomId) {
        try {
            // 先测试序列化是否正常
            redisTemplate.opsForValue().set(key, roomDetailVo, RedisConstant.APP_ROOM_CACHE_TTL_SEC, TimeUnit.SECONDS);
            
            // 立即验证缓存是否写入成功
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("后台管理系统房源详情缓存写入并验证成功，房源ID: {}, TTL: {}秒", roomId, RedisConstant.APP_ROOM_CACHE_TTL_SEC);
            } else {
                log.error("后台管理系统房源详情缓存写入后验证失败，缓存为空，房源ID: {}", roomId);
            }
        } catch (Exception e) {
            log.error("后台管理系统房源详情缓存写入失败，房源ID: {}, 错误详情: ", roomId, e);
            // 不抛出异常，避免影响主要业务流程，但记录详细错误信息
        }
    }

    @Override//根据id删除房间信息
    public void removeRoomById(Long id) {
        // 写操作：延迟双删策略 - 第一次删除缓存
        deleteAdminRoomCache(id, "后台管理系统删除前");
        
        //1.删除RoomInfo
        super.removeById(id);
        //2.删除graphInfoList
        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType,ItemType.ROOM);
        graphQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphQueryWrapper);
        //3.删除attrValueList
        LambdaQueryWrapper<RoomAttrValue> attrQueryWrapper = new LambdaQueryWrapper<>();
        attrQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryWrapper);
        //4.删除facilityInfoList
        LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityQueryWrapper);
        //5.删除labelInfoList
        LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelQueryWrapper);
        //6.删除paymentTypeList
        LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
        paymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentQueryWrapper);
        //7.删除leaseTermList
        LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
        termQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(termQueryWrapper);
        
        // 写操作：延迟双删策略 - 第二次删除缓存
        delayedDeleteAdminRoomCache(id, "后台管理系统删除后", "删除");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRoom(Long roomId, String rejectReason) {
        // 1. 验证房间是否存在
        RoomInfo roomInfo = this.getById(roomId);
        if (roomInfo == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 2. 验证房间是否为房东发布
        if (!PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
            throw new RuntimeException("只能打回房东发布的房间");
        }
        
        // 3. 验证房间当前状态是否为审核通过
        if (!ReviewStatus.APPROVED.equals(roomInfo.getReviewStatus())) {
            throw new RuntimeException("只能打回审核通过的房间");
        }
        
        // 写操作：延迟双删策略 - 第一次删除缓存
        deleteAdminRoomCache(roomId, "后台管理系统打回前");
        
        // 4. 更新房间审核状态为审核拒绝，同时重置发布状态为未发布
        LambdaUpdateWrapper<RoomInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RoomInfo::getId, roomId)
                    .set(RoomInfo::getReviewStatus, ReviewStatus.REJECTED)
                    .set(RoomInfo::getIsRelease, ReleaseStatus.NOT_RELEASED) // 重置为未发布
                    .set(RoomInfo::getLastReviewedAt, LocalDateTime.now());
        this.update(updateWrapper);
        
        // 5. 创建新的审核记录（打回记录）
        RoomReview rejectReview = new RoomReview();
        rejectReview.setRoomId(roomId);
        rejectReview.setApartmentId(roomInfo.getApartmentId()); // 设置公寓ID
        rejectReview.setLandlordId(roomInfo.getPublisherId());
        rejectReview.setStatus(ReviewStatus.REJECTED); // 使用正确的字段名
        rejectReview.setComplianceScore(BigDecimal.ZERO); // 设置合规评分为0分
        rejectReview.setReviewComments("官方打回：" + rejectReason);
        rejectReview.setReviewedAt(new java.util.Date()); // 使用Date类型
        rejectReview.setReviewerId(1L); // 假设系统管理员ID为1，实际应该从当前登录用户获取
        rejectReview.setOverallRating(1); // 使用Integer类型
        rejectReview.setPhotoQualityScore(BigDecimal.ZERO);
        rejectReview.setDescriptionAccuracyScore(BigDecimal.ZERO);
        rejectReview.setFacilityCompletenessScore(BigDecimal.ZERO);
        rejectReview.setLocationAccuracyScore(BigDecimal.ZERO);
        rejectReview.setCreateTime(new java.util.Date());
        rejectReview.setUpdateTime(new java.util.Date());
        
        // 插入新的审核记录
        roomReviewMapper.insert(rejectReview);
        
        // 写操作：延迟双删策略 - 第二次删除缓存
        delayedDeleteAdminRoomCache(roomId, "后台管理系统打回后", "打回");
        
        // 注意：打回操作会同时执行以下操作：
        // 1. 保留审核历史记录，没有断开与之前审核记录的关联
        // 2. 创建新的打回审核记录，形成完整的审核历史链
        // 3. 将房间审核状态改为"审核拒绝"
        // 4. 将房间发布状态重置为"未发布"，防止租客继续看到被打回的房间
        // 5. 房东需要修改房间信息后重新提交审核，审核通过后才能重新发布
    }

    @Override
    public void updateReleaseStatus(Long roomId, com.atguigu.lease.model.enums.ReleaseStatus status) {
        try {
            // 写操作：延迟双删策略 - 第一次删除缓存
            deleteAdminRoomCache(roomId, "后台管理系统发布状态更新前");
            
            // 更新房源发布状态
            LambdaUpdateWrapper<RoomInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RoomInfo::getId, roomId);
            updateWrapper.set(RoomInfo::getIsRelease, status);
            updateWrapper.set(RoomInfo::getUpdateTime, LocalDateTime.now());
            
            this.update(updateWrapper);
            
            log.info("后台管理系统更新房源发布状态成功：房源ID={}, 状态={}", roomId, status);
            
            // 写操作：延迟双删策略 - 第二次删除缓存
            delayedDeleteAdminRoomCache(roomId, "后台管理系统发布状态更新后", "发布状态更新");
            
        } catch (Exception e) {
            log.error("后台管理系统更新房源发布状态失败：房源ID={}, 状态={}", roomId, status, e);
            throw new RuntimeException("更新房源发布状态失败：" + e.getMessage());
        }
    }

    /**
     * 删除房源缓存（后台管理系统）
     */
    private void deleteAdminRoomCache(Long roomId, String operation) {
        try {
            String cacheKey = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(cacheKey);
            log.debug("{}删除房源缓存成功，房源ID: {}", operation, roomId);
        } catch (Exception e) {
            log.warn("{}删除房源缓存失败，房源ID: {}, 错误: {}", operation, roomId, e.getMessage());
        }
    }

    /**
     * 延迟删除房源缓存（后台管理系统双删策略）
     */
    private void delayedDeleteAdminRoomCache(Long roomId, String operation, String operationType) {
        try {
            Thread.sleep(RedisConstant.CACHE_DOUBLE_DELETE_DELAY_MS);
            deleteAdminRoomCache(roomId, operation + "延迟");
            log.info("后台管理系统房源{}后清除缓存成功，房源ID: {}", operationType, roomId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("缓存双删延迟被中断，房源ID: {}", roomId);
        } catch (Exception e) {
            log.error("后台管理系统房源{}后清除缓存失败，房源ID: {}", operationType, roomId, e);
        }
    }


}




