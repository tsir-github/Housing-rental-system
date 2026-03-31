package com.atguigu.lease.web.app.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI聊天请求VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户输入的消息
     */
    @NotBlank(message = "消息不能为空")
    @Size(max = 1000, message = "消息长度不能超过1000字符")
    private String message;
}
