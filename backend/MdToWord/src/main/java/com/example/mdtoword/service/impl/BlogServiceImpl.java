package com.example.mdtoword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.mapper.BlogMapper;
import com.example.mdtoword.mapper.CategoryMapper;
import com.example.mdtoword.pojo.Blog;
import com.example.mdtoword.pojo.Category;
import com.example.mdtoword.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 博客文章服务实现类
 * 实现博客管理的具体业务逻辑
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    
    @Autowired
    private BlogMapper blogMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    /**
     * 分页查询博客列表（支持多条件筛选和权限过滤）
     * 
     * 实现逻辑：
     * 1. 创建分页对象，设置页码和每页大小
     * 2. 构建查询条件，默认按创建时间降序排序
     * 3. 根据传入的参数动态添加筛选条件
     * 4. 应用权限过滤：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 5. 执行分页查询并返回结果
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
    @Override
    public Page<Blog> list(int page, int size, String status, Integer categoryId, String keyword, Integer authorId, Integer currentUserId) {
        // 创建分页对象，MyBatis Plus的页码从1开始
        Page<Blog> pageParam = new Page<>(page, size);
        
        // 构建查询条件，默认按创建时间降序排序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blog::getCreateTime);
        
        // 添加状态筛选条件：只有当status不为null且不为空时才添加
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Blog::getStatus, status);
        }
        
        // 添加分类筛选条件：验证分类存在性并添加查询条件
        if (categoryId != null) {
            // 验证分类是否存在，如果不存在则抛出异常
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
            queryWrapper.eq(Blog::getCategoryId, categoryId);
        }
        
        // 添加关键词搜索条件：在标题和内容中进行模糊搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Blog::getTitle, keyword.trim())
                       .or()
                       .like(Blog::getContent, keyword.trim());
        }
        
        // 添加作者筛选条件
        if (authorId != null) {
            queryWrapper.eq(Blog::getAuthorId, authorId);
        }
        
        // 应用权限过滤：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
        if (currentUserId == null) {
            // 未登录用户只能查看已发布的文章
            queryWrapper.eq(Blog::getStatus, "published");
        } else {
            // 已登录用户可以查看自己的所有文章+别人的已发布文章
            queryWrapper.and(wrapper -> wrapper
                .eq(Blog::getStatus, "published")  // 已发布的文章
                .or()
                .eq(Blog::getAuthorId, currentUserId)  // 或者自己的文章（包括草稿）
            );
        }
        
        // 执行分页查询并返回结果
        return blogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 根据ID获取博客详情（带权限验证）
     * 
     * 实现逻辑：
     * 1. 根据博客ID查询博客信息
     * 2. 应用权限验证：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 3. 返回博客信息或null
     * 
     * @param id 博客ID
     * @param currentUserId 当前登录用户ID，null表示未登录用户
     * @return 博客信息，如果不存在或无权限访问返回null
     */
    @Override
    public Blog getById(Integer id, Integer currentUserId) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            return null;
        }
        
        // 权限验证：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
        if (currentUserId == null) {
            // 未登录用户只能查看已发布的文章
            return "published".equals(blog.getStatus()) ? blog : null;
        } else {
            // 已登录用户可以查看自己的所有文章+别人的已发布文章
            if ("published".equals(blog.getStatus()) || currentUserId.equals(blog.getAuthorId())) {
                return blog;
            }
            return null;
        }
    }
    
    /**
     * 创建新博客
     * 
     * 实现逻辑：
     * 1. 验证博客数据的完整性（标题、内容、作者ID）
     * 2. 验证分类ID的有效性（如果指定了分类）
     * 3. 设置默认状态为draft（如果未指定状态）
     * 4. 插入数据库并返回操作结果
     * 
     * @param blog 博客信息，必须包含标题、内容、作者ID
     * @return 是否创建成功
     */
    @Override
    @Transactional
    public boolean create(Blog blog) {
        // 验证博客数据的完整性
        validateBlogData(blog);
        
        // 验证分类ID的有效性（如果指定了分类）
        if (blog.getCategoryId() != null) {
            Category category = categoryMapper.selectById(blog.getCategoryId());
            if (category == null) {
                throw new BusinessException("指定的分类不存在");
            }
        }
        
        // 设置默认状态为draft（如果未指定状态）
        if (!StringUtils.hasText(blog.getStatus())) {
            blog.setStatus("draft");
        }
        
        // 插入数据库并返回操作结果
        return blogMapper.insert(blog) > 0;
    }
    
    /**
     * 更新博客信息
     * 
     * 实现逻辑：
     * 1. 验证博客是否存在
     * 2. 验证博客数据的完整性（标题、内容）
     * 3. 验证分类ID的有效性（如果指定了分类）
     * 4. 更新数据库并返回操作结果
     * 
     * @param blog 博客信息，必须包含ID、标题、内容
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean update(Blog blog) {
        // 验证博客是否存在
        Blog existingBlog = blogMapper.selectById(blog.getId());
        if (existingBlog == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 验证博客数据的完整性
        validateBlogData(blog);
        
        // 验证分类ID的有效性（如果指定了分类）
        if (blog.getCategoryId() != null) {
            Category category = categoryMapper.selectById(blog.getCategoryId());
            if (category == null) {
                throw new BusinessException("指定的分类不存在");
            }
        }
        
        // 更新数据库并返回操作结果
        return blogMapper.updateById(blog) > 0;
    }
    
    /**
     * 删除博客
     * 
     * 实现逻辑：
     * 1. 验证博客是否存在
     * 2. 执行物理删除操作
     * 3. 返回删除结果
     * 
     * @param id 博客ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean delete(Integer id) {
        // 验证博客是否存在
        if (blogMapper.selectById(id) == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 执行物理删除操作
        return blogMapper.deleteById(id) > 0;
    }
    
    /**
     * 更新博客状态
     * 
     * 实现逻辑：
     * 1. 验证博客是否存在
     * 2. 验证状态值的有效性（只能是draft或published）
     * 3. 创建更新对象，只更新状态字段
     * 4. 执行更新操作并返回结果
     * 
     * @param id 博客ID
     * @param status 新状态，只能是draft或published
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateStatus(Integer id, String status) {
        // 验证博客是否存在
        if (blogMapper.selectById(id) == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 验证状态值的有效性
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException("无效的状态值，只能是 draft 或 published");
        }
        
        // 创建更新对象，只更新状态字段
        Blog updateBlog = new Blog();
        updateBlog.setId(id);
        updateBlog.setStatus(status);
        
        // 执行更新操作并返回结果
        return blogMapper.updateById(updateBlog) > 0;
    }
    
    /**
     * 检查是否为博客作者
     * 
     * 实现逻辑：
     * 1. 根据博客ID查询博客信息
     * 2. 如果博客不存在返回false
     * 3. 比较博客的作者ID与传入的作者ID
     * 4. 返回比较结果
     * 
     * @param blogId 博客ID
     * @param authorId 作者ID
     * @return 是否为作者，如果博客不存在返回false
     */
    @Override
    public boolean isAuthor(Integer blogId, Integer authorId) {
        Blog blog = blogMapper.selectById(blogId);
        return blog != null && authorId.equals(blog.getAuthorId());
    }
    
    /**
     * 验证博客数据
     * 
     * 验证规则：
     * - 博客标题不能为空
     * - 博客内容不能为空
     * - 作者ID不能为空
     * 
     * @param blog 博客信息
     * @throws BusinessException 当验证失败时抛出异常
     */
    private void validateBlogData(Blog blog) {
        if (!StringUtils.hasText(blog.getTitle())) {
            throw new BusinessException("博客标题不能为空");
        }
        if (!StringUtils.hasText(blog.getContent())) {
            throw new BusinessException("博客内容不能为空");
        }
        if (blog.getAuthorId() == null) {
            throw new BusinessException("作者ID不能为空");
        }
    }
} 