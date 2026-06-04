package com.alibaba.ai.agent.controller;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多 Agent 旅行规划接口。
 */
@RestController
@RequestMapping("/api/agents")
public class MultiAgentDemoController {

    /**
     * 负责执行多 Agent 旅行规划流程的服务。
     */
    private final MultiAgentDemoService multiAgentDemoService;

    /**
     * 注入多 Agent 示例服务。
     */
    public MultiAgentDemoController(MultiAgentDemoService multiAgentDemoService) {
        this.multiAgentDemoService = multiAgentDemoService;
    }

    /**
     * 接收旅行规划请求并返回多 Agent 生成的结构化结果。
     */
    @PostMapping("/travel-plan")
    public TravelPlanResponse createTravelPlan(@Valid @RequestBody TravelPlanRequest request) {
        return multiAgentDemoService.createPlan(request);
    }

}
