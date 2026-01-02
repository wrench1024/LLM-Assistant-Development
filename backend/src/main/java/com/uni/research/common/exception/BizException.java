package com.uni.research.common.exception;

import com.uni.research.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 
 * 对应 408 考点：软件工程 - 异常处理机制
 * 
 * 设计思路：
 * 1. 继承 RuntimeException，无需强制捕获
 * 2. 包含错误码和错误消息，便于统一处理
 * 3. 提供多种构造方法，灵活使用
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Getter
public class BizException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private int code;
    
    /**
     * 错误消息
     */
    private String message;
    
    public BizException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }
    
    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    
    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
}
