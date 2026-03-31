package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 房东预约管理控制器
 */
@Tag(name = "房东预约管理")
@RestController
@RequestMapping("/app/landlord/appointment")
@Slf4j
public class LandlordAppointmentController {

    @Autowired
    private ViewAppointmentService viewAppointmentService;

    @Operation(summary = "房东查看自己的预约列表")
    @GetMapping("/list")
    public Result<List<AppointmentItemVo>> getLandlordAppointments(@RequestParam Long landlordId) {
        try {
            List<AppointmentItemVo> appointments = viewAppointmentService.listLandlordAppointments(landlordId);
            return Result.ok(appointments);
        } catch (Exception e) {
            log.error("查询房东预约列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东处理预约（确认/拒绝）")
    @PostMapping("/process")
    public Result processAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Long landlordId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remarks) {
        try {
            // 这里可以添加权限验证，确保只有对应的房东才能处理预约
            viewAppointmentService.processAppointment(appointmentId, status, remarks);
            return Result.ok();
        } catch (Exception e) {
            log.error("房东处理预约失败", e);
            return Result.fail("处理失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东预约统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAppointmentStatistics(@RequestParam Long landlordId) {
        try {
            List<AppointmentItemVo> appointments = viewAppointmentService.listLandlordAppointments(landlordId);
            
            // 统计各种状态的预约数量
            long totalAppointments = appointments.size();
            long waitingAppointments = appointments.stream()
                .filter(apt -> AppointmentStatus.WAITING.equals(apt.getAppointmentStatus()))
                .count();
            long canceledAppointments = appointments.stream()
                .filter(apt -> AppointmentStatus.CANCELED.equals(apt.getAppointmentStatus()))
                .count();
            long viewedAppointments = appointments.stream()
                .filter(apt -> AppointmentStatus.VIEWED.equals(apt.getAppointmentStatus()))
                .count();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalAppointments", totalAppointments);
            statistics.put("waitingAppointments", waitingAppointments);
            statistics.put("canceledAppointments", canceledAppointments);
            statistics.put("viewedAppointments", viewedAppointments);
            statistics.put("confirmationRate", totalAppointments > 0 ? 
                Math.round((viewedAppointments * 100.0 / totalAppointments) * 10.0) / 10.0 : 0.0);
            
            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("查询房东预约统计失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据状态查询房东预约")
    @GetMapping("/listByStatus")
    public Result<List<AppointmentItemVo>> getAppointmentsByStatus(
            @RequestParam Long landlordId,
            @RequestParam Integer status) {
        try {
            List<AppointmentItemVo> allAppointments = viewAppointmentService.listLandlordAppointments(landlordId);
            
            // 根据状态过滤
            List<AppointmentItemVo> filteredAppointments = allAppointments.stream()
                .filter(apt -> apt.getAppointmentStatus() != null && 
                              apt.getAppointmentStatus().getCode().equals(status))
                .collect(Collectors.toList());
            
            return Result.ok(filteredAppointments);
        } catch (Exception e) {
            log.error("根据状态查询房东预约失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东批量处理预约")
    @PostMapping("/batchProcess")
    public Result batchProcessAppointments(
            @RequestParam Long landlordId,
            @RequestBody List<Long> appointmentIds,
            @RequestParam Integer status,
            @RequestParam(required = false) String remarks) {
        try {
            for (Long appointmentId : appointmentIds) {
                viewAppointmentService.processAppointment(appointmentId, status, remarks);
            }
            return Result.ok();
        } catch (Exception e) {
            log.error("房东批量处理预约失败", e);
            return Result.fail("批量处理失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东确认预约并获取租约创建信息")
    @PostMapping("/confirmAndCreateLease")
    public Result<Map<String, Object>> confirmAppointmentAndCreateLease(
            @RequestParam Long appointmentId,
            @RequestParam Long landlordId) {
        try {
            // 先获取预约详情
            AppointmentDetailVo appointmentDetail = viewAppointmentService.getLandlordAppointmentDetail(appointmentId, landlordId);
            
            // 确认预约
            viewAppointmentService.processAppointment(appointmentId, AppointmentStatus.VIEWED.getCode(), "预约已确认，准备创建租约");
            
            // 返回租约创建所需的信息
            Map<String, Object> leaseInfo = new HashMap<>();
            leaseInfo.put("appointmentId", appointmentId);
            leaseInfo.put("name", appointmentDetail.getName());
            leaseInfo.put("phone", appointmentDetail.getPhone());
            leaseInfo.put("apartmentId", appointmentDetail.getApartmentId());
            leaseInfo.put("roomId", appointmentDetail.getRoomId());
            leaseInfo.put("apartmentName", appointmentDetail.getApartmentItemVo() != null ? 
                appointmentDetail.getApartmentItemVo().getName() : "");
            leaseInfo.put("roomNumber", appointmentDetail.getRoomInfo() != null ? 
                appointmentDetail.getRoomInfo().getRoomNumber() : "");
            
            return Result.ok(leaseInfo);
        } catch (Exception e) {
            log.error("确认预约并获取租约信息失败", e);
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查看预约详情")
    @GetMapping("/detail")
    public Result<AppointmentDetailVo> getAppointmentDetail(
            @RequestParam Long appointmentId,
            @RequestParam Long landlordId) {
        AppointmentDetailVo detail = viewAppointmentService.getLandlordAppointmentDetail(appointmentId, landlordId);
        return Result.ok(detail);
    }
}