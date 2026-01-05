package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * 访问 Token
     */
    private String accessToken;

    /**
     * 刷新 Token
     */
    private String refreshToken;

    /**
     * 用户 ID
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
     * Token 过期时间（毫秒）
     */
    private Long expiresIn;
}
