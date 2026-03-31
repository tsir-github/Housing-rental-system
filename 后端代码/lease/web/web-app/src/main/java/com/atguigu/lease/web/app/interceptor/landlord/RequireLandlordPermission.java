package com.atguigu.lease.web.app.interceptor.landlord;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 房东权限验证注解
 * 用于标记需要特定房东权限才能访问的方法或类
 * 配合LandlordPermissionInterceptor使用
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLandlordPermission {
    
    /**
     * 需要的权限代码
     * 可以使用LandlordBusinessConfig.Permissions中定义的常量
     */
    String value();
    
    /**
     * 权限描述（可选）
     */
    String description() default "";
}