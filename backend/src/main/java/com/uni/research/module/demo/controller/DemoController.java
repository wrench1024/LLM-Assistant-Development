package com.uni.research.module.demo.controller;

import com.uni.research.common.exception.BizException;
import com.uni.research.common.result.Result;
import com.uni.research.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示测试控制器
 * 
 * 用于快速展示系统功能：
 * 1. 统一响应格式
 * 2. 全局异常处理
 * 3. AOP 日志记录
 * 4. API 文档生成
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Slf4j
@Tag(name = "演示测试", description = "用于测试系统基础功能的演示接口")
@RestController
@RequestMapping("/demo")
public class DemoController {
    
    /**
     * 测试成功响应
     */
    @Operation(summary = "测试成功响应", description = "返回一个成功的响应示例")
    @GetMapping("/success")
    public Result<Map<String, Object>> testSuccess() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Hello, Uni-Research-Assistant!");
        data.put("timestamp", LocalDateTime.now());
        data.put("status", "running");
        data.put("version", "1.0.0");
        
        log.info("测试成功响应接口被调用");
        return Result.success(data);
    }
    
    /**
     * 测试带参数的接口
     */
    @Operation(summary = "测试带参数接口", description = "接收参数并返回处理结果")
    @GetMapping("/echo")
    public Result<Map<String, Object>> testEcho(@RequestParam(defaultValue = "World") String name) {
        Map<String, Object> data = new HashMap<>();
        data.put("input", name);
        data.put("output", "Hello, " + name + "!");
        data.put("length", name.length());
        
        log.info("Echo 接口被调用，参数: {}", name);
        return Result.success("处理成功", data);
    }
    
    /**
     * 测试业务异常
     */
    @Operation(summary = "测试业务异常", description = "模拟业务异常，测试全局异常处理器")
    @GetMapping("/error/biz")
    public Result<Void> testBizException() {
        log.warn("即将抛出业务异常");
        throw new BizException(ResultCode.USER_NOT_FOUND, "这是一个模拟的业务异常");
    }
    
    /**
     * 测试系统异常
     */
    @Operation(summary = "测试系统异常", description = "模拟系统异常，测试全局异常处理器")
    @GetMapping("/error/system")
    public Result<Void> testSystemException() {
        log.warn("即将抛出系统异常");
        throw new RuntimeException("这是一个模拟的系统异常");
    }
    
    /**
     * 测试参数校验异常
     */
    @Operation(summary = "测试参数校验", description = "测试参数校验和异常处理")
    @GetMapping("/error/param")
    public Result<Void> testParamException(@RequestParam Integer age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("年龄参数不合法：" + age);
        }
        return Result.success();
    }
    
    /**
     * 测试耗时操作（用于测试 AOP 日志）
     */
    @Operation(summary = "测试耗时操作", description = "模拟耗时操作，测试 AOP 日志记录")
    @GetMapping("/slow")
    public Result<Map<String, Object>> testSlowOperation() throws InterruptedException {
        log.info("开始执行耗时操作");
        
        // 模拟耗时操作
        Thread.sleep(2000);
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "耗时操作完成");
        data.put("duration", "2000ms");
        
        log.info("耗时操作执行完成");
        return Result.success(data);
    }
    
    /**
     * 获取系统信息
     */
    @Operation(summary = "获取系统信息", description = "返回系统运行状态和配置信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // 系统信息
        info.put("projectName", "Uni-Research-Assistant");
        info.put("version", "1.0.0");
        info.put("author", "wrench1024");
        
        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("processors", runtime.availableProcessors());
        runtimeInfo.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        runtimeInfo.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        info.put("runtime", runtimeInfo);
        
        // Java 信息
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("home", System.getProperty("java.home"));
        info.put("java", javaInfo);
        
        // 操作系统信息
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("arch", System.getProperty("os.arch"));
        info.put("os", osInfo);
        
        return Result.success(info);
    }
}
