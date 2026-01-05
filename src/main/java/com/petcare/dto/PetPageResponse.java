package com.petcare.dto;

import com.petcare.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 宠物分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPageResponse {
    /**
     * 宠物列表
     */
    private List<Pet> records;

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
}
