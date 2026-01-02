package com.uni.research.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 
 * 对应 408 考点：操作系统 - 线程管理与调度
 * 
 * 设计思路：
 * 1. 根据 I/O 密集型任务特点设计线程池参数
 * 2. 核心线程数 = CPU 核心数（避免过多线程切换）
 * 3. 最大线程数 = CPU 核心数 * 2（应对突发流量）
 * 4. 使用有界队列防止内存溢出（OOM）
 * 5. 使用 CallerRunsPolicy 拒绝策略实现优雅降级
 * 
 * 面试话术：
 * "我针对 AI 接口调用场景设计了专用线程池。
 * 
 * 因为调用外部 AI API 是典型的 I/O 密集型任务，线程大部分时间在等待网络响应，
 * 所以我将核心线程数设置为 CPU 核心数，最大线程数设置为核心数的 2 倍。
 * 
 * 队列选择了 LinkedBlockingQueue 有界队列，容量 100，防止任务堆积导致内存溢出。
 * 
 * 拒绝策略选择 CallerRunsPolicy：
 * - 当线程池和队列都满时，由调用线程（通常是 Tomcat 工作线程）直接执行任务
 * - 这样可以实现优雅降级，避免任务丢失
 * - 同时也能起到限流作用，防止系统过载
 * 
 * 此外，我还配置了 waitForTasksToCompleteOnShutdown = true，
 * 确保应用关闭时等待所有任务执行完毕，避免数据丢失。
 * 
 * 对应 408 考点：
 * - 操作系统：线程池原理、线程生命周期、线程调度算法
 * - 数据结构：阻塞队列（BlockingQueue）的实现原理
 * - 计算机组成：CPU 核心数与并发性能的关系"
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {
    
    /**
     * AI 调用专用线程池
     * 
     * 参数说明：
     * - corePoolSize: 核心线程数，即使空闲也不会被回收
     * - maxPoolSize: 最大线程数，队列满时才会创建新线程
     * - queueCapacity: 队列容量，核心线程忙时任务会进入队列
     * - keepAliveTime: 非核心线程的空闲存活时间（默认 60 秒）
     * - rejectedExecutionHandler: 拒绝策略
     */
    @Bean("aiTaskExecutor")
    public ThreadPoolTaskExecutor aiTaskExecutor() {
        // 获取 CPU 核心数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        
        log.info("初始化 AI 任务线程池，CPU 核心数: {}", corePoolSize);
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数 = CPU 核心数
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数 = CPU 核心数 * 2
        executor.setMaxPoolSize(corePoolSize * 2);
        
        // 队列容量 = 100（有界队列，防止 OOM）
        executor.setQueueCapacity(100);
        
        // 线程名称前缀（便于日志排查）
        executor.setThreadNamePrefix("ai-task-");
        
        // 拒绝策略：CallerRunsPolicy（由调用线程执行，实现优雅降级）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("AI 任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                corePoolSize, corePoolSize * 2, 100);
        
        return executor;
    }
}
