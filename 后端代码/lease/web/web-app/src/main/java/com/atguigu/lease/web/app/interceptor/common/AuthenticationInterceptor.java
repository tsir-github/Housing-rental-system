package com.atguigu.lease.web.app.interceptor.common;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 通用身份认证拦截器
 * 负责JWT Token解析和用户身份验证
 * 适用于所有需要登录的接口
 */
@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 添加详细的调试信息
            log.info("=== 认证拦截器调试信息 ===");
            log.info("请求URI: {}", request.getRequestURI());
            log.info("请求方法: {}", request.getMethod());
            
            // 打印所有请求头
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            log.info("所有请求头:");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.info("  {}: {}", headerName, headerValue);
            }
            
            String token = request.getHeader("access-token");
            
            // 如果没有access-token请求头，尝试从authorization请求头获取
            if (token == null || token.trim().isEmpty()) {
                String authHeader = request.getHeader("authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7); // 移除"Bearer "前缀
                    log.info("从authorization请求头获取token: {}", token);
                }
            }
            
            log.info("最终使用的token: {}", token);
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("身份认证失败：Token为空, URI: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            
            // 从数据库查询完整的用户信息
            UserInfo userInfo = userInfoMapper.selectById(userId);
            if (userInfo == null) {
                log.warn("身份认证失败：用户不存在, userId: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 构建LoginUser对象，包含完整的用户信息
            LoginUser loginUser = LoginUser.builder()
                .userId(userId)
                .username(username)
                .userType(1) // 移动端用户默认为租客类型，房东权限通过landlord_permission表验证
                .status(userInfo.getStatus())
                .phone(userInfo.getPhone())
                .avatarUrl(userInfo.getAvatarUrl())
                .build();
            
            LoginUserHolder.setLoginUser(loginUser);
            
            log.debug("身份认证成功: userId={}, username={}, status={}, URI={}", 
                userId, username, userInfo.getStatus(), request.getRequestURI());
            
            return true;
            
        } catch (Exception e) {
            log.error("身份认证异常: URI={}", request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginUserHolder.clear();
    }
}