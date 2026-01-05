package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为识别创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisCreateRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 行为类型，如sleep/eat/walk
     */
    private String behaviorType;

    /**
     * 行为抓拍图片路径
     */
    private String imageUrl;

    /**
     * 行为开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 行为结束时间
     */
    private String endTime;

    /**
     * 模型识别置信度
     */
    private Float confidence;
}