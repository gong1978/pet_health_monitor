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
 * 行为识别实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("behavior_analysis")
public class BehaviorAnalysis {
    /**
     * 行为记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long behaviorId;

    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 行为类型，如sleep/eat/walk
     */
    private String behaviorType;

    /**
     * 行为抓拍图片路径 (必须有这个字段)
     */
    private String imageUrl;

    /**
     * 行为开始时间
     */
    private LocalDateTime startTime;

    /**
     * 行为结束时间
     */
    private LocalDateTime endTime;

    /**
     * 模型识别置信度
     */
    private Float confidence;
}