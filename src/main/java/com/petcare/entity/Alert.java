package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableId(value = "alert_id", type = IdType.AUTO)
    private Integer alertId;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Integer petId;

    /**
     * 预警类型（高温/低活动等）
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 预警内容
     */
    @TableField("alert_message")
    private String alertMessage;

    /**
     * 预警等级 warning/critical
     */
    @TableField("level")
    private String level;

    /**
     * 预警时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 是否已处理
     * 注意：数据库列是 is_resolved，Java 字段用 Boolean 避免拆箱 NPE
     */
    @TableField("is_resolved")
    private Boolean isResolved;

    /**
     * 处理用户ID（管理员ID）
     * 注意：数据库列是 resolved_by
     */
    @TableField("resolved_by")
    private Integer resolvedBy;
}
