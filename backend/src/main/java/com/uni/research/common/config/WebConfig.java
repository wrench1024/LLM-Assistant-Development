package com.uni.research.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 
 * 对应 408 考点：计算机网络 - 跨域资源共享（CORS）
 * 
 * 设计思路：
 * 1. 配置 CORS 允许前端跨域访问
 * 2. 开发环境允许所有域名，生产环境需限制
 * 
 * 面试话术：
 * "我配置了 CORS 跨域支持，允许前端应用访问后端 API。
 * CORS 是浏览器的同源策略限制，需要服务器端配置允许跨域请求。
 * 开发环境我允许所有域名访问，生产环境需要限制为特定域名，提高安全性。"
 * 
 * @author wrench1024
 * @since 2026-01-02
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // 允许所有域名（生产环境需限制）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // 预检请求缓存时间（秒）
    }
}
