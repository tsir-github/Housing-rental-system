package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.model.enums.ReviewStatus;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrKeyVo;
import com.atguigu.lease.web.app.vo.landlord.CustomAttributeRequest;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.atguigu.lease.web.app.vo.room.RoomSubmitVo;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.PaymentType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房东房间管理控制器
 */
@Tag(name = "房东房间管理")
@RestController
@RequestMapping("/app/landlord/room")
@Slf4j
public class LandlordRoomController {

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private AttrKeyService attrKeyService;

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Autowired
    private LabelInfoService labelInfoService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Autowired
    private LeaseTermService leaseTermService;

    @Operation(summary = "房东发布房源")
    @PostMapping("/publish")
    public Result publishRoom(@RequestBody RoomSubmitVo roomSubmitVo, @RequestParam Long landlordId) {
        try {
            // 设置发布者信息
            roomSubmitVo.setPublisherType(PublisherType.LANDLORD);
            roomSubmitVo.setPublisherId(landlordId);
            
            // 新发布的房源默认为未发布状态，等待审核
            roomSubmitVo.setIsRelease(ReleaseStatus.NOT_RELEASED);
            roomSubmitVo.setReviewStatus(ReviewStatus.PENDING);
            
            // 保存房源信息
            roomInfoService.saveOrUpdateRoom(roomSubmitVo);
            
            log.info("房东发布房源成功：房东ID={}, 房源ID={}", landlordId, roomSubmitVo.getId());
            
            return Result.ok("房源发布成功，等待审核");
        } catch (Exception e) {
            log.error("房东发布房源失败", e);
            return Result.fail("发布房源失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看自己发布的房源列表")
    @GetMapping("/my-rooms")
    public Result<IPage<RoomItemVo>> getMyRooms(
            @RequestParam long current, 
            @RequestParam long size, 
            @RequestParam Long landlordId,
            RoomQueryVo queryVo) {
        
        try {
            Page<RoomItemVo> page = new Page<>(current, size);
            
            // 设置查询条件：只查询该房东发布的房源
            if (queryVo == null) {
                queryVo = new RoomQueryVo();
            }
            queryVo.setPublisherType(PublisherType.LANDLORD);
            queryVo.setPublisherId(landlordId);
            
            IPage<RoomItemVo> result = roomInfoService.pageItem(page, queryVo);
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东房源列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看自己发布的房源列表（自动认证）")
    @GetMapping("/my-rooms-auto")
    public Result<IPage<RoomItemVo>> getMyRoomsAuto(
            @RequestParam long current, 
            @RequestParam long size, 
            RoomQueryVo queryVo) {
        
        try {
            // 从Token自动获取房东ID
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            Page<RoomItemVo> page = new Page<>(current, size);
            
            // 设置查询条件：只查询该房东发布的房源
            if (queryVo == null) {
                queryVo = new RoomQueryVo();
            }
            queryVo.setPublisherType(PublisherType.LANDLORD);
            queryVo.setPublisherId(landlordId);
            
            IPage<RoomItemVo> result = roomInfoService.pageItem(page, queryVo);
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东房源列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东根据ID获取房源详细信息")
    @GetMapping("/detail/{roomId}")
    public Result<RoomDetailVo> getRoomDetail(@PathVariable Long roomId, @RequestParam Long landlordId) {
        return roomInfoService.getLandlordRoomDetail(roomId, landlordId);
    }

    @Operation(summary = "房东修改房源信息")
    @PutMapping("/update")
    public Result updateRoom(@RequestBody RoomSubmitVo roomSubmitVo, @RequestParam Long landlordId) {
        try {
            // 验证房源是否属于该房东
            RoomInfo existingRoom = roomInfoService.getById(roomSubmitVo.getId());
            if (existingRoom == null) {
                return Result.fail("房源不存在");
            }
            
            if (!PublisherType.LANDLORD.equals(existingRoom.getPublisherType()) || 
                !existingRoom.getPublisherId().equals(landlordId)) {
                return Result.fail("无权修改该房源");
            }
            
            // 保持发布者信息不变
            roomSubmitVo.setPublisherType(PublisherType.LANDLORD);
            roomSubmitVo.setPublisherId(landlordId);
            
            // 如果房源之前被拒绝，修改后重新设为待审核状态
            if (ReviewStatus.REJECTED.equals(existingRoom.getReviewStatus())) {
                roomSubmitVo.setReviewStatus(ReviewStatus.PENDING);
                roomSubmitVo.setIsRelease(ReleaseStatus.NOT_RELEASED);
            }
            
            roomInfoService.saveOrUpdateRoom(roomSubmitVo);
            
            log.info("房东修改房源成功：房东ID={}, 房源ID={}", landlordId, roomSubmitVo.getId());
            
            return Result.ok("房源修改成功");
        } catch (Exception e) {
            log.error("房东修改房源失败", e);
            return Result.fail("修改房源失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除房源")
    @DeleteMapping("/{roomId}")
    public Result deleteRoom(@PathVariable Long roomId, @RequestParam Long landlordId) {
        try {
            // 验证房源是否属于该房东
            RoomInfo roomInfo = roomInfoService.getById(roomId);
            if (roomInfo == null) {
                return Result.fail("房源不存在");
            }
            
            if (!PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) || 
                !roomInfo.getPublisherId().equals(landlordId)) {
                return Result.fail("无权删除该房源");
            }
            
            // 检查房源是否有活跃的租约
            LambdaQueryWrapper<LeaseAgreement> leaseQueryWrapper = new LambdaQueryWrapper<>();
            leaseQueryWrapper.eq(LeaseAgreement::getRoomId, roomId)
                           .eq(LeaseAgreement::getIsDeleted, 0)
                           .in(LeaseAgreement::getStatus, LeaseStatus.SIGNING, LeaseStatus.SIGNED);
            
            long activeLeaseCount = leaseAgreementService.count(leaseQueryWrapper);
            if (activeLeaseCount > 0) {
                return Result.fail("该房源存在活跃的租约，无法删除");
            }
            
            roomInfoService.removeRoomById(roomId);
            
            log.info("房东删除房源：房东ID={}, 房源ID={}", landlordId, roomId);
            
            return Result.ok("房源删除成功");
        } catch (Exception e) {
            log.error("房东删除房源失败", e);
            return Result.fail("删除房源失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东修改房源发布状态")
    @PostMapping("/update-release-status")
    public Result updateReleaseStatus(@RequestParam Long roomId, 
                                    @RequestParam ReleaseStatus status, 
                                    @RequestParam Long landlordId) {
        return roomInfoService.updateReleaseStatus(roomId, status, landlordId);
    }

    @Operation(summary = "房东重新提交审核")
    @PostMapping("/resubmit/{roomId}")
    public Result resubmitForReview(@PathVariable Long roomId, @RequestParam Long landlordId) {
        return roomInfoService.resubmitForReview(roomId, landlordId);
    }

    @Operation(summary = "房东房源统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getRoomStatistics(@RequestParam Long landlordId) {
        try {
            // 查询房东的所有房源
            LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RoomInfo::getPublisherType, PublisherType.LANDLORD)
                       .eq(RoomInfo::getPublisherId, landlordId)
                       .eq(RoomInfo::getIsDeleted, 0);
            
            List<RoomInfo> roomList = roomInfoService.list(queryWrapper);
            
            // 统计各种状态的房源数量
            long totalRooms = roomList.size();
            long pendingReview = roomList.stream()
                .filter(room -> ReviewStatus.PENDING.equals(room.getReviewStatus()))
                .count();
            long approved = roomList.stream()
                .filter(room -> ReviewStatus.APPROVED.equals(room.getReviewStatus()))
                .count();
            long rejected = roomList.stream()
                .filter(room -> ReviewStatus.REJECTED.equals(room.getReviewStatus()))
                .count();
            long published = roomList.stream()
                .filter(room -> ReleaseStatus.RELEASED.equals(room.getIsRelease()))
                .count();
            
            // 计算平均符合率评分
            double avgComplianceScore = roomList.stream()
                .filter(room -> room.getComplianceScore() != null)
                .mapToDouble(room -> room.getComplianceScore().doubleValue())
                .average()
                .orElse(0.0);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalRooms", totalRooms);
            statistics.put("pendingReview", pendingReview);
            statistics.put("approved", approved);
            statistics.put("rejected", rejected);
            statistics.put("published", published);
            statistics.put("avgComplianceScore", Math.round(avgComplianceScore * 10.0) / 10.0);
            statistics.put("approvalRate", totalRooms > 0 ? 
                Math.round((approved * 100.0 / totalRooms) * 10.0) / 10.0 : 0.0);
            
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("查询房东房源统计失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "清除房源详情缓存")
    @PostMapping("/clear-cache/{roomId}")
    public Result clearRoomCache(@PathVariable Long roomId, @RequestParam Long landlordId) {
        try {
            // 验证房源是否属于该房东
            RoomInfo roomInfo = roomInfoService.getById(roomId);
            if (roomInfo == null) {
                return Result.fail("房源不存在");
            }
            
            if (!PublisherType.LANDLORD.equals(roomInfo.getPublisherType()) || 
                !roomInfo.getPublisherId().equals(landlordId)) {
                return Result.fail("无权操作该房源");
            }
            
            roomInfoService.clearRoomDetailCache(roomId);
            
            log.info("房东清除房源缓存：房东ID={}, 房源ID={}", landlordId, roomId);
            
            return Result.ok("缓存清除成功");
        } catch (Exception e) {
            log.error("清除房源缓存失败", e);
            return Result.fail("清除缓存失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的公寓列表")
    @GetMapping("/available-apartments")
    public Result<List<ApartmentItemVo>> getAvailableApartments(@RequestParam Long landlordId) {
        try {
            List<ApartmentItemVo> apartments = apartmentInfoService.getAvailableApartmentsForLandlord(landlordId);
            return Result.ok(apartments);
        } catch (Exception e) {
            log.error("获取房东可选择的公寓列表失败", e);
            return Result.fail("获取公寓列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的属性列表")
    @GetMapping("/available-attributes")
    public Result<List<AttrKeyVo>> getAvailableAttributes(@RequestParam Long landlordId) {
        try {
            List<AttrKeyVo> attributes = attrKeyService.getAvailableAttributesForLandlord(landlordId);
            return Result.ok(attributes);
        } catch (Exception e) {
            log.error("获取房东可选择的属性列表失败", e);
            return Result.fail("获取属性列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的配套设施列表")
    @GetMapping("/available-facilities")
    public Result<List<FacilityInfo>> getAvailableFacilities(@RequestParam Long landlordId) {
        try {
            List<FacilityInfo> facilities = facilityInfoService.getAvailableFacilitiesForLandlord(landlordId);
            return Result.ok(facilities);
        } catch (Exception e) {
            log.error("获取房东可选择的配套设施列表失败", e);
            return Result.fail("获取配套设施列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的标签列表")
    @GetMapping("/available-labels")
    public Result<List<LabelInfo>> getAvailableLabels(@RequestParam Long landlordId) {
        try {
            List<LabelInfo> labels = labelInfoService.getAvailableLabelsForLandlord(landlordId);
            return Result.ok(labels);
        } catch (Exception e) {
            log.error("获取房东可选择的标签列表失败", e);
            return Result.fail("获取标签列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的支付方式列表")
    @GetMapping("/available-payment-types")
    public Result<List<PaymentType>> getAvailablePaymentTypes(@RequestParam Long landlordId) {
        try {
            List<PaymentType> paymentTypes = paymentTypeService.getAvailablePaymentTypesForLandlord(landlordId);
            return Result.ok(paymentTypes);
        } catch (Exception e) {
            log.error("获取房东可选择的支付方式列表失败", e);
            return Result.fail("获取支付方式列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东可选择的租期列表")
    @GetMapping("/available-lease-terms")
    public Result<List<LeaseTerm>> getAvailableLeaseTerms(@RequestParam Long landlordId) {
        try {
            List<LeaseTerm> leaseTerms = leaseTermService.getAvailableLeaseTermsForLandlord(landlordId);
            return Result.ok(leaseTerms);
        } catch (Exception e) {
            log.error("获取房东可选择的租期列表失败", e);
            return Result.fail("获取租期列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东创建自定义属性")
    @PostMapping("/create-custom-attribute")
    public Result<AttrKeyVo> createCustomAttribute(@RequestBody CustomAttributeRequest request) {
        try {
            // 验证请求参数
            if (request.getLandlordId() == null) {
                return Result.fail("房东ID不能为空");
            }
            
            AttrKeyVo customAttribute = attrKeyService.createCustomAttribute(request);
            
            log.info("房东创建自定义属性成功：房东ID={}, 属性名={}", 
                request.getLandlordId(), request.getAttrKeyName());
            
            return Result.ok(customAttribute);
        } catch (Exception e) {
            log.error("房东创建自定义属性失败", e);
            return Result.fail("创建自定义属性失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取公寓的杂费信息")
    @GetMapping("/apartment-fees/{apartmentId}")
    public Result<List<Map<String, Object>>> getApartmentFees(@PathVariable Long apartmentId, @RequestParam Long landlordId) {
        return apartmentInfoService.getApartmentFeesByIdForLandlord(apartmentId, landlordId);
    }
}