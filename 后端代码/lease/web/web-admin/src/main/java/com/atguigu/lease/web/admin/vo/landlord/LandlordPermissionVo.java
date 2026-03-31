package com.atguigu.lease.web.admin.vo.landlord;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 房东权限信息VO（简化版）
 */
@Data
public class LandlordPermissionVo {

    /**
     * 房东用户ID
     */
    private Long landlordId;

    /**
     * 房东手机号
     */
    private String phone;

    /**
     * 房东昵称
     */
    private String nickname;

    /**
     * 房东头像
     */
    private String avatarUrl;

    /**
     * 账户状态：1-正常，2-暂停，3-封禁
     */
    private Integer status;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 权限数量
     */
    private Integer permissionCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否有房东权限
     */
    private Boolean hasLandlordPermission;
}