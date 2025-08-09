package com.example.mdtoword.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储用户基本信息和认证数据
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Data
@TableName("tb_user") // 指定表名
public class User {
    
    // 校验分组接口
    public interface RegisterGroup {}
    public interface UpdateInfoGroup {}
    
    @TableId(value = "id", type = IdType.AUTO) // 指定主键及生成策略
    private Integer id; // 主键ID
    
    @NotBlank(message = "用户名不能为空", groups = RegisterGroup.class)
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间", groups = RegisterGroup.class)
    private String username; // 用户名
    
    @JsonIgnore
    @NotBlank(message = "密码不能为空", groups = RegisterGroup.class)
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间", groups = RegisterGroup.class)
    private String password; // 密码
    
    @NotBlank(message = "昵称不能为空", groups = {RegisterGroup.class, UpdateInfoGroup.class})
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20个字符之间", groups = {RegisterGroup.class, UpdateInfoGroup.class})
    private String nickname; // 昵称

    @NotBlank(message = "邮箱不能为空", groups = {RegisterGroup.class, UpdateInfoGroup.class})
    @Email(message = "邮箱格式不正确", groups = {RegisterGroup.class, UpdateInfoGroup.class})
    private String email; // 邮箱
    
    @TableField("user_pic")
    private String userPic; // 用户头像地址
    
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 更新时间
}

