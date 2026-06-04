package com.alibaba.ai.agent.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetToolTests {

    private final BudgetTool budgetTool = new BudgetTool();

    /**
     * 验证预算工具会返回包含关键预算项的结果。
     */
    @Test
    void shouldEstimateBudgetBreakdown() {
        String result = budgetTool.estimateBudget("杭州", 3, 3000, "美食/轻松");

        assertThat(result)
                .contains("目的地：杭州")
                .contains("住宿")
                .contains("交通")
                .contains("餐饮");
    }

}
