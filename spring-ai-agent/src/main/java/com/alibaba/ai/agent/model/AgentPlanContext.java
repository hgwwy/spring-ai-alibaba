package com.alibaba.ai.agent.model;

/**
 * 传递给各个 Agent 的统一上下文。
 *
 * @param destination 旅行目的地
 * @param days 旅行天数
 * @param budget 总预算，单位人民币
 * @param preferencesText 拼接后的用户偏好文本
 * @param userPrompt 发给模型的用户提示词
 */
public record AgentPlanContext(
        String destination,
        int days,
        int budget,
        String preferencesText,
        String userPrompt
) {
}
