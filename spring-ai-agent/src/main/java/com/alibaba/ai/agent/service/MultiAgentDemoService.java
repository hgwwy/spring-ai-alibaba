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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 编排交通专家、住宿专家和本地工具，生成旅行规划结果。
 */
@Service
public class MultiAgentDemoService {

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

        return new TravelPlanResponse(
                summary,
                transportPlan.content(),
                hotelPlan.content(),
                List.of(itineraryDraft.split("\\n")),
                budgetPlan,
                specialistResults.stream().map(AgentResult::agentName).toList(),
                travelCoordinatorAgent.isMockEnabled()
        );
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

}
