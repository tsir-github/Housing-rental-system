package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.service.LandlordLeaseService;
import com.atguigu.lease.web.app.vo.landlord.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东租约管理控制器
 * 职责：房东管理自己创建的租约
 * 用户：房东（user_info表，user_type=房东）
 */
@Tag(name = "房东租约管理")
@RestController
@RequestMapping("/app/landlord/leases")
@Slf4j
public class LandlordLeaseController {

    @Autowired
    private LandlordLeaseService landlordLeaseService;

    @Operation(summary = "分页查询房东租约列表")
    @GetMapping("/list")
    public Result<IPage<LandlordLeaseVo>> getLandlordLeases(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            LandlordLeaseQueryVo queryVo) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("查询房东租约列表 - landlordId: {}, current: {}, size: {}", landlordId, current, size);
            
            Page<LandlordLeaseVo> page = new Page<>(current, size);
            IPage<LandlordLeaseVo> result = landlordLeaseService.pageLandlordLeaseByQuery(page, landlordId, queryVo);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东租约列表失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据id查询房东租约基本信息")
    @GetMapping("/detail/{leaseId}")
    public Result<LandlordLeaseVo> getLandlordLeaseDetail(@PathVariable Long leaseId) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("查询房东租约详情 - leaseId: {}, landlordId: {}", leaseId, landlordId);
            
            LandlordLeaseVo result = landlordLeaseService.getLandlordLeaseById(leaseId, landlordId);
            if (result == null) {
                return Result.fail("租约不存在或无权限访问");
            }
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东租约详情失败 - leaseId: {}", leaseId, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据id查询房东租约完整详情")
    @GetMapping("/detail-full/{leaseId}")
    public Result<LandlordLeaseDetailVo> getLandlordLeaseDetailFull(@PathVariable Long leaseId) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("查询房东租约完整详情 - leaseId: {}, landlordId: {}", leaseId, landlordId);
            
            LandlordLeaseDetailVo result = landlordLeaseService.getLandlordLeaseDetailById(leaseId, landlordId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询房东租约完整详情失败 - leaseId: {}", leaseId, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东更新租约状态")
    @PostMapping("/status/{leaseId}")
    public Result<Void> updateLandlordLeaseStatus(
            @PathVariable Long leaseId,
            @RequestParam LeaseStatus status,
            @RequestParam(required = false, defaultValue = "true") Boolean approved,
            @RequestParam(required = false) String reason) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("房东更新租约状态 - leaseId: {}, landlordId: {}, status: {}, approved: {}", leaseId, landlordId, status, approved);
            
            landlordLeaseService.updateLandlordLeaseStatus(leaseId, landlordId, status, approved, reason);
            return Result.ok();
        } catch (Exception e) {
            log.error("更新房东租约状态失败 - leaseId: {}, status: {}", leaseId, status, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取房东租约统计数据")
    @GetMapping("/statistics")
    public Result<LandlordLeaseStatisticsVo> getLandlordLeaseStatistics() {
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("获取房东租约统计数据 - landlordId: {}", landlordId);
            
            LandlordLeaseStatisticsVo statistics = landlordLeaseService.getLandlordLeaseStatistics(landlordId);
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("获取房东租约统计数据失败", e);
            return Result.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取房东即将到期的租约")
    @GetMapping("/expiring")
    public Result<List<LandlordLeaseVo>> getLandlordExpiringLeases() {
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("获取房东即将到期租约 - landlordId: {}", landlordId);
            
            List<LandlordLeaseVo> expiringLeases = landlordLeaseService.getLandlordExpiringLeases(landlordId);
            return Result.ok(expiringLeases);
        } catch (Exception e) {
            log.error("获取房东即将到期租约失败", e);
            return Result.fail("获取即将到期租约失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东创建新租约")
    @PostMapping("/create")
    public Result<Long> createLandlordLease(@RequestBody LandlordLeaseCreateVo createVo) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("房东创建新租约 - landlordId: {}", landlordId);
            
            Long leaseId = landlordLeaseService.createLandlordLease(createVo, landlordId);
            return Result.ok(leaseId);
        } catch (Exception e) {
            log.error("房东创建租约失败", e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东修改租约信息")
    @PutMapping("/update/{leaseId}")
    public Result<Void> updateLandlordLease(
            @PathVariable Long leaseId,
            @RequestBody LandlordLeaseUpdateVo updateVo) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("房东修改租约 - leaseId: {}, landlordId: {}", leaseId, landlordId);
            
            landlordLeaseService.updateLandlordLease(leaseId, updateVo, landlordId);
            return Result.ok();
        } catch (Exception e) {
            log.error("房东修改租约失败 - leaseId: {}", leaseId, e);
            return Result.fail("修改失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东发送租约到期提醒")
    @PostMapping("/reminder/{leaseId}")
    public Result<Void> sendLandlordLeaseReminder(@PathVariable Long leaseId) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("房东发送租约提醒 - leaseId: {}, landlordId: {}", leaseId, landlordId);
            
            landlordLeaseService.sendLandlordLeaseReminder(leaseId, landlordId);
            return Result.ok();
        } catch (Exception e) {
            log.error("发送房东租约提醒失败 - leaseId: {}", leaseId, e);
            return Result.fail("发送提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东重发租约")
    @PostMapping("/resend/{leaseId}")
    public Result<Long> resendLandlordLease(
            @PathVariable Long leaseId,
            @RequestBody(required = false) LandlordLeaseCreateVo createVo) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            if (createVo != null) {
                log.info("房东重发租约（带修改数据） - leaseId: {}, landlordId: {}", leaseId, landlordId);
                Long newLeaseId = landlordLeaseService.resendLandlordLeaseWithData(leaseId, createVo, landlordId);
                return Result.ok(newLeaseId);
            } else {
                log.info("房东重发租约（简单重发） - leaseId: {}, landlordId: {}", leaseId, landlordId);
                Long newLeaseId = landlordLeaseService.resendLandlordLease(leaseId, landlordId);
                return Result.ok(newLeaseId);
            }
        } catch (Exception e) {
            log.error("房东重发租约失败 - leaseId: {}", leaseId, e);
            return Result.fail("重发租约失败: " + e.getMessage());
        }
    }

    @Operation(summary = "房东删除租约")
    @DeleteMapping("/remove/{leaseId}")
    public Result<Void> removeLandlordLease(@PathVariable Long leaseId) {
        
        try {
            // 从当前登录用户获取房东ID
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            Long landlordId = loginUser.getUserId();
            
            log.info("房东删除租约 - leaseId: {}, landlordId: {}", leaseId, landlordId);
            
            landlordLeaseService.removeLandlordLease(leaseId, landlordId);
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除租约失败 - leaseId: {}", leaseId, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
}