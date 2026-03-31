package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private ApartmentFacilityMapper apartmentFacilityMapper;
    @Autowired
    private ApartmentLabelMapper apartmentLabelMapper;
    @Autowired
    private ApartmentFeeValueMapper apartmentFeeValueMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private FeeKeyMapper feeKeyMapper;
    @Autowired
    private ProvinceInfoMapper provinceInfoMapper;
    @Autowired
    private CityInfoMapper cityInfoMapper;
    @Autowired
    private DistrictInfoMapper districtInfoMapper;
    @Override
    public ApartmentItemVo selectApartmentItemVoById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        
        // 检查公寓信息是否存在
        if (apartmentInfo == null) {
            return null;
        }
        
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);
        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);
        
        // 查询公寓杂费信息
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setFacilityInfoList(facilityInfoList);
        apartmentItemVo.setMinRent(minRent);
        apartmentItemVo.setFeeValueVoList(feeValueVoList);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getApartmentDetailById(Long id) {
        //1.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //2.查询图片信息
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
        //3.查询标签信息
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        //4.查询配套信息
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);
        //5.查询最小租金
        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);
        //6.查询杂费信息
        List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);
        //7.查询该公寓下该房东的房源列表（只查询属于该房东的房源）
        List<RoomItemVo> roomList = new ArrayList<>();
        if (apartmentInfo != null && apartmentInfo.getLandlordId() != null) {
            // 查询该公寓下该房东发布的房源
            LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
            roomQueryWrapper.eq(RoomInfo::getApartmentId, id)
                           .eq(RoomInfo::getPublisherType, PublisherType.LANDLORD)
                           .eq(RoomInfo::getPublisherId, apartmentInfo.getLandlordId())
                           .eq(RoomInfo::getIsDeleted, 0)
                           .orderByDesc(RoomInfo::getCreateTime);
            
            List<RoomInfo> roomInfoList = roomInfoMapper.selectList(roomQueryWrapper);
            roomList = roomInfoList.stream().map(roomInfo -> {
                RoomItemVo roomItemVo = new RoomItemVo();
                BeanUtils.copyProperties(roomInfo, roomItemVo);
                return roomItemVo;
            }).collect(Collectors.toList());
        }

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();

        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setMinRent(minRent);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        apartmentDetailVo.setRoomList(roomList);
        return apartmentDetailVo;
    }

    @Override
    @Transactional
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        try {
            log.info("开始保存或更新公寓信息 - 公寓ID: {}, 房东ID: {}", apartmentSubmitVo.getId(), apartmentSubmitVo.getLandlordId());
            
            boolean isUpdate = apartmentSubmitVo.getId() != null;
            log.info("操作类型: {}", isUpdate ? "更新" : "创建");
            
            // 验证坐标数据
            validateCoordinates(apartmentSubmitVo);
            
            // 保存或更新公寓基本信息
            log.info("保存公寓基本信息");
            super.saveOrUpdate(apartmentSubmitVo);
            log.info("公寓基本信息保存完成，公寓ID: {}", apartmentSubmitVo.getId());
            
            if (isUpdate) {
                log.info("更新操作：删除原有关联关系");
                
                // 更新时先删除原有关联关系
                LambdaQueryWrapper<ApartmentFacility> facilityWrapper = new LambdaQueryWrapper<>();
                facilityWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
                int deletedFacilities = apartmentFacilityMapper.delete(facilityWrapper);
                log.info("删除原有配套设施关联: {} 条", deletedFacilities);
                
                LambdaQueryWrapper<ApartmentLabel> labelWrapper = new LambdaQueryWrapper<>();
                labelWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
                int deletedLabels = apartmentLabelMapper.delete(labelWrapper);
                log.info("删除原有标签关联: {} 条", deletedLabels);
                
                LambdaQueryWrapper<ApartmentFeeValue> feeWrapper = new LambdaQueryWrapper<>();
                feeWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
                int deletedFees = apartmentFeeValueMapper.delete(feeWrapper);
                log.info("删除原有杂费关联: {} 条", deletedFees);
                
                LambdaQueryWrapper<GraphInfo> graphWrapper = new LambdaQueryWrapper<>();
                graphWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
                graphWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
                int deletedGraphs = graphInfoMapper.delete(graphWrapper);
                log.info("删除原有图片信息: {} 条", deletedGraphs);
            }
            
            // 保存配套设施关联
            if (apartmentSubmitVo.getFacilityInfoIds() != null && !apartmentSubmitVo.getFacilityInfoIds().isEmpty()) {
                log.info("保存配套设施关联，数量: {}", apartmentSubmitVo.getFacilityInfoIds().size());
                
                List<ApartmentFacility> apartmentFacilityList = apartmentSubmitVo.getFacilityInfoIds().stream().map(facilityId -> {
                    ApartmentFacility apartmentFacility = new ApartmentFacility();
                    apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                    apartmentFacility.setFacilityId(facilityId);
                    return apartmentFacility;
                }).toList();
                
                for (ApartmentFacility apartmentFacility : apartmentFacilityList) {
                    apartmentFacilityMapper.insert(apartmentFacility);
                }
                log.info("配套设施关联保存完成");
            } else {
                log.info("无配套设施需要保存");
            }
            
            // 保存标签关联
            if (apartmentSubmitVo.getLabelIds() != null && !apartmentSubmitVo.getLabelIds().isEmpty()) {
                log.info("保存标签关联，数量: {}", apartmentSubmitVo.getLabelIds().size());
                
                List<ApartmentLabel> apartmentLabelList = apartmentSubmitVo.getLabelIds().stream().map(labelId -> {
                    ApartmentLabel apartmentLabel = new ApartmentLabel();
                    apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                    apartmentLabel.setLabelId(labelId);
                    return apartmentLabel;
                }).toList();
                
                for (ApartmentLabel apartmentLabel : apartmentLabelList) {
                    apartmentLabelMapper.insert(apartmentLabel);
                }
                log.info("标签关联保存完成");
            } else {
                log.info("无标签需要保存");
            }
            
            // 处理杂费信息
            List<Long> finalFeeValueIds = new ArrayList<>();
            
            // 1. 如果有直接的杂费值ID列表，直接使用
            if (apartmentSubmitVo.getFeeValueIds() != null && !apartmentSubmitVo.getFeeValueIds().isEmpty()) {
                log.info("使用直接杂费值ID列表，数量: {}", apartmentSubmitVo.getFeeValueIds().size());
                finalFeeValueIds.addAll(apartmentSubmitVo.getFeeValueIds());
            }
            
            // 2. 如果有杂费信息列表，需要创建或查找对应的杂费值
            if (apartmentSubmitVo.getFeeValueVoList() != null && !apartmentSubmitVo.getFeeValueVoList().isEmpty()) {
                log.info("处理杂费信息列表，数量: {}", apartmentSubmitVo.getFeeValueVoList().size());
                Long landlordId = apartmentSubmitVo.getLandlordId();
                
                for (FeeValueVo feeValueVo : apartmentSubmitVo.getFeeValueVoList()) {
                    try {
                        log.info("处理杂费项: {}", feeValueVo.getFeeKeyName());
                        Long feeValueId = createOrFindFeeValue(feeValueVo, landlordId);
                        if (feeValueId != null) {
                            finalFeeValueIds.add(feeValueId);
                            log.info("杂费项处理成功: {} -> ID: {}", feeValueVo.getFeeKeyName(), feeValueId);
                        } else {
                            log.error("处理杂费失败，杂费信息: {}", feeValueVo);
                            throw new RuntimeException("处理杂费失败: " + feeValueVo.getFeeKeyName());
                        }
                    } catch (Exception e) {
                        log.error("处理杂费异常，杂费信息: {}", feeValueVo, e);
                        // 重新抛出异常，确保事务回滚
                        throw new RuntimeException("处理杂费异常: " + feeValueVo.getFeeKeyName(), e);
                    }
                }
                log.info("杂费信息列表处理完成");
            } else {
                log.info("无杂费信息列表需要处理");
            }
            
            // 保存杂费关联
            if (!finalFeeValueIds.isEmpty()) {
                log.info("保存杂费关联，数量: {}", finalFeeValueIds.size());
                
                List<ApartmentFeeValue> apartmentFeeValueList = finalFeeValueIds.stream().map(feeValueId -> {
                    ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                    apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                    apartmentFeeValue.setFeeValueId(feeValueId);
                    return apartmentFeeValue;
                }).toList();
                
                for (ApartmentFeeValue apartmentFeeValue : apartmentFeeValueList) {
                    apartmentFeeValueMapper.insert(apartmentFeeValue);
                }
                log.info("杂费关联保存完成");
            } else {
                log.info("无杂费关联需要保存");
            }
            
            // 保存图片信息
            if (apartmentSubmitVo.getGraphVoList() != null && !apartmentSubmitVo.getGraphVoList().isEmpty()) {
                log.info("保存图片信息，数量: {}", apartmentSubmitVo.getGraphVoList().size());
                
                List<GraphInfo> graphInfoList = apartmentSubmitVo.getGraphVoList().stream().map(graphVo -> {
                    GraphInfo graphInfo = new GraphInfo();
                    graphInfo.setItemType(ItemType.APARTMENT);
                    graphInfo.setItemId(apartmentSubmitVo.getId());
                    graphInfo.setName(graphVo.getName());
                    graphInfo.setUrl(graphVo.getUrl());
                    return graphInfo;
                }).toList();
                
                for (GraphInfo graphInfo : graphInfoList) {
                    graphInfoMapper.insert(graphInfo);
                }
                log.info("图片信息保存完成");
            } else {
                log.info("无图片信息需要保存");
            }
            
            log.info("公寓信息保存或更新完成 - 公寓ID: {}, 房东ID: {}", apartmentSubmitVo.getId(), apartmentSubmitVo.getLandlordId());
            
        } catch (Exception e) {
            log.error("保存或更新公寓信息失败 - 公寓ID: {}, 房东ID: {}", apartmentSubmitVo.getId(), apartmentSubmitVo.getLandlordId(), e);
            throw e; // 重新抛出异常，确保事务回滚
        }
    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        // 执行分页查询
        IPage<ApartmentItemVo> result = apartmentInfoMapper.pageItem(page, queryVo);
        
        // 为每个公寓补充关联数据
        for (ApartmentItemVo apartmentItemVo : result.getRecords()) {
            // 查询图片信息
            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, apartmentItemVo.getId());
            apartmentItemVo.setGraphVoList(graphVoList);
            
            // 查询标签信息
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(apartmentItemVo.getId());
            apartmentItemVo.setLabelInfoList(labelInfoList);
            
            // 查询配套设施信息
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(apartmentItemVo.getId());
            apartmentItemVo.setFacilityInfoList(facilityInfoList);
            
            // 查询杂费信息
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(apartmentItemVo.getId());
            apartmentItemVo.setFeeValueVoList(feeValueVoList);
            
            // 查询该公寓下该房东的房源统计信息
            if (apartmentItemVo.getLandlordId() != null) {
                LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
                roomQueryWrapper.eq(RoomInfo::getApartmentId, apartmentItemVo.getId())
                               .eq(RoomInfo::getPublisherType, PublisherType.LANDLORD)
                               .eq(RoomInfo::getPublisherId, apartmentItemVo.getLandlordId())
                               .eq(RoomInfo::getIsDeleted, 0);
                
                List<RoomInfo> roomList = roomInfoMapper.selectList(roomQueryWrapper);
                
                // 计算房源统计
                int totalRooms = roomList.size();
                int availableRooms = (int) roomList.stream()
                    .filter(room -> ReleaseStatus.RELEASED.equals(room.getIsRelease()))
                    .count();
                
                apartmentItemVo.setRoomCount(totalRooms);
                apartmentItemVo.setAvailableRoomCount(availableRooms);
            } else {
                // 如果是官方公寓，设置为0
                apartmentItemVo.setRoomCount(0);
                apartmentItemVo.setAvailableRoomCount(0);
            }
            
            log.debug("为公寓 {} 补充关联数据：图片{}张，标签{}个，配套{}个，杂费{}项，房源{}间，可用{}间", 
                apartmentItemVo.getId(), 
                graphVoList.size(), 
                labelInfoList.size(), 
                facilityInfoList.size(), 
                feeValueVoList.size(),
                apartmentItemVo.getRoomCount(),
                apartmentItemVo.getAvailableRoomCount());
        }
        
        return result;
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        return getApartmentDetailById(id);
    }

    @Override
    @Transactional
    public void removeApartmentById(Long id) {
        // 删除公寓基本信息
        super.removeById(id);
        
        // 删除关联的配套设施
        LambdaQueryWrapper<ApartmentFacility> facilityWrapper = new LambdaQueryWrapper<>();
        facilityWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityMapper.delete(facilityWrapper);
        
        // 删除关联的标签
        LambdaQueryWrapper<ApartmentLabel> labelWrapper = new LambdaQueryWrapper<>();
        labelWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelMapper.delete(labelWrapper);
        
        // 删除关联的杂费
        LambdaQueryWrapper<ApartmentFeeValue> feeWrapper = new LambdaQueryWrapper<>();
        feeWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueMapper.delete(feeWrapper);
        
        // 删除关联的图片
        LambdaQueryWrapper<GraphInfo> graphWrapper = new LambdaQueryWrapper<>();
        graphWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphWrapper.eq(GraphInfo::getItemId, id);
        graphInfoMapper.delete(graphWrapper);
        
        // 删除关联的房间信息
        LambdaQueryWrapper<RoomInfo> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(RoomInfo::getApartmentId, id);
        roomInfoMapper.delete(roomWrapper);
    }

    @Override
    public List<ApartmentItemVo> getAvailableApartmentsForLandlord(Long landlordId) {
        try {
            // 查询条件：已发布的官方公寓 + 该房东已发布的公寓
            LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApartmentInfo::getIsDeleted, 0)
                       .eq(ApartmentInfo::getIsRelease, ReleaseStatus.RELEASED)
                       .and(wrapper -> wrapper
                           .isNull(ApartmentInfo::getLandlordId)
                           .or()
                           .eq(ApartmentInfo::getLandlordId, landlordId)
                       )
                       .orderByDesc(ApartmentInfo::getCreateTime);

            List<ApartmentInfo> apartmentList = this.list(queryWrapper);
            
            return apartmentList.stream().map(apartment -> {
                ApartmentItemVo vo = new ApartmentItemVo();
                BeanUtils.copyProperties(apartment, vo);
                
                // 添加发布者类型标识
                if (apartment.getLandlordId() == null) {
                    vo.setName("[官方] " + apartment.getName());
                } else if (apartment.getLandlordId().equals(landlordId)) {
                    vo.setName("[我的] " + apartment.getName());
                }
                
                return vo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取房东可选择的公寓列表失败，房东ID: {}", landlordId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getApartmentFeesByIdForLandlord(Long apartmentId, Long landlordId) {
        try {
            // 1. 权限验证：检查公寓是否存在
            ApartmentInfo apartmentInfo = this.getById(apartmentId);
            if (apartmentInfo == null) {
                return Result.fail("公寓不存在");
            }
            
            // 2. 权限验证：房东可以查看自己的公寓或官方公寓（landlord_id为null）的杂费信息
            if (apartmentInfo.getLandlordId() != null && 
                !apartmentInfo.getLandlordId().equals(landlordId)) {
                return Result.fail("无权查看该公寓的杂费信息");
            }
            
            // 3. 查询公寓的杂费信息
            List<Map<String, Object>> apartmentFees = apartmentFeeValueMapper.selectFeesByApartmentId(apartmentId);
            
            log.info("获取公寓杂费信息成功：公寓ID={}, 房东ID={}, 杂费数量={}, 公寓类型={}", 
                apartmentId, landlordId, apartmentFees.size(), 
                apartmentInfo.getLandlordId() == null ? "官方公寓" : "房东公寓");
            
            return Result.ok(apartmentFees);
            
        } catch (Exception e) {
            log.error("获取公寓杂费信息失败：公寓ID={}, 房东ID={}", apartmentId, landlordId, e);
            return Result.fail("获取公寓杂费信息失败：" + e.getMessage());
        }
    }

    @Override
    public Result<ApartmentDetailVo> getApartmentDetailForEdit(Long apartmentId, Long landlordId) {
        try {
            // 1. 权限验证：检查公寓是否存在
            ApartmentInfo apartmentInfo = this.getById(apartmentId);
            if (apartmentInfo == null) {
                return Result.fail("公寓不存在");
            }
            
            // 2. 权限验证：房东只能编辑自己发布的公寓
            if (apartmentInfo.getLandlordId() == null || 
                !apartmentInfo.getLandlordId().equals(landlordId)) {
                return Result.fail("无权编辑该公寓");
            }
            
            // 3. 获取公寓详细信息
            ApartmentDetailVo apartmentDetail = getApartmentDetailById(apartmentId);
            if (apartmentDetail == null) {
                return Result.fail("获取公寓详情失败");
            }
            
            log.info("获取公寓编辑详情成功：公寓ID={}, 房东ID={}, 公寓名称={}", 
                apartmentId, landlordId, apartmentDetail.getName());
            
            return Result.ok(apartmentDetail);
            
        } catch (Exception e) {
            log.error("获取公寓编辑详情失败：公寓ID={}, 房东ID={}", apartmentId, landlordId, e);
            return Result.fail("获取公寓编辑详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建或查找杂费值
     * @param feeValueVo 杂费信息
     * @param landlordId 房东ID
     * @return 杂费值ID
     */
    private Long createOrFindFeeValue(FeeValueVo feeValueVo, Long landlordId) {
        try {
            // 1. 查找或创建杂费键（杂费类型）
            Long feeKeyId = findOrCreateFeeKey(feeValueVo.getFeeKeyName(), landlordId);
            if (feeKeyId == null) {
                log.error("创建杂费键失败：{}", feeValueVo.getFeeKeyName());
                return null;
            }
            
            // 2. 查找是否已存在相同的杂费值
            LambdaQueryWrapper<FeeValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FeeValue::getFeeKeyId, feeKeyId)
                       .eq(FeeValue::getName, feeValueVo.getName())
                       .eq(FeeValue::getUnit, feeValueVo.getUnit())
                       .eq(FeeValue::getCreatorType, 2) // 房东创建
                       .eq(FeeValue::getCreatorId, landlordId)
                       .eq(FeeValue::getIsDeleted, 0);
            
            FeeValue existingFeeValue = feeValueMapper.selectOne(queryWrapper);
            if (existingFeeValue != null) {
                return existingFeeValue.getId();
            }
            
            // 3. 创建新的杂费值
            FeeValue newFeeValue = new FeeValue();
            newFeeValue.setFeeKeyId(feeKeyId);
            newFeeValue.setName(feeValueVo.getName());
            newFeeValue.setUnit(feeValueVo.getUnit());
            newFeeValue.setCreatorType(2); // 房东创建
            newFeeValue.setCreatorId(landlordId);
            
            feeValueMapper.insert(newFeeValue);
            
            log.info("创建杂费值成功：房东ID={}, 杂费键ID={}, 杂费值={}", landlordId, feeKeyId, feeValueVo.getName());
            
            return newFeeValue.getId();
            
        } catch (Exception e) {
            log.error("创建或查找杂费值失败：房东ID={}, 杂费信息={}", landlordId, feeValueVo, e);
            return null;
        }
    }

    /**
     * 查找或创建杂费键
     * @param feeKeyName 杂费键名称
     * @param landlordId 房东ID
     * @return 杂费键ID
     */
    private Long findOrCreateFeeKey(String feeKeyName, Long landlordId) {
        try {
            // 1. 先查找官方杂费键（如果有多个同名的，选择ID最小的）
            LambdaQueryWrapper<FeeKey> officialQueryWrapper = new LambdaQueryWrapper<>();
            officialQueryWrapper.eq(FeeKey::getName, feeKeyName)
                               .eq(FeeKey::getCreatorType, 1) // 官方创建
                               .eq(FeeKey::getIsDeleted, 0)
                               .orderByAsc(FeeKey::getId) // 按ID升序排列
                               .last("LIMIT 1"); // 只取第一条
            
            List<FeeKey> officialFeeKeys = feeKeyMapper.selectList(officialQueryWrapper);
            if (!officialFeeKeys.isEmpty()) {
                FeeKey officialFeeKey = officialFeeKeys.get(0);
                log.info("找到官方杂费键：ID={}, 名称={}", officialFeeKey.getId(), feeKeyName);
                return officialFeeKey.getId();
            }
            
            // 2. 查找房东自己创建的杂费键
            LambdaQueryWrapper<FeeKey> landlordQueryWrapper = new LambdaQueryWrapper<>();
            landlordQueryWrapper.eq(FeeKey::getName, feeKeyName)
                               .eq(FeeKey::getCreatorType, 2) // 房东创建
                               .eq(FeeKey::getCreatorId, landlordId)
                               .eq(FeeKey::getIsDeleted, 0);
            
            FeeKey landlordFeeKey = feeKeyMapper.selectOne(landlordQueryWrapper);
            if (landlordFeeKey != null) {
                log.info("找到房东杂费键：ID={}, 名称={}", landlordFeeKey.getId(), feeKeyName);
                return landlordFeeKey.getId();
            }
            
            // 3. 创建新的杂费键
            FeeKey newFeeKey = new FeeKey();
            newFeeKey.setName(feeKeyName);
            newFeeKey.setCreatorType(2); // 房东创建
            newFeeKey.setCreatorId(landlordId);
            
            feeKeyMapper.insert(newFeeKey);
            
            log.info("创建杂费键成功：房东ID={}, 杂费键名称={}, 新ID={}", landlordId, feeKeyName, newFeeKey.getId());
            
            return newFeeKey.getId();
            
        } catch (Exception e) {
            log.error("查找或创建杂费键失败：房东ID={}, 杂费键名称={}", landlordId, feeKeyName, e);
            return null;
        }
    }

    /**
     * 自动填充省市区名称
     * @param apartmentSubmitVo 公寓提交信息
     */
    private void fillRegionNames(ApartmentSubmitVo apartmentSubmitVo) {
        try {
            log.info("开始填充省市区名称 - 省份ID: {}, 城市ID: {}, 区县ID: {}", 
                apartmentSubmitVo.getProvinceId(), apartmentSubmitVo.getCityId(), apartmentSubmitVo.getDistrictId());
            
            // 填充省份名称
            if (apartmentSubmitVo.getProvinceId() != null && apartmentSubmitVo.getProvinceId() > 0) {
                ProvinceInfo provinceInfo = provinceInfoMapper.selectById(apartmentSubmitVo.getProvinceId());
                if (provinceInfo != null) {
                    apartmentSubmitVo.setProvinceName(provinceInfo.getName());
                    log.info("填充省份名称：{} -> {}", apartmentSubmitVo.getProvinceId(), provinceInfo.getName());
                } else {
                    log.warn("未找到省份信息：ID={}", apartmentSubmitVo.getProvinceId());
                }
            }
            
            // 填充城市名称
            if (apartmentSubmitVo.getCityId() != null && apartmentSubmitVo.getCityId() > 0) {
                CityInfo cityInfo = cityInfoMapper.selectById(apartmentSubmitVo.getCityId());
                if (cityInfo != null) {
                    apartmentSubmitVo.setCityName(cityInfo.getName());
                    log.info("填充城市名称：{} -> {}", apartmentSubmitVo.getCityId(), cityInfo.getName());
                } else {
                    log.warn("未找到城市信息：ID={}", apartmentSubmitVo.getCityId());
                }
            }
            
            // 填充区县名称
            if (apartmentSubmitVo.getDistrictId() != null && apartmentSubmitVo.getDistrictId() > 0) {
                DistrictInfo districtInfo = districtInfoMapper.selectById(apartmentSubmitVo.getDistrictId());
                if (districtInfo != null) {
                    apartmentSubmitVo.setDistrictName(districtInfo.getName());
                    log.info("填充区县名称：{} -> {}", apartmentSubmitVo.getDistrictId(), districtInfo.getName());
                } else {
                    log.warn("未找到区县信息：ID={}", apartmentSubmitVo.getDistrictId());
                }
            }
            
            log.info("省市区名称填充完成 - 省份: {}, 城市: {}, 区县: {}", 
                apartmentSubmitVo.getProvinceName(), apartmentSubmitVo.getCityName(), apartmentSubmitVo.getDistrictName());
            
        } catch (Exception e) {
            log.error("填充省市区名称失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 验证公寓坐标数据
     * @param apartmentSubmitVo 公寓提交信息
     */
    private void validateCoordinates(ApartmentSubmitVo apartmentSubmitVo) {
        try {
            log.info("开始验证公寓坐标数据 - 经度: {}, 纬度: {}", apartmentSubmitVo.getLongitude(), apartmentSubmitVo.getLatitude());
            
            // 检查坐标是否为空
            if (StringUtils.isEmpty(apartmentSubmitVo.getLatitude()) || StringUtils.isEmpty(apartmentSubmitVo.getLongitude())) {
                log.warn("坐标数据为空，公寓ID: {}", apartmentSubmitVo.getId());
                return;
            }
            
            try {
                // 将坐标字符串转换为数字
                double latitude = Double.parseDouble(apartmentSubmitVo.getLatitude());
                double longitude = Double.parseDouble(apartmentSubmitVo.getLongitude());
                
                log.info("坐标数据转换成功 - 经度: {}, 纬度: {}", longitude, latitude);
                
                // 检查坐标是否为零
                if (latitude == 0 || longitude == 0) {
                    log.error("❌ 坐标为零值，无法保存 - 经度: {}, 纬度: {}, 公寓ID: {}, 公寓名称: {}", 
                        longitude, latitude, apartmentSubmitVo.getId(), apartmentSubmitVo.getName());
                    throw new RuntimeException("坐标数据无效：经度和纬度不能为零");
                }
                
                // 验证坐标范围（中国坐标范围）
                // 经度范围：73°E - 135°E
                // 纬度范围：18°N - 53°N
                if (longitude < 73 || longitude > 135 || latitude < 18 || latitude > 53) {
                    log.error("❌ 坐标超出中国范围，无法保存 - 经度: {}, 纬度: {}, 公寓ID: {}, 公寓名称: {}", 
                        longitude, latitude, apartmentSubmitVo.getId(), apartmentSubmitVo.getName());
                    
                    // 坐标超出范围，抛出异常阻止保存
                    throw new RuntimeException(String.format(
                        "坐标数据无效：经度 %.6f 或纬度 %.6f 超出中国范围（经度73-135°，纬度18-53°）", 
                        longitude, latitude));
                }
                
                log.info("✅ 坐标验证通过 - 经度: {}, 纬度: {}, 公寓ID: {}, 公寓名称: {}", 
                    longitude, latitude, apartmentSubmitVo.getId(), apartmentSubmitVo.getName());
                
            } catch (NumberFormatException e) {
                log.error("❌ 坐标格式错误，无法转换为数字 - 经度: {}, 纬度: {}, 公寓ID: {}, 错误信息: {}", 
                    apartmentSubmitVo.getLongitude(), apartmentSubmitVo.getLatitude(), apartmentSubmitVo.getId(), e.getMessage());
                
                // 坐标格式错误，抛出异常阻止保存
                throw new RuntimeException("坐标格式错误：经度和纬度必须是有效的数字");
            }
            
        } catch (RuntimeException e) {
            log.error("坐标验证失败 - 公寓ID: {}, 公寓名称: {}, 错误信息: {}", 
                apartmentSubmitVo.getId(), apartmentSubmitVo.getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("坐标验证异常 - 公寓ID: {}, 公寓名称: {}", apartmentSubmitVo.getId(), apartmentSubmitVo.getName(), e);
            throw new RuntimeException("坐标验证异常：" + e.getMessage());
        }
    }
}




