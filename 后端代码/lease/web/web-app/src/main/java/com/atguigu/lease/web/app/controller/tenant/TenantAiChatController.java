package com.atguigu.lease.web.app.controller.tenant;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.AiConversation;
import com.atguigu.lease.web.app.service.TenantAiChatService;
import com.atguigu.lease.web.app.vo.ai.ChatRequest;
import com.atguigu.lease.web.app.vo.ai.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 租客AI聊天Controller
 * 负责接收HTTP请求、参数验证、调用Service方法、返回响应结果
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/ai")
@Tag(name = "租客AI助手", description = "租客AI聊天、房源推荐等功能")
public class TenantAiChatController {

    @Autowired
    private TenantAiChatService tenantAiChatService;

    /**
     * 发送消息给AI
     * 
     * @param request 聊天请求
     * @param httpRequest HTTP请求（用于获取用户信息）
     * @return 聊天响应
     */
    @PostMapping("/chat")
    @Operation(summary = "发送消息给AI", description = "用户发送消息给AI助手，获取AI回复和房源推荐")
    public Result<ChatResponse> sendMessage(
        @Valid @RequestBody ChatRequest request,
        HttpServletRequest httpRequest
    ) {
        log.info("收到AI聊天请求: {}", request.getMessage());

        try {
            // 从HTTP请求中获取用户ID（从JWT token或session中提取）
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return Result.fail("用户未授权");
            }

            // 验证用户是否为租客角色
            if (!isTenantUser(httpRequest)) {
                return Result.fail("只有租客可以使用AI助手");
            }

            // 调用Service处理业务逻辑
            ChatResponse response = tenantAiChatService.sendMessage(userId, request);
            return Result.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("参数验证失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.fail("系统错误，请稍后重试");
        }
    }

    /**
     * 获取对话历史
     * 
     * @param page 页码（默认1）
     * @param pageSize 每页数量（默认20）
     * @param httpRequest HTTP请求
     * @return 分页结果
     */
    @GetMapping("/history")
    @Operation(summary = "获取对话历史", description = "分页获取用户与AI的对话历史记录")
    public Result<Page<AiConversation>> getConversationHistory(
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer pageSize,
        HttpServletRequest httpRequest
    ) {
        log.info("获取对话历史，页码: {}, 每页数量: {}", page, pageSize);

        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return Result.fail("用户未授权");
            }

            if (!isTenantUser(httpRequest)) {
                return Result.fail("只有租客可以查看对话历史");
            }

            Page<AiConversation> result = tenantAiChatService.getConversationHistory(userId, page, pageSize);
            return Result.ok(result);

        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return Result.fail("系统错误，请稍后重试");
        }
    }

    /**
     * 获取指定时间范围内的对话历史
     * 
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @param page 页码
     * @param pageSize 每页数量
     * @param httpRequest HTTP请求
     * @return 分页结果
     */
    @GetMapping("/history/range")
    @Operation(summary = "获取时间范围内的对话历史", description = "分页获取指定时间范围内的对话记录")
    public Result<Page<AiConversation>> getConversationHistoryByDateRange(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer pageSize,
        HttpServletRequest httpRequest
    ) {
        log.info("获取时间范围内的对话历史，时间范围: {} 到 {}", startDate, endDate);

        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return Result.fail("用户未授权");
            }

            if (!isTenantUser(httpRequest)) {
                return Result.fail("只有租客可以查看对话历史");
            }

            Page<AiConversation> result = tenantAiChatService.getConversationHistoryByDateRange(
                userId, startDate, endDate, page, pageSize
            );
            return Result.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("参数验证失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            return Result.fail("系统错误，请稍后重试");
        }
    }

    /**
     * 清空对话记录
     * 
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空对话记录", description = "删除用户的所有对话记录")
    public Result<String> clearConversationHistory(HttpServletRequest httpRequest) {
        log.info("清空对话记录");

        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return Result.fail("用户未授权");
            }

            if (!isTenantUser(httpRequest)) {
                return Result.fail("只有租客可以清空对话记录");
            }

            tenantAiChatService.clearConversationHistory(userId);
            return Result.ok("对话记录已清空");

        } catch (Exception e) {
            log.error("清空对话记录失败", e);
            return Result.fail("系统错误，请稍后重试");
        }
    }

    /**
     * 获取对话统计信息
     * 
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param httpRequest HTTP请求
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取对话统计", description = "获取用户的对话统计信息，包括消息数、token消耗等")
    public Result<TenantAiChatService.ConversationStatistics> getConversationStatistics(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        HttpServletRequest httpRequest
    ) {
        log.info("获取对话统计，时间范围: {} 到 {}", startDate, endDate);

        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return Result.fail("用户未授权");
            }

            if (!isTenantUser(httpRequest)) {
                return Result.fail("只有租客可以查看统计信息");
            }

            TenantAiChatService.ConversationStatistics stats = 
                tenantAiChatService.getConversationStatistics(userId, startDate, endDate);
            return Result.ok(stats);

        } catch (IllegalArgumentException e) {
            log.warn("参数验证失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.fail("系统错误，请稍后重试");
        }
    }

    /**
     * 从HTTP请求中获取当前用户ID
     * 从JWT token中提取用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            String token = request.getHeader("access-token");
            
            // 如果没有access-token请求头，尝试从authorization请求头获取
            if (token == null || token.trim().isEmpty()) {
                String authHeader = request.getHeader("authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7); // 移除"Bearer "前缀
                }
            }
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("Token为空");
                return null;
            }
            
            // 使用JwtUtil解析token获取用户ID
            io.jsonwebtoken.Claims claims = com.atguigu.lease.common.utils.JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            
            log.debug("从token中提取用户ID: {}", userId);
            return userId;
            
        } catch (Exception e) {
            log.warn("从token中提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证用户是否为租客角色
     * 租客是指移动端的普通用户（非房东）
     */
    private boolean isTenantUser(HttpServletRequest request) {
        try {
            // 从LoginUserHolder获取当前登录用户信息
            com.atguigu.lease.common.login.LoginUser loginUser = 
                com.atguigu.lease.common.login.LoginUserHolder.getLoginUser();
            
            if (loginUser == null) {
                log.warn("用户未登录");
                return false;
            }
            
            // 移动端用户默认为租客（userType=1）
            // 房东权限通过landlord_permission表单独管理
            Integer userType = loginUser.getUserType();
            log.debug("用户类型检查 - userId: {}, userType: {}, userType类型: {}", 
                loginUser.getUserId(), userType, userType != null ? userType.getClass().getName() : "null");
            
            boolean isTenant = userType != null && userType.equals(1);
            
            if (!isTenant) {
                log.warn("用户不是租客，userType: {}", userType);
            }
            
            return isTenant;
            
        } catch (Exception e) {
            log.warn("验证用户角色失败: {}", e.getMessage());
            return false;
        }
    }
}
