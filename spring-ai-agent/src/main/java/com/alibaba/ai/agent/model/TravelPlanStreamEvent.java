package com.alibaba.ai.agent.model;

/**
 * 旅行规划流式事件。
 *
 * @param type 事件类型，例如 chunk、done、error
 * @param stage 当前事件所属阶段，例如 flight、hotel、coordinator
 * @param content 当前事件携带的文本内容
 * @param sequence 当前事件的顺序号
 * @param mock 当前事件是否来自 mock 模式
 * @param errorMessage 当前事件的错误信息
 * @param finalResponse 流式结束时附带的完整结果
 */
public record TravelPlanStreamEvent(
        String type,
        String stage,
        String content,
        long sequence,
        boolean mock,
        String errorMessage,
        TravelPlanStreamResponse finalResponse
) {

    /**
     * 创建文本分片事件。
     */
    public static TravelPlanStreamEvent chunk(String stage, String content, long sequence, boolean mock) {
        return new TravelPlanStreamEvent("chunk", stage, content, sequence, mock, null, null);
    }

    /**
     * 创建阶段完成事件。
     */
    public static TravelPlanStreamEvent done(String stage, long sequence, boolean mock) {
        return new TravelPlanStreamEvent("done", stage, null, sequence, mock, null, null);
    }

    /**
     * 创建整体完成事件。
     */
    public static TravelPlanStreamEvent done(String stage, long sequence, boolean mock, TravelPlanStreamResponse finalResponse) {
        return new TravelPlanStreamEvent("done", stage, null, sequence, mock, null, finalResponse);
    }

    /**
     * 创建错误事件。
     */
    public static TravelPlanStreamEvent error(String stage, String errorMessage, long sequence, boolean mock) {
        return new TravelPlanStreamEvent("error", stage, null, sequence, mock, errorMessage, null);
    }

}
