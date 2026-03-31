package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.RoomRecommendationService;
import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;
import com.atguigu.lease.web.app.vo.ai.RoomRecommendation;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 房源推荐Service实现类
 * 负责根据关键词查询和推荐房源
 * 
 * 支持的查询维度:
 * 1. 地址: 省市区 (province_info, city_info, district_info)
 * 2. 房间属性: 房型、朝向等 (attr_value)
 * 3. 房间配套: 空调、洗衣机等 (facility_info where type=2)
 * 4. 房间标签: 朝南、独卫等 (label_info where type=2)
 * 5. 价格范围: 支持范围查询 (room_info.rent)
 * 6. 发布者类型: 房东/官方 (room_info.publisher_type)
 */
@Slf4j
@Service
public class RoomRecommendationServiceImpl implements RoomRecommendationService {

    @Autowired(required = false)
    private RoomInfoMapper roomInfoMapper;

    @Autowired(required = false)
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired(required = false)
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired(required = false)
    private RoomReviewMapper roomReviewMapper;

    @Autowired(required = false)
    private RoomAttrValueMapper roomAttrValueMapper;

    @Autowired(required = false)
    private RoomFacilityMapper roomFacilityMapper;

    @Autowired(required = false)
    private RoomLabelMapper roomLabelMapper;

    @Autowired(required = false)
    private GraphInfoMapper graphInfoMapper;

    /**
     * 根据关键词获取推荐房源
     * 推荐算法:
     * 1. 地址过滤: 根据districtIds精确匹配
     * 2. 价格过滤: 根据priceRange范围查询
     * 3. 发布者过滤: 根据publisherType过滤
     * 4. 属性过滤: 根据attrValueIds、facilityIds、labelIds过滤
     * 5. 排序优化: 按匹配度评分排序
     */
    @Override
    public List<RoomRecommendation> getRecommendations(Long userId, KeywordExtractionResult keywords) {
        log.info("获取推荐房源，用户ID: {}, 关键词置信度: {}", userId, keywords.getConfidence());

        List<RoomRecommendation> recommendations = new ArrayList<>();

        try {
            if (roomInfoMapper == null || apartmentInfoMapper == null) {
                log.warn("Mapper未初始化，返回空列表");
                return Collections.emptyList();
            }

            // 1. 构建基础查询条件
            LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
            
            // 1.1 过滤已发布且已审核的房源
            queryWrapper.eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);
            queryWrapper.eq(RoomInfo::getReviewStatus, ReviewStatus.APPROVED);

            // 1.2 价格范围过滤
            if (keywords.getPriceRange() != null) {
                queryWrapper.ge(RoomInfo::getRent, BigDecimal.valueOf(keywords.getPriceRange().getMin()));
                queryWrapper.le(RoomInfo::getRent, BigDecimal.valueOf(keywords.getPriceRange().getMax()));
            }

            // 1.3 发布者类型过滤
            if (keywords.getPublisherType() != null) {
                try {
                    PublisherType publisherType = PublisherType.valueOf(keywords.getPublisherType());
                    queryWrapper.eq(RoomInfo::getPublisherType, publisherType);
                } catch (IllegalArgumentException e) {
                    log.warn("无效的发布者类型: {}", keywords.getPublisherType());
                }
            }

            // 2. 查询所有符合基础条件的房源
            List<RoomInfo> allRooms = roomInfoMapper.selectList(queryWrapper);
            log.info("基础查询条件查询结果: {} 个房源", allRooms.size());
            if (allRooms.isEmpty()) {
                log.info("未找到符合条件的房源");
                return Collections.emptyList();
            }

            // 3. 地址过滤 (如果指定了地区)
            List<RoomInfo> filteredRooms = allRooms;
            if (keywords.getDistrictIds() != null && !keywords.getDistrictIds().isEmpty()) {
                log.info("开始地区过滤，地区ID: {}", keywords.getDistrictIds());
                filteredRooms = filterByDistrict(allRooms, keywords.getDistrictIds());
                log.info("地区过滤后: {} 个房源", filteredRooms.size());
            }

            if (filteredRooms.isEmpty()) {
                log.info("地址过滤后未找到房源");
                return Collections.emptyList();
            }

            // 4. 属性过滤 (房间属性、配套、标签)
            if ((keywords.getAttrValueIds() != null && !keywords.getAttrValueIds().isEmpty()) ||
                (keywords.getFacilityIds() != null && !keywords.getFacilityIds().isEmpty()) ||
                (keywords.getLabelIds() != null && !keywords.getLabelIds().isEmpty())) {
                filteredRooms = filterByAttributes(filteredRooms, keywords);
            }

            if (filteredRooms.isEmpty()) {
                log.info("属性过滤后未找到房源");
                return Collections.emptyList();
            }

            // 5. 映射到RoomRecommendation并计算匹配度
            for (RoomInfo room : filteredRooms) {
                ApartmentInfo apartment = apartmentInfoMapper.selectById(room.getApartmentId());
                if (apartment != null) {
                    RoomRecommendation recommendation = mapToRecommendation(room, apartment);
                    
                    // 计算匹配度评分
                    double matchScore = calculateMatchScore(room, apartment, keywords);
                    recommendation.setMatchScore(matchScore);

                    recommendations.add(recommendation);
                }
            }

            // 6. 按匹配度排序
            recommendations.sort((r1, r2) -> Double.compare(r2.getMatchScore(), r1.getMatchScore()));

            // 7. 限制推荐数量
            recommendations = recommendations.stream()
                .limit(5)
                .collect(Collectors.toList());

            log.info("房源推荐查询完成，推荐数量: {}", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("获取推荐房源失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据地区ID列表过滤房源
     */
    private List<RoomInfo> filterByDistrict(List<RoomInfo> rooms, List<Long> districtIds) {
        if (districtIds == null || districtIds.isEmpty()) {
            return rooms;
        }

        List<Long> apartmentIds = new ArrayList<>();
        for (Long districtId : districtIds) {
            LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApartmentInfo::getDistrictId, districtId);
            List<ApartmentInfo> apartments = apartmentInfoMapper.selectList(queryWrapper);
            apartments.forEach(a -> apartmentIds.add(a.getId()));
        }

        final List<Long> finalApartmentIds = apartmentIds;
        return rooms.stream()
            .filter(r -> finalApartmentIds.contains(r.getApartmentId()))
            .collect(Collectors.toList());
    }

    /**
     * 根据房间属性、配套、标签过滤房源
     */
    private List<RoomInfo> filterByAttributes(List<RoomInfo> rooms, KeywordExtractionResult keywords) {
        List<RoomInfo> filteredRooms = new ArrayList<>(rooms);

        // 1. 房间属性过滤 (attr_value)
        if (keywords.getAttrValueIds() != null && !keywords.getAttrValueIds().isEmpty()) {
            filteredRooms = filterByAttrValues(filteredRooms, keywords.getAttrValueIds());
        }

        // 2. 房间配套过滤 (facility_info where type=2)
        if (keywords.getFacilityIds() != null && !keywords.getFacilityIds().isEmpty()) {
            filteredRooms = filterByFacilities(filteredRooms, keywords.getFacilityIds());
        }

        // 3. 房间标签过滤 (label_info where type=2)
        if (keywords.getLabelIds() != null && !keywords.getLabelIds().isEmpty()) {
            filteredRooms = filterByLabels(filteredRooms, keywords.getLabelIds());
        }

        return filteredRooms;
    }

    /**
     * 根据房间属性值过滤房源
     */
    private List<RoomInfo> filterByAttrValues(List<RoomInfo> rooms, List<Long> attrValueIds) {
        if (roomAttrValueMapper == null || attrValueIds.isEmpty()) {
            return rooms;
        }

        Set<Long> roomIdsWithAttrs = new HashSet<>();
        for (Long attrValueId : attrValueIds) {
            LambdaQueryWrapper<RoomAttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomAttrValue::getAttrValueId, attrValueId);
            List<RoomAttrValue> roomAttrs = roomAttrValueMapper.selectList(queryWrapper);
            roomAttrs.forEach(ra -> roomIdsWithAttrs.add(ra.getRoomId()));
        }

        return rooms.stream()
            .filter(r -> roomIdsWithAttrs.contains(r.getId()))
            .collect(Collectors.toList());
    }

    /**
     * 根据房间配套过滤房源
     */
    private List<RoomInfo> filterByFacilities(List<RoomInfo> rooms, List<Long> facilityIds) {
        if (roomFacilityMapper == null || facilityIds.isEmpty()) {
            return rooms;
        }

        Set<Long> roomIdsWithFacilities = new HashSet<>();
        for (Long facilityId : facilityIds) {
            LambdaQueryWrapper<RoomFacility> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomFacility::getFacilityId, facilityId);
            List<RoomFacility> roomFacilities = roomFacilityMapper.selectList(queryWrapper);
            roomFacilities.forEach(rf -> roomIdsWithFacilities.add(rf.getRoomId()));
        }

        return rooms.stream()
            .filter(r -> roomIdsWithFacilities.contains(r.getId()))
            .collect(Collectors.toList());
    }

    /**
     * 根据房间标签过滤房源
     */
    private List<RoomInfo> filterByLabels(List<RoomInfo> rooms, List<Long> labelIds) {
        if (roomLabelMapper == null || labelIds.isEmpty()) {
            return rooms;
        }

        Set<Long> roomIdsWithLabels = new HashSet<>();
        for (Long labelId : labelIds) {
            LambdaQueryWrapper<RoomLabel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomLabel::getLabelId, labelId);
            List<RoomLabel> roomLabels = roomLabelMapper.selectList(queryWrapper);
            roomLabels.forEach(rl -> roomIdsWithLabels.add(rl.getRoomId()));
        }

        return rooms.stream()
            .filter(r -> roomIdsWithLabels.contains(r.getId()))
            .collect(Collectors.toList());
    }

    /**
     * 获取相似房源
     */
    @Override
    public List<RoomRecommendation> getSimilarRooms(Long userId, Long roomId) {
        log.info("获取相似房源，用户ID: {}, 房源ID: {}", userId, roomId);

        List<RoomRecommendation> similarRooms = new ArrayList<>();

        try {
            if (roomInfoMapper == null || apartmentInfoMapper == null) {
                return Collections.emptyList();
            }

            // 1. 查询指定房源的信息
            RoomInfo baseRoom = roomInfoMapper.selectById(roomId);
            if (baseRoom == null) {
                return Collections.emptyList();
            }

            // 2. 查询公寓信息
            ApartmentInfo apartment = apartmentInfoMapper.selectById(baseRoom.getApartmentId());
            if (apartment == null) {
                return Collections.emptyList();
            }

            // 3. 构建查询条件（相同地区，相近价格）
            KeywordExtractionResult keywords = new KeywordExtractionResult();
            keywords.setDistrictIds(List.of(apartment.getDistrictId()));
            
            int basePrice = baseRoom.getRent().intValue();
            keywords.setPriceRange(new KeywordExtractionResult.PriceRange(
                Math.max(basePrice - 500, 0),
                basePrice + 500
            ));

            // 4. 查询相似房源
            similarRooms = getRecommendations(userId, keywords);

            // 5. 排除基础房源本身
            similarRooms = similarRooms.stream()
                .filter(r -> !r.getRoomId().equals(roomId))
                .collect(Collectors.toList());

            log.info("相似房源查询完成，相似房源数: {}", similarRooms.size());
            return similarRooms;

        } catch (Exception e) {
            log.error("获取相似房源失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算房源与关键词的匹配度
     */
    @Override
    public Double calculateMatchScore(Long roomId, KeywordExtractionResult keywords) {
        log.debug("计算房源 {} 的匹配度", roomId);

        try {
            if (roomInfoMapper == null || apartmentInfoMapper == null) {
                return 0.0;
            }

            RoomInfo room = roomInfoMapper.selectById(roomId);
            if (room == null) {
                return 0.0;
            }

            ApartmentInfo apartment = apartmentInfoMapper.selectById(room.getApartmentId());
            if (apartment == null) {
                return 0.0;
            }

            return calculateMatchScore(room, apartment, keywords);

        } catch (Exception e) {
            log.error("计算匹配度失败", e);
            return 0.0;
        }
    }

    /**
     * 计算房源与关键词的匹配度（内部方法）
     * 
     * 匹配度计算规则:
     * - 地区匹配: 25%
     * - 价格匹配: 25%
     * - 房间属性匹配: 20%
     * - 房间配套匹配: 15%
     * - 房间标签匹配: 10%
     * - 发布者类型匹配: 5%
     */
    private double calculateMatchScore(RoomInfo room, ApartmentInfo apartment, KeywordExtractionResult keywords) {
        double score = 0.0;

        // 1. 地区匹配度（权重25%）
        if (keywords.getDistrictIds() != null && keywords.getDistrictIds().contains(apartment.getDistrictId())) {
            score += 0.25;
        }

        // 2. 价格匹配度（权重25%）
        if (keywords.getPriceRange() != null) {
            BigDecimal rent = room.getRent();
            int min = keywords.getPriceRange().getMin();
            int max = keywords.getPriceRange().getMax();
            
            if (rent.compareTo(BigDecimal.valueOf(min)) >= 0 && 
                rent.compareTo(BigDecimal.valueOf(max)) <= 0) {
                score += 0.25;
            } else if (rent.compareTo(BigDecimal.valueOf(min)) >= 0 && 
                       rent.compareTo(BigDecimal.valueOf(max + 500)) <= 0) {
                // 略高于预算范围，给予部分分数
                score += 0.15;
            } else if (rent.compareTo(BigDecimal.valueOf(Math.max(min - 500, 0))) >= 0 && 
                       rent.compareTo(BigDecimal.valueOf(min)) < 0) {
                // 略低于预算范围，给予部分分数
                score += 0.15;
            }
        }

        // 3. 房间属性匹配度（权重20%）
        if (keywords.getAttrValueIds() != null && !keywords.getAttrValueIds().isEmpty() && roomAttrValueMapper != null) {
            try {
                int matchedAttrs = 0;
                for (Long attrValueId : keywords.getAttrValueIds()) {
                    LambdaQueryWrapper<RoomAttrValue> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(RoomAttrValue::getRoomId, room.getId());
                    queryWrapper.eq(RoomAttrValue::getAttrValueId, attrValueId);
                    long count = roomAttrValueMapper.selectCount(queryWrapper);
                    if (count > 0) {
                        matchedAttrs++;
                    }
                }
                
                if (matchedAttrs > 0) {
                    double attrScore = (double) matchedAttrs / keywords.getAttrValueIds().size();
                    score += 0.20 * attrScore;
                }
            } catch (Exception e) {
                log.warn("计算房间属性匹配度失败: {}", e.getMessage());
            }
        }

        // 4. 房间配套匹配度（权重15%）
        if (keywords.getFacilityIds() != null && !keywords.getFacilityIds().isEmpty() && roomFacilityMapper != null) {
            try {
                int matchedFacilities = 0;
                for (Long facilityId : keywords.getFacilityIds()) {
                    LambdaQueryWrapper<RoomFacility> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(RoomFacility::getRoomId, room.getId());
                    queryWrapper.eq(RoomFacility::getFacilityId, facilityId);
                    long count = roomFacilityMapper.selectCount(queryWrapper);
                    if (count > 0) {
                        matchedFacilities++;
                    }
                }
                
                if (matchedFacilities > 0) {
                    double facilityScore = (double) matchedFacilities / keywords.getFacilityIds().size();
                    score += 0.15 * facilityScore;
                }
            } catch (Exception e) {
                log.warn("计算房间配套匹配度失败: {}", e.getMessage());
            }
        }

        // 5. 房间标签匹配度（权重10%）
        if (keywords.getLabelIds() != null && !keywords.getLabelIds().isEmpty() && roomLabelMapper != null) {
            try {
                int matchedLabels = 0;
                for (Long labelId : keywords.getLabelIds()) {
                    LambdaQueryWrapper<RoomLabel> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(RoomLabel::getRoomId, room.getId());
                    queryWrapper.eq(RoomLabel::getLabelId, labelId);
                    long count = roomLabelMapper.selectCount(queryWrapper);
                    if (count > 0) {
                        matchedLabels++;
                    }
                }
                
                if (matchedLabels > 0) {
                    double labelScore = (double) matchedLabels / keywords.getLabelIds().size();
                    score += 0.10 * labelScore;
                }
            } catch (Exception e) {
                log.warn("计算房间标签匹配度失败: {}", e.getMessage());
            }
        }

        // 6. 发布者类型匹配度（权重5%）
        if (keywords.getPublisherType() != null && room.getPublisherType() != null) {
            try {
                PublisherType publisherType = PublisherType.valueOf(keywords.getPublisherType());
                if (publisherType.equals(room.getPublisherType())) {
                    score += 0.05;
                }
            } catch (IllegalArgumentException e) {
                log.warn("无效的发布者类型: {}", keywords.getPublisherType());
            }
        }

        return Math.min(score, 1.0);
    }

    /**
     * 将RoomInfo和ApartmentInfo映射到RoomRecommendation
     */
    private RoomRecommendation mapToRecommendation(RoomInfo room, ApartmentInfo apartment) {
        RoomRecommendation recommendation = new RoomRecommendation();

        // 房源基础信息
        recommendation.setRoomId(room.getId());
        recommendation.setRoomNumber(room.getRoomNumber());
        recommendation.setRent(room.getRent());
        recommendation.setReviewStatus(room.getReviewStatus() != null ? room.getReviewStatus().toString() : "");

        // 公寓信息
        recommendation.setApartmentId(apartment.getId());
        recommendation.setApartmentName(apartment.getName());
        recommendation.setAddressDetail(apartment.getAddressDetail());
        recommendation.setLatitude(apartment.getLatitude());
        recommendation.setLongitude(apartment.getLongitude());

        // 组合地理位置信息
        String location = String.format("%s%s%s%s",
            apartment.getProvinceName() != null ? apartment.getProvinceName() : "",
            apartment.getCityName() != null ? apartment.getCityName() : "",
            apartment.getDistrictName() != null ? apartment.getDistrictName() : "",
            apartment.getAddressDetail() != null ? apartment.getAddressDetail() : ""
        );
        recommendation.setLocation(location);

        // 获取房间图片
        if (graphInfoMapper != null) {
            try {
                List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, room.getId());
                if (graphVoList != null && !graphVoList.isEmpty()) {
                    List<String> imageUrls = graphVoList.stream()
                        .map(GraphVo::getUrl)
                        .collect(Collectors.toList());
                    recommendation.setImageUrls(imageUrls);
                } else {
                    recommendation.setImageUrls(new ArrayList<>());
                }
            } catch (Exception e) {
                log.warn("获取房间图片失败，房源ID: {}, 错误: {}", room.getId(), e.getMessage());
                recommendation.setImageUrls(new ArrayList<>());
            }
        }

        // 获取设施信息
        if (facilityInfoMapper != null) {
            try {
                // 注：设施信息通过关联表连接，这里暂不实现
                recommendation.setFacilities(new ArrayList<>());
            } catch (Exception e) {
                log.warn("获取设施信息失败: {}", e.getMessage());
                recommendation.setFacilities(new ArrayList<>());
            }
        }

        // 设置详情页链接
        recommendation.setDetailPageUrl("/room/" + room.getId());

        return recommendation;
    }
}
