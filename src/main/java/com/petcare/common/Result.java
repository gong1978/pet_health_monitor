package com.petcare.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .build();
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失败响应（默认码）
     */
    public static <T> Result<T> fail(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .build();
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> paramError(String message) {
        return Result.<T>builder()
                .code(400)
                .message(message)
                .build();
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder()
                .code(401)
                .message(message)
                .build();
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return Result.<T>builder()
                .code(403)
                .message(message)
                .build();
    }
}
