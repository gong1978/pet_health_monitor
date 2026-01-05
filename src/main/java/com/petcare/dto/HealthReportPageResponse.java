package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 健康报告分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportPageResponse {
    /**
     * 健康报告列表
     */
    private List<HealthReportResponse> records;

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
     * 健康报告响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthReportResponse {
        /**
         * 健康报告ID
         */
        private Integer reportId;

        /**
         * 宠物ID
         */
        private Integer petId;

        /**
         * 宠物名字
         */
        private String petName;

        /**
         * 报告生成日期
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
         * 审核兽医ID
         */
        private Integer reviewedBy;

        /**
         * 审核兽医姓名
         */
        private String reviewerName;
    }
}
