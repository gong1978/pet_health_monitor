package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 传感器数据分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataPageResponse {
    /**
     * 传感器数据列表
     */
    private List<SensorDataResponse> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 传感器数据响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorDataResponse {
        /**
         * 采集数据ID
         */
        private Long dataId;

        /**
         * 关联宠物ID
         */
        private Integer petId;

        /**
         * 宠物名字
         */
        private String petName;

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
         * 数据采集时间
         */
        private String collectedAt;
    }
}
