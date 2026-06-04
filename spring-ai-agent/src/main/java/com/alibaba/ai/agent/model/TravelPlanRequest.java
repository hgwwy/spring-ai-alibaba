package com.alibaba.ai.agent.model;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 旅行规划请求体。
 *
 * @param destination 旅行目的地
 * @param days 旅行天数
 * @param budget 总预算，单位人民币
 * @param preferences 用户偏好列表
 */
public record TravelPlanRequest(
                @NotBlank(message = "destination must not be blank") String destination,
                @NotNull(message = "days must not be null") @Min(value = 1, message = "days must be at least 1") @Max(value = 14, message = "days must be at most 14") Integer days,
                @NotNull(message = "budget must not be null") @Min(value = 500, message = "budget must be at least 500") Integer budget,
                @NotEmpty(message = "preferences must not be empty") List<@NotBlank(message = "preference must not be blank") String> preferences) {
}
