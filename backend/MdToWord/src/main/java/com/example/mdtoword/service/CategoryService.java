package com.example.mdtoword.service;

import com.example.mdtoword.pojo.Category;
import java.util.List;
import java.util.Map;

/**
 * 博客分类服务接口
 * 提供分类管理的业务逻辑方法
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
public interface CategoryService {
    
    /**
     * 获取所有分类列表（按排序升序）
     * 
     * @return 分类列表
     */
    List<Category> listAll();
    
    /**
     * 根据ID获取分类详情
     * 
     * @param id 分类ID
     * @return 分类信息，如果不存在返回null
     */
    Category getById(Integer id);
    
    /**
     * 创建新分类
     * 
     * @param category 分类信息
     * @return 是否创建成功
     */
    boolean create(Category category);
    
    /**
     * 更新分类信息
     * 
     * @param category 分类信息
     * @return 是否更新成功
     */
    boolean update(Category category);
    
    /**
     * 删除分类
     * 如果分类下有文章，则不允许删除
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean delete(Integer id);
    
    /**
     * 检查分类名称是否已存在
     * 
     * @param name 分类名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查分类名称是否已存在（排除指定ID）
     * 
     * @param name 分类名称
     * @param excludeId 排除的分类ID
     * @return 是否存在
     */
    boolean existsByName(String name, Integer excludeId);
    
    /**
     * 获取分类文章统计
     * 
     * @return Map<分类ID, 文章数量>
     */
    Map<Integer, Long> getCategoryArticleCount();
} 