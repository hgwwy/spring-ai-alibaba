package com.alibaba.ai.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 多 Agent 示例
 */
@ConfigurationProperties(prefix = "demo.multi-agent")
public class AgentProperties {

    /**
     * 是否启用 mock 模式。
     */
    private boolean mockEnabled = false;

    /**
     * 协调专家的系统提示词。
     */
    private String coordinatorPrompt = """
            你是旅行规划协调专家。
            请将各个专家 Agent 的结果和工具输出整合为一份清晰、可执行的旅行方案。
            回答务必务实、简洁，并优先给出用户可以直接执行的建议。
            """;

    /**
     * 交通专家的系统提示词。
     */
    private String flightPrompt = """
            你是交通出行专家。
            请重点关注到达方式、市内交通、行程节奏和预算匹配度。
            给出务实、易执行的建议。
            """;

    /**
     * 住宿专家的系统提示词。
     */
    private String hotelPrompt = """
            你是住宿规划专家。
            请重点关注住宿区域、酒店类型、出行便利性和预算匹配度。
            给出务实、易执行的建议。
            """;

    /**
     * 返回是否启用 mock 模式。
     */
    public boolean isMockEnabled() {
        return mockEnabled;
    }

    /**
     * 设置是否启用 mock 模式。
     */
    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    /**
     * 返回协调专家的系统提示词。
     */
    public String getCoordinatorPrompt() {
        return coordinatorPrompt;
    }

    /**
     * 设置协调专家的系统提示词。
     */
    public void setCoordinatorPrompt(String coordinatorPrompt) {
        this.coordinatorPrompt = coordinatorPrompt;
    }

    /**
     * 返回交通专家的系统提示词。
     */
    public String getFlightPrompt() {
        return flightPrompt;
    }

    /**
     * 设置交通专家的系统提示词。
     */
    public void setFlightPrompt(String flightPrompt) {
        this.flightPrompt = flightPrompt;
    }

    /**
     * 返回住宿专家的系统提示词。
     */
    public String getHotelPrompt() {
        return hotelPrompt;
    }

    /**
     * 设置住宿专家的系统提示词。
     */
    public void setHotelPrompt(String hotelPrompt) {
        this.hotelPrompt = hotelPrompt;
    }

}
