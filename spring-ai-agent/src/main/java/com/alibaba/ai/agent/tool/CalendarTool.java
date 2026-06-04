package com.alibaba.ai.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

public class CalendarTool {

    /**
     * 根据目的地、天数和偏好生成逐日行程草案。
     */
    @Tool(description = "生成简洁的逐日行程草案")
    public String draftItinerary(
            @ToolParam(description = "旅行目的地") String destination,
            @ToolParam(description = "旅行天数") int days,
            @ToolParam(description = "用户偏好，使用自然语言描述") String preferences
    ) {
        List<String> itinerary = new ArrayList<>();
        for (int day = 1; day <= days; day++) {
            itinerary.add("第" + day + "天：围绕“" + destination + "”展开行程，重点满足“" + preferences + "”相关偏好，整体节奏保持轻松。");
        }
        return String.join("\n", itinerary);
    }

}
