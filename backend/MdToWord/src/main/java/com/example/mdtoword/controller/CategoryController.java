package com.example.mdtoword.controller;

import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Category;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 博客分类控制器
 * 提供分类管理的RESTful API接口
 * 
 * @author 坤坤
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/category")
@Validated
public class CategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有分类列表（公开接口）
     * 
     * 接口特点：
     * 1. 公开接口，无需认证
     * 2. 按排序字段升序返回
     * 3. 包含分类的基本信息
     * 4. 适用于前端下拉框、导航菜单等场景
     * 
     * @return 分类列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<List<Category>>> list() {
        logger.info("获取所有分类列表");
        
        try {
            List<Category> categories = categoryService.listAll();
            logger.info("获取分类列表成功，数量: {}", categories.size());
            return ResponseEntity.ok(Result.success(categories));
        } catch (Exception e) {
            logger.error("获取分类列表失败", e);
            throw new BusinessException("获取分类列表失败");
        }
    }
    
    /**
     * 获取分类统计信息
     * 
     * 业务价值：
     * 1. 显示每个分类下的文章数量
     * 2. 用于分类管理页面
     * 3. 帮助用户了解内容分布
     * 
     * @return 分类统计Map<分类ID, 文章数量>
     */
    @GetMapping("/statistics")
    public ResponseEntity<Result<Map<Integer, Long>>> getStatistics() {
        logger.info("获取分类统计信息");
        
        try {
            Map<Integer, Long> statistics = categoryService.getCategoryArticleCount();
            logger.info("获取分类统计成功，分类数量: {}", statistics.size());
            return ResponseEntity.ok(Result.success(statistics));
        } catch (Exception e) {
            logger.error("获取分类统计失败", e);
            throw new BusinessException("获取分类统计失败");
        }
    }
    
    /**
     * 根据ID获取分类详情
     * 
     * 使用场景：
     * 1. 分类详情页面
     * 2. 编辑分类时回显数据
     * 3. API调用验证分类存在性
     * 
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Category>> getById(@PathVariable @NotNull @Min(1) Integer id) {
        logger.info("获取分类详情，ID: {}", id);
        
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                logger.warn("分类不存在，ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            logger.info("获取分类详情成功，ID: {}, 名称: {}", id, category.getName());
            return ResponseEntity.ok(Result.success(category));
        } catch (Exception e) {
            logger.error("获取分类详情失败，ID: {}", id, e);
            throw new BusinessException("获取分类详情失败");
        }
    }
    
    /**
     * 创建新分类（管理员接口）
     * 
     * 业务逻辑：
     * 1. 验证分类名称唯一性
     * 2. 自动设置排序值（如果未指定）
     * 3. 参数验证使用JSR303注解
     * 4. 事务保证数据一致性
     * 
     * 权限控制：
     * - 需要管理员权限（这里简化为登录用户）
     * 
     * @param category 分类信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Result<String>> create(@Valid @RequestBody Category category) {
        logger.info("创建分类，名称: {}", category.getName());
        
        try {
            // 这里可以添加管理员权限检查
            // if (!isAdmin()) {
            //     return ResponseEntity.status(403).body(Result.forbidden("需要管理员权限"));
            // }
            
            boolean success = categoryService.create(category);
            if (success) {
                logger.info("分类创建成功，名称: {}", category.getName());
                return ResponseEntity.ok(Result.success("分类创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "分类创建失败"));
            }
        } catch (BusinessException e) {
            logger.warn("创建分类失败，名称: {}, 错误: {}", category.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新分类信息（管理员接口）
     * 
     * 业务逻辑：
     * 1. 验证分类存在性
     * 2. 验证名称唯一性（排除当前分类）
     * 3. 支持部分字段更新
     * 4. 保持数据完整性
     * 
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> update(
            @PathVariable @NotNull @Min(1) Integer id,
            @Valid @RequestBody Category category) {
        
        logger.info("更新分类，ID: {}, 名称: {}", id, category.getName());
        
        try {
            // 设置分类ID
            category.setId(id);
            
            boolean success = categoryService.update(category);
            if (success) {
                logger.info("分类更新成功，ID: {}, 名称: {}", id, category.getName());
                return ResponseEntity.ok(Result.success("分类更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "分类更新失败"));
            }
        } catch (BusinessException e) {
            logger.warn("更新分类失败，ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除分类（管理员接口）
     * 
     * 业务规则：
     * 1. 验证分类存在性
     * 2. 检查分类下是否有文章
     * 3. 有文章的分类不能删除
     * 4. 删除操作不可逆
     * 
     * 安全考虑：
     * 1. 需要管理员权限
     * 2. 可以考虑软删除
     * 3. 记录删除日志
     * 
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable @NotNull @Min(1) Integer id) {
        logger.info("删除分类，ID: {}", id);
        
        try {
            boolean success = categoryService.delete(id);
            if (success) {
                logger.info("分类删除成功，ID: {}", id);
                return ResponseEntity.ok(Result.success("分类删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "分类删除失败"));
            }
        } catch (BusinessException e) {
            logger.warn("删除分类失败，ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 检查分类名称是否可用
     * 
     * 使用场景：
     * 1. 创建分类时的实时验证
     * 2. 编辑分类时的名称检查
     * 3. 前端表单验证
     * 
     * @param name 分类名称
     * @param excludeId 排除的分类ID（编辑时使用）
     * @return 检查结果
     */
    @GetMapping("/check-name")
    public ResponseEntity<Result<Boolean>> checkNameAvailable(
            @RequestParam String name,
            @RequestParam(required = false) Integer excludeId) {
        
        logger.info("检查分类名称是否可用，名称: {}, 排除ID: {}", name, excludeId);
        
        try {
            boolean available;
            if (excludeId != null) {
                // 编辑时检查（排除当前分类）
                available = !categoryService.existsByName(name, excludeId);
            } else {
                // 新建时检查
                available = !categoryService.existsByName(name);
            }
            
            logger.info("分类名称检查结果，名称: {}, 可用: {}", name, available);
            return ResponseEntity.ok(Result.success(available));
        } catch (Exception e) {
            logger.error("检查分类名称失败，名称: {}", name, e);
            throw new BusinessException("检查分类名称失败");
        }
    }
}