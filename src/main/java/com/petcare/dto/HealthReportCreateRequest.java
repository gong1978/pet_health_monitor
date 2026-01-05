package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康报告创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportCreateRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 报告生成日期（格式：yyyy-MM-dd）
     * 如果为空，则使用当前日期
     */
    private String reportDate;

    /**
     * 健康评分
     */
    private Integer healthScore;

    /**
     * 健康总结
     */
    private String summary;

    /**
     * 健康/饲养建议
     */
    private String suggestions;

    /**
     * 审核兽医ID（users.user_id，角色=2）
     */
    private Integer reviewedBy;
}
