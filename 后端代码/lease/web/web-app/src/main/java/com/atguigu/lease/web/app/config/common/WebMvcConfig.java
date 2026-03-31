package com.atguigu.lease.web.app.config.common;

import com.atguigu.lease.web.app.converter.StringToBaseEnumConverterFactory;
import com.atguigu.lease.web.app.interceptor.common.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用Web配置类
 * 负责系统级通用拦截器配置和枚举转换器配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    
    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 通用身份认证拦截器 - 适用于所有需要登录的接口
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/app/**", "/api/**")  // 拦截所有移动端接口和API接口
                .excludePathPatterns(
                        "/app/login/**",         // 排除登录相关接口
                        "/app/register",         // 排除注册接口
                        "/app/captcha",          // 排除验证码接口
                        "/app/public/**",        // 排除公共接口
                        "/app/common/**",        // 排除共用接口
                        "/api/login/**",         // 排除API登录接口
                        "/api/public/**"         // 排除API公共接口
                )
                .order(1);  // 最高优先级
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 使用通用枚举转换器工厂，自动处理所有实现BaseEnum接口的枚举类型
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }
}