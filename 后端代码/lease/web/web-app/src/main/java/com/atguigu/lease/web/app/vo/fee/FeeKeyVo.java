package com.atguigu.lease.web.app.vo.fee;

import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.model.entity.FeeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "杂费键信息")
public class FeeKeyVo extends FeeKey {

    @Schema(description = "杂费值列表")
    private List<FeeValue> feeValueList;
}