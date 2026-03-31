package com.atguigu.lease.common.login;

import com.atguigu.lease.model.enums.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型
     * 1: 官方人员
     * 2: 房东
     * 3: 租客
     */
    private Integer userType;

    /**
     * 用户状态
     */
    private BaseStatus status;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 部门ID（仅官方人员）
     */
    private Long departmentId;

    /**
     * 岗位ID（仅官方人员）
     */
    private Long postId;
}