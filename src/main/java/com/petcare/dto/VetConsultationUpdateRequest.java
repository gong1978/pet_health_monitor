package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兽医咨询更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetConsultationUpdateRequest {
    /**
     * 咨询记录ID
     */
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
     * 回答兽医ID（users.user_id，角色=2）
     */
    private Integer answeredBy;
}
