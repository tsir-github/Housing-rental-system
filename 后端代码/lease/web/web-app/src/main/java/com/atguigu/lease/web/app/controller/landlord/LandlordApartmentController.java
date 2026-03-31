package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.FacilityInfoService;
import com.atguigu.lease.web.app.service.LabelInfoService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房东公寓管理控制器
 */
@Tag(name = "房东公寓管理")
@RestController
@RequestMapping("/app/landlord/apartment")
@Slf4j
public class LandlordApartmentController {

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Autowired
    private LabelInfoService labelInfoService;

    @Operation(summary = "房东创建或更新公寓信息")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ApartmentSubmitVo apartmentSubmitVo, @RequestParam Long landlordId) {
        try {
            log.info("房东公寓保存请求开始 - 房东ID: {}, 公寓数据: {}", landlordId, apartmentSubmitVo);
            
            // 基础参数验证
            if (landlordId == null || landlordId <= 0) {
                log.error("房东ID无效: {}", landlordId);
                return Result.fail("房东ID无效");
            }
            
            if (apartmentSubmitVo == null) {
                log.error("公寓数据为空");
                return Result.fail("公寓数据不能为空");
            }
            
            // 验证必填字段
            if (apartmentSubmitVo.getName() == null || apartmentSubmitVo.getName().trim().isEmpty()) {
                return Result.fail("公寓名称不能为空");
            }
            
            if (apartmentSubmitVo.getIntroduction() == null || apartmentSubmitVo.getIntroduction().trim().isEmpty()) {
                return Result.fail("公寓介绍不能为空");
            }
            
            if (apartmentSubmitVo.getPhone() == null || apartmentSubmitVo.getPhone().trim().isEmpty()) {
                return Result.fail("联系电话不能为空");
            }
            
            if (apartmentSubmitVo.getProvinceId() == null || apartmentSubmitVo.getProvinceId() <= 0) {
                return Result.fail("请选择省份");
            }
            
            if (apartmentSubmitVo.getCityId() == null || apartmentSubmitVo.getCityId() <= 0) {
                return Result.fail("请选择城市");
            }
            
            if (apartmentSubmitVo.getDistrictId() == null || apartmentSubmitVo.getDistrictId() <= 0) {
                return Result.fail("请选择区县");
            }
            
            // 验证权限：如果是更新操作，需要验证公寓是否属于该房东
            if (apartmentSubmitVo.getId() != null) {
                log.info("更新公寓操作 - 公寓ID: {}, 房东ID: {}", apartmentSubmitVo.getId(), landlordId);
                
                ApartmentInfo existingApartment = apartmentInfoService.getById(apartmentSubmitVo.getId());
                if (existingApartment == null) {
                    log.error("公寓不存在 - 公寓ID: {}", apartmentSubmitVo.getId());
                    return Result.fail("公寓不存在");
                }
                
                // 验证公寓所有者权限
                if (existingApartment.getLandlordId() != null && !existingApartment.getLandlordId().equals(landlordId)) {
                    log.error("权限验证失败 - 公寓所有者: {}, 当前房东: {}", existingApartment.getLandlordId(), landlordId);
                    return Result.fail("无权修改该公寓");
                }
            } else {
                log.info("创建新公寓操作 - 房东ID: {}", landlordId);
            }
            
            // 设置房东ID
            apartmentSubmitVo.setLandlordId(landlordId);
            
            // 新创建的公寓默认为未发布状态
            if (apartmentSubmitVo.getId() == null) {
                apartmentSubmitVo.setIsRelease(ReleaseStatus.NOT_RELEASED);
                log.info("设置新公寓为未发布状态");
            }
            
            log.info("开始调用Service层保存公寓信息");
            apartmentInfoService.saveOrUpdateApartment(apartmentSubmitVo);
            log.info("Service层保存完成");
            
            log.info("房东保存公寓信息成功：房东ID={}, 公寓ID={}", landlordId, apartmentSubmitVo.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存公寓信息失败 - 房东ID: {}, 公寓数据: {}", landlordId, apartmentSubmitVo, e);
            
            // 根据异常类型返回不同的错误信息
            String errorMessage = "保存公寓信息失败";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMessage = e.getMessage();
            }
            
            return Result.fail(errorMessage);
        }
    }

    @Operation(summary = "房东分页查询自己的公寓列表")
    @GetMapping("/pageItem")
    public Result<IPage<ApartmentItemVo>> pageItem(
            @RequestParam long current, 
            @RequestParam long size, 
            @RequestParam Long landlordId,
            ApartmentQueryVo queryVo) {
        
        try {
            log.info("房东公寓分页查询：current={}, size={}, landlordId={}, queryVo={}", current, size, landlordId, queryVo);
            
            Page<ApartmentItemVo> page = new Page<>(current, size);
            
            // 设置房东ID过滤条件，确保只查询当前房东的公寓
            queryVo.setLandlordId(landlordId);
            
            IPage<ApartmentItemVo> result = apartmentInfoService.pageItem(page, queryVo);
            
            log.info("房东公寓分页查询结果：total={}, records={}", result.getTotal(), result.getRecords().size());
            
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东公寓列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东根据ID获取公寓详细信息")
    @GetMapping("/getDetailById")
    public Result<ApartmentDetailVo> getDetailById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证公寓是否属于该房东
            ApartmentInfo apartmentInfo = apartmentInfoService.getById(id);
            if (apartmentInfo == null) {
                return Result.fail("公寓不存在");
            }
            
            // 验证公寓所有权
            if (apartmentInfo.getLandlordId() != null && !apartmentInfo.getLandlordId().equals(landlordId)) {
                return Result.fail("无权查看该公寓");
            }
            
            ApartmentDetailVo result = apartmentInfoService.getDetailById(id);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询公寓详情失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东获取公寓编辑详情（用于编辑回显）")
    @GetMapping("/getDetailForEdit")
    public Result<ApartmentDetailVo> getDetailForEdit(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            log.info("房东获取公寓编辑详情：公寓ID={}, 房东ID={}", id, landlordId);
            
            // 调用Service层方法，包含权限验证
            Result<ApartmentDetailVo> result = apartmentInfoService.getApartmentDetailForEdit(id, landlordId);
            
            if (result.getCode() == 200) {
                log.info("获取公寓编辑详情成功：公寓ID={}, 房东ID={}", id, landlordId);
            } else {
                log.warn("获取公寓编辑详情失败：公寓ID={}, 房东ID={}, 错误信息={}", id, landlordId, result.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取公寓编辑详情异常：公寓ID={}, 房东ID={}", id, landlordId, e);
            return Result.fail("获取公寓编辑详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除公寓信息")
    @DeleteMapping("/removeById")
    public Result removeById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证公寓是否属于该房东
            ApartmentInfo apartmentInfo = apartmentInfoService.getById(id);
            if (apartmentInfo == null) {
                return Result.fail("公寓不存在");
            }
            
            // 验证公寓所有权
            if (apartmentInfo.getLandlordId() != null && !apartmentInfo.getLandlordId().equals(landlordId)) {
                return Result.fail("无权删除该公寓");
            }
            
            // 检查公寓下是否还有房间
            LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
            roomQueryWrapper.eq(RoomInfo::getApartmentId, id);
            roomQueryWrapper.eq(RoomInfo::getIsDeleted, 0);
            long roomCount = roomInfoService.count(roomQueryWrapper);
            
            if (roomCount > 0) {
                return Result.fail("该公寓下还有房间，请先删除所有房间后再删除公寓");
            }
            
            apartmentInfoService.removeApartmentById(id);
            
            log.info("房东删除公寓：房东ID={}, 公寓ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除公寓失败", e);
            return Result.fail("删除公寓失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东修改公寓发布状态")
    @PostMapping("/updateReleaseStatusById")
    public Result updateReleaseStatusById(@RequestParam Long id, @RequestParam ReleaseStatus status, @RequestParam Long landlordId) {
        try {
            // 验证公寓是否属于该房东
            ApartmentInfo apartmentInfo = apartmentInfoService.getById(id);
            if (apartmentInfo == null) {
                return Result.fail("公寓不存在");
            }
            
            // 验证公寓所有权
            if (apartmentInfo.getLandlordId() != null && !apartmentInfo.getLandlordId().equals(landlordId)) {
                return Result.fail("无权操作该公寓");
            }
            
            LambdaUpdateWrapper<ApartmentInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ApartmentInfo::getId, id);
            updateWrapper.set(ApartmentInfo::getIsRelease, status);
            
            apartmentInfoService.update(updateWrapper);
            
            log.info("房东修改公寓发布状态：房东ID={}, 公寓ID={}, 状态={}", landlordId, id, status);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东修改公寓发布状态失败", e);
            return Result.fail("修改发布状态失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询自己的公寓基础信息列表")
    @GetMapping("/listMyApartments")
    public Result<List<ApartmentInfo>> listMyApartments(@RequestParam Long landlordId) {
        try {
            LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
            // 添加房东ID过滤条件
            queryWrapper.eq(ApartmentInfo::getLandlordId, landlordId);
            queryWrapper.eq(ApartmentInfo::getIsDeleted, 0);
            queryWrapper.orderByDesc(ApartmentInfo::getCreateTime);
            
            List<ApartmentInfo> list = apartmentInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房东公寓列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询自己的公寓基础信息列表（自动认证）")
    @GetMapping("/listMyApartmentsAuto")
    public Result<List<ApartmentInfo>> listMyApartmentsAuto() {
        try {
            // 从Token自动获取房东ID
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApartmentInfo::getLandlordId, landlordId);
            queryWrapper.eq(ApartmentInfo::getIsDeleted, 0);
            queryWrapper.orderByDesc(ApartmentInfo::getCreateTime);
            
            List<ApartmentInfo> list = apartmentInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房东公寓列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东公寓统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam Long landlordId) {
        try {
            // 统计房东的公寓信息
            LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApartmentInfo::getLandlordId, landlordId);
            queryWrapper.eq(ApartmentInfo::getIsDeleted, 0);
            
            List<ApartmentInfo> apartmentList = apartmentInfoService.list(queryWrapper);
            
            // 统计总公寓数
            long totalApartments = apartmentList.size();
            
            // 统计已发布和未发布的公寓数
            long publishedApartments = apartmentList.stream()
                    .filter(apartment -> ReleaseStatus.RELEASED.equals(apartment.getIsRelease()))
                    .count();
            long unpublishedApartments = totalApartments - publishedApartments;
            
            // 统计房间信息
            LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
            roomQueryWrapper.in(RoomInfo::getApartmentId, 
                    apartmentList.stream().map(ApartmentInfo::getId).toList());
            roomQueryWrapper.eq(RoomInfo::getIsDeleted, 0);
            
            List<RoomInfo> roomList = roomInfoService.list(roomQueryWrapper);
            long totalRooms = roomList.size();
            
            // 构建统计结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalApartments", totalApartments);
            statistics.put("publishedApartments", publishedApartments);
            statistics.put("unpublishedApartments", unpublishedApartments);
            statistics.put("totalRooms", totalRooms);
            
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("查询房东公寓统计失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东获取可用的配套设施列表（用于编辑选择）")
    @GetMapping("/facilities")
    public Result<List<FacilityInfo>> getFacilities(@RequestParam Long landlordId) {
        try {
            log.info("房东获取可用配套设施列表：房东ID={}", landlordId);
            
            // 获取房东可用的配套设施（官方+房东发布）
            List<FacilityInfo> facilities = facilityInfoService.getAvailableFacilitiesForLandlord(landlordId);
            
            log.info("获取配套设施列表成功：房东ID={}, 设施数量={}", landlordId, facilities.size());
            
            return Result.ok(facilities);
        } catch (Exception e) {
            log.error("获取配套设施列表失败：房东ID={}", landlordId, e);
            return Result.fail("获取配套设施列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东获取可用的标签列表（用于编辑选择）")
    @GetMapping("/labels")
    public Result<List<LabelInfo>> getLabels(@RequestParam Long landlordId) {
        try {
            log.info("房东获取可用标签列表：房东ID={}", landlordId);
            
            // 获取房东可用的标签（官方+房东发布）
            List<LabelInfo> labels = labelInfoService.getAvailableLabelsForLandlord(landlordId);
            
            log.info("获取标签列表成功：房东ID={}, 标签数量={}", landlordId, labels.size());
            
            return Result.ok(labels);
        } catch (Exception e) {
            log.error("获取标签列表失败：房东ID={}", landlordId, e);
            return Result.fail("获取标签列表失败：" + e.getMessage());
        }
    }
}