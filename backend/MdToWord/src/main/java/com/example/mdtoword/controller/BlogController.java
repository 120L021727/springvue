package com.example.mdtoword.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mdtoword.exception.BusinessException;
import com.example.mdtoword.pojo.Blog;
import com.example.mdtoword.pojo.Result;
import com.example.mdtoword.pojo.User;
import com.example.mdtoword.service.BlogService;
import com.example.mdtoword.service.UserService;
import com.example.mdtoword.util.SecurityUtil;
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

/**
 * 博客文章控制器
 * 提供博客管理的RESTful API接口
 * 
 * @author 坤坤
 * @since 2025-08-09
 */
@RestController
@RequestMapping("/api/blog")
@Validated
public class BlogController {
    
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);
    
    @Autowired
    private BlogService blogService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    /**
     * 分页查询博客列表（公开接口）
     * 
     * 接口设计说明：
     * 1. 使用GET方法，符合RESTful规范
     * 2. 支持分页参数，默认值处理
     * 3. 支持状态筛选和分类筛选
     * 4. 返回统一的Result格式
     * 5. 参数验证确保数据有效性
     * 6. 权限过滤：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 
     * @param page 页码，默认1
     * @param size 每页大小，默认10，最大100
     * @param status 状态筛选，可选
     * @param categoryId 分类筛选，可选
     * @param keyword 关键词搜索，可选
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<Result<Page<Blog>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        
        logger.info("查询博客列表，页码: {}, 每页大小: {}, 状态: {}, 分类: {}, 关键词: {}", 
                   page, size, status, categoryId, keyword);
        
        // 限制每页最大数量，防止性能问题
        if (size > 100) {
            size = 100;
        }
        
        try {
            // 获取当前登录用户ID（可能为null表示未登录）
            Integer currentUserId = null;
            try {
                currentUserId = securityUtil.getCurrentUserId();
            } catch (BusinessException e) {
                // 用户未登录，currentUserId保持为null
                logger.debug("用户未登录，只能查看已发布的文章");
            }
            
            // 使用统一的查询方法，传入权限过滤参数
            Page<Blog> result = blogService.list(page, size, status, categoryId, keyword, null, currentUserId);
            
            logger.info("查询成功，总记录数: {}", result.getTotal());
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("查询博客列表失败", e);
            throw new BusinessException("查询博客列表失败");
        }
    }
    
    /**
     * 管理员查询所有博客（包括草稿）
     * 
     * 权限控制：
     * 1. 需要管理员权限（这里简化为登录用户）
     * 2. 可以查看所有状态的博客
     * 3. 支持按状态筛选
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 状态筛选，可选
     * @return 分页结果
     */
    @GetMapping("/admin/list")
    public ResponseEntity<Result<Page<Blog>>> adminList(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String status) {
        
        logger.info("管理员查询博客列表，页码: {}, 每页大小: {}, 状态: {}", page, size, status);
        
        if (size > 100) size = 100;
        
        // 管理员接口需要登录，传入当前用户ID进行权限过滤
        Integer currentUserId = securityUtil.getCurrentUserId();
        Page<Blog> result = blogService.list(page, size, status, null, null, null, currentUserId);
        
        return ResponseEntity.ok(Result.success(result));
    }
    
    /**
     * 按分类查询博客
     * 
     * 业务逻辑：
     * 1. 验证分类ID有效性
     * 2. 只返回已发布的博客
     * 3. 按创建时间降序排列
     * 4. 权限过滤：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Result<Page<Blog>>> listByCategory(
            @PathVariable @NotNull @Min(1) Integer categoryId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        
        logger.info("按分类查询博客，分类ID: {}, 页码: {}, 每页大小: {}", categoryId, page, size);
        
        if (size > 100) size = 100;
        
        try {
            // 获取当前登录用户ID（可能为null表示未登录）
            Integer currentUserId = null;
            try {
                currentUserId = securityUtil.getCurrentUserId();
            } catch (BusinessException e) {
                // 用户未登录，currentUserId保持为null
                logger.debug("用户未登录，只能查看已发布的文章");
            }
            
            Page<Blog> result = blogService.list(page, size, "published", categoryId, null, null, currentUserId);
            return ResponseEntity.ok(Result.success(result));
        } catch (BusinessException e) {
            logger.warn("按分类查询博客失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取博客详情
     * 
     * 业务逻辑：
     * 1. 验证博客存在性
     * 2. 权限验证：未登录只能看已发布，已登录可以看自己的所有文章+别人的已发布文章
     * 3. 返回完整的博客信息
     * 
     * @param id 博客ID
     * @return 博客详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Blog>> getById(@PathVariable @NotNull @Min(1) Integer id) {
        logger.info("获取博客详情，ID: {}", id);
        
        try {
            // 获取当前登录用户ID（可能为null表示未登录）
            Integer currentUserId = null;
            try {
                currentUserId = securityUtil.getCurrentUserId();
            } catch (BusinessException e) {
                // 用户未登录，currentUserId保持为null
                logger.debug("用户未登录，只能查看已发布的文章");
            }
            
            Blog blog = blogService.getById(id, currentUserId);
            if (blog == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Result.success(blog));
        } catch (Exception e) {
            logger.error("获取博客详情失败，ID: {}", id, e);
            throw new BusinessException("获取博客详情失败");
        }
    }
    
    /**
     * 创建新博客
     * 
     * 权限控制：
     * 1. 需要登录用户
     * 2. 自动设置当前用户为作者
     * 3. 使用JSR303验证参数
     * 
     * @param blog 博客信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Result<String>> create(@Valid @RequestBody Blog blog) {
        logger.info("创建博客，标题: {}", blog.getTitle());
        
        try {
            // 获取当前登录用户ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            blog.setAuthorId(currentUserId);
            
            boolean success = blogService.create(blog);
            if (success) {
                logger.info("博客创建成功，标题: {}", blog.getTitle());
                return ResponseEntity.ok(Result.success("博客创建成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "博客创建失败"));
            }
        } catch (BusinessException e) {
            logger.warn("创建博客失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新博客
     * 
     * 权限控制：
     * 1. 只有作者可以更新自己的博客
     * 2. 验证博客存在性
     * 3. 保持原有的作者ID不变
     * 
     * @param id 博客ID
     * @param blog 博客信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> update(
            @PathVariable @NotNull @Min(1) Integer id,
            @Valid @RequestBody Blog blog) {
        
        logger.info("更新博客，ID: {}, 标题: {}", id, blog.getTitle());
        
        try {
            // 权限检查：只有作者可以更新
            Integer currentUserId = securityUtil.getCurrentUserId();
            if (!blogService.isAuthor(id, currentUserId)) {
                return ResponseEntity.status(403)
                    .body(Result.forbidden("您没有权限更新此博客"));
            }
            
            // 设置ID和作者ID
            blog.setId(id);
            blog.setAuthorId(currentUserId);
            
            boolean success = blogService.update(blog);
            if (success) {
                logger.info("博客更新成功，ID: {}", id);
                return ResponseEntity.ok(Result.success("博客更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "博客更新失败"));
            }
        } catch (BusinessException e) {
            logger.warn("更新博客失败，ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 删除博客
     * 
     * 权限控制：
     * 1. 只有作者可以删除自己的博客
     * 2. 软删除或硬删除（这里是硬删除）
     * 
     * @param id 博客ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable @NotNull @Min(1) Integer id) {
        logger.info("删除博客，ID: {}", id);
        
        try {
            // 权限检查：只有作者可以删除
            Integer currentUserId = securityUtil.getCurrentUserId();
            if (!blogService.isAuthor(id, currentUserId)) {
                return ResponseEntity.status(403)
                    .body(Result.forbidden("您没有权限删除此博客"));
            }
            
            boolean success = blogService.delete(id);
            if (success) {
                logger.info("博客删除成功，ID: {}", id);
                return ResponseEntity.ok(Result.success("博客删除成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "博客删除失败"));
            }
        } catch (BusinessException e) {
            logger.warn("删除博客失败，ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
    
    /**
     * 更新博客状态
     * 
     * 业务场景：
     * 1. 发布草稿：draft -> published
     * 2. 撤回发布：published -> draft
     * 
     * @param id 博客ID
     * @param status 新状态
     * @return 更新结果
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Result<String>> updateStatus(
            @PathVariable @NotNull @Min(1) Integer id,
            @RequestParam @NotNull String status) {
        
        logger.info("更新博客状态，ID: {}, 新状态: {}", id, status);
        
        try {
            // 权限检查
            Integer currentUserId = securityUtil.getCurrentUserId();
            if (!blogService.isAuthor(id, currentUserId)) {
                return ResponseEntity.status(403)
                    .body(Result.forbidden("您没有权限修改此博客状态"));
            }
            
            boolean success = blogService.updateStatus(id, status);
            if (success) {
                logger.info("博客状态更新成功，ID: {}, 新状态: {}", id, status);
                return ResponseEntity.ok(Result.success("博客状态更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Result.error(400, "博客状态更新失败"));
            }
        } catch (BusinessException e) {
            logger.warn("更新博客状态失败，ID: {}, 错误: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
}