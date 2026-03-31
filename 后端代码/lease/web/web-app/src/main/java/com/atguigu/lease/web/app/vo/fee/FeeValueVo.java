package com.atguigu.lease.web.app.vo.fee;

import com.atguigu.lease.model.entity.FeeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "杂费值信息")
public class FeeValueVo extends FeeValue {

    @Schema(description = "杂费键名称")
    private String feeKeyName;
}