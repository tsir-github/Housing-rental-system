package com.atguigu.lease.web.app.vo.attr;

import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 属性名称和属性值VO
 */
@Schema(description = "属性名称和属性值信息")
@Data
public class AttrKeyVo extends AttrKey {

    @Schema(description = "属性值列表")
    private List<AttrValue> attrValueList;

}