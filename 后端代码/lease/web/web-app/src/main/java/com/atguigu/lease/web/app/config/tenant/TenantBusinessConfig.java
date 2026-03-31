package com.atguigu.lease.web.app.config.tenant;

import org.springframework.context.annotation.Configuration;

/**
 * 租客业务配置类
 * 负责租客相关的业务规则配置
 */
@Configuration
public class TenantBusinessConfig {

    /**
     * 租客权限配置常量（如需要）
     */
    public static class Permissions {
        public static final String APARTMENT_VIEW = "apartment:view";
        public static final String ROOM_VIEW = "room:view";
        public static final String APPLICATION_CREATE = "application:create";
        public static final String APPLICATION_VIEW = "application:view";
        public static final String APPOINTMENT_CREATE = "appointment:create";
        public static final String APPOINTMENT_VIEW = "appointment:view";
        public static final String AGREEMENT_VIEW = "agreement:view";
    }

    /**
     * 租客业务规则配置
     */
    public static class BusinessRules {
        // 租客最大同时申请数量
        public static final int MAX_CONCURRENT_APPLICATIONS = 5;
        
        // 租客最大同时预约数量
        public static final int MAX_CONCURRENT_APPOINTMENTS = 3;
        
        // 租客每日最大申请数量
        public static final int MAX_DAILY_APPLICATIONS = 10;
        
        // 租客每日最大预约数量
        public static final int MAX_DAILY_APPOINTMENTS = 5;
        
        // 租客申请冷却时间（分钟）
        public static final int APPLICATION_COOLDOWN_MINUTES = 30;
        
        // 租客预约提前时间（小时）
        public static final int APPOINTMENT_ADVANCE_HOURS = 2;
    }

    /**
     * 租客状态配置
     */
    public static class Status {
        public static final int ACTIVE = 1;      // 正常
        public static final int SUSPENDED = 2;   // 暂停
        public static final int BANNED = 3;      // 封禁
    }

    /**
     * 租客信用等级配置
     */
    public static class CreditLevel {
        public static final int EXCELLENT = 5;   // 优秀
        public static final int GOOD = 4;        // 良好
        public static final int FAIR = 3;        // 一般
        public static final int POOR = 2;        // 较差
        public static final int BAD = 1;         // 很差
    }
}