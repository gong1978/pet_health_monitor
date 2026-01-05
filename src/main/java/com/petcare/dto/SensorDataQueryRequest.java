package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传感器数据查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataQueryRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 心率范围 - 最小值
     */
    private Integer minHeartRate;

    /**
     * 心率范围 - 最大值
     */
    private Integer maxHeartRate;

    /**
     * 体温范围 - 最小值
     */
    private Float minTemperature;

    /**
     * 体温范围 - 最大值
     */
    private Float maxTemperature;

    /**
     * 活动量范围 - 最小值
     */
    private Integer minActivity;

    /**
     * 活动量范围 - 最大值
     */
    private Integer maxActivity;

    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}
