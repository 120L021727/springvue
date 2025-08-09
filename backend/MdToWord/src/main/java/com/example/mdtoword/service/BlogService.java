package com.example.mdtoword.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mdtoword.pojo.Blog;
import java.util.List;

/**
 * 博客文章服务接口
 * 提供博客管理的业务逻辑方法
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
public interface BlogService {
    
    /**
     * 分页查询博客列表（支持多条件筛选和权限过滤）
     * 
     * 功能说明：
     * - 支持按状态筛选（draft/published）
     * - 支持按分类ID筛选
     * - 支持按关键词搜索（标题和内容）
     * - 支持按作者ID筛选
     * - 按创建时间降序排列
     * - 自动验证分类ID的有效性
     * - 权限过滤：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param status 状态筛选，可选值：draft/published，null表示查询所有状态
     * @param categoryId 分类ID，可选，null表示查询所有分类
     * @param keyword 关键词，可选，null表示不进行关键词搜索
     * @param authorId 作者ID，可选，null表示查询所有作者
     * @param currentUserId 当前登录用户ID，null表示未登录用户
     * @return 分页结果，包含博客列表和分页信息
     */
    Page<Blog> list(int page, int size, String status, Integer categoryId, String keyword, Integer authorId, Integer currentUserId);
    
    /**
     * 根据ID获取博客详情
     * 
     * 功能说明：
     * - 根据博客ID查询完整的博客信息
     * - 如果博客不存在返回null
     * - 权限验证：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 
     * @param id 博客ID
     * @param currentUserId 当前登录用户ID，null表示未登录用户
     * @return 博客信息，如果不存在或无权限访问返回null
     */
    Blog getById(Integer id, Integer currentUserId);
    
    /**
     * 创建新博客
     * 
     * 功能说明：
     * - 验证博客标题、内容、作者ID不能为空
     * - 验证分类ID的有效性（如果指定了分类）
     * - 设置默认状态为draft（如果未指定状态）
     * - 自动填充创建时间和更新时间
     * - 返回创建是否成功
     * 
     * @param blog 博客信息，必须包含标题、内容、作者ID
     * @return 是否创建成功
     */
    boolean create(Blog blog);
    
    /**
     * 更新博客信息
     * 
     * 功能说明：
     * - 验证博客是否存在
     * - 验证博客标题、内容不能为空
     * - 验证分类ID的有效性（如果指定了分类）
     * - 自动更新更新时间
     * - 返回更新是否成功
     * 
     * @param blog 博客信息，必须包含ID、标题、内容
     * @return 是否更新成功
     */
    boolean update(Blog blog);
    
    /**
     * 删除博客
     * 
     * 功能说明：
     * - 验证博客是否存在
     * - 执行物理删除操作
     * - 返回删除是否成功
     * 
     * @param id 博客ID
     * @return 是否删除成功
     */
    boolean delete(Integer id);
    
    /**
     * 更新博客状态
     * 
     * 功能说明：
     * - 验证博客是否存在
     * - 验证状态值是否有效（只能是draft或published）
     * - 只更新状态字段，不影响其他字段
     * - 自动更新更新时间
     * - 返回更新是否成功
     * 
     * @param id 博客ID
     * @param status 新状态，只能是draft或published
     * @return 是否更新成功
     */
    boolean updateStatus(Integer id, String status);
    
    /**
     * 检查是否为博客作者
     * 
     * 功能说明：
     * - 验证博客是否存在
     * - 比较博客的作者ID与传入的作者ID
     * - 用于权限验证，判断用户是否有权限操作博客
     * 
     * @param blogId 博客ID
     * @param authorId 作者ID
     * @return 是否为作者，如果博客不存在返回false
     */
    boolean isAuthor(Integer blogId, Integer authorId);
} 