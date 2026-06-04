package com.alibaba.ai.agent.agent;

import com.alibaba.ai.agent.config.AgentProperties;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;
import org.springframework.ai.chat.client.ChatClient;

/**
 * 负责生成住宿规划建议的专家 Agent。
 */
public class HotelPlanningAgent implements SpecialistAgent {

    /**
     * 真实模型模式下使用的 ChatClient。
     */
    private final ChatClient chatClient;

    /**
     * 当前 Agent 使用的配置项。
     */
    private final AgentProperties properties;

    /**
     * 创建住宿专家 Agent。
     */
    public HotelPlanningAgent(ChatClient chatClient, AgentProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    /**
     * 生成住宿规划建议。
     */
    @Override
    public AgentResult plan(AgentPlanContext context) {
        if (properties.isMockEnabled()) {
            String content = "建议住在交通便利、靠近餐饮聚集区的商圈酒店，优先选择可步行到地铁站的中档酒店。";
            return new AgentResult("住宿专家", content);
        }

        String content = chatClient.prompt()
                .system(properties.getHotelPrompt())
                .user(context.userPrompt())
                .call()
                .content();
        return new AgentResult("住宿专家", content);
    }

}
