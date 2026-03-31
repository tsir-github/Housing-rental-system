package com.atguigu.lease.web.app.controller.appointment;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.GuideType;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "租客看房预约")
@RestController
@RequestMapping("/app/appointment")
@Slf4j
public class ViewAppointmentController {

    @Autowired
    private ViewAppointmentService service;

    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        try {
            viewAppointment.setUserId(LoginUserHolder.getLoginUser().getUserId());
            
            // 如果是新建预约，需要根据房源发布者类型设置带看人信息
            if (viewAppointment.getId() == null) {
                Long appointmentId = service.createAppointmentWithGuide(viewAppointment);
                return Result.ok(appointmentId);
            } else {
                // 更新预约
                service.updateById(viewAppointment);
                return Result.ok();
            }
        } catch (Exception e) {
            log.error("保存或更新预约失败", e);
            return Result.fail("操作失败：" + e.getMessage());
        }
    }

    @Operation(summary = "创建预约（支持带看人选择）")
    @PostMapping("/create")
    public Result<Long> createAppointment(@RequestBody ViewAppointment viewAppointment) {
        try {
            viewAppointment.setUserId(LoginUserHolder.getLoginUser().getUserId());
            Long appointmentId = service.createAppointmentWithGuide(viewAppointment);
            return Result.ok(appointmentId);
        } catch (Exception e) {
            log.error("创建预约失败", e);
            return Result.fail("创建预约失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        List<AppointmentItemVo> list = service.listItemByUserId(LoginUserHolder.getLoginUser().getUserId());
        return Result.ok(list);
    }

    @Operation(summary = "根据带看人类型和ID查询预约列表")
    @GetMapping("/listByGuide")
    public Result<List<AppointmentItemVo>> listByGuide(
            @RequestParam Integer guideType,
            @RequestParam Long guideId) {
        try {
            List<AppointmentItemVo> list = service.listByGuideTypeAndId(guideType, guideId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询预约列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据房间ID查询预约列表")
    @GetMapping("/listByRoom")
    public Result<List<AppointmentItemVo>> listByRoom(@RequestParam Long roomId) {
        try {
            List<AppointmentItemVo> list = service.listByRoomId(roomId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房间预约列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "处理预约（确认/拒绝）")
    @PostMapping("/process")
    public Result processAppointment(
            @RequestParam Long appointmentId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remarks) {
        try {
            service.processAppointment(appointmentId, status, remarks);
            return Result.ok();
        } catch (Exception e) {
            log.error("处理预约失败", e);
            return Result.fail("处理失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取官方预约列表")
    @GetMapping("/official")
    public Result<List<AppointmentItemVo>> listOfficialAppointments() {
        try {
            List<AppointmentItemVo> list = service.listOfficialAppointments();
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询官方预约列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取房东预约列表")
    @GetMapping("/landlord")
    public Result<List<AppointmentItemVo>> listLandlordAppointments(@RequestParam Long landlordId) {
        try {
            List<AppointmentItemVo> list = service.listLandlordAppointments(landlordId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房东预约列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(@RequestParam Long id) {
        AppointmentDetailVo appointmentDetailVo = service.getDetailById(id);
        return Result.ok(appointmentDetailVo);
    }

    @Operation(summary = "获取房源可选的带看人类型")
    @GetMapping("/getGuideOptions")
    public Result<List<Map<String, Object>>> getGuideOptions(@RequestParam Long roomId) {
        try {
            List<Map<String, Object>> options = service.getGuideOptionsByRoomId(roomId);
            return Result.ok(options);
        } catch (Exception e) {
            log.error("获取带看人选项失败", e);
            return Result.fail("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查房源预约可用性（含友好提示）")
    @GetMapping("/checkRoomAvailability/{roomId}")
    public Result<RoomAvailabilityVo> checkRoomAvailabilityForAppointment(@PathVariable Long roomId) {
        try {
            RoomAvailabilityVo availability = service.checkRoomAvailabilityForAppointment(roomId);
            return Result.ok(availability);
        } catch (Exception e) {
            log.error("检查房源预约可用性失败", e);
            return Result.fail("检查失败：" + e.getMessage());
        }
    }

}

