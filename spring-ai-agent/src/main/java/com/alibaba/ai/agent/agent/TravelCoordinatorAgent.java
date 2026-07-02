package com.alibaba.ai.agent.agent;

import com.alibaba.ai.agent.config.AgentProperties;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 负责汇总专家建议并生成最终旅行方案的协调 Agent。
 */
public class TravelCoordinatorAgent {

    /**
     * 真实模型模式下使用的 ChatClient。
     */
    private final ChatClient chatClient;

    /**
     * 当前 Agent 使用的配置项。
     */
    private final AgentProperties properties;

    /**
     * 创建协调专家 Agent。
     */
    public TravelCoordinatorAgent(ChatClient chatClient, AgentProperties properties) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    /**
     * 汇总专家建议与工具输出，生成最终旅行方案。
     */
    public String summarize(AgentPlanContext context, List<AgentResult> specialistResults, String budgetPlan, String itineraryDraft) {
        if (properties.isMockEnabled()) {
            return "这是一个适合轻松出行的方案：先保证到达和住宿便利，再围绕偏好安排每日活动，并把预算重点放在住宿与交通上。";
        }

        return chatClient.prompt()
                .system(properties.getCoordinatorPrompt())
                .user(buildUserPrompt(context, specialistResults, budgetPlan, itineraryDraft))
                .call()
                .content();
    }

    /**
     * 以流式方式汇总专家建议与工具输出。
     */
    public Flux<String> streamSummarize(AgentPlanContext context, List<AgentResult> specialistResults, String budgetPlan, String itineraryDraft) {
        if (properties.isMockEnabled()) {
            return Flux.just(
                    "这是一个适合轻松出行的方案：",
                    "先保证到达和住宿便利，",
                    "再围绕偏好安排每日活动，",
                    "并把预算重点放在住宿与交通上。"
            );
        }

        return chatClient.prompt()
                .system(properties.getCoordinatorPrompt())
                .user(buildUserPrompt(context, specialistResults, budgetPlan, itineraryDraft))
                .stream()
                .content();
    }

    /**
     * 构建协调专家使用的用户提示词。
     */
    private String buildUserPrompt(AgentPlanContext context, List<AgentResult> specialistResults, String budgetPlan, String itineraryDraft) {
        String specialistText = specialistResults.stream()
                .map(result -> result.agentName() + "：" + result.content())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("暂无专家建议");

        return """
                请为 %s 生成一份简洁、可执行的旅行方案。
                天数：%d
                预算：%d 元
                偏好：%s

                专家建议：
                %s

                预算工具输出：
                %s

                行程工具输出：
                %s
                """.formatted(
                context.destination(),
                context.days(),
                context.budget(),
                context.preferencesText(),
                specialistText,
                budgetPlan,
                itineraryDraft
        );
    }

    /**
     * 返回当前是否处于 mock 模式。
     */
    public boolean isMockEnabled() {
        return properties.isMockEnabled();
    }

}
