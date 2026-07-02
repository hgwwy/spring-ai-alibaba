package com.alibaba.ai.agent.service;

import com.alibaba.ai.agent.agent.FlightPlanningAgent;
import com.alibaba.ai.agent.agent.HotelPlanningAgent;
import com.alibaba.ai.agent.agent.TravelCoordinatorAgent;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;
import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.model.TravelPlanStreamEvent;
import com.alibaba.ai.agent.tool.BudgetTool;
import com.alibaba.ai.agent.tool.CalendarTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 验证多 Agent 服务层的同步与流式编排行为。
 */
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

    /**
     * 验证流式接口会按 flight、hotel、coordinator 三阶段顺序返回事件。
     */
    @Test
    void shouldCreateTravelPlanStreamEvents() {
        when(flightPlanningAgent.streamPlan(any(AgentPlanContext.class)))
                .thenReturn(Flux.just("交", "通"));
        when(hotelPlanningAgent.streamPlan(any(AgentPlanContext.class)))
                .thenReturn(Flux.just("住", "宿"));
        when(budgetTool.estimateBudget(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn("预算拆分");
        when(calendarTool.draftItinerary(anyString(), anyInt(), anyString()))
                .thenReturn("第1天\n第2天");
        when(travelCoordinatorAgent.streamSummarize(any(AgentPlanContext.class), org.mockito.ArgumentMatchers.<AgentResult>anyList(), anyString(), anyString()))
                .thenReturn(Flux.just("总", "览"));
        when(travelCoordinatorAgent.isMockEnabled()).thenReturn(true);

        List<TravelPlanStreamEvent> events = multiAgentDemoService.createPlanStream(new TravelPlanRequest(
                "杭州",
                2,
                2000,
                List.of("美食", "轻松")
        )).collectList().block();

        assertThat(events).hasSize(10);
        assertThat(events.get(0).stage()).isEqualTo("flight");
        assertThat(events.get(0).type()).isEqualTo("chunk");
        assertThat(events.get(1).stage()).isEqualTo("flight");
        assertThat(events.get(2).stage()).isEqualTo("flight");
        assertThat(events.get(2).type()).isEqualTo("done");
        assertThat(events.get(3).stage()).isEqualTo("hotel");
        assertThat(events.get(5).stage()).isEqualTo("hotel");
        assertThat(events.get(5).type()).isEqualTo("done");
        assertThat(events.get(6).stage()).isEqualTo("coordinator");
        assertThat(events.get(8).stage()).isEqualTo("coordinator");
        assertThat(events.get(8).type()).isEqualTo("done");
        assertThat(events.get(9).type()).isEqualTo("done");
        assertThat(events.get(9).finalResponse()).isNotNull();
        assertThat(events.get(9).finalResponse().summary()).isEqualTo("总览");
        assertThat(events.get(9).finalResponse().transportSuggestion()).isEqualTo("交通");
        assertThat(events.get(9).finalResponse().hotelSuggestion()).isEqualTo("住宿");
        assertThat(events).extracting(TravelPlanStreamEvent::sequence)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

}
