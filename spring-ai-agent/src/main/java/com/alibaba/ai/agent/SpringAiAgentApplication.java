package com.alibaba.ai.agent;

import com.alibaba.ai.agent.config.AgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 多 Agent 演示应用启动入口。
 */
@SpringBootApplication
@EnableConfigurationProperties(AgentProperties.class)
public class SpringAiAgentApplication {

    /**
     * 启动 Spring Boot 应用。
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringAiAgentApplication.class, args);
    }

}
