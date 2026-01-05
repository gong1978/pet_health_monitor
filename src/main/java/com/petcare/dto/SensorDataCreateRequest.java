package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传感器数据创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataCreateRequest {
    /**
     * 关联宠物ID
     */
    private Integer petId;

    /**
     * 心率（bpm）
     */
    private Integer heartRate;

    /**
     * 体温（℃）
     */
    private Float temperature;

    /**
     * 活动量（步数或活动指数）
     */
    private Integer activity;

    /**
     * 数据采集时间（格式：yyyy-MM-dd HH:mm:ss）
     * 如果为空，则使用当前时间
     */
    private String collectedAt;
}
