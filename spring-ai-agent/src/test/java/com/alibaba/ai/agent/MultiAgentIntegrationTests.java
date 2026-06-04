package com.alibaba.ai.agent;

import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.service.MultiAgentDemoService;
import com.alibaba.cloud.ai.autoconfigure.rag.RagElasticSearchAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "demo.multi-agent.mock-enabled=true",
        "spring.ai.dashscope.api-key=test-key",
        "spring.ai.dashscope.chat.api-key=test-key",
        "spring.ai.dashscope.agent.api-key=test-key"
})
@ImportAutoConfiguration(exclude = {
        ElasticsearchVectorStoreAutoConfiguration.class,
        RagElasticSearchAutoConfiguration.class
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

}
