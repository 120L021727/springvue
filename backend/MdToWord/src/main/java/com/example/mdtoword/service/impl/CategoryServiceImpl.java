package com.example.mdtoword.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.mapper.BlogMapper;
import com.example.mdtoword.mapper.CategoryMapper;
import com.example.mdtoword.pojo.Category;
import com.example.mdtoword.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private BlogMapper blogMapper; // 用于检查分类下是否有文章
    
    /**
     * 获取所有分类列表（按排序升序）
     * 
     * 实现思路：
     * 1. 使用LambdaQueryWrapper构建查询条件
     * 2. 按sort_order字段升序排序
     * 3. 如果sort_order相同，按创建时间降序排序
     * 
     * @return 分类列表
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
     * 实现思路：
     * 1. 直接使用MyBatis Plus的selectById方法
     * 2. 如果ID不存在，返回null
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
     * 实现思路：
     * 1. 验证分类名称是否已存在
     * 2. 设置默认排序值（如果没有提供）
     * 3. 插入数据库
     * 4. 返回操作结果
     * 
     * @param category 分类信息
     * @return 是否创建成功
     */
    @Override
    @Transactional
    public boolean create(Category category) {
        // 1. 验证分类名称是否已存在
        if (existsByName(category.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 2. 如果没有设置排序值，自动设置
        if (category.getSortOrder() == null) {
            // 获取当前最大排序值，新分类排序值为最大值+1
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Category::getSortOrder)
                       .orderByDesc(Category::getSortOrder)
                       .last("LIMIT 1");
            
            Category maxSortCategory = categoryMapper.selectOne(queryWrapper);
            int newSortOrder = (maxSortCategory != null && maxSortCategory.getSortOrder() != null) 
                ? maxSortCategory.getSortOrder() + 1 : 1;
            category.setSortOrder(newSortOrder);
        }
        
        // 3. 插入数据库
        int result = categoryMapper.insert(category);
        return result > 0;
    }
    
    /**
     * 更新分类信息
     * 
     * 实现思路：
     * 1. 验证分类是否存在
     * 2. 验证分类名称是否与其他分类重复
     * 3. 更新数据库
     * 4. 返回操作结果
     * 
     * @param category 分类信息
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean update(Category category) {
        // 1. 验证分类是否存在
        Category existingCategory = categoryMapper.selectById(category.getId());
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 2. 验证分类名称是否与其他分类重复（排除当前分类）
        if (existsByName(category.getName(), category.getId())) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 3. 更新数据库
        int result = categoryMapper.updateById(category);
        return result > 0;
    }
    
    /**
     * 删除分类
     * 
     * 实现思路：
     * 1. 验证分类是否存在
     * 2. 检查分类下是否有文章
     * 3. 如果有文章，禁止删除
     * 4. 如果没有文章，执行删除
     * 5. 返回操作结果
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean delete(Integer id) {
        // 1. 验证分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 2. 检查分类下是否有文章
        LambdaQueryWrapper<com.example.mdtoword.pojo.Blog> blogQueryWrapper = new LambdaQueryWrapper<>();
        blogQueryWrapper.eq(com.example.mdtoword.pojo.Blog::getCategoryId, id);
        Long articleCount = blogMapper.selectCount(blogQueryWrapper);
        
        if (articleCount > 0) {
            throw new BusinessException("该分类下有 " + articleCount + " 篇文章，无法删除");
        }
        
        // 3. 执行删除
        int result = categoryMapper.deleteById(id);
        return result > 0;
    }
    
    /**
     * 检查分类名称是否已存在
     * 
     * 实现思路：
     * 1. 构建查询条件，按名称查询
     * 2. 返回查询结果数量是否大于0
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
     * 实现思路：
     * 1. 构建查询条件，按名称查询但排除指定ID
     * 2. 返回查询结果数量是否大于0
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
     * 实现思路：
     * 1. 查询所有分类
     * 2. 对每个分类统计文章数量
     * 3. 返回分类ID和文章数量的映射
     * 
     * @return Map<分类ID, 文章数量>
     */
    @Override
    public Map<Integer, Long> getCategoryArticleCount() {
        Map<Integer, Long> categoryCountMap = new HashMap<>();
        
        // 1. 获取所有分类
        List<Category> categories = listAll();
        
        // 2. 统计每个分类的文章数量
        for (Category category : categories) {
            LambdaQueryWrapper<com.example.mdtoword.pojo.Blog> blogQueryWrapper = new LambdaQueryWrapper<>();
            blogQueryWrapper.eq(com.example.mdtoword.pojo.Blog::getCategoryId, category.getId());
            Long count = blogMapper.selectCount(blogQueryWrapper);
            categoryCountMap.put(category.getId(), count);
        }
        
        return categoryCountMap;
    }
} 