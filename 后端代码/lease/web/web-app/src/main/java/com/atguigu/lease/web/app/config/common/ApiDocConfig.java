package com.atguigu.lease.web.app.config.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API文档配置类
 * 负责Swagger/Knife4j文档配置
 */
@Configuration
public class ApiDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("租房系统移动端API")
                        .version("2.0")
                        .description("支持租客和房东的移动端API接口")
                        .termsOfService("http://doc.xiaominfo.com")
                        .license(new License().name("Apache 2.0")
                                .url("http://doc.xiaominfo.com")));
    }

    @Bean
    public GroupedOpenApi loginAPI() {
        return GroupedOpenApi.builder().group("登录认证").
                pathsToMatch("/app/login/**", "/app/info", "/app/register").
                build();
    }

    @Bean
    public GroupedOpenApi tenantAPI() {
        return GroupedOpenApi.builder().group("租客功能").
                pathsToMatch(
                        "/app/tenant/**",
                        "/app/history/**",
                        "/app/appointment/**",
                        "/app/agreement/**",
                        "/app/direct-move-in/**"
                ).
                build();
    }

    @Bean
    public GroupedOpenApi landlordAPI() {
        return GroupedOpenApi.builder().group("房东功能").
                pathsToMatch(
                        "/app/landlord/**"
                ).
                build();
    }

    @Bean
    public GroupedOpenApi commonAPI() {
        return GroupedOpenApi.builder().group("共用功能").
                pathsToMatch(
                        "/app/apartment/**",
                        "/app/room/**",
                        "/app/payment/**",
                        "/app/region/**",
                        "/app/term/**",
                        "/app/common/**"
                ).
                build();
    }
}