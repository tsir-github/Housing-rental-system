package com.atguigu.lease.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.atguigu.lease.model.enums.ItemType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Schema(description = "图片信息表")
@TableName(value = "graph_info")
@Data
public class GraphInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图片名称")
    @TableField(value = "name")
    private String name;

    /**
     * 图片所属对象类型
     * 关联逻辑：
     * - ItemType.APARTMENT (1) = 公寓图片，itemId 指向 apartment_info.id
     * - ItemType.ROOM (2) = 房间图片，itemId 指向 room_info.id
     */
    @Schema(description = "图片所属对象类型")
    @TableField(value = "item_type")
    private ItemType itemType;

    /**
     * 图片所属对象ID
     * 关联逻辑：
     * - 当 itemType = APARTMENT(1) 时，itemId 指向 apartment_info.id
     * - 当 itemType = ROOM(2) 时，itemId 指向 room_info.id
     */
    @Schema(description = "图片所有对象id")
    @TableField(value = "item_id")
    private Long itemId;

    @Schema(description = "图片地址")
    @TableField(value = "url")
    private String url;

}