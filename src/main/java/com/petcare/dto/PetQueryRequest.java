package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetQueryRequest {
    /**
     * 宠物名字（模糊查询）
     */
    private String name;

    /**
     * 物种
     */
    private String species;

    /**
     * 品种
     */
    private String breed;

    /**
     * 性别
     */
    private String gender;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}
