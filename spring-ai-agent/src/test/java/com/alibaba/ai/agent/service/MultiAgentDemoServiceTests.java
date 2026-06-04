package com.alibaba.ai.agent.service;

import com.alibaba.ai.agent.agent.FlightPlanningAgent;
import com.alibaba.ai.agent.agent.HotelPlanningAgent;
import com.alibaba.ai.agent.agent.TravelCoordinatorAgent;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;
import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.tool.BudgetTool;
import com.alibaba.ai.agent.tool.CalendarTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiAgentDemoServiceTests {

    @Mock
    private FlightPlanningAgent flightPlanningAgent;

    @Mock
    private HotelPlanningAgent hotelPlanningAgent;

    @Mock
    private TravelCoordinatorAgent travelCoordinatorAgent;

    @Mock
    private BudgetTool budgetTool;

    @Mock
    private CalendarTool calendarTool;

    @InjectMocks
    private MultiAgentDemoService multiAgentDemoService;

    /**
     * 验证服务层可以组装出结构化旅行规划结果。
     */
    @Test
    void shouldCreateStructuredTravelPlan() {
        when(flightPlanningAgent.plan(any(AgentPlanContext.class)))
                .thenReturn(new AgentResult("交通专家", "交通建议"));
        when(hotelPlanningAgent.plan(any(AgentPlanContext.class)))
                .thenReturn(new AgentResult("住宿专家", "住宿建议"));
        when(budgetTool.estimateBudget(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn("预算拆分");
        when(calendarTool.draftItinerary(anyString(), anyInt(), anyString()))
                .thenReturn("第1天\n第2天");
        when(travelCoordinatorAgent.summarize(any(AgentPlanContext.class), org.mockito.ArgumentMatchers.<AgentResult>anyList(), anyString(), anyString()))
                .thenReturn("总览");
        when(travelCoordinatorAgent.isMockEnabled()).thenReturn(true);

        TravelPlanResponse response = multiAgentDemoService.createPlan(new TravelPlanRequest(
                "杭州",
                2,
                2000,
                List.of("美食", "轻松")
        ));

        assertThat(response.summary()).isEqualTo("总览");
        assertThat(response.transportSuggestion()).isEqualTo("交通建议");
        assertThat(response.hotelSuggestion()).isEqualTo("住宿建议");
        assertThat(response.itineraryDraft()).containsExactly("第1天", "第2天");
        assertThat(response.agentTrace()).containsExactly("交通专家", "住宿专家");
        assertThat(response.mock()).isTrue();
    }

}
