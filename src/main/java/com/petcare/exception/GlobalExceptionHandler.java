package com.petcare.exception;

import com.petcare.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理数据库完整性约束异常 (核心修复：捕获外键冲突)
     */
    @ExceptionHandler({SQLIntegrityConstraintViolationException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result<String> handleSQLException(Exception e) {
        log.error("数据库操作异常: {}", e.getMessage(), e);
        // 提取错误信息中的关键字
        String msg = e.getMessage();
        if (msg != null && (msg.contains("foreign key constraint fails") || msg.contains("ConstraintViolationException"))) {
            return Result.fail(500, "删除失败：该数据仍被其他记录引用（即使是已逻辑删除的历史数据）。请检查数据库外键约束或联系管理员。");
        }
        if (msg != null && msg.contains("Duplicate entry")) {
            return Result.fail(500, "操作失败：数据已存在（重复键值）。");
        }
        return Result.fail(500, "数据库操作失败: " + msg);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.fail(500, e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(500, "系统异常，请稍后重试");
    }
}