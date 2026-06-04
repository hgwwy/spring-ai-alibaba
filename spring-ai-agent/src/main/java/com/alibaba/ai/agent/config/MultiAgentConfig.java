package com.alibaba.ai.agent.config;

import com.alibaba.ai.agent.agent.FlightPlanningAgent;
import com.alibaba.ai.agent.agent.HotelPlanningAgent;
import com.alibaba.ai.agent.agent.TravelCoordinatorAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册多 Agent 示例所需的专家 Agent Bean。
 */
@Configuration
public class MultiAgentConfig {

    /**
     * 在真实模型模式下注册交通专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "false")
    public FlightPlanningAgent flightPlanningAgent(ChatClient chatClient, AgentProperties properties) {
        return new FlightPlanningAgent(chatClient, properties);
    }

    /**
     * 在真实模型模式下注册住宿专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "false")
    public HotelPlanningAgent hotelPlanningAgent(ChatClient chatClient, AgentProperties properties) {
        return new HotelPlanningAgent(chatClient, properties);
    }

    /**
     * 在真实模型模式下注册协调专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "false")
    public TravelCoordinatorAgent travelCoordinatorAgent(ChatClient chatClient, AgentProperties properties) {
        return new TravelCoordinatorAgent(chatClient, properties);
    }

    /**
     * 在 mock 模式下注册交通专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public FlightPlanningAgent mockFlightPlanningAgent(AgentProperties properties) {
        return new FlightPlanningAgent(null, properties);
    }

    /**
     * 在 mock 模式下注册住宿专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public HotelPlanningAgent mockHotelPlanningAgent(AgentProperties properties) {
        return new HotelPlanningAgent(null, properties);
    }

    /**
     * 在 mock 模式下注册协调专家 Agent。
     */
    @Bean
    @ConditionalOnProperty(prefix = "demo.multi-agent", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public TravelCoordinatorAgent mockTravelCoordinatorAgent(AgentProperties properties) {
        return new TravelCoordinatorAgent(null, properties);
    }

}
