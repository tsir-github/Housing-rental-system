package com.atguigu.lease.web.admin.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预约看房查询实体")
public class AppointmentQueryVo {

    @Schema(description="预约公寓所在省份")
    private Long provinceId;

    @Schema(description="预约公寓所在城市")
    private Long cityId;

    @Schema(description="预约公寓所在区")
    private Long districtId;

    @Schema(description="预约公寓所在公寓")
    private Long apartmentId;

    @Schema(description="预约用户姓名")
    private String name;

    @Schema(description="预约用户手机号码")
    private String phone;

    @Schema(description="带看人类型：1-官方带看，2-房东带看")
    private Integer guideType;

    @Schema(description="排序字段：appointmentTime-预约时间，createTime-创建时间")
    private String sortField;

    @Schema(description="排序方向：asc-升序，desc-降序")
    private String sortOrder;

}
