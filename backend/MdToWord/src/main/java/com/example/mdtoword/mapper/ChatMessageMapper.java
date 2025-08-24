package com.example.mdtoword.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mdtoword.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 * 
 * 功能：
 * 1. 继承MyBatis-Plus BaseMapper，提供基础CRUD操作
 * 2. 自定义查询方法，支持聊天记录查询
 * 3. 支持分页查询和条件筛选
 * 
 * @author 坤坤
 * @since 2025-08-24
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 查询公共房间最近的聊天记录
     * 
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 聊天消息列表
     */
    List<ChatMessage> selectRecentPublicMessages(@Param("roomId") String roomId, @Param("limit") int limit);
    
    /**
     * 查询两个用户之间的私聊记录
     * 
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param limit 限制数量
     * @return 聊天消息列表
     */
    List<ChatMessage> selectPrivateMessages(@Param("userId1") Integer userId1, 
                                          @Param("userId2") Integer userId2, 
                                          @Param("limit") int limit);
}
