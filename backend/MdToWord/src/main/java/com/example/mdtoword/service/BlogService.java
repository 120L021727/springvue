package com.example.mdtoword.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mdtoword.pojo.Blog;
import java.util.List;

/**
 * 博客文章服务接口
 * 提供博客管理的业务逻辑方法
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
public interface BlogService {
    
    /**
     * 分页查询博客列表
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Blog> list(int page, int size);
    
    /**
     * 分页查询已发布的博客列表
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Blog> listPublished(int page, int size);
    
    /**
     * 按分类分页查询博客列表
     * 
     * @param categoryId 分类ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Blog> listByCategory(Integer categoryId, int page, int size);
    
    /**
     * 查询指定作者的博客列表
     * 
     * @param authorId 作者ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    Page<Blog> listByAuthor(Integer authorId, int page, int size);
    
    /**
     * 根据ID获取博客详情
     * 
     * @param id 博客ID
     * @return 博客信息，如果不存在返回null
     */
    Blog getById(Integer id);
    
    /**
     * 创建新博客
     * 
     * @param blog 博客信息
     * @return 是否创建成功
     */
    boolean create(Blog blog);
    
    /**
     * 更新博客信息
     * 
     * @param blog 博客信息
     * @return 是否更新成功
     */
    boolean update(Blog blog);
    
    /**
     * 删除博客
     * 
     * @param id 博客ID
     * @return 是否删除成功
     */
    boolean delete(Integer id);
    
    /**
     * 更新博客状态
     * 
     * @param id 博客ID
     * @param status 新状态（draft/published）
     * @return 是否更新成功
     */
    boolean updateStatus(Integer id, String status);
    
    /**
     * 检查博客是否存在
     * 
     * @param id 博客ID
     * @return 是否存在
     */
    boolean exists(Integer id);
    
    /**
     * 检查是否为博客作者
     * 
     * @param blogId 博客ID
     * @param authorId 作者ID
     * @return 是否为作者
     */
    boolean isAuthor(Integer blogId, Integer authorId);
    
    /**
     * 获取指定状态的所有博客
     * 
     * @param status 博客状态
     * @return 博客列表
     */
    List<Blog> listByStatus(String status);
} 