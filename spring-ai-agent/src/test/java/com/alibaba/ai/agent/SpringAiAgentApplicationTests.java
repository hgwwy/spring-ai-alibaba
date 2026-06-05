package com.alibaba.ai.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "demo.multi-agent.mock-enabled=true",
        "spring.ai.dashscope.api-key=test-key",
        "spring.ai.dashscope.chat.api-key=test-key",
        "spring.ai.dashscope.agent.api-key=test-key"
})
class SpringAiAgentApplicationTests {

    /**
     * 验证应用上下文可以正常加载。
     */
    @Test
    void contextLoads() {
    }

}
