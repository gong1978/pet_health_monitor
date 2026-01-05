package com.petcare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人信息更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
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
     * 当前密码（修改密码时需要）
     */
    private String currentPassword;

    /**
     * 新密码（可选）
     */
    private String newPassword;

    /**
     * 确认新密码（可选）
     */
    private String confirmPassword;
}
