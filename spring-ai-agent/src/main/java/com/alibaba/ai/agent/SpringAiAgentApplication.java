package com.alibaba.ai.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.alibaba.ai.agent.config.AgentProperties;

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