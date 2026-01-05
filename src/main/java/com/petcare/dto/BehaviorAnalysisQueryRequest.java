package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为识别查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisQueryRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 行为类型
     */
    private String behaviorType;

    /**
     * 置信度范围 - 最小值
     */
    private Float minConfidence;

    /**
     * 置信度范围 - 最大值
     */
    private Float maxConfidence;

    /**
     * 开始时间范围 - 起始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTimeFrom;

    /**
     * 开始时间范围 - 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTimeTo;

    /**
     * 结束时间范围 - 起始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTimeFrom;

    /**
     * 结束时间范围 - 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTimeTo;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}
