package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.model.enums.SystemUserType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.RoomReviewService;
import com.atguigu.lease.web.admin.vo.review.RoomReviewQueryVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewSubmitVo;
import com.atguigu.lease.web.admin.vo.review.RoomReviewVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * @author system
 * @description 房源审核服务实现类
 * @createDate 2024-01-05 00:00:00
 */
@Slf4j
@Service
public class RoomReviewServiceImpl extends ServiceImpl<RoomReviewMapper, RoomReview>
        implements RoomReviewService {

    @Autowired
    private RoomReviewMapper roomReviewMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private SystemPostMapper systemPostMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean validateReviewerPermission(Long reviewerId) {
        SystemUser reviewer = systemUserMapper.selectById(reviewerId);
        if (reviewer == null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if (reviewer.getIsAdmin() != null && reviewer.getIsAdmin() == 1) {
            return true;
        }
        
        // 普通用户检查岗位权限
        if (reviewer.getIsAdmin() == null || reviewer.getIsAdmin() == 0) {
            SystemPost post = systemPostMapper.selectById(reviewer.getPostId());
            return post != null && "官方线下审核员".equals(post.getName());
        }
        
        return false;
    }

    @Override
    public IPage<RoomReviewVo> pageRoomReviewByQuery(IPage<RoomReviewVo> page, RoomReviewQueryVo queryVo) {
        // 如果查询待审核状态，需要查询room_info表中待审核的房源
        if (queryVo.getStatus() != null && queryVo.getStatus() == ReviewStatus.PENDING) {
            return pageRoomReviewForPendingRooms(page, queryVo);
        }
        
        // 其他状态查询已有的审核记录
        LambdaQueryWrapper<RoomReview> queryWrapper = new LambdaQueryWrapper<>();
        
        if (queryVo.getRoomId() != null) {
            queryWrapper.eq(RoomReview::getRoomId, queryVo.getRoomId());
        }
        if (queryVo.getApartmentId() != null) {
            queryWrapper.eq(RoomReview::getApartmentId, queryVo.getApartmentId());
        }
        if (queryVo.getLandlordId() != null) {
            queryWrapper.eq(RoomReview::getLandlordId, queryVo.getLandlordId());
        }
        if (queryVo.getReviewerId() != null) {
            queryWrapper.eq(RoomReview::getReviewerId, queryVo.getReviewerId());
        }
        if (queryVo.getStatus() != null) {
            queryWrapper.eq(RoomReview::getStatus, queryVo.getStatus());
        }
        if (queryVo.getMinComplianceScore() != null) {
            queryWrapper.ge(RoomReview::getComplianceScore, queryVo.getMinComplianceScore());
        }
        if (queryVo.getMaxComplianceScore() != null) {
            queryWrapper.le(RoomReview::getComplianceScore, queryVo.getMaxComplianceScore());
        }
        if (queryVo.getReviewStartTime() != null) {
            queryWrapper.ge(RoomReview::getReviewedAt, queryVo.getReviewStartTime());
        }
        if (queryVo.getReviewEndTime() != null) {
            queryWrapper.le(RoomReview::getReviewedAt, queryVo.getReviewEndTime());
        }

        queryWrapper.orderByDesc(RoomReview::getCreateTime);

        // 创建正确类型的分页对象
        IPage<RoomReview> reviewPage = new Page<>(page.getCurrent(), page.getSize());
        reviewPage = this.page(reviewPage, queryWrapper);
        
        // 转换为VO并填充关联信息
        IPage<RoomReviewVo> voPage = reviewPage.convert(this::convertToVo);
        
        return voPage;
    }

    /**
     * 查询待审核房源（从room_info表查询）
     */
    private IPage<RoomReviewVo> pageRoomReviewForPendingRooms(IPage<RoomReviewVo> page, RoomReviewQueryVo queryVo) {
        // 查询room_info表中待审核的房源
        LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
        roomQueryWrapper.eq(RoomInfo::getReviewStatus, ReviewStatus.PENDING)
                        .eq(RoomInfo::getIsDeleted, 0);
        
        // 如果指定了房东ID，只查询该房东的房源
        if (queryVo.getLandlordId() != null) {
            roomQueryWrapper.eq(RoomInfo::getPublisherType, com.atguigu.lease.model.enums.PublisherType.LANDLORD)
                           .eq(RoomInfo::getPublisherId, queryVo.getLandlordId());
        }
        
        // 如果指定了公寓ID
        if (queryVo.getApartmentId() != null) {
            roomQueryWrapper.eq(RoomInfo::getApartmentId, queryVo.getApartmentId());
        }
        
        // 如果指定了房间ID
        if (queryVo.getRoomId() != null) {
            roomQueryWrapper.eq(RoomInfo::getId, queryVo.getRoomId());
        }
        
        roomQueryWrapper.orderByDesc(RoomInfo::getCreateTime);
        
        // 分页查询房源
        IPage<RoomInfo> roomPage = new Page<>(page.getCurrent(), page.getSize());
        roomPage = roomInfoMapper.selectPage(roomPage, roomQueryWrapper);
        
        // 转换为RoomReviewVo
        IPage<RoomReviewVo> voPage = roomPage.convert(this::convertRoomInfoToReviewVo);
        
        return voPage;
    }

    /**
     * 将RoomInfo转换为RoomReviewVo（用于待审核房源）
     */
    private RoomReviewVo convertRoomInfoToReviewVo(RoomInfo roomInfo) {
        RoomReviewVo vo = new RoomReviewVo();
        
        // 设置基本信息
        vo.setId(null); // 待审核房源没有审核记录ID
        vo.setRoomId(roomInfo.getId());
        vo.setApartmentId(roomInfo.getApartmentId());
        vo.setStatus(ReviewStatus.PENDING);
        vo.setCreateTime(roomInfo.getCreateTime());
        
        // 设置房东ID：如果是房东发布的房源
        if (com.atguigu.lease.model.enums.PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
            vo.setLandlordId(roomInfo.getPublisherId());
        } else {
            vo.setLandlordId(null); // 官方房源没有房东
        }
        
        // 填充房间信息
        vo.setRoomInfo(roomInfo);
        
        // 填充公寓信息
        if (roomInfo.getApartmentId() != null) {
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
            vo.setApartmentInfo(apartmentInfo);
        }
        
        // 填充房东姓名：只有房东房源才有房东信息
        if (vo.getLandlordId() != null) {
            UserInfo landlord = userInfoMapper.selectById(vo.getLandlordId());
            if (landlord != null) {
                vo.setLandlordName(landlord.getNickname());
            }
        } else {
            vo.setLandlordName(null); // 官方房源没有房东姓名
        }
        
        // 初始化审核相关字段为null（待审核状态）
        vo.setReviewerId(null);
        vo.setComplianceScore(null);
        vo.setPhotoQualityScore(null);
        vo.setDescriptionAccuracyScore(null);
        vo.setFacilityCompletenessScore(null);
        vo.setLocationAccuracyScore(null);
        vo.setOverallRating(null);
        vo.setReviewComments(null);
        vo.setRejectionReasons(null);
        vo.setReviewedAt(null);
        vo.setReviewerName(null);
        
        return vo;
    }

    @Override
    @Transactional
    public void submitRoomReview(RoomReviewSubmitVo submitVo) {
        RoomReview roomReview = new RoomReview();
        BeanUtils.copyProperties(submitVo, roomReview);

        // 处理拒绝原因列表
        if (!CollectionUtils.isEmpty(submitVo.getRejectionReasons())) {
            try {
                String rejectionReasonsJson = objectMapper.writeValueAsString(submitVo.getRejectionReasons());
                roomReview.setRejectionReasons(rejectionReasonsJson);
            } catch (JsonProcessingException e) {
                log.error("序列化拒绝原因失败", e);
                throw new RuntimeException("处理拒绝原因失败");
            }
        }

        // 设置审核完成时间
        if (submitVo.getStatus() != ReviewStatus.PENDING) {
            roomReview.setReviewedAt(new Date());
        }

        // 保存或更新审核记录
        this.saveOrUpdate(roomReview);

        // 更新房间的审核状态和评分
        updateRoomReviewStatus(submitVo.getRoomId());
    }

    @Override
    public RoomReviewVo getRoomReviewById(Long id) {
        RoomReview roomReview = this.getById(id);
        if (roomReview == null) {
            return null;
        }
        return convertToVo(roomReview);
    }

    @Override
    public BigDecimal calculateComplianceScore(Long roomId) {
        // 房源符合率评分算法（15%权重）
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = BigDecimal.valueOf(10.0); // 基础分10分

        // 检查必填信息完整性
        if (roomInfo.getRent() == null || roomInfo.getRent().compareTo(BigDecimal.ZERO) <= 0) {
            score = score.subtract(BigDecimal.valueOf(1.0)); // 租金信息缺失扣1分
        }

        // 检查房间图片
        LambdaQueryWrapper<GraphInfo> graphQuery = new LambdaQueryWrapper<>();
        graphQuery.eq(GraphInfo::getItemType, ItemType.ROOM)
                  .eq(GraphInfo::getItemId, roomId);
        List<GraphInfo> graphList = graphInfoMapper.selectList(graphQuery);
        
        if (CollectionUtils.isEmpty(graphList)) {
            score = score.subtract(BigDecimal.valueOf(3.0)); // 无图片扣3分
        } else if (graphList.size() < 3) {
            score = score.subtract(BigDecimal.valueOf(1.0)); // 图片少于3张扣1分
        }

        // 检查设施信息
        List<FacilityInfo> facilityList = facilityInfoMapper.selectListByRoomId(roomId);
        if (CollectionUtils.isEmpty(facilityList)) {
            score = score.subtract(BigDecimal.valueOf(1.0)); // 无设施信息扣1分
        }

        // 检查标签信息
        List<LabelInfo> labelList = labelInfoMapper.selectListByRoomId(roomId);
        if (CollectionUtils.isEmpty(labelList)) {
            score = score.subtract(BigDecimal.valueOf(1.0)); // 无标签信息扣1分
        }

        return score.max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculatePhotoQualityScore(Long roomId) {
        // 照片质量评分算法
        LambdaQueryWrapper<GraphInfo> graphQuery = new LambdaQueryWrapper<>();
        graphQuery.eq(GraphInfo::getItemType, ItemType.ROOM)
                  .eq(GraphInfo::getItemId, roomId);
        List<GraphInfo> graphList = graphInfoMapper.selectList(graphQuery);

        if (CollectionUtils.isEmpty(graphList)) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = BigDecimal.valueOf(8.0); // 基础分8分

        // 根据图片数量调整分数
        int photoCount = graphList.size();
        if (photoCount >= 8) {
            score = score.add(BigDecimal.valueOf(2.0)); // 8张以上加2分
        } else if (photoCount >= 5) {
            score = score.add(BigDecimal.valueOf(1.0)); // 5-7张加1分
        } else if (photoCount < 3) {
            score = score.subtract(BigDecimal.valueOf(2.0)); // 少于3张扣2分
        }

        return score.min(BigDecimal.valueOf(10.0)).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateDescriptionAccuracyScore(Long roomId) {
        // 描述准确性评分算法
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal score = BigDecimal.valueOf(9.0); // 基础分9分

        // 检查房间号是否填写
        if (!StringUtils.hasText(roomInfo.getRoomNumber())) {
            score = score.subtract(BigDecimal.valueOf(1.0));
        }

        return score.setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFacilityCompletenessScore(Long roomId) {
        // 设施完整性评分算法
        List<FacilityInfo> facilityList = facilityInfoMapper.selectListByRoomId(roomId);
        
        BigDecimal score = BigDecimal.valueOf(6.0); // 基础分6分

        if (!CollectionUtils.isEmpty(facilityList)) {
            // 根据设施数量给分
            int facilityCount = facilityList.size();
            if (facilityCount >= 10) {
                score = score.add(BigDecimal.valueOf(4.0)); // 10个以上设施加4分
            } else if (facilityCount >= 5) {
                score = score.add(BigDecimal.valueOf(2.0)); // 5-9个设施加2分
            } else if (facilityCount >= 3) {
                score = score.add(BigDecimal.valueOf(1.0)); // 3-4个设施加1分
            }
        }

        return score.min(BigDecimal.valueOf(10.0)).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateLocationAccuracyScore(Long roomId) {
        // 位置准确性评分算法
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            return BigDecimal.ZERO;
        }

        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
        if (apartmentInfo == null) {
            return BigDecimal.valueOf(5.0); // 公寓信息缺失给5分
        }

        BigDecimal score = BigDecimal.valueOf(8.0); // 基础分8分

        // 检查地址信息完整性
        if (apartmentInfo.getProvinceId() != null && 
            apartmentInfo.getCityId() != null && 
            apartmentInfo.getDistrictId() != null) {
            score = score.add(BigDecimal.valueOf(2.0)); // 地址信息完整加2分
        }

        return score.min(BigDecimal.valueOf(10.0)).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void autoReviewRoom(Long roomId, Long reviewerId) {
        // 自动审核房源
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo == null) {
            throw new RuntimeException("房间信息不存在");
        }

        // 计算各项评分
        BigDecimal complianceScore = calculateComplianceScore(roomId);
        BigDecimal photoQualityScore = calculatePhotoQualityScore(roomId);
        BigDecimal descriptionAccuracyScore = calculateDescriptionAccuracyScore(roomId);
        BigDecimal facilityCompletenessScore = calculateFacilityCompletenessScore(roomId);
        BigDecimal locationAccuracyScore = calculateLocationAccuracyScore(roomId);

        // 计算综合评分（描述准确性占40%权重）
        BigDecimal totalScore = complianceScore.multiply(BigDecimal.valueOf(0.15))
                .add(photoQualityScore.multiply(BigDecimal.valueOf(0.15)))
                .add(descriptionAccuracyScore.multiply(BigDecimal.valueOf(0.4)))
                .add(facilityCompletenessScore.multiply(BigDecimal.valueOf(0.15)))
                .add(locationAccuracyScore.multiply(BigDecimal.valueOf(0.15)));

        // 根据综合评分确定审核状态和评级
        ReviewStatus status;
        Integer overallRating;
        String reviewComments;

        if (totalScore.compareTo(BigDecimal.valueOf(8.0)) >= 0) {
            status = ReviewStatus.APPROVED;
            overallRating = 1; // 优秀
            reviewComments = "系统自动审核：房源质量优秀，符合发布标准";
        } else if (totalScore.compareTo(BigDecimal.valueOf(6.0)) >= 0) {
            status = ReviewStatus.APPROVED;
            overallRating = 2; // 良好
            reviewComments = "系统自动审核：房源质量良好，符合发布标准";
        } else if (totalScore.compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            status = ReviewStatus.APPROVED;
            overallRating = 3; // 一般
            reviewComments = "系统自动审核：房源质量一般，建议优化后发布";
        } else {
            status = ReviewStatus.REJECTED;
            overallRating = 4; // 较差
            reviewComments = "系统自动审核：房源质量不达标，请完善信息后重新提交";
        }

        // 创建审核记录
        RoomReview roomReview = new RoomReview();
        roomReview.setRoomId(roomId);
        roomReview.setApartmentId(roomInfo.getApartmentId());
        roomReview.setLandlordId(roomInfo.getPublisherId());
        roomReview.setReviewerId(reviewerId);
        roomReview.setComplianceScore(complianceScore);
        roomReview.setPhotoQualityScore(photoQualityScore);
        roomReview.setDescriptionAccuracyScore(descriptionAccuracyScore);
        roomReview.setFacilityCompletenessScore(facilityCompletenessScore);
        roomReview.setLocationAccuracyScore(locationAccuracyScore);
        roomReview.setStatus(status);
        roomReview.setOverallRating(overallRating);
        roomReview.setReviewComments(reviewComments);
        roomReview.setReviewedAt(new Date());

        this.save(roomReview);

        // 更新房间审核状态
        updateRoomReviewStatus(roomId);
    }

    @Override
    @Transactional
    public void updateRoomReviewStatus(Long roomId) {
        // 获取最新的审核记录
        RoomReview latestReview = roomReviewMapper.selectLatestByRoomId(roomId);
        
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        if (roomInfo != null && latestReview != null) {
            roomInfo.setReviewStatus(latestReview.getStatus());
            roomInfo.setComplianceScore(latestReview.getComplianceScore());
            roomInfo.setLastReviewedAt(latestReview.getReviewedAt());
            roomInfoMapper.updateById(roomInfo);
            
            // 清除房源详情缓存，确保数据一致性（双删策略）
            try {
                String cacheKey = RedisConstant.APP_ROOM_PREFIX + roomId;
                redisTemplate.delete(cacheKey); // 第一次删除
                
                // 延迟删除缓存
                Thread.sleep(100);
                redisTemplate.delete(cacheKey); // 第二次删除
                
                log.info("房源审核状态更新后清除缓存成功，房源ID: {}", roomId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("缓存双删延迟被中断，房源ID: {}", roomId);
            } catch (Exception e) {
                log.error("房源审核状态更新后清除缓存失败，房源ID: {}", roomId, e);
            }
        }
    }

    /**
     * 转换为VO对象并填充关联信息
     */
    private RoomReviewVo convertToVo(RoomReview roomReview) {
        RoomReviewVo vo = new RoomReviewVo();
        BeanUtils.copyProperties(roomReview, vo);

        // 填充房间信息
        RoomInfo roomInfo = null;
        if (roomReview.getRoomId() != null) {
            roomInfo = roomInfoMapper.selectById(roomReview.getRoomId());
            vo.setRoomInfo(roomInfo);
        }

        // 填充公寓信息 - 优先从房间信息中获取公寓ID
        Long apartmentId = null;
        if (roomInfo != null && roomInfo.getApartmentId() != null) {
            apartmentId = roomInfo.getApartmentId(); // 从房间信息中获取正确的公寓ID
        } else if (roomReview.getApartmentId() != null) {
            apartmentId = roomReview.getApartmentId(); // 备用方案
        }
        
        if (apartmentId != null) {
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);
            vo.setApartmentInfo(apartmentInfo);
        }

        // 填充审核员姓名
        if (roomReview.getReviewerId() != null) {
            SystemUser reviewer = systemUserMapper.selectById(roomReview.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getName());
            }
        }

        // 填充房东姓名
        if (roomReview.getLandlordId() != null) {
            UserInfo landlord = userInfoMapper.selectById(roomReview.getLandlordId());
            if (landlord != null) {
                vo.setLandlordName(landlord.getNickname());
            }
        }

        return vo;
    }
}