package com.uni.research.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装类
 * 
 * 对应 408 考点：软件工程 - 接口设计规范
 * 
 * 设计思路：
 * 1. 使用泛型 T 支持任意类型的数据返回
 * 2. 包含 code、message、data 三个核心字段
 * 3. 提供静态工厂方法，简化调用
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // ========== 成功响应 ==========
    
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    // ========== 失败响应 ==========
    
    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), 
                           ResultCode.INTERNAL_SERVER_ERROR.getMessage(), null);
    }
    
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }
    
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }
    
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    // ========== 判断方法 ==========
    
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
