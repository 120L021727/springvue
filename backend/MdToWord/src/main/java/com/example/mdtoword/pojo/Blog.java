package com.example.mdtoword.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 博客文章实体类
 * 用于存储博客文章的基本信息
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Data
@TableName("tb_blog") // 指定表名
public class Blog {
    
    // 校验分组接口
    public interface CreateGroup {}
    public interface UpdateGroup {}
    
    @TableId(value = "id", type = IdType.AUTO) // 指定主键及生成策略
    private Integer id; // 主键ID
    
    @Size(min = 1, max = 200, message = "文章标题长度必须在1-200个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String title; // 文章标题
    
    private String content; // 文章内容

    // 富文本HTML内容
    @TableField("content_html")
    private String contentHtml;

    // 列表摘要
    private String summary;
    
    @TableField("category_id")
    private Integer categoryId; // 分类ID
    
    @TableField("author_id")
    private Integer authorId; // 作者ID
    
    private String status; // 状态：draft-草稿，published-已发布
    
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime; // 创建时间
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime; // 更新时间
}
