package com.atguigu.lease.web.admin.vo.landlord;

import lombok.Data;

/**
 * 房东权限查询条件VO
 */
@Data
public class LandlordPermissionQueryVo {

    /**
     * 房东ID
     */
    private Long landlordId;

    /**
     * 房东姓名
     */
    private String landlordName;

    /**
     * 权限键
     */
    private String permissionKey;

    /**
     * 权限状态
     * 1: 启用
     * 0: 禁用
     */
    private Integer status;

    /**
     * 创建时间开始
     */
    private String createTimeStart;

    /**
     * 创建时间结束
     */
    private String createTimeEnd;
}