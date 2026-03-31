package com.atguigu.lease.web.app.config.landlord;

import com.atguigu.lease.web.app.interceptor.landlord.LandlordPermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 房东专用配置类
 * 负责房东相关的拦截器和配置
 */
@Configuration
public class LandlordConfig implements WebMvcConfigurer {

    @Autowired
    private LandlordPermissionInterceptor landlordPermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 房东权限验证拦截器 - 只针对房东接口
        registry.addInterceptor(landlordPermissionInterceptor)
                .addPathPatterns("/app/landlord/**")  // 只拦截房东相关的接口
                .excludePathPatterns(
                        "/app/landlord/login",        // 排除登录接口
                        "/app/landlord/register",     // 排除注册接口
                        "/app/landlord/profile"       // 排除基础信息接口
                )
                .order(2);  // 在通用认证之后执行
    }
}