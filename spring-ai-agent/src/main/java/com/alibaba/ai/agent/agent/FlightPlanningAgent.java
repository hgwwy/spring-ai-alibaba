package com.alibaba.ai.agent.agent;

import org.springframework.ai.chat.client.ChatClient;

import com.alibaba.ai.agent.config.AgentProperties;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;

import reactor.core.publisher.Flux;

/**
 * 负责生成交通出行建议的专家 Agent。
 */
public class FlightPlanningAgent implements SpecialistAgent {

    /**
     * 真实模型模式下使用的 ChatClient。
     */
    private final ChatClient chatClient;

    /**
     * 当前 Agent 使用的配置项。
     */
    private final AgentProperties properties;

    /**
     * 创建交通专家 Agent。
     */
    public FlightPlanningAgent(ChatClient chatClient, AgentProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    /**
     * 生成交通出行建议。
     */
    @Override
    public AgentResult plan(AgentPlanContext context) {
        if (properties.isMockEnabled()) {
            String content = "建议优先选择到达后换乘方便的交通方案，首日安排轻松节奏，市内以地铁和打车结合为主。";
            return new AgentResult("交通专家", content);
        }

        String content = chatClient.prompt()
                .system(properties.getFlightPrompt())
                .user(context.userPrompt())
                .call()
                .content();
        return new AgentResult("交通专家", content);
    }

    /**
     * 流式生成交通出行建议。
     */
    @Override
    public Flux<String> streamPlan(AgentPlanContext context) {
        if (properties.isMockEnabled()) {
            return Flux.just(
                    "建议优先选择到达后换乘方便的交通方案，",
                    "首日安排轻松节奏，",
                    "市内以地铁和打车结合为主。"
            );
        }

        return chatClient.prompt()
                .system(properties.getFlightPrompt())
                .user(context.userPrompt())
                .stream()
                .content();
    }

}
