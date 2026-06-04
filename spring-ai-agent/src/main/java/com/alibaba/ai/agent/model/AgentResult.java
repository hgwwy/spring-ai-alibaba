package com.alibaba.ai.agent.model;

/**
 * 单个专家 Agent 的输出结果。
 *
 * @param agentName Agent 名称
 * @param content Agent 输出内容
 */
public record AgentResult(
        String agentName,
        String content
) {
}
