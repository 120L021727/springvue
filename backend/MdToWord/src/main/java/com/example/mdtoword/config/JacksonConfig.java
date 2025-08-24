package com.example.mdtoword.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson配置类
 * 
 * 功能：
 * 1. 配置ObjectMapper的序列化和反序列化规则
 * 2. 支持Java 8时间类型
 * 3. 用于Redis中JSON数据的转换
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Configuration
public class JacksonConfig {
    
    /**
     * 配置ObjectMapper Bean
     * 
     * 特性：
     * 1. 支持Java 8时间类型（LocalDateTime等）
     * 2. 配置合适的序列化格式
     * 3. 用于Redis存储OnlineUser等对象
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
