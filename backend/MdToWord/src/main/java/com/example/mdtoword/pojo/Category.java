package com.example.mdtoword.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 博客分类实体类
 * 用于管理博客文章的分类信息
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@Data
@TableName("tb_category") // 指定表名
public class Category {
    
    // 校验分组接口
    public interface CreateGroup {}
    public interface UpdateGroup {}
    
    @TableId(value = "id", type = IdType.AUTO) // 指定主键及生成策略
    private Integer id; // 主键ID
    
    @NotBlank(message = "分类名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String name; // 分类名称
    
    @Size(max = 200, message = "分类描述长度不能超过200个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String description; // 分类描述
    
    @TableField("sort_order")
    private Integer sortOrder; // 排序，数字越小越靠前
    
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 更新时间
}
