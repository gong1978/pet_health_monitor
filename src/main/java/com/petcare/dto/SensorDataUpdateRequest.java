package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传感器数据更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataUpdateRequest {
    /**
     * 采集数据ID
     */
    private Long dataId;

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
     */
    private String collectedAt;
}
