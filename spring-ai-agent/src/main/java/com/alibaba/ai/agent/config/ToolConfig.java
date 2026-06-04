package com.alibaba.ai.agent.config;

import com.alibaba.ai.agent.tool.BudgetTool;
import com.alibaba.ai.agent.tool.CalendarTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册本地工具相关 Bean。
 */
@Configuration
public class ToolConfig {

    /**
     * 注册预算估算工具。
     */
    @Bean
    public BudgetTool budgetTool() {
        return new BudgetTool();
    }

    /**
     * 注册逐日行程生成工具。
     */
    @Bean
    public CalendarTool calendarTool() {
        return new CalendarTool();
    }

}
