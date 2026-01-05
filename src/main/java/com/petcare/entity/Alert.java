package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常预警实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alerts")
public class Alert {
    /**
     * 预警ID（主键）
     */
    @TableId(type = IdType.AUTO)
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
     * 预警时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否已处理
     */
    private Boolean isResolved;

    /**
     * 处理用户ID（管理员ID）
     */
    private Integer resolvedBy;
}
