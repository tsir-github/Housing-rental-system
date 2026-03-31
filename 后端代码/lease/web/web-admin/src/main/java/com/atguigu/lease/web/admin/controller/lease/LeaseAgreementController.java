package com.atguigu.lease.web.admin.controller.lease;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "租约管理")
@RestController
@RequestMapping("/admin/agreement")
@Slf4j
public class LeaseAgreementController {
    @Autowired
    private LeaseAgreementService service;

    @Operation(summary = "保存或修改租约信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseAgreement leaseAgreement) {
        try {
            return service.saveOrUpdateLease(leaseAgreement);
        } catch (Exception e) {
            log.error("保存或修改租约信息失败", e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据条件分页查询租约列表")
    @GetMapping("page")
    public Result<IPage<AgreementVo>> page(@RequestParam long current, @RequestParam long size, AgreementQueryVo queryVo) {
        Page<AgreementVo> page = new Page<>(current, size);
        IPage<AgreementVo> list= service.pageAgreementByQuery(page, queryVo);
        return Result.ok(list);
    }

    @Operation(summary = "获取所有租约列表（官方后台统一查看）", description = "支持按创建者类型、来源类型筛选，显示官方和房东生成的所有合同")
    @GetMapping("all")
    public Result<IPage<AgreementVo>> getAllLeases(@RequestParam long current, @RequestParam long size, AgreementQueryVo queryVo) {
        Page<AgreementVo> page = new Page<>(current, size);
        IPage<AgreementVo> list = service.getAllLeases(page, queryVo);
        return Result.ok(list);
    }

    @Operation(summary = "根据id查询租约信息")
    @GetMapping(name = "getById")
    public Result<AgreementVo> getById(@RequestParam Long id) {
        AgreementVo result=service.getAgreementById(id);
        return Result.ok(result);
    }

    @Operation(summary = "根据id删除租约信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        service.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id更新租约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, 
                                   @RequestParam LeaseStatus status,
                                   @RequestParam(required = false, defaultValue = "true") Boolean approved) {
        try {
            // 如果是从"续约待确认"改为"已签约"，需要判断是同意还是拒绝
            LeaseAgreement currentLease = service.getById(id);
            if (currentLease == null) {
                return Result.fail("租约不存在");
            }
            
            // 如果是续约确认操作（从状态7改为状态2）
            if (currentLease.getStatus() == LeaseStatus.RENEWING && status == LeaseStatus.SIGNED) {
                if (approved) {
                    // 同意续约：调用增强版方法，会自动延长日期
                    service.updateLeaseStatus(id, status.name(), "官方同意续约");
                    return Result.ok("续约已同意，租约日期已自动延长");
                } else {
                    // 拒绝续约：直接更新状态，不延长日期
                    LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(LeaseAgreement::getId, id);
                    updateWrapper.set(LeaseAgreement::getStatus, status);
                    service.update(updateWrapper);
                    service.recordLeaseStatusChange(id, currentLease.getStatus().name(), status.name(), "官方拒绝续约");
                    return Result.ok("续约已拒绝，租约状态已恢复");
                }
            } else {
                // 其他状态变更：直接更新
                LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(LeaseAgreement::getId, id);
                updateWrapper.set(LeaseAgreement::getStatus, status);
                service.update(updateWrapper);
                return Result.ok();
            }
        } catch (Exception e) {
            log.error("更新租约状态失败", e);
            return Result.fail("更新租约状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新租约状态（增强版）", description = "支持记录变更原因和发送通知")
    @PostMapping("updateStatus")
    public Result updateLeaseStatus(@RequestParam Long leaseId, 
                                   @RequestParam String newStatus, 
                                   @RequestParam(required = false) String reason) {
        try {
            service.updateLeaseStatus(leaseId, newStatus, reason);
            return Result.ok("租约状态更新成功");
        } catch (Exception e) {
            log.error("更新租约状态失败", e);
            return Result.fail("更新租约状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量更新租约状态", description = "支持批量操作多个租约状态")
    @PostMapping("batchUpdateStatus")
    public Result batchUpdateStatus(@RequestBody List<Long> leaseIds, 
                                   @RequestParam String newStatus, 
                                   @RequestParam(required = false) String reason) {
        try {
            for (Long leaseId : leaseIds) {
                service.updateLeaseStatus(leaseId, newStatus, reason);
            }
            return Result.ok("批量更新租约状态成功");
        } catch (Exception e) {
            log.error("批量更新租约状态失败", e);
            return Result.fail("批量更新租约状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取租约统计数据", description = "获取各种状态的租约数量统计")
    @GetMapping("statistics")
    public Result<Object> getLeaseStatistics() {
        try {
            Object statistics = service.getLeaseStatistics();
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("获取租约统计数据失败", e);
            return Result.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查即将到期的租约", description = "获取即将到期的租约列表")
    @GetMapping("expiring")
    public Result<List<LeaseAgreement>> getExpiringLeases() {
        try {
            List<LeaseAgreement> expiringLeases = service.checkExpiringLeases();
            return Result.ok(expiringLeases);
        } catch (Exception e) {
            log.error("获取即将到期租约失败", e);
            return Result.fail("获取即将到期租约失败: " + e.getMessage());
        }
    }

    @Operation(summary = "修改租约并重新发送", description = "支持租约修改和重新发送给租客")
    @PostMapping("updateAndResend")
    public Result updateAndResendLease(@RequestParam Long leaseId, 
                                      @RequestBody LeaseAgreement updatedLease) {
        try {
            service.updateAndResendLease(leaseId, updatedLease);
            return Result.ok("租约修改并重新发送成功");
        } catch (Exception e) {
            log.error("租约修改并重新发送失败", e);
            return Result.fail("租约修改失败: " + e.getMessage());
        }
    }

    @Operation(summary = "终止租约", description = "支持租约终止流程（退租）")
    @PostMapping("terminate")
    public Result terminateLease(@RequestParam Long leaseId, 
                                @RequestParam String terminationReason) {
        try {
            service.terminateLease(leaseId, terminationReason);
            return Result.ok("租约终止成功");
        } catch (Exception e) {
            log.error("租约终止失败", e);
            return Result.fail("租约终止失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发送租约到期提醒", description = "手动触发租约到期提醒")
    @PostMapping("sendExpirationReminder")
    public Result sendExpirationReminder(@RequestParam Long leaseId) {
        try {
            LeaseAgreement lease = service.getById(leaseId);
            if (lease == null) {
                return Result.fail("租约不存在");
            }
            service.sendExpirationNotification(lease);
            return Result.ok("到期提醒发送成功");
        } catch (Exception e) {
            log.error("发送到期提醒失败", e);
            return Result.fail("发送到期提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取租约状态变更历史", description = "查看租约的状态变更记录")
    @GetMapping("statusHistory")
    public Result<Object> getLeaseStatusHistory(@RequestParam Long leaseId) {
        try {
            // TODO: 实现状态变更历史查询
            // 这里可以查询专门的日志表或者从系统日志中提取
            return Result.ok("状态变更历史查询功能待实现");
        } catch (Exception e) {
            log.error("获取租约状态变更历史失败", e);
            return Result.fail("获取状态变更历史失败: " + e.getMessage());
        }
    }
}

