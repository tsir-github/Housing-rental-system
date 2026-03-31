package com.atguigu.lease.web.app.vo.appointment;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "APP端预约看房详情")
public class AppointmentDetailVo extends ViewAppointment {

    @Schema(description = "公寓基本信息")
    private ApartmentItemVo apartmentItemVo;

    @Schema(description = "房间详细信息")
    private RoomInfo roomInfo;

    @Schema(description = "租客用户信息")
    private UserInfo userInfo;
}
