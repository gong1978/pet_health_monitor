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
 * 兽医咨询实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vet_consultations")
public class VetConsultation {
    /**
     * 咨询记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer consultId;

    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 提问人（宠物主人ID）
     */
    private Integer userId;

    /**
     * 咨询问题
     */
    private String question;

    /**
     * 兽医回复
     */
    private String answer;

    /**
     * 提问时间
     */
    private LocalDateTime askedAt;

    /**
     * 回答兽医ID（users.user_id，角色=2）
     */
    private Integer answeredBy;
}
