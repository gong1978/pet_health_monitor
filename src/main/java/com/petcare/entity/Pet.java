package com.petcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic; // [新增]
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宠物实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pets")
public class Pet {
    /**
     * 宠物ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer petId;

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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标识 (0:未删除, 1:已删除)
     * [核心修改] 加上这个字段和注解，MyBatis-Plus 就会执行 UPDATE 而不是 DELETE
     */
    @TableLogic
    private Integer deleted;
}