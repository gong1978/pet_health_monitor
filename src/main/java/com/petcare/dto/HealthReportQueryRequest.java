package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康报告查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportQueryRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 健康评分范围 - 最小值
     */
    private Integer minHealthScore;

    /**
     * 健康评分范围 - 最大值
     */
    private Integer maxHealthScore;

    /**
     * 审核兽医ID
     */
    private Integer reviewedBy;

    /**
     * 开始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（格式：yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}
