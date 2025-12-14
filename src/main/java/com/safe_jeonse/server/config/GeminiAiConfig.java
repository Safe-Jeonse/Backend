package com.safe_jeonse.server.config;

import com.safe_jeonse.server.service.GeminiChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GeminiAiConfig {

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Value("${spring.ai.gemini.base-url}")
    private String baseUrl;

    @Value("${spring.ai.gemini.model}")
    private String model;

    @Primary
    @Bean("geminiChatModel")
    public ChatModel chatModel() {
        return new GeminiChatModel(apiKey, baseUrl, model);
    }
}

