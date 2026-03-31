package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface ViewAppointmentService extends IService<ViewAppointment> {
    List<AppointmentItemVo> listItemByUserId(Long userId);

    AppointmentDetailVo getDetailById(Long id);

    /**
     * 创建预约（支持带看人选择）
     */
    Long createAppointmentWithGuide(ViewAppointment appointment);

    /**
     * 根据带看人类型和ID查询预约列表
     */
    List<AppointmentItemVo> listByGuideTypeAndId(Integer guideType, Long guideId);

    /**
     * 根据房间ID查询预约列表
     */
    List<AppointmentItemVo> listByRoomId(Long roomId);

    /**
     * 处理预约（确认/拒绝）
     */
    void processAppointment(Long appointmentId, Integer status, String remarks);

    /**
     * 获取官方预约列表
     */
    List<AppointmentItemVo> listOfficialAppointments();

    /**
     * 获取房东预约列表
     */
    List<AppointmentItemVo> listLandlordAppointments(Long landlordId);

    /**
     * 根据房间ID获取可选的带看人类型选项
     */
    List<Map<String, Object>> getGuideOptionsByRoomId(Long roomId);

    /**
     * 房东查看预约详情（包含权限验证）
     */
    AppointmentDetailVo getLandlordAppointmentDetail(Long appointmentId, Long landlordId);

    /**
     * 检查房源是否可预约看房（含友好提示）
     */
    RoomAvailabilityVo checkRoomAvailabilityForAppointment(Long roomId);
}
