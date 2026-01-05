package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兽医咨询查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetConsultationQueryRequest {
    /**
     * 宠物ID
     */
    private Integer petId;

    /**
     * 提问人ID
     */
    private Integer userId;

    /**
     * 回答兽医ID
     */
    private Integer answeredBy;

    /**
     * 回答状态（true=已回答，false=未回答）
     */
    private Boolean answered;

    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 关键词搜索（搜索问题内容）
     */
    private String keyword;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}
