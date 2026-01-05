package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异常预警创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertCreateRequest {
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
     * 预警时间（格式：yyyy-MM-dd HH:mm:ss）
     * 如果为空，则使用当前时间
     */
    private String createdAt;
}
