package com.alibaba.ai.agent.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarToolTests {

    private final CalendarTool calendarTool = new CalendarTool();

    /**
     * 验证行程工具会为每天生成一行行程。
     */
    @Test
    void shouldCreateOneLinePerDay() {
        String result = calendarTool.draftItinerary("杭州", 3, "美食/轻松");

        assertThat(result.lines().count()).isEqualTo(3);
        assertThat(result).contains("第1天").contains("第3天");
    }

}
