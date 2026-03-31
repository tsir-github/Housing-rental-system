package com.atguigu.lease.web.app.vo.appointment;

import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.model.enums.GuideType;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "APP端预约看房基本信息")
public class AppointmentItemVo {

    @Schema(description = "预约Id")
    private Long id;

    @Schema(description = "租客用户ID")
    private Long userId;

    @Schema(description = "租客姓名")
    private String name;

    @Schema(description = "租客手机号")
    private String phone;

    @Schema(description = "预约公寓名称")
    private String apartmentName;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "公寓图片列表")
    private List<GraphVo> graphVoList;

    @Schema(description = "预约时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date appointmentTime;

    @Schema(description = "当前预约状态")
    private AppointmentStatus appointmentStatus;

    @Schema(description = "带看人类型：1-官方，2-房东")
    private GuideType guideType;

    @Schema(description = "带看人ID")
    private Long guideId;

    @Schema(description = "备注信息")
    private String additionalInfo;
}
