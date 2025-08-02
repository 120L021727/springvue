package com.example.mdtoword.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储用户基本信息和认证数据
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@Data
@TableName("tb_user") // 指定表名
public class User {
    
    @TableId(value = "id", type = IdType.AUTO) // 指定主键及生成策略
    private Integer id; // 主键ID
    
    private String username; // 用户名
    
    @JsonIgnore
    private String password; // 密码
    
    private String nickname; // 昵称

    private String email; // 邮箱
    
    @TableField("user_pic")
    private String userPic; // 用户头像地址
    
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 更新时间
}

