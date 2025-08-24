package com.example.mdtoword.config;

import com.example.mdtoword.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 聊天相关定时任务
 * 
 * 功能：
 * 1. 定期清理过期的在线用户
 * 2. 维护Redis中用户状态的准确性
 * 3. 防止内存泄漏和状态不一致
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Component
@EnableScheduling
public class ChatScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatScheduledTasks.class);
    
    @Autowired
    private ChatService chatService;
    
    /**
     * 定时清理过期的在线用户
     * 
     * 执行频率：每2分钟执行一次
     * 功能：清理Redis中过期的在线用户记录
     * 
     * 说明：虽然Redis有TTL自动过期机制，但定时清理可以确保：
     * 1. 及时释放Redis内存
     * 2. 保持在线用户列表的准确性
     * 3. 处理可能的TTL设置异常情况
     */
    @Scheduled(fixedRate = 120000) // 每2分钟执行一次
    public void cleanExpiredOnlineUsers() {
        try {
            chatService.cleanExpiredOnlineUsers();
            logger.debug("定时清理过期在线用户任务执行完成");
        } catch (Exception e) {
            logger.error("定时清理过期在线用户任务执行失败", e);
        }
    }
}
