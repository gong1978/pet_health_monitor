package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetCreateRequest {
    /**
     * 宠物名字
     */
    private String name;

    /**
     * 物种（狗/猫等）
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
     * 年龄（岁）
     */
    private Integer age;

    /**
     * 体重（kg）
     */
    private Float weight;

    /**
     * 疫苗记录
     */
    private String vaccineRecord;

    /**
     * 宠物图片URL
     */
    private String imageUrl;
}
