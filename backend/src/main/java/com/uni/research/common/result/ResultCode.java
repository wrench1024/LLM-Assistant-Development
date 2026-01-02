package com.uni.research.common.result;

/**
 * 统一响应码枚举
 * 
 * 对应 408 考点：软件工程 - 错误处理与异常管理
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，拒绝访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    
    // 业务错误 4xx
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_ALREADY_EXISTS(4002, "用户已存在"),
    WRONG_PASSWORD(4003, "密码错误"),
    TOKEN_EXPIRED(4004, "Token 已过期"),
    TOKEN_INVALID(4005, "Token 无效"),
    
    DOCUMENT_NOT_FOUND(4101, "文档不存在"),
    DOCUMENT_PROCESSING(4102, "文档正在处理中"),
    
    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    AI_SERVICE_ERROR(5001, "AI 服务调用失败"),
    DATABASE_ERROR(5002, "数据库操作失败");
    
    private final int code;
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
