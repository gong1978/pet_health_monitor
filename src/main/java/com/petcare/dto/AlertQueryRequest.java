package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 异常预警查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertQueryRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 预警类型
     */
    private String alertType;

    /**
     * 预警等级
     */
    private String level;

    /**
     * 处理状态
     */
    private Boolean isResolved;

    /**
     * 处理用户ID
     */
    private Integer resolvedBy;

    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;

    /**
     * 用户的宠物ID列表（用于权限过滤）
     */
    private List<Integer> userPetIds;
}
