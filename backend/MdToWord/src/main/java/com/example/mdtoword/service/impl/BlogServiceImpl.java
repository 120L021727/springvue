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

import java.util.List;

/**
 * 博客文章服务实现类
 * 实现博客管理的具体业务逻辑
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    
    @Autowired
    private BlogMapper blogMapper;
    
    @Autowired
    private CategoryMapper categoryMapper; // 用于验证分类是否存在
    
    /**
     * 分页查询博客列表
     * 
     * 实现思路：
     * 1. 创建分页对象
     * 2. 构建查询条件（按创建时间降序）
     * 3. 执行分页查询
     * 4. 返回分页结果
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<Blog> list(int page, int size) {
        // 1. 创建分页对象（MyBatis Plus的页码从1开始）
        Page<Blog> pageParam = new Page<>(page, size);
        
        // 2. 构建查询条件：按创建时间降序排序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blog::getCreateTime);
        
        // 3. 执行分页查询
        return blogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 分页查询已发布的博客列表
     * 
     * 实现思路：
     * 1. 创建分页对象
     * 2. 构建查询条件（状态为published，按创建时间降序）
     * 3. 执行分页查询
     * 4. 返回分页结果
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<Blog> listPublished(int page, int size) {
        // 1. 创建分页对象
        Page<Blog> pageParam = new Page<>(page, size);
        
        // 2. 构建查询条件：状态为published，按创建时间降序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getStatus, "published")
                   .orderByDesc(Blog::getCreateTime);
        
        // 3. 执行分页查询
        return blogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 按分类分页查询博客列表
     * 
     * 实现思路：
     * 1. 验证分类是否存在
     * 2. 创建分页对象
     * 3. 构建查询条件（指定分类，按创建时间降序）
     * 4. 执行分页查询
     * 5. 返回分页结果
     * 
     * @param categoryId 分类ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<Blog> listByCategory(Integer categoryId, int page, int size) {
        // 1. 验证分类是否存在
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 2. 创建分页对象
        Page<Blog> pageParam = new Page<>(page, size);
        
        // 3. 构建查询条件：指定分类，按创建时间降序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getCategoryId, categoryId)
                   .orderByDesc(Blog::getCreateTime);
        
        // 4. 执行分页查询
        return blogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 查询指定作者的博客列表
     * 
     * 实现思路：
     * 1. 创建分页对象
     * 2. 构建查询条件（指定作者，按创建时间降序）
     * 3. 执行分页查询
     * 4. 返回分页结果
     * 
     * @param authorId 作者ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<Blog> listByAuthor(Integer authorId, int page, int size) {
        // 1. 创建分页对象
        Page<Blog> pageParam = new Page<>(page, size);
        
        // 2. 构建查询条件：指定作者，按创建时间降序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getAuthorId, authorId)
                   .orderByDesc(Blog::getCreateTime);
        
        // 3. 执行分页查询
        return blogMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 根据ID获取博客详情
     * 
     * 实现思路：
     * 1. 直接使用MyBatis Plus的selectById方法
     * 2. 如果ID不存在，返回null
     * 
     * @param id 博客ID
     * @return 博客信息，如果不存在返回null
     */
    @Override
    public Blog getById(Integer id) {
        return blogMapper.selectById(id);
    }
    
    /**
     * 创建新博客
     * 
     * 实现思路：
     * 1. 验证输入参数
     * 2. 验证分类是否存在
     * 3. 设置默认状态（如果没有提供）
     * 4. 插入数据库
     * 5. 返回操作结果
     * 
     * @param blog 博客信息
     * @return 是否创建成功
     */
    @Override
    @Transactional
    public boolean create(Blog blog) {
        // 1. 验证输入参数
        if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
            throw new BusinessException("博客标题不能为空");
        }
        if (blog.getContent() == null || blog.getContent().trim().isEmpty()) {
            throw new BusinessException("博客内容不能为空");
        }
        if (blog.getAuthorId() == null) {
            throw new BusinessException("作者ID不能为空");
        }
        
        // 2. 验证分类是否存在（如果指定了分类）
        if (blog.getCategoryId() != null) {
            Category category = categoryMapper.selectById(blog.getCategoryId());
            if (category == null) {
                throw new BusinessException("指定的分类不存在");
            }
        }
        
        // 3. 设置默认状态（如果没有提供）
        if (blog.getStatus() == null || blog.getStatus().trim().isEmpty()) {
            blog.setStatus("draft"); // 默认为草稿状态
        }
        
        // 4. 插入数据库
        int result = blogMapper.insert(blog);
        return result > 0;
    }
    
    /**
     * 更新博客信息
     * 
     * 实现思路：
     * 1. 验证博客是否存在
     * 2. 验证输入参数
     * 3. 验证分类是否存在（如果指定了分类）
     * 4. 更新数据库
     * 5. 返回操作结果
     * 
     * @param blog 博客信息
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean update(Blog blog) {
        // 1. 验证博客是否存在
        Blog existingBlog = blogMapper.selectById(blog.getId());
        if (existingBlog == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 2. 验证输入参数
        if (blog.getTitle() == null || blog.getTitle().trim().isEmpty()) {
            throw new BusinessException("博客标题不能为空");
        }
        if (blog.getContent() == null || blog.getContent().trim().isEmpty()) {
            throw new BusinessException("博客内容不能为空");
        }
        
        // 3. 验证分类是否存在（如果指定了分类）
        if (blog.getCategoryId() != null) {
            Category category = categoryMapper.selectById(blog.getCategoryId());
            if (category == null) {
                throw new BusinessException("指定的分类不存在");
            }
        }
        
        // 4. 更新数据库
        int result = blogMapper.updateById(blog);
        return result > 0;
    }
    
    /**
     * 删除博客
     * 
     * 实现思路：
     * 1. 验证博客是否存在
     * 2. 执行删除操作
     * 3. 返回操作结果
     * 
     * @param id 博客ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean delete(Integer id) {
        // 1. 验证博客是否存在
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 2. 执行删除操作
        int result = blogMapper.deleteById(id);
        return result > 0;
    }
    
    /**
     * 更新博客状态
     * 
     * 实现思路：
     * 1. 验证博客是否存在
     * 2. 验证状态值是否有效
     * 3. 更新状态
     * 4. 返回操作结果
     * 
     * @param id 博客ID
     * @param status 新状态（draft/published）
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateStatus(Integer id, String status) {
        // 1. 验证博客是否存在
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }
        
        // 2. 验证状态值是否有效
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException("无效的状态值，只能是 draft 或 published");
        }
        
        // 3. 更新状态
        Blog updateBlog = new Blog();
        updateBlog.setId(id);
        updateBlog.setStatus(status);
        
        int result = blogMapper.updateById(updateBlog);
        return result > 0;
    }
    
    /**
     * 检查博客是否存在
     * 
     * 实现思路：
     * 1. 使用selectById查询
     * 2. 返回查询结果是否为null
     * 
     * @param id 博客ID
     * @return 是否存在
     */
    @Override
    public boolean exists(Integer id) {
        return blogMapper.selectById(id) != null;
    }
    
    /**
     * 检查是否为博客作者
     * 
     * 实现思路：
     * 1. 查询博客信息
     * 2. 比较作者ID
     * 3. 返回比较结果
     * 
     * @param blogId 博客ID
     * @param authorId 作者ID
     * @return 是否为作者
     */
    @Override
    public boolean isAuthor(Integer blogId, Integer authorId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            return false;
        }
        return authorId.equals(blog.getAuthorId());
    }
    
    /**
     * 获取指定状态的所有博客
     * 
     * 实现思路：
     * 1. 构建查询条件（指定状态，按创建时间降序）
     * 2. 执行查询
     * 3. 返回结果列表
     * 
     * @param status 博客状态
     * @return 博客列表
     */
    @Override
    public List<Blog> listByStatus(String status) {
        // 1. 构建查询条件：指定状态，按创建时间降序
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getStatus, status)
                   .orderByDesc(Blog::getCreateTime);
        
        // 2. 执行查询
        return blogMapper.selectList(queryWrapper);
    }
} 