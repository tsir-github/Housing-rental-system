package com.atguigu.lease.web.app.vo.apartment;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 公寓列表项VO
 */
@Schema(description = "公寓列表项信息")
@Data
public class ApartmentItemVo extends ApartmentInfo implements Serializable {

    private static final long serialVersionUID = 4L; // 更新版本号

    @Schema(description = "公寓图片列表")
    private List<GraphVo> graphVoList;

    @Schema(description = "公寓标签列表")
    private List<LabelInfo> labelInfoList;

    @Schema(description = "公寓配套列表")
    private List<FacilityInfo> facilityInfoList;

    @Schema(description = "公寓最低租金")
    private BigDecimal minRent;

    @Schema(description = "公寓杂费列表")
    private List<FeeValueVo> feeValueVoList;

    @Schema(description = "房源总数")
    private Integer roomCount;

    @Schema(description = "可用房源数")
    private Integer availableRoomCount;

    /**
     * 重写父类的getPublisherType方法，确保序列化时忽略此字段
     * @return 发布者类型：1-官方，2-房东
     */
    @Override
    @JsonIgnore
    public PublisherType getPublisherType() {
        return super.getPublisherType();
    }

}