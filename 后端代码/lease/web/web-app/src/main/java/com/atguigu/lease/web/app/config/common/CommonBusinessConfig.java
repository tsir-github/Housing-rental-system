package com.atguigu.lease.web.app.config.common;

import org.springframework.context.annotation.Configuration;

/**
 * 共用业务配置类
 * 负责系统级通用业务规则配置
 */
@Configuration
public class CommonBusinessConfig {

    /**
     * 用户类型配置
     */
    public static class UserType {
        public static final int OFFICIAL = 1;    // 官方人员
        public static final int LANDLORD = 2;    // 房东
        public static final int TENANT = 3;      // 租客
    }

    /**
     * 通用状态配置
     */
    public static class CommonStatus {
        public static final int ENABLE = 1;      // 启用
        public static final int DISABLE = 0;     // 禁用
        public static final int DELETED = -1;    // 已删除
    }

    /**
     * 审核状态配置
     */
    public static class ReviewStatus {
        public static final int PENDING = 1;     // 待审核
        public static final int APPROVED = 2;    // 审核通过
        public static final int REJECTED = 3;    // 审核拒绝
    }

    /**
     * 发布状态配置
     */
    public static class ReleaseStatus {
        public static final int NOT_RELEASED = 0;  // 未发布
        public static final int RELEASED = 1;      // 已发布
    }

    /**
     * 创建者类型配置
     */
    public static class CreatorType {
        public static final int SYSTEM = 1;      // 系统默认
        public static final int LANDLORD = 2;    // 房东创建
    }

    /**
     * 系统配置常量
     */
    public static class SystemConfig {
        // JWT Token过期时间（秒）
        public static final long JWT_EXPIRATION_TIME = 7 * 24 * 60 * 60; // 7天
        
        // 验证码过期时间（秒）
        public static final long CAPTCHA_EXPIRATION_TIME = 5 * 60; // 5分钟
        
        // 文件上传大小限制（字节）
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        
        // 图片上传大小限制（字节）
        public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
        
        // 分页默认大小
        public static final int DEFAULT_PAGE_SIZE = 10;
        
        // 分页最大大小
        public static final int MAX_PAGE_SIZE = 100;
    }

    /**
     * 缓存配置
     */
    public static class CacheConfig {
        // 用户信息缓存时间（秒）
        public static final long USER_CACHE_TIME = 30 * 60; // 30分钟
        
        // 权限信息缓存时间（秒）
        public static final long PERMISSION_CACHE_TIME = 60 * 60; // 1小时
        
        // 配置信息缓存时间（秒）
        public static final long CONFIG_CACHE_TIME = 24 * 60 * 60; // 24小时
    }
}