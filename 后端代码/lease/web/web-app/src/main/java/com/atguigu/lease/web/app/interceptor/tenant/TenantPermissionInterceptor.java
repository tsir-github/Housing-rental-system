package com.atguigu.lease.web.app.interceptor.tenant;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.config.common.CommonBusinessConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 租客权限验证拦截器
 * 用于验证租客身份和权限（预留）
 * 只针对 /app/tenant/** 路径的接口
 */
@Component
@Slf4j
public class TenantPermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 获取当前登录用户（由AuthenticationInterceptor设置）
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            
            if (loginUser == null) {
                log.warn("租客权限验证失败：用户未登录, URI: {}", request.getRequestURI());
                writeErrorResponse(response, "用户未登录");
                return false;
            }

            // 验证用户状态
            if (!BaseStatus.ENABLE.equals(loginUser.getStatus())) {
                log.warn("租客权限验证失败：用户状态异常, userId: {}, status: {}", 
                    loginUser.getUserId(), loginUser.getStatus());
                writeErrorResponse(response, "用户状态异常");
                return false;
            }

            // 验证用户类型（使用配置常量）
            if (loginUser.getUserType() == null || 
                !loginUser.getUserType().equals(CommonBusinessConfig.UserType.TENANT)) {
                log.warn("租客权限验证失败：非租客用户, userId: {}, userType: {}", 
                    loginUser.getUserId(), loginUser.getUserType());
                writeErrorResponse(response, "无权访问租客功能");
                return false;
            }

            // TODO: 这里可以添加更细粒度的权限检查
            // 例如检查租客的信用等级、状态等
            // checkTenantSpecificRules(loginUser, request);

            // 记录访问日志
            log.info("租客权限验证通过: userId={}, username={}, URI={}", 
                loginUser.getUserId(), loginUser.getUsername(), request.getRequestURI());
            
            return true;
            
        } catch (Exception e) {
            log.error("租客权限验证异常: URI={}", request.getRequestURI(), e);
            writeErrorResponse(response, "权限验证异常");
            return false;
        }
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<Object> result = Result.fail(message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * 检查租客特定规则（预留方法）
     */
    private boolean checkTenantSpecificRules(LoginUser loginUser, HttpServletRequest request) {
        // TODO: 根据租客的信用等级、申请数量等进行业务规则检查
        // 可以结合TenantBusinessConfig中的规则使用
        return true;
    }
}