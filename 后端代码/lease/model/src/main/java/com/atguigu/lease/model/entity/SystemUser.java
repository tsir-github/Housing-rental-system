package com.atguigu.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 用于管理官方工作人员信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_user")
public class SystemUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("name")
    private String name;

    /**
     * 用户类型
     * 0: 管理员
     * 1: 普通用户
     */
    @TableField("type")
    private Integer type;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 附加信息
     */
    @TableField("additional_info")
    private String additionalInfo;

    /**
     * 岗位ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户状态
     * 1: 启用
     * 0: 禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     * 1: 已删除
     * 0: 未删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 兼容性方法：判断是否为管理员
     * @return true if admin, false otherwise
     */
    public Integer getIsAdmin() {
        return this.type;
    }

    /**
     * 兼容性方法：设置管理员状态
     * @param isAdmin 管理员状态
     */
    public void setIsAdmin(Integer isAdmin) {
        this.type = isAdmin;
    }
}