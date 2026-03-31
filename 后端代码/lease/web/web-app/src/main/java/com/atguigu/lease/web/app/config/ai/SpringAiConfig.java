package com.atguigu.lease.web.app.config.ai;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring AI 配置类
 * 用于配置 OpenAI Chat Model（DeepSeek API 兼容 OpenAI 接口）
 */
@Configuration
@Profile("ai")  // 只在 ai profile 激活时生效
public class SpringAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private Integer maxTokens;

    /**
     * 创建 OpenAiChatModel Bean
     * DeepSeek API 兼容 OpenAI 接口，所以可以直接使用 OpenAiChatModel
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        // 创建 OpenAI API 客户端，指向 DeepSeek API
        // 注意：baseUrl 应该已经包含 /v1，不需要再添加
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
        
        // 配置默认选项
        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        
        // 创建并返回 ChatModel
        return new OpenAiChatModel(openAiApi, defaultOptions);
    }
}
