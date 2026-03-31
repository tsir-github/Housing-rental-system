package com.atguigu.lease.web.app.vo.room;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 房东房间提交信息VO
 */
@Data
@Schema(description = "房东房间提交信息")
public class RoomSubmitVo extends RoomInfo {

    @Schema(description = "房间图片列表")
    private List<GraphVo> graphVoList;

    @Schema(description = "房间属性信息列表")
    private List<Long> attrValueIds;

    @Schema(description = "房间配套信息列表")
    private List<Long> facilityInfoIds;

    @Schema(description = "房间标签信息列表")
    private List<Long> labelInfoIds;

    @Schema(description = "支付方式列表")
    private List<Long> paymentTypeIds;

    @Schema(description = "可选租期列表")
    private List<Long> leaseTermIds;

}