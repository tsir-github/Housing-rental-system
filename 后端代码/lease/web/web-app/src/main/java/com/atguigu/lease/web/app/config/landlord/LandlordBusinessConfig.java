package com.atguigu.lease.web.app.config.landlord;

import org.springframework.context.annotation.Configuration;

/**
 * 房东业务配置类
 * 负责房东相关的业务规则配置
 */
@Configuration
public class LandlordBusinessConfig {

    /**
     * 房东权限配置常量
     */
    public static class Permissions {
        public static final String APARTMENT_VIEW = "apartment:view";
        public static final String APARTMENT_CREATE = "apartment:create";
        public static final String APARTMENT_UPDATE = "apartment:update";
        public static final String APARTMENT_DELETE = "apartment:delete";
        
        public static final String ROOM_VIEW = "room:view";
        public static final String ROOM_CREATE = "room:create";
        public static final String ROOM_UPDATE = "room:update";
        public static final String ROOM_DELETE = "room:delete";
        
        public static final String FACILITY_VIEW = "facility:view";
        public static final String FACILITY_CREATE = "facility:create";
        public static final String FACILITY_UPDATE = "facility:update";
        public static final String FACILITY_DELETE = "facility:delete";
        
        public static final String LABEL_VIEW = "label:view";
        public static final String LABEL_CREATE = "label:create";
        public static final String LABEL_UPDATE = "label:update";
        public static final String LABEL_DELETE = "label:delete";
        
        public static final String APPLICATION_VIEW = "application:view";
        public static final String APPLICATION_PROCESS = "application:process";
        
        public static final String APPOINTMENT_VIEW = "appointment:view";
        public static final String APPOINTMENT_PROCESS = "appointment:process";
        
        public static final String STATISTICS_VIEW = "statistics:view";
    }

    /**
     * 房东业务规则配置
     */
    public static class BusinessRules {
        // 房东最大公寓数量限制
        public static final int MAX_APARTMENTS_PER_LANDLORD = 50;
        
        // 房东最大房源数量限制
        public static final int MAX_ROOMS_PER_LANDLORD = 200;
        
        // 房东每日最大发布房源数量
        public static final int MAX_DAILY_ROOM_PUBLISH = 10;
        
        // 房东申请处理超时时间（小时）
        public static final int APPLICATION_TIMEOUT_HOURS = 72;
        
        // 房东预约处理超时时间（小时）
        public static final int APPOINTMENT_TIMEOUT_HOURS = 24;
    }

    /**
     * 房东状态配置
     */
    public static class Status {
        public static final int ACTIVE = 1;      // 正常
        public static final int SUSPENDED = 2;   // 暂停
        public static final int BANNED = 3;      // 封禁
    }
}