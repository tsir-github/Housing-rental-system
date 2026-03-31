package com.atguigu.lease.web.app.vo.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 图片信息VO
 * 
 * 关联逻辑说明：
 * 图片通过 graph_info 表与公寓/房间关联：
 * - item_type = 1：公寓图片，item_id 指向 apartment_info.id
 * - item_type = 2：房间图片，item_id 指向 room_info.id
 * 
 * 注意：此VO只包含展示所需的基本字段（id, name, url），
 * 不包含关联字段（item_type, item_id），关联逻辑在Service层处理
 */
@Schema(description = "图片信息")
@Data
public class GraphVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "图片名称")
    private String name;

    @Schema(description = "图片URL")
    private String url;

}