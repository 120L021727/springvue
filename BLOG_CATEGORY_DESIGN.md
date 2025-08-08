# Blog和Category模块设计文档

## 1. 数据库设计

### 1.1 表结构

#### Category表 (tb_category)
```sql
CREATE TABLE `tb_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int DEFAULT 0 COMMENT '排序字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT '分类名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客分类表';
```

#### Blog表 (tb_blog)
```sql
CREATE TABLE `tb_blog` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '博客ID',
  `title` varchar(200) NOT NULL COMMENT '博客标题',
  `content` longtext COMMENT '博客内容（Markdown格式）',
  `summary` varchar(500) DEFAULT NULL COMMENT '博客摘要',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `author_id` int NOT NULL COMMENT '作者ID',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`) COMMENT '分类索引',
  KEY `idx_author_id` (`author_id`) COMMENT '作者索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  CONSTRAINT `fk_blog_category` FOREIGN KEY (`category_id`) REFERENCES `tb_category` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_blog_author` FOREIGN KEY (`author_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';
```

### 1.2 设计要点

1. **外键约束**：博客表通过外键关联分类表和用户表
2. **索引优化**：为常用查询字段创建索引
3. **状态管理**：支持草稿和已发布两种状态
4. **软删除**：通过状态字段实现软删除
5. **时间戳**：自动维护创建和更新时间

## 2. 实体类设计

### 2.1 Category实体类

```java
@Data
@TableName("tb_category")
public class Category {
    
    // 校验分组接口
    public interface CreateGroup {}
    public interface UpdateGroup {}
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @NotBlank(message = "分类名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    @Size(max = 200, message = "分类描述长度不能超过200个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String description;
    
    @Min(value = 0, message = "排序字段不能小于0", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer sortOrder;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 2.2 Blog实体类

```java
@Data
@TableName("tb_blog")
public class Blog {
    
    // 校验分组接口
    public interface CreateGroup {}
    public interface UpdateGroup {}
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @NotBlank(message = "博客标题不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 1, max = 200, message = "博客标题长度必须在1-200个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;
    
    @NotBlank(message = "博客内容不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String content;
    
    @Size(max = 500, message = "博客摘要长度不能超过500个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String summary;
    
    @TableField("category_id")
    private Integer categoryId;
    
    @TableField("author_id")
    private Integer authorId;
    
    private String status = "draft"; // 默认草稿状态
    
    @TableField("view_count")
    private Integer viewCount = 0;
    
    private String tags; // JSON格式存储标签
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

## 3. 数据访问层设计

### 3.1 Mapper接口

```java
// CategoryMapper.java
public interface CategoryMapper extends BaseMapper<Category> {
    // 继承BaseMapper，获得基础的CRUD功能
}

// BlogMapper.java
public interface BlogMapper extends BaseMapper<Blog> {
    // 继承BaseMapper，获得基础的CRUD功能
}
```

### 3.2 自定义查询方法

```java
// BlogMapper.java 扩展
public interface BlogMapper extends BaseMapper<Blog> {
    
    /**
     * 根据状态和分类查询博客列表
     */
    @Select("SELECT * FROM tb_blog WHERE status = #{status} AND category_id = #{categoryId} ORDER BY create_time DESC")
    List<Blog> selectByStatusAndCategory(@Param("status") String status, @Param("categoryId") Integer categoryId);
    
    /**
     * 增加浏览次数
     */
    @Update("UPDATE tb_blog SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Integer id);
}
```

## 4. 业务逻辑层设计

### 4.1 Service接口设计

```java
// CategoryService.java
public interface CategoryService {
    // 基础CRUD
    List<Category> list();
    Category getById(Integer id);
    boolean create(Category category);
    boolean update(Category category);
    boolean delete(Integer id);
    
    // 业务方法
    boolean existsByName(String name);
    List<Category> listBySortOrder();
}

// BlogService.java
public interface BlogService {
    // 基础CRUD
    Page<Blog> list(int page, int size);
    Blog getById(Integer id);
    boolean create(Blog blog);
    boolean update(Blog blog);
    boolean delete(Integer id);
    
    // 查询方法
    Page<Blog> listPublished(int page, int size);
    Page<Blog> listByCategory(Integer categoryId, int page, int size);
    Page<Blog> listByAuthor(Integer authorId, int page, int size);
    Page<Blog> listWithFilters(int page, int size, String status, Integer categoryId, String keyword);
    
    // 业务方法
    boolean updateStatus(Integer id, String status);
    boolean isAuthor(Integer blogId, Integer authorId);
    boolean exists(Integer id);
    List<Blog> listByStatus(String status);
}
```

### 4.2 Service实现类

```java
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name);
        return baseMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public List<Category> listBySortOrder() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSortOrder);
        return baseMapper.selectList(queryWrapper);
    }
}

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    
    @Override
    public Page<Blog> listWithFilters(int page, int size, String status, Integer categoryId, String keyword) {
        Page<Blog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(Blog::getStatus, status);
        }
        
        // 分类筛选
        if (categoryId != null) {
            queryWrapper.eq(Blog::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Blog::getTitle, keyword)
                       .or()
                       .like(Blog::getContent, keyword);
        }
        
        // 按创建时间降序排列
        queryWrapper.orderByDesc(Blog::getCreateTime);
        
        return baseMapper.selectPage(pageParam, queryWrapper);
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Integer id, String status) {
        Blog blog = baseMapper.selectById(id);
        if (blog == null) {
            throw new BusinessException("博客不存在");
        }
        
        if (!"draft".equals(status) && !"published".equals(status)) {
            throw new BusinessException("无效的状态值");
        }
        
        Blog updateBlog = new Blog();
        updateBlog.setId(id);
        updateBlog.setStatus(status);
        
        return baseMapper.updateById(updateBlog) > 0;
    }
    
    @Override
    public boolean isAuthor(Integer blogId, Integer authorId) {
        Blog blog = baseMapper.selectById(blogId);
        return blog != null && authorId.equals(blog.getAuthorId());
    }
}
```

## 5. 控制器层设计

### 5.1 RESTful API设计

```java
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    
    @GetMapping("/list")
    public ResponseEntity<Result<List<Category>>> list() {
        // 获取分类列表
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<Category>> getById(@PathVariable Integer id) {
        // 获取分类详情
    }
    
    @PostMapping
    public ResponseEntity<Result<String>> create(@Valid @RequestBody Category category) {
        // 创建分类
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> update(@PathVariable Integer id, @Valid @RequestBody Category category) {
        // 更新分类
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable Integer id) {
        // 删除分类
    }
}

@RestController
@RequestMapping("/api/blog")
public class BlogController {
    
    @GetMapping("/list")
    public ResponseEntity<Result<Page<Blog>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        // 分页查询博客列表
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Result<Blog>> getById(@PathVariable Integer id) {
        // 获取博客详情
    }
    
    @PostMapping
    public ResponseEntity<Result<String>> create(@Valid @RequestBody Blog blog) {
        // 创建博客
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> update(@PathVariable Integer id, @Valid @RequestBody Blog blog) {
        // 更新博客
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable Integer id) {
        // 删除博客
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Result<String>> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        // 更新博客状态
    }
}
```

## 6. 权限控制设计

### 6.1 权限策略

1. **分类管理**：所有登录用户都可以查看分类列表
2. **博客管理**：
   - 查看：所有人可以查看已发布的博客
   - 创建：登录用户可以创建博客
   - 编辑/删除：只有作者可以编辑/删除自己的博客
   - 状态管理：只有作者可以修改博客状态

### 6.2 权限检查实现

```java
private Integer getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        if (user != null) {
            return user.getId();
        }
    }
    throw new BusinessException("用户未登录");
}

// 权限检查示例
public ResponseEntity<Result<String>> update(@PathVariable Integer id, @Valid @RequestBody Blog blog) {
    Integer currentUserId = getCurrentUserId();
    if (!blogService.isAuthor(id, currentUserId)) {
        return ResponseEntity.status(403)
            .body(Result.forbidden("您没有权限更新此博客"));
    }
    // 执行更新操作
}
```

## 7. 异常处理设计

### 7.1 业务异常定义

```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

### 7.2 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<String>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Result.error(message));
    }
}
```

## 8. 前端设计

### 8.1 页面结构

```
/blog                    # 博客列表页
/blog/:id               # 博客详情页
/blog/create            # 创建博客页
/blog/edit/:id          # 编辑博客页
/category-management    # 分类管理页
```

### 8.2 组件设计

```vue
<!-- BlogList.vue -->
<template>
  <div class="blog-list">
    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-select v-model="selectedStatus" placeholder="文章状态">
        <el-option label="全部文章" value="" />
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
      </el-select>
      
      <el-select v-model="selectedCategory" placeholder="选择分类">
        <el-option label="全部分类" value="" />
        <el-option v-for="category in categories" :key="category.id" 
                   :label="category.name" :value="category.id" />
      </el-select>
      
      <el-input v-model="searchKeyword" placeholder="搜索博客标题..." />
      
      <el-button type="primary" @click="loadBlogs">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>
    
    <!-- 博客列表 -->
    <div class="blog-grid">
      <el-card v-for="blog in blogs" :key="blog.id" class="blog-card">
        <div class="blog-header">
          <h3 class="blog-title">{{ blog.title }}</h3>
          <el-tag v-if="blog.status === 'draft'" type="warning">草稿</el-tag>
        </div>
        
        <div class="blog-meta">
          <span class="blog-category">{{ getCategoryName(blog.categoryId) }}</span>
          <span class="blog-date">{{ formatDate(blog.createTime) }}</span>
          <span class="blog-author">{{ getAuthorName(blog.authorId) }}</span>
        </div>
        
        <div class="blog-excerpt">{{ getExcerpt(blog.content) }}</div>
        
        <div class="blog-actions" v-if="isAuthor(blog.authorId)">
          <el-button v-if="blog.status === 'draft'" type="success" 
                     @click="publishDraft(blog.id)">发布</el-button>
          <el-button type="primary" @click="editBlog(blog.id)">编辑</el-button>
          <el-button type="danger" @click="deleteBlog(blog.id)">删除</el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <el-pagination v-model:current-page="currentPage" 
                   v-model:page-size="pageSize"
                   :total="total"
                   layout="total, sizes, prev, pager, next, jumper" />
  </div>
</template>
```

### 8.3 状态管理

```javascript
// stores/blog.js
export const useBlogStore = defineStore('blog', () => {
  const blogs = ref([])
  const categories = ref([])
  const loading = ref(false)
  
  const loadBlogs = async (params) => {
    loading.value = true
    try {
      const response = await service.get('/api/blog/list', { params })
      blogs.value = response.data.data.records
    } catch (error) {
      console.error('加载博客失败:', error)
    } finally {
      loading.value = false
    }
  }
  
  const loadCategories = async () => {
    try {
      const response = await service.get('/api/category/list')
      categories.value = response.data.data
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }
  
  return {
    blogs,
    categories,
    loading,
    loadBlogs,
    loadCategories
  }
})
```

## 9. 优化建议

### 9.1 性能优化

1. **数据库优化**：
   - 为常用查询字段添加索引
   - 使用分页查询避免大量数据加载
   - 考虑使用缓存减少数据库查询

2. **前端优化**：
   - 使用虚拟滚动处理大量数据
   - 实现数据懒加载
   - 使用防抖处理搜索输入

### 9.2 功能扩展

1. **标签系统**：支持博客标签管理
2. **评论系统**：添加博客评论功能
3. **搜索优化**：实现全文搜索
4. **统计分析**：添加博客访问统计
5. **SEO优化**：添加meta标签和sitemap

### 9.3 代码优化

1. **统一异常处理**：完善全局异常处理机制
2. **参数验证**：加强输入参数验证
3. **日志记录**：添加详细的操作日志
4. **单元测试**：为关键业务逻辑添加测试用例

## 10. 总结

Blog和Category模块采用了分层架构设计，通过MyBatis-Plus简化了数据访问层，使用Spring Security实现权限控制，前端采用Vue3 + Element Plus构建用户界面。

关键设计要点：
1. **数据完整性**：通过外键约束保证数据一致性
2. **权限控制**：基于用户身份实现细粒度权限管理
3. **状态管理**：支持博客的草稿和发布状态
4. **用户体验**：提供直观的操作界面和反馈机制
5. **可扩展性**：模块化设计便于功能扩展

这个设计为博客系统提供了完整的功能框架，可以根据实际需求进行进一步的功能扩展和性能优化。 