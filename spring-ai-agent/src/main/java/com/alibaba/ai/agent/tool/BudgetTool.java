package com.alibaba.ai.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class BudgetTool {

    /**
     * 根据目的地、天数和预算估算预算分配。
     */
    @Tool(description = "估算旅行预算分配")
    public String estimateBudget(
            @ToolParam(description = "旅行目的地") String destination,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "总预算，单位人民币") int budget,
            @ToolParam(description = "用户偏好，使用自然语言描述") String preferences
    ) {
        int accommodation = Math.max((int) (budget * 0.45), days * 250);
        int transport = Math.max((int) (budget * 0.25), 300);
        int food = Math.max((int) (budget * 0.2), days * 120);
        int flexible = Math.max(budget - accommodation - transport - food, 0);

        return """
                目的地：%s
                天数：%d
                偏好：%s
                建议预算拆分（人民币）：
                - 住宿：%d
                - 交通：%d
                - 餐饮：%d
                - 弹性支出：%d
                建议：保持预算分配均衡，避免在第一天过度消费。
                """.formatted(destination, days, preferences, accommodation, transport, food, flexible);
    }

}
