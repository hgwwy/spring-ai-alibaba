package com.alibaba.ai.agent.controller;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MultiAgentDemoController.class)
@Import(MultiAgentDemoController.class)
class MultiAgentDemoControllerTests {

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
        when(multiAgentDemoService.createPlan(any(TravelPlanRequest.class))).thenReturn(new TravelPlanResponse(
                "总览",
                "交通建议",
                "住宿建议",
                List.of("第1天"),
                "预算拆分",
                List.of("交通专家", "住宿专家"),
                true
        ));

        TravelPlanRequest request = new TravelPlanRequest("杭州", 2, 2000, List.of("美食", "轻松"));

        mockMvc.perform(post("/api/agents/travel-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("总览"))
                .andExpect(jsonPath("$.agentTrace[0]").value("交通专家"));
    }

}
