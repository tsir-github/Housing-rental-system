package com.atguigu.lease.web.app.vo.apartment;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 房东公寓提交信息VO
 */
@Schema(description = "房东公寓提交信息")
@Data
public class ApartmentSubmitVo extends ApartmentInfo {

    @Schema(description="公寓配套设施ID列表")
    private List<Long> facilityInfoIds;

    @Schema(description="公寓标签ID列表")
    private List<Long> labelIds;

    @Schema(description="公寓杂费值ID列表")
    private List<Long> feeValueIds;

    @Schema(description="公寓杂费信息列表（用于创建新杂费）")
    private List<FeeValueVo> feeValueVoList;

    @Schema(description="公寓图片列表")
    private List<GraphVo> graphVoList;

}