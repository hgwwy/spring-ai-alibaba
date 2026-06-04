package com.alibaba.ai.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理 ChatClient 相关 Bean。
 */
@Configuration
public class ChatClientConfig {

    /**
     * 在真实模型模式下创建 ChatClient。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "false")
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

}
