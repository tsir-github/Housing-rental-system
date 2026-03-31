package com.atguigu.lease.common.constant;

/**
 * 房东权限常量
 * 定义系统中所有可用的房东权限
 */
public class LandlordPermissions {

    /**
     * 房源管理权限
     */
    public static final String MANAGE_ROOMS = "manage_rooms";

    /**
     * 预约管理权限
     */
    public static final String MANAGE_APPOINTMENTS = "manage_appointments";

    /**
     * 创建租约权限
     */
    public static final String CREATE_LEASE = "create_lease";

    /**
     * 查看申请权限
     */
    public static final String VIEW_APPLICATIONS = "view_applications";

    /**
     * 处理申请权限
     */
    public static final String PROCESS_APPLICATIONS = "process_applications";

    /**
     * 查看统计权限
     */
    public static final String VIEW_STATISTICS = "view_statistics";

    /**
     * 管理租客权限
     */
    public static final String MANAGE_TENANTS = "manage_tenants";

    /**
     * 财务管理权限
     */
    public static final String FINANCIAL_MANAGEMENT = "financial_management";

    /**
     * 房源发布权限
     */
    public static final String PUBLISH_ROOMS = "publish_rooms";

    /**
     * 房源审核权限（房东查看审核状态）
     */
    public static final String VIEW_ROOM_REVIEW = "view_room_review";

    /**
     * 基础权限数组（新房东默认拥有的权限）
     */
    public static final String[] BASIC_PERMISSIONS = {
            MANAGE_ROOMS,
            MANAGE_APPOINTMENTS,
            VIEW_APPLICATIONS,
            VIEW_STATISTICS,
            PUBLISH_ROOMS,
            VIEW_ROOM_REVIEW
    };

    /**
     * 高级权限数组（需要特殊授权的权限）
     */
    public static final String[] ADVANCED_PERMISSIONS = {
            CREATE_LEASE,
            PROCESS_APPLICATIONS,
            MANAGE_TENANTS,
            FINANCIAL_MANAGEMENT
    };

    /**
     * 所有权限数组
     */
    public static final String[] ALL_PERMISSIONS = {
            MANAGE_ROOMS,
            MANAGE_APPOINTMENTS,
            CREATE_LEASE,
            VIEW_APPLICATIONS,
            PROCESS_APPLICATIONS,
            VIEW_STATISTICS,
            MANAGE_TENANTS,
            FINANCIAL_MANAGEMENT,
            PUBLISH_ROOMS,
            VIEW_ROOM_REVIEW
    };
}