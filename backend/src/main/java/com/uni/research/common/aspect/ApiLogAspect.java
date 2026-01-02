package com.uni.research.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API 日志切面
 * 
 * 对应 408 考点：设计模式 - 代理模式（动态代理）
 * 
 * 设计思路：
 * 1. 使用 Spring AOP 实现非侵入式日志记录
 * 2. 基于 JDK 动态代理或 CGLIB 代理实现
 * 3. 记录请求参数、响应结果、执行耗时
 * 4. 便于性能监控和问题排查
 * 
 * 面试话术：
 * "我使用 Spring AOP 实现了统一的 API 日志切面。
 * AOP 底层基于动态代理模式，对于实现了接口的类使用 JDK 动态代理，
 * 对于没有接口的类使用 CGLIB 字节码增强。
 * 
 * 这个切面会拦截所有 Controller 层的方法，记录：
 * 1. 请求 URL 和 HTTP 方法
 * 2. 请求参数（用于问题复现）
 * 3. 执行耗时（用于性能监控）
 * 4. 异常信息（用于错误排查）
 * 
 * 这体现了面向切面编程（AOP）的思想，将横切关注点（日志）
 * 从业务逻辑中分离，提高了代码的可维护性。
 * 
 * 对应 408 考点：
 * - 设计模式：代理模式
 * - 软件工程：关注点分离（Separation of Concerns）
 * - 操作系统：系统调用拦截机制"
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {
    
    /**
     * 定义切点：拦截所有 Controller
     */
    @Pointcut("execution(public * com.uni.research.module.*.controller.*.*(..))")
    public void apiLog() {
    }
    
    /**
     * 环绕通知：记录 API 调用日志
     */
    @Around("apiLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String url = request.getRequestURI();
            String method = request.getMethod();
            String className = point.getTarget().getClass().getSimpleName();
            String methodName = point.getSignature().getName();
            Object[] args = point.getArgs();
            
            log.info("========== API 请求开始 ==========");
            log.info("请求 URL: {} {}", method, url);
            log.info("请求方法: {}.{}", className, methodName);
            log.info("请求参数: {}", args);
            
            try {
                // 执行目标方法
                Object result = point.proceed();
                
                long costTime = System.currentTimeMillis() - startTime;
                log.info("响应结果: {}", result);
                log.info("执行耗时: {} ms", costTime);
                log.info("========== API 请求结束 ==========\n");
                
                return result;
                
            } catch (Throwable e) {
                long costTime = System.currentTimeMillis() - startTime;
                log.error("========== API 请求异常 ==========");
                log.error("异常信息: {}", e.getMessage());
                log.error("执行耗时: {} ms", costTime);
                log.error("========== API 请求结束 ==========\n");
                throw e;
            }
        }
        
        // 如果不是 HTTP 请求，直接执行
        return point.proceed();
    }
}
