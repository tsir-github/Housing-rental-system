package com.atguigu.lease.web.app.interceptor.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租客权限验证注解
 * 用于标记需要特定租客权限才能访问的方法或类（预留）
 * 配合TenantPermissionInterceptor使用
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTenantPermission {
    
    /**
     * 需要的权限代码
     * 可以使用TenantBusinessConfig.Permissions中定义的常量
     */
    String value();
    
    /**
     * 权限描述（可选）
     */
    String description() default "";
}