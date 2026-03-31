package com.atguigu.lease.web.admin.mapper;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.atguigu.lease.web.admin.vo.appointment.AppointmentVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Mapper
* @createDate 2023-07-24 15:48:00
* @Entity com.atguigu.lease.model.ViewAppointment
*/
public interface ViewAppointmentMapper extends BaseMapper<ViewAppointment> {

    IPage<AppointmentVo> pageAppointment(Page<AppointmentVo> page, AppointmentQueryVo queryVo);

    /**
     * 根据带看人类型查询预约列表
     */
    List<ViewAppointment> selectByGuideType(@Param("guideType") Integer guideType);

    /**
     * 根据带看人ID查询预约列表
     */
    List<ViewAppointment> selectByGuideId(@Param("guideId") Long guideId);

    /**
     * 根据带看人类型和ID查询预约列表
     */
    List<ViewAppointment> selectByGuideTypeAndId(@Param("guideType") Integer guideType, 
                                                @Param("guideId") Long guideId);

    /**
     * 根据房间ID查询预约列表
     */
    List<ViewAppointment> selectByRoomId(@Param("roomId") Long roomId);

    /**
     * 查询官方预约列表
     */
    List<ViewAppointment> selectOfficialAppointments();

    /**
     * 查询房东预约列表
     */
    List<ViewAppointment> selectLandlordAppointments(@Param("landlordId") Long landlordId);
}




