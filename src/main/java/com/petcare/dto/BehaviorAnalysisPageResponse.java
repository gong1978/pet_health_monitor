package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 行为识别分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisPageResponse {
    /**
     * 行为识别列表
     */
    private List<BehaviorAnalysisResponse> records;

    private Long total;
    private Long current;
    private Long size;
    private Long pages;

    /**
     * 行为识别响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorAnalysisResponse {
        private Long behaviorId;
        private Integer petId;
        private String petName;
        private String behaviorType;

        // 新增图片路径字段
        private String imageUrl;

        private String startTime;
        private String endTime;
        private Long duration;
        private Float confidence;
    }
}