package com.example.mdtoword;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.mdtoword.mapper")
public class MdToWordApplication {
    // 定义一个Logger对象，用于记录日志
    private static final Logger logger = LoggerFactory.getLogger(MdToWordApplication.class);
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(MdToWordApplication.class, args);
        // 记录日志，表示应用已启动
        logger.info("✅ 日志框架测试：应用已启动");
    }

}
