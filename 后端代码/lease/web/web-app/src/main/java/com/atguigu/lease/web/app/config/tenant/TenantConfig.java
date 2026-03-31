package com.atguigu.lease.web.app.config.tenant;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 租客专用配置类
 * 负责租客相关的拦截器和配置
 */
@Configuration
public class TenantConfig implements WebMvcConfigurer {

    // 目前租客功能使用通用认证即可
    // 如果后续需要租客特定的权限控制，可以在这里添加
    
    // 示例：租客特定拦截器
    // @Autowired
    // private TenantPermissionInterceptor tenantPermissionInterceptor;
    
    // @Override
    // public void addInterceptors(InterceptorRegistry registry) {
    //     registry.addInterceptor(tenantPermissionInterceptor)
    //             .addPathPatterns("/app/tenant/**")
    //             .order(3);
    // }
}