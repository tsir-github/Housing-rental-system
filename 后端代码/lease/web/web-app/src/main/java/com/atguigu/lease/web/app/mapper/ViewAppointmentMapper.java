package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author liubo
* @description 针对表【view_appointment(预约看房信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.ViewAppointment
*/
public interface ViewAppointmentMapper extends BaseMapper<ViewAppointment> {

    List<AppointmentItemVo> listItemByUserId(Long userId);

    /**
     * 根据带看人类型查询预约列表
     */
    List<AppointmentItemVo> listByGuideType(@Param("guideType") Integer guideType);

    /**
     * 根据带看人类型和ID查询预约列表
     */
    List<AppointmentItemVo> listByGuideTypeAndId(@Param("guideType") Integer guideType, 
                                                @Param("guideId") Long guideId);

    /**
     * 根据房间ID查询预约列表
     */
    List<AppointmentItemVo> listByRoomId(@Param("roomId") Long roomId);

    /**
     * 查询房东的预约列表（分页）
     */
    IPage<AppointmentItemVo> selectLandlordAppointments(Page<AppointmentItemVo> page, 
                                                       @Param("landlordId") Long landlordId, 
                                                       @Param("status") Integer status);

    /**
     * 查询房东的预约列表（不分页）
     */
    List<AppointmentItemVo> selectLandlordAppointmentsList(@Param("landlordId") Long landlordId, 
                                                          @Param("status") Integer status);

    /**
     * 统计房东的预约数量
     */
    long countLandlordAppointments(@Param("landlordId") Long landlordId);

    /**
     * 统计房东指定状态的预约数量
     */
    long countLandlordAppointmentsByStatus(@Param("landlordId") Long landlordId, @Param("status") Integer status);

    /**
     * 统计房东今日的预约数量
     */
    long countLandlordTodayAppointments(@Param("landlordId") Long landlordId);
}




