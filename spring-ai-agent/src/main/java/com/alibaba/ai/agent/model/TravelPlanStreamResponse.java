package com.alibaba.ai.agent.model;

import java.util.List;

/**
 * 旅行规划流式接口的完整收尾结果。
 *
 * @param summary 最终汇总说明
 * @param transportSuggestion 交通建议
 * @param hotelSuggestion 住宿建议
 * @param itineraryDraft 逐日行程草案
 * @param estimatedBudget 预算拆分说明
 * @param agentTrace Agent 执行轨迹
 * @param mock 是否为 mock 模式结果
 */
public record TravelPlanStreamResponse(
        String summary,
        String transportSuggestion,
        String hotelSuggestion,
        List<String> itineraryDraft,
        String estimatedBudget,
        List<String> agentTrace,
        boolean mock
) {
}
