package com.alibaba.ai.agent.controller;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.model.TravelPlanStreamEvent;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 多 Agent 旅行规划接口，统一使用 /agents 前缀对外暴露同步与流式规划能力。
 */
@RestController
@RequestMapping("/agents")
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
     * 返回一次性旅行规划结果。
     */
    @PostMapping("/travel-plan")
    public TravelPlanResponse createTravelPlan(@Valid @RequestBody TravelPlanRequest request) {
        return multiAgentDemoService.createPlan(request);
    }

    /**
     * 以 token 级流式方式返回旅行规划总结。
     */
    @PostMapping(path = "/travel-plan/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TravelPlanStreamEvent> createTravelPlanStream(@Valid @RequestBody TravelPlanRequest request) {
        return multiAgentDemoService.createPlanStream(request);
    }

}
