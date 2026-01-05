package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 健康报告实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_reports")
public class HealthReport {
    /**
     * 健康报告ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer reportId;

    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 报告生成日期
     */
    private LocalDate reportDate;

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
