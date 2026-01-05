package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 兽医咨询分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetConsultationPageResponse {
    /**
     * 兽医咨询列表
     */
    private List<VetConsultationResponse> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 兽医咨询响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VetConsultationResponse {
        /**
         * 咨询记录ID
         */
        private Integer consultId;

        /**
         * 宠物ID
         */
        private Integer petId;

        /**
         * 宠物名字
         */
        private String petName;

        /**
         * 提问人ID
         */
        private Integer userId;

        /**
         * 提问人姓名
         */
        private String userName;

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
        private String askedAt;

        /**
         * 回答兽医ID
         */
        private Integer answeredBy;

        /**
         * 回答兽医姓名
         */
        private String answeredByName;

        /**
         * 是否已回答
         */
        private Boolean answered;
    }
}
