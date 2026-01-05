package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兽医咨询创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetConsultationCreateRequest {
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
     * 提问时间（格式：yyyy-MM-dd HH:mm:ss）
     * 如果为空，则使用当前时间
     */
    private String askedAt;
}
