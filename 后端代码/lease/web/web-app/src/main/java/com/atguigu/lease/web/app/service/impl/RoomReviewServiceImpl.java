package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.RoomReview;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.mapper.RoomReviewMapper;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.service.RoomReviewService;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewDetailVo;
import com.atguigu.lease.web.app.vo.landlord.RoomReviewStatusVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author system
 * @description 房源审核服务实现类 - 移动端
 * @createDate 2024-01-05 00:00:00
 */
@Slf4j
@Service
public class RoomReviewServiceImpl extends ServiceImpl<RoomReviewMapper, RoomReview>
        implements RoomReviewService {

    @Autowired
    private RoomReviewMapper roomReviewMapper;

    @Autowired
    private RoomInfoService roomInfoService;

    @Override
    public RoomReview getLatestReviewByRoomId(Long roomId) {
        return roomReviewMapper.selectLatestByRoomId(roomId);
    }

    @Override
    public List<RoomReview> getReviewsByLandlordId(Long landlordId) {
        return roomReviewMapper.selectByLandlordId(landlordId);
    }

    @Override
    public IPage<RoomReviewStatusVo> getLandlordRoomReviewStatus(
            Page<RoomReviewStatusVo> page,
            Long landlordId,
            Integer reviewStatus) {
        
        log.info("查询房东房源审核状态：房东ID={}, 审核状态={}", landlordId, reviewStatus);
        
        IPage<RoomReviewStatusVo> result = roomReviewMapper.selectLandlordRoomReviewStatus(page, landlordId, reviewStatus);
        
        // 为每条记录添加改进建议
        result.getRecords().forEach(record -> {
            String suggestion = getImprovementSuggestion(record.getComplianceScore());
            record.setImprovementSuggestion(suggestion);
        });
        
        log.info("查询房东房源审核状态完成：总数={}", result.getTotal());
        return result;
    }

    @Override
    public RoomReviewDetailVo getRoomReviewDetail(Long roomId, Long landlordId) {
        log.info("获取房源审核详情：房间ID={}, 房东ID={}", roomId, landlordId);
        
        // 获取房间信息
        RoomInfo roomInfo = roomInfoService.getById(roomId);
        if (roomInfo == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 验证房间是否属于该房东
        if (!PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) || 
            !roomInfo.getPublisherId().equals(landlordId)) {
            throw new RuntimeException("无权查看该房间信息");
        }
        
        // 获取公寓信息
        ApartmentInfo apartmentInfo = roomInfoService.getApartmentInfoByRoomId(roomId);
        
        // 获取最新审核记录
        RoomReview latestReview = getLatestReviewByRoomId(roomId);
        
        // 获取审核历史
        List<RoomReview> reviewHistory = getAllReviewHistoryByRoomId(roomId, landlordId);
        
        // 构建返回对象
        RoomReviewDetailVo detailVo = new RoomReviewDetailVo();
        detailVo.setRoomInfo(roomInfo);
        detailVo.setApartmentInfo(apartmentInfo);
        detailVo.setReviewStatus(roomInfo.getReviewStatus());
        detailVo.setReviewStatusDesc(getReviewStatusDesc(roomInfo.getReviewStatus()));
        detailVo.setComplianceScore(roomInfo.getComplianceScore());
        detailVo.setLastReviewedAt(roomInfo.getLastReviewedAt());
        detailVo.setLatestReview(latestReview);
        detailVo.setReviewHistory(reviewHistory);
        
        if (latestReview != null) {
            detailVo.setOverallRating(latestReview.getOverallRating());
            detailVo.setReviewComments(latestReview.getReviewComments());
            detailVo.setPhotoQualityScore(latestReview.getPhotoQualityScore());
            detailVo.setDescriptionAccuracyScore(latestReview.getDescriptionAccuracyScore());
            detailVo.setFacilityCompletenessScore(latestReview.getFacilityCompletenessScore());
            detailVo.setLocationAccuracyScore(latestReview.getLocationAccuracyScore());
            detailVo.setReviewedAt(latestReview.getReviewedAt());
            detailVo.setReviewerName("审核员" + latestReview.getReviewerId());
        }
        
        // 添加改进建议
        String suggestion = getImprovementSuggestion(roomInfo.getComplianceScore());
        detailVo.setImprovementSuggestion(suggestion);
        
        log.info("获取房源审核详情完成：房间ID={}", roomId);
        return detailVo;
    }

    @Override
    public List<RoomReview> getAllReviewHistoryByRoomId(Long roomId, Long landlordId) {
        log.info("获取房源审核历史：房间ID={}, 房东ID={}", roomId, landlordId);
        
        LambdaQueryWrapper<RoomReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomReview::getRoomId, roomId)
                   .eq(RoomReview::getLandlordId, landlordId)
                   .eq(RoomReview::getIsDeleted, 0)
                   .orderByDesc(RoomReview::getCreateTime);
        
        List<RoomReview> reviewHistory = this.list(queryWrapper);
        
        log.info("获取房源审核历史完成：记录数={}", reviewHistory.size());
        return reviewHistory;
    }

    /**
     * 获取审核状态描述
     */
    private String getReviewStatusDesc(com.atguigu.lease.model.enums.ReviewStatus reviewStatus) {
        if (reviewStatus == null) {
            return "未审核";
        }
        
        switch (reviewStatus.getCode()) {
            case 1:
                return "待审核";
            case 2:
                return "审核通过";
            case 3:
                return "审核拒绝";
            default:
                return "未知状态";
        }
    }

    /**
     * 根据符合率评分给出改进建议
     */
    private String getImprovementSuggestion(BigDecimal complianceScore) {
        if (complianceScore == null) {
            return "暂无评分，请等待审核";
        }
        
        double score = complianceScore.doubleValue();
        
        if (score >= 9.0) {
            return "房源质量优秀，继续保持！";
        } else if (score >= 7.5) {
            return "房源质量良好，可适当优化照片和描述";
        } else if (score >= 6.0) {
            return "房源质量一般，建议完善房源信息、增加高质量照片";
        } else if (score >= 4.0) {
            return "房源质量需要改进，请检查基础信息完整性、照片质量和设施描述";
        } else {
            return "房源质量不达标，请完善所有必填信息、上传清晰照片、详细描述设施";
        }
    }
}