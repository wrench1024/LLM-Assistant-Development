package com.uni.research.common.exception;

import com.uni.research.common.result.Result;
import com.uni.research.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 对应 408 考点：软件工程 - 异常处理与系统健壮性
 * 
 * 设计思路：
 * 1. 使用 @RestControllerAdvice 统一拦截所有 Controller 异常
 * 2. 针对不同异常类型返回不同的错误信息
 * 3. 记录异常日志，便于问题排查
 * 4. 避免敏感信息泄露（生产环境不返回堆栈信息）
 * 
 * 面试话术：
 * "我实现了全局异常处理器，统一拦截和处理所有异常，避免异常信息直接暴露给前端。
 * 对于业务异常（BizException），返回自定义的错误码和消息；
 * 对于参数校验异常，提取具体的校验失败字段；
 * 对于未知异常，记录完整日志但只返回通用错误信息，保证系统安全性。"
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常: {}", errorMsg);
        return Result.fail(ResultCode.BAD_REQUEST, errorMsg);
    }
    
    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常: {}", errorMsg);
        return Result.fail(ResultCode.BAD_REQUEST, errorMsg);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, e.getMessage());
    }
    
    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 生产环境不返回具体异常信息，避免信息泄露
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
