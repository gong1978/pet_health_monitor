package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户查询请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryRequest {
    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 真实姓名（模糊查询）
     */
    private String fullName;

    /**
     * 邮箱（模糊查询）
     */
    private String email;

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 角色：1管理员、2兽医、3宠物主人
     */
    private Integer role;

    /**
     * 排序字段（默认created_at）
     */
    private String sortBy;

    /**
     * 排序方向：asc或desc（默认desc）
     */
    private String sortOrder;
}
