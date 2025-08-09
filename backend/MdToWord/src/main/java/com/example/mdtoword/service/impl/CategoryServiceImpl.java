package com.example.mdtoword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.mapper.BlogMapper;
import com.example.mdtoword.mapper.CategoryMapper;
import com.example.mdtoword.pojo.Category;
import com.example.mdtoword.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博客分类服务实现类
 * 实现分类管理的具体业务逻辑
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private BlogMapper blogMapper;
    
    /**
     * 获取所有分类列表（按排序升序）
     * 
     * 实现逻辑：
     * 1. 构建查询条件，按排序字段升序排列
     * 2. 如果排序值相同，按创建时间降序排列
     * 3. 返回排序后的分类列表
     * 
     * @return 分类列表，按排序字段升序排列
     */
    @Override
    public List<Category> listAll() {
        // 构建查询条件：按排序字段升序，相同排序按创建时间降序
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSortOrder)
                   .orderByDesc(Category::getCreateTime);
        
        return categoryMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取分类详情
     * 
     * 实现逻辑：
     * 直接使用MyBatis Plus的selectById方法查询分类信息
     * 
     * @param id 分类ID
     * @return 分类信息，如果不存在返回null
     */
    @Override
    public Category getById(Integer id) {
        return categoryMapper.selectById(id);
    }
    
    /**
     * 创建新分类
     * 
     * 实现逻辑：
     * 1. 验证分类名称不能为空
     * 2. 检查分类名称的唯一性
     * 3. 设置默认排序值（如果未指定）
     * 4. 插入数据库并返回操作结果
     * 
     * @param category 分类信息，必须包含分类名称
     * @return 是否创建成功
     */
    @Override
    @Transactional
    public boolean create(Category category) {
        // 验证分类名称不能为空
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        
        // 检查分类名称的唯一性
        if (existsByName(category.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 设置默认排序值（如果未指定）
        if (category.getSortOrder() == null) {
            category.setSortOrder(getNextSortOrder());
        }
        
        // 插入数据库并返回操作结果
        return categoryMapper.insert(category) > 0;
    }
    
    /**
     * 更新分类信息
     * 
     * 实现逻辑：
     * 1. 验证分类是否存在
     * 2. 验证分类名称不能为空
     * 3. 检查分类名称的唯一性（排除当前分类）
     * 4. 更新数据库并返回操作结果
     * 
     * @param category 分类信息，必须包含ID和分类名称
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean update(Category category) {
        // 验证分类是否存在
        if (categoryMapper.selectById(category.getId()) == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 验证分类名称不能为空
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        
        // 检查分类名称的唯一性（排除当前分类）
        if (existsByName(category.getName(), category.getId())) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 更新数据库并返回操作结果
        return categoryMapper.updateById(category) > 0;
    }
    
    /**
     * 删除分类
     * 
     * 实现逻辑：
     * 1. 验证分类是否存在
     * 2. 检查分类下是否有文章（有文章则不允许删除）
     * 3. 执行删除操作并返回结果
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean delete(Integer id) {
        // 验证分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类下是否有文章（有文章则不允许删除）
        LambdaQueryWrapper<com.example.mdtoword.pojo.Blog> blogQueryWrapper = new LambdaQueryWrapper<>();
        blogQueryWrapper.eq(com.example.mdtoword.pojo.Blog::getCategoryId, id);
        Long articleCount = blogMapper.selectCount(blogQueryWrapper);
        
        if (articleCount > 0) {
            throw new BusinessException("该分类下有 " + articleCount + " 篇文章，无法删除");
        }
        
        // 执行删除操作并返回结果
        return categoryMapper.deleteById(id) > 0;
    }
    
    /**
     * 检查分类名称是否已存在
     * 
     * 实现逻辑：
     * 1. 构建查询条件，按分类名称查询
     * 2. 统计查询结果数量
     * 3. 返回是否存在（数量大于0表示存在）
     * 
     * @param name 分类名称
     * @return 是否存在
     */
    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name);
        return categoryMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查分类名称是否已存在（排除指定ID）
     * 
     * 实现逻辑：
     * 1. 构建查询条件，按分类名称查询但排除指定ID
     * 2. 统计查询结果数量
     * 3. 返回是否存在（数量大于0表示存在）
     * 
     * 使用场景：
     * 在更新分类时，需要检查名称唯一性但排除当前分类本身
     * 
     * @param name 分类名称
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    @Override
    public boolean existsByName(String name, Integer excludeId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name)
                   .ne(Category::getId, excludeId);
        return categoryMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 获取分类文章统计
     * 
     * 实现逻辑：
     * 1. 获取所有分类列表
     * 2. 遍历每个分类，统计该分类下的文章数量
     * 3. 返回分类ID与文章数量的映射关系
     * 
     * 使用场景：
     * 在分类管理页面显示每个分类下的文章数量
     * 
     * @return 分类ID与文章数量的映射关系
     */
    @Override
    public Map<Integer, Long> getCategoryArticleCount() {
        Map<Integer, Long> categoryCountMap = new HashMap<>();
        
        // 获取所有分类
        List<Category> categories = listAll();
        
        // 统计每个分类的文章数量
        for (Category category : categories) {
            LambdaQueryWrapper<com.example.mdtoword.pojo.Blog> blogQueryWrapper = new LambdaQueryWrapper<>();
            blogQueryWrapper.eq(com.example.mdtoword.pojo.Blog::getCategoryId, category.getId());
            Long count = blogMapper.selectCount(blogQueryWrapper);
            categoryCountMap.put(category.getId(), count);
        }
        
        return categoryCountMap;
    }
    
    /**
     * 获取下一个排序值
     * 
     * 实现逻辑：
     * 1. 查询当前最大的排序值
     * 2. 返回最大值+1作为新的排序值
     * 3. 如果没有分类，返回1作为默认排序值
     * 
     * 使用场景：
     * 在创建新分类时，如果未指定排序值，自动设置为当前最大值+1
     * 
     * @return 下一个排序值
     */
    private Integer getNextSortOrder() {
        // 查询当前最大的排序值
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Category::getSortOrder)
                   .orderByDesc(Category::getSortOrder)
                   .last("LIMIT 1");
        
        Category maxSortCategory = categoryMapper.selectOne(queryWrapper);
        
        // 返回最大值+1，如果没有分类则返回1
        return (maxSortCategory != null && maxSortCategory.getSortOrder() != null) 
            ? maxSortCategory.getSortOrder() + 1 : 1;
    }
} 