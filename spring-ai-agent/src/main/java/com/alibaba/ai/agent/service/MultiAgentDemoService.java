package com.alibaba.ai.agent.service;

import com.alibaba.ai.agent.agent.FlightPlanningAgent;
import com.alibaba.ai.agent.agent.HotelPlanningAgent;
import com.alibaba.ai.agent.agent.TravelCoordinatorAgent;
import com.alibaba.ai.agent.model.AgentPlanContext;
import com.alibaba.ai.agent.model.AgentResult;
import com.alibaba.ai.agent.model.TravelPlanRequest;
import com.alibaba.ai.agent.model.TravelPlanResponse;
import com.alibaba.ai.agent.model.TravelPlanStreamEvent;
import com.alibaba.ai.agent.model.TravelPlanStreamResponse;
import com.alibaba.ai.agent.tool.BudgetTool;
import com.alibaba.ai.agent.tool.CalendarTool;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 编排交通专家、住宿专家和本地工具，生成旅行规划结果。
 */
@Service
public class MultiAgentDemoService {

    /**
     * 交通专家阶段名称。
     */
    private static final String FLIGHT_STAGE = "flight";

    /**
     * 住宿专家阶段名称。
     */
    private static final String HOTEL_STAGE = "hotel";

    /**
     * 协调总结阶段名称。
     */
    private static final String COORDINATOR_STAGE = "coordinator";

    /**
     * 负责生成交通建议的专家 Agent。
     */
    private final FlightPlanningAgent flightPlanningAgent;

    /**
     * 负责生成住宿建议的专家 Agent。
     */
    private final HotelPlanningAgent hotelPlanningAgent;

    /**
     * 负责汇总各方结果的协调 Agent。
     */
    private final TravelCoordinatorAgent travelCoordinatorAgent;

    /**
     * 负责预算拆分的本地工具。
     */
    private final BudgetTool budgetTool;

    /**
     * 负责逐日行程草案生成的本地工具。
     */
    private final CalendarTool calendarTool;

    /**
     * 注入多 Agent 工作流需要的专家 Agent 和本地工具。
     */
    public MultiAgentDemoService(
            FlightPlanningAgent flightPlanningAgent,
            HotelPlanningAgent hotelPlanningAgent,
            TravelCoordinatorAgent travelCoordinatorAgent,
            BudgetTool budgetTool,
            CalendarTool calendarTool
    ) {
        this.flightPlanningAgent = flightPlanningAgent;
        this.hotelPlanningAgent = hotelPlanningAgent;
        this.travelCoordinatorAgent = travelCoordinatorAgent;
        this.budgetTool = budgetTool;
        this.calendarTool = calendarTool;
    }

    /**
     * 执行多 Agent 旅行规划流程，并组装最终响应。
     */
    public TravelPlanResponse createPlan(TravelPlanRequest request) {
        PlanExecution execution = executePlan(request);
        return new TravelPlanResponse(
                execution.summary(),
                execution.transportPlan().content(),
                execution.hotelPlan().content(),
                List.of(execution.itineraryDraft().split("\\n")),
                execution.budgetPlan(),
                execution.specialistResults().stream().map(AgentResult::agentName).toList(),
                travelCoordinatorAgent.isMockEnabled()
        );
    }

    /**
     * 以流式方式执行旅行规划，并按阶段返回专家与协调总结的增量输出事件。
     */
    public Flux<TravelPlanStreamEvent> createPlanStream(TravelPlanRequest request) {
        AgentPlanContext context = toContext(request);
        AtomicLong sequence = new AtomicLong(0L);
        boolean mock = travelCoordinatorAgent.isMockEnabled();

        return streamStage(flightPlanningAgent.streamPlan(context), FLIGHT_STAGE, sequence, mock)
                .collectList()
                .flatMapMany(flightEvents -> {
                    String transportSuggestion = joinChunkContent(flightEvents);
                    AgentResult transportPlan = new AgentResult("交通专家", transportSuggestion);
                    return streamStage(hotelPlanningAgent.streamPlan(context), HOTEL_STAGE, sequence, mock)
                            .collectList()
                            .flatMapMany(hotelEvents -> {
                                String hotelSuggestion = joinChunkContent(hotelEvents);
                                AgentResult hotelPlan = new AgentResult("住宿专家", hotelSuggestion);
                                List<AgentResult> specialistResults = List.of(transportPlan, hotelPlan);
                                String budgetPlan = budgetTool.estimateBudget(
                                        context.destination(),
                                        context.days(),
                                        context.budget(),
                                        context.preferencesText()
                                );
                                String itineraryDraft = calendarTool.draftItinerary(
                                        context.destination(),
                                        context.days(),
                                        context.preferencesText()
                                );
                                return streamStage(
                                        travelCoordinatorAgent.streamSummarize(context, specialistResults, budgetPlan, itineraryDraft),
                                        COORDINATOR_STAGE,
                                        sequence,
                                        mock
                                ).collectList().flatMapMany(coordinatorEvents -> {
                                    String summary = joinChunkContent(coordinatorEvents);
                                    TravelPlanStreamResponse finalResponse = new TravelPlanStreamResponse(
                                            summary,
                                            transportSuggestion,
                                            hotelSuggestion,
                                            List.of(itineraryDraft.split("\\n")),
                                            budgetPlan,
                                            specialistResults.stream().map(AgentResult::agentName).toList(),
                                            mock
                                    );
                                    return Flux.fromIterable(flightEvents)
                                            .concatWith(Flux.fromIterable(hotelEvents))
                                            .concatWith(Flux.fromIterable(coordinatorEvents))
                                            .concatWith(Flux.just(
                                                    TravelPlanStreamEvent.done(
                                                            COORDINATOR_STAGE,
                                                            sequence.incrementAndGet(),
                                                            mock,
                                                            finalResponse
                                                    )
                                            ));
                                });
                            });
                })
                .onErrorResume(ex -> Flux.just(
                        TravelPlanStreamEvent.error(
                                resolveStage(ex),
                                "流式旅行规划生成失败：" + ex.getMessage(),
                                sequence.incrementAndGet(),
                                mock
                        )
                ));
    }

    /**
     * 将某个阶段的 token 流包装为带阶段信息的流式事件。
     */
    private Flux<TravelPlanStreamEvent> streamStage(Flux<String> tokenStream, String stage, AtomicLong sequence, boolean mock) {
        return tokenStream
                .map(content -> TravelPlanStreamEvent.chunk(stage, content, sequence.incrementAndGet(), mock))
                .concatWith(Flux.defer(() -> Flux.just(
                        TravelPlanStreamEvent.done(stage, sequence.incrementAndGet(), mock)
                )));
    }

    /**
     * 从阶段事件中提取并拼接文本分片。
     */
    private String joinChunkContent(List<TravelPlanStreamEvent> events) {
        return events.stream()
                .filter(event -> "chunk".equals(event.type()))
                .map(TravelPlanStreamEvent::content)
                .reduce("", String::concat);
    }

    /**
     * 根据异常信息推断出错阶段，当前默认归属到协调阶段。
     */
    private String resolveStage(Throwable throwable) {
        return COORDINATOR_STAGE;
    }

    /**
     * 执行前置专家与工具步骤，并返回统一的执行结果。
     */
    private PlanExecution executePlan(TravelPlanRequest request) {
        AgentPlanContext context = toContext(request);
        AgentResult transportPlan = flightPlanningAgent.plan(context);
        AgentResult hotelPlan = hotelPlanningAgent.plan(context);
        List<AgentResult> specialistResults = List.of(transportPlan, hotelPlan);

        String budgetPlan = budgetTool.estimateBudget(
                context.destination(),
                context.days(),
                context.budget(),
                context.preferencesText()
        );
        String itineraryDraft = calendarTool.draftItinerary(
                context.destination(),
                context.days(),
                context.preferencesText()
        );
        String summary = travelCoordinatorAgent.summarize(context, specialistResults, budgetPlan, itineraryDraft);
        return new PlanExecution(context, transportPlan, hotelPlan, specialistResults, budgetPlan, itineraryDraft, summary);
    }

    /**
     * 将 HTTP 请求转换为统一的 Agent 上下文。
     */
    private AgentPlanContext toContext(TravelPlanRequest request) {
        String preferencesText = String.join("/", request.preferences());
        String userPrompt = """
                请为 %s 规划一趟 %d 天的旅行，总预算为 %d 元。
                用户偏好：%s
                请给出务实、易执行且符合预算的建议。
                """.formatted(request.destination(), request.days(), request.budget(), preferencesText);
        return new AgentPlanContext(
                request.destination(),
                request.days(),
                request.budget(),
                preferencesText,
                userPrompt
        );
    }

    /**
     * 封装一次旅行规划执行的中间结果，供同步接口复用。
     */
    private record PlanExecution(
            AgentPlanContext context,
            AgentResult transportPlan,
            AgentResult hotelPlan,
            List<AgentResult> specialistResults,
            String budgetPlan,
            String itineraryDraft,
            String summary
    ) {
    }

}
