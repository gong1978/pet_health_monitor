package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户分页响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 用户列表
     */
    private List<UserDTO> records;

    /**
     * 用户信息 DTO（用于列表展示）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        /**
         * 用户ID
         */
        private Integer userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 真实姓名
         */
        private String fullName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 角色
         */
        private Integer role;

        /**
         * 创建时间
         */
        private String createdAt;
    }
}
