package com.alibaba.ai.agent;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.model.TravelPlanStreamEvent;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 mock 模式下同步与流式接口的集成行为。
 */
@SpringBootTest(properties = {
        "demo.multi-agent.mock-enabled=true",
        "spring.ai.dashscope.api-key=test-key",
        "spring.ai.dashscope.chat.api-key=test-key",
        "spring.ai.dashscope.agent.api-key=test-key"
})
class MultiAgentIntegrationTests {

    @Autowired
    private MultiAgentDemoService multiAgentDemoService;

    /**
     * 验证 mock 模式下的多 Agent 集成流程可以正常执行。
     */
    @Test
    void shouldCreateTravelPlanInMockMode() {
        TravelPlanResponse response = multiAgentDemoService.createPlan(new TravelPlanRequest(
                "杭州",
                3,
                3000,
                List.of("美食", "轻松")
        ));

        assertThat(response.summary()).isNotBlank();
        assertThat(response.transportSuggestion()).isNotBlank();
        assertThat(response.hotelSuggestion()).isNotBlank();
        assertThat(response.itineraryDraft()).hasSize(3);
        assertThat(response.mock()).isTrue();
    }

    /**
     * 验证 mock 模式下的三阶段流式旅行规划可以正常结束。
     */
    @Test
    void shouldCreateTravelPlanStreamInMockMode() {
        List<TravelPlanStreamEvent> events = multiAgentDemoService.createPlanStream(new TravelPlanRequest(
                "杭州",
                3,
                3000,
                List.of("美食", "轻松")
        )).collectList().block();

        assertThat(events).isNotEmpty();
        assertThat(events.get(0).stage()).isEqualTo("flight");
        assertThat(events.stream().anyMatch(event -> "hotel".equals(event.stage()))).isTrue();
        assertThat(events.stream().anyMatch(event -> "coordinator".equals(event.stage()))).isTrue();
        assertThat(events.get(events.size() - 1).type()).isEqualTo("done");
        assertThat(events.get(events.size() - 1).finalResponse()).isNotNull();
        assertThat(events.get(events.size() - 1).finalResponse().mock()).isTrue();
    }

}
