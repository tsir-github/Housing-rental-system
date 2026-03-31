package com.atguigu.lease.web.app.interceptor.landlord;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.LandlordPermissionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 房东权限验证拦截器
 * 用于验证房东身份和权限
 * 只针对 /app/landlord/** 路径的接口
 */
@Component
@Slf4j
public class LandlordPermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 获取当前登录用户（由AuthenticationInterceptor设置）
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            
            if (loginUser == null) {
                log.warn("房东权限验证失败：用户未登录, URI: {}", request.getRequestURI());
                writeErrorResponse(response, "用户未登录");
                return false;
            }

            // 验证用户状态
            if (!BaseStatus.ENABLE.equals(loginUser.getStatus())) {
                log.warn("房东权限验证失败：用户状态异常, userId: {}, status: {}", 
                    loginUser.getUserId(), loginUser.getStatus());
                writeErrorResponse(response, "用户状态异常");
                return false;
            }

            // 验证房东权限 - 检查landlord_permission表
            List<String> permissions = landlordPermissionMapper.selectPermissionsByUserId(loginUser.getUserId());
            
            if (permissions == null || permissions.isEmpty()) {
                log.warn("房东权限验证失败：非房东用户, userId: {}", loginUser.getUserId());
                writeErrorResponse(response, "无权访问房东功能");
                return false;
            }

            // 记录访问日志
            log.info("房东权限验证通过: userId={}, username={}, permissions={}, URI={}", 
                loginUser.getUserId(), loginUser.getUsername(), permissions.size(), request.getRequestURI());
            
            return true;
            
        } catch (Exception e) {
            log.error("房东权限验证异常: URI={}", request.getRequestURI(), e);
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
     * 检查特定权限（预留方法）
     */
    private boolean checkSpecificPermission(LoginUser loginUser, HttpServletRequest request) {
        // TODO: 根据请求路径和用户权限进行细粒度权限检查
        // 可以结合 @RequireLandlordPermission 注解使用
        return true;
    }
}