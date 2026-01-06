package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 异常预警分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertPageResponse {
    /**
     * 异常预警列表
     */
    private List<AlertResponse> records;

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
     * 异常预警响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertResponse {
        /**
         * 预警ID
         */
        private Integer alertId;

        /**
         * 宠物ID
         */
        private Integer petId;

        /**
         * 宠物名字
         */
        private String petName;

        /**
         * 预警类型
         */
        private String alertType;

        /**
         * 预警内容
         */
        private String alertMessage;

        /**
         * 预警等级 warning / critical
         */
        private String level;

        /**
         * 预警时间（格式化字符串）
         */
        private String createdAt;

        /**
         * 是否已处理
         * 注意：使用 Boolean（包装类型）避免数据库 NULL 时拆箱 NPE
         * 建议 Service 层输出时做 null -> false 兜底
         */
        private Boolean isResolved;

        /**
         * 处理用户ID
         * 注意：使用 Integer（包装类型）避免 NULL 拆箱 NPE
         */
        private Integer resolvedBy;

        /**
         * 处理用户姓名
         */
        private String resolverName;
    }
}
