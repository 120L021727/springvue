package com.example.mdtoword;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MdToWordApplication {
    private static final Logger logger = LoggerFactory.getLogger(MdToWordApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(MdToWordApplication.class, args);
        logger.info("✅ 日志框架测试：应用已启动");
    }

}
