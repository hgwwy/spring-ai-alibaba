package com.alibaba.ai.agent.agent;

import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;

/**
 * 专家 Agent 的统一抽象。
 */
public interface SpecialistAgent {

    /**
     * 基于统一上下文生成专家建议。
     */
    AgentResult plan(AgentPlanContext context);

}
