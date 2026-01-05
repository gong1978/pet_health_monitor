package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异常预警更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertUpdateRequest {
    /**
     * 预警ID
     */
    private Integer alertId;

    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 预警类型（高温/低活动等）
     */
    private String alertType;

    /**
     * 预警内容
     */
    private String alertMessage;

    /**
     * 预警等级 warning/critical
     */
    private String level;

    /**
     * 是否已处理
     */
    private Boolean isResolved;

    /**
     * 处理用户ID（管理员ID）
     */
    private Integer resolvedBy;
}
