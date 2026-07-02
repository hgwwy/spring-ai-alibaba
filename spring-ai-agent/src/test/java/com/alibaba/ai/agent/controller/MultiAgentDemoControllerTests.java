package com.alibaba.ai.agent.controller;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.model.TravelPlanStreamEvent;
import com.alibaba.ai.agent.model.TravelPlanStreamResponse;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证旅行规划控制器的同步与流式响应。
 */
@WebMvcTest(MultiAgentDemoController.class)
@Import(MultiAgentDemoController.class)
class MultiAgentDemoControllerTests {

    /**
     * 旅行规划控制器统一使用的接口前缀。
     */
    private static final String API_PREFIX = "/agents";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MultiAgentDemoService multiAgentDemoService;

    /**
     * 验证控制器会返回结构化旅行规划响应。
     */
    @Test
    void shouldReturnTravelPlanResponse() throws Exception {
        mockTravelPlanResponse();

        expectTravelPlanResponse();
    }

    /**
     * 验证控制器会返回包含多个阶段的 text/event-stream 流式响应。
     */
    @Test
    void shouldReturnTravelPlanStreamResponse() throws Exception {
        mockTravelPlanStreamResponse();

        expectTravelPlanStreamResponse();
    }

    /**
     * 构造同步规划的模拟结果，确保路径测试只关注 HTTP 契约。
     */
    private void mockTravelPlanResponse() {
        when(multiAgentDemoService.createPlan(any(TravelPlanRequest.class))).thenReturn(new TravelPlanResponse(
                "总览",
                "交通建议",
                "住宿建议",
                List.of("第1天"),
                "预算拆分",
                List.of("交通专家", "住宿专家"),
                true
        ));
    }

    /**
     * 构造流式规划的模拟结果，覆盖交通、住宿和协调三个阶段事件。
     */
    private void mockTravelPlanStreamResponse() {
        when(multiAgentDemoService.createPlanStream(any(TravelPlanRequest.class))).thenReturn(Flux.just(
                TravelPlanStreamEvent.chunk("flight", "交", 1L, true),
                TravelPlanStreamEvent.done("flight", 2L, true),
                TravelPlanStreamEvent.chunk("hotel", "住", 3L, true),
                TravelPlanStreamEvent.done("hotel", 4L, true),
                TravelPlanStreamEvent.chunk("coordinator", "总", 5L, true),
                TravelPlanStreamEvent.done(
                        "coordinator",
                        6L,
                        true,
                        new TravelPlanStreamResponse(
                                "总览",
                                "交通",
                                "住宿",
                                List.of("第1天"),
                                "预算拆分",
                                List.of("交通专家", "住宿专家"),
                                true
                        )
                )
        ));
    }

    /**
     * 对 /agents 前缀发起同步规划请求，并断言响应结构符合 controller 契约。
     */
    private void expectTravelPlanResponse() throws Exception {
        TravelPlanRequest request = new TravelPlanRequest("杭州", 2, 2000, List.of("美食", "轻松"));

        mockMvc.perform(post(API_PREFIX + "/travel-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("总览"))
                .andExpect(jsonPath("$.agentTrace[0]").value("交通专家"));
    }

    /**
     * 对 /agents 前缀发起流式规划请求，并断言响应包含核心阶段事件。
     */
    private void expectTravelPlanStreamResponse() throws Exception {
        TravelPlanRequest request = new TravelPlanRequest("杭州", 2, 2000, List.of("美食", "轻松"));

        mockMvc.perform(post(API_PREFIX + "/travel-plan/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("\"stage\":\"flight\"")))
                .andExpect(content().string(containsString("\"stage\":\"hotel\"")))
                .andExpect(content().string(containsString("\"stage\":\"coordinator\"")));
    }

}
