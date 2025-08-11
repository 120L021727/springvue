<template>
  <LayoutBase>
    <div class="main-content">
      <div class="blog-layout">
        <aside class="overview-sidebar glass-effect" v-loading="overviewLoading">
          <div class="overview-header">目录</div>
          <el-scrollbar class="overview-scroll">
            <ul class="overview-list">
              <li v-for="item in overview" :key="item.id" class="overview-item" @click="viewBlog(item.id)">
                <el-icon class="overview-icon"><Document /></el-icon>
                <span class="title" :title="item.title">{{ item.title }}</span>
              </li>
            </ul>
          </el-scrollbar>
        </aside>
        <div class="blog-container">

        <!-- 筛选和搜索区域 -->
        <div class="filter-section">
          <div class="filter-left">
            <!-- 状态筛选 -->
            <el-select 
              v-model="filters.status" 
              placeholder="文章状态" 
              clearable
              @change="handleFilterChange"
              style="width: 120px; margin-right: 15px;"
            >
              <el-option label="全部文章" value="" />
              <el-option label="已发布" value="published" />
              <el-option label="草稿" value="draft" />
            </el-select>

            <!-- 分类筛选 -->
            <el-select 
              v-model="filters.categoryId" 
              placeholder="选择分类" 
              clearable
              @change="handleFilterChange"
              style="width: 200px; margin-right: 15px;"
            >
              <el-option label="全部分类" value="" />
              <el-option 
                v-for="category in categories" 
                :key="category.id" 
                :label="category.name" 
                :value="category.id"
              />
            </el-select>
          </div>

          <div class="filter-right">
            <!-- 搜索框 -->
            <el-input
              v-model="filters.keyword"
              placeholder="搜索博客标题..."
              clearable
              style="width: 250px; margin-right: 15px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <!-- 查询按钮 -->
            <el-button 
              type="primary" 
              @click="loadBlogs"
              :loading="loading"
              style="margin-right: 15px;"
            >
              <el-icon><Search /></el-icon>
              查询
            </el-button>

            <!-- 重置按钮 -->
            <el-button 
              @click="resetFilters"
              style="margin-right: 15px;"
            >
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>

            <!-- 写博客按钮 -->
            <el-button 
              type="primary" 
              @click="goToCreateBlog"
              v-if="isLoggedIn()"
            >
              <el-icon><Edit /></el-icon>
              写博客
            </el-button>
          </div>
        </div>

        <!-- 博客列表 -->
        <div class="blog-list" v-loading="loading">
          <el-empty v-if="blogs.length === 0 && !loading" description="暂无博客文章" />
          
          <div v-else class="blog-grid">
            <el-card 
              v-for="blog in blogs" 
              :key="blog.id" 
              class="blog-card"
              shadow="hover"
              @click="viewBlog(blog.id)"
            >
              <div class="blog-card-content">
                <!-- 博客标题和状态 -->
                <div class="blog-header">
                  <div class="blog-title-section">
                    <h3 class="blog-title">{{ blog.title }}</h3>
                    <el-tag 
                      v-if="blog.status === 'draft'" 
                      type="warning" 
                      size="small"
                      class="status-tag"
                    >
                      草稿
                    </el-tag>
                  </div>
                  
                  <!-- 博客元信息 -->
                  <div class="blog-meta">
                    <span class="blog-category" v-if="getCategoryName(blog.categoryId, categories)">
                      {{ getCategoryName(blog.categoryId, categories) }}
                    </span>
                    <span class="blog-date">{{ formatDate(blog.createTime) }}</span>
                  </div>
                </div>
                
                <!-- 博客摘要 -->
                <div class="blog-excerpt">
                  {{ getExcerpt(blog.content) }}
                </div>
                
                <!-- 博客底部信息 -->
                <div class="blog-footer">
                  <div class="blog-author">
                    <el-avatar :size="24" icon="UserFilled" />
                    <span>{{ getAuthorName(blog.authorId) }}</span>
                  </div>
                  
                  <!-- 作者操作按钮 -->
                  <div class="blog-actions" v-if="isAuthor(blog.authorId)" @click.stop>
                    <!-- 草稿发布按钮 -->
                    <el-button 
                      v-if="blog.status === 'draft'"
                      type="success" 
                      size="small"
                      @click="publishDraft(blog.id)"
                    >
                      发布
                    </el-button>
                    
                    <!-- 编辑按钮 -->
                    <el-button 
                      type="primary" 
                      size="small"
                      @click="editBlog(blog.id)"
                    >
                      编辑
                    </el-button>
                    
                    <!-- 删除按钮 -->
                    <el-button 
                      type="danger" 
                      size="small"
                      @click="deleteBlog(blog.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 分页组件 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.page"
            :page-size="pagination.size"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="handleCurrentChange"
          />
        </div>
        </div>
      </div>
    </div>
  </LayoutBase>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Edit, Document } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'
import { blogApi } from '@/utils/blogApi'
import { useAuthorCache } from '@/composables/useAuthorCache'
import { formatDate, getExcerpt, getCategoryName } from '@/utils/blogUtils'

const router = useRouter()
const { isLoggedIn, getCurrentUserId } = useAuth()

// 响应式数据
const loading = ref(false)
const blogs = ref([])
const categories = ref([])
const total = ref(0)
const overview = ref([])
const overviewLoading = ref(false)

// 作者信息缓存（复用组合式，跨页面共享）
const { ensureAuthors, getAuthorName } = useAuthorCache()

// 筛选条件
const filters = reactive({
  status: '',
  categoryId: '',
  keyword: ''
})

// 分页配置（固定每页4条，不允许修改）
const pagination = reactive({
  page: 1,
  size: 4
})

// 加载博客列表
const loadBlogs = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filters
    }
    
    const response = await blogApi.getBlogList(params)
    
    if (response.data.success) {
      blogs.value = response.data.data.records
      total.value = response.data.data.total
      
      // 加载作者信息
      await ensureAuthors(blogs.value.map(b => b.authorId))
    }
  } catch (error) {
    console.error('加载博客列表失败:', error)
    ElMessage.error('加载博客列表失败')
  } finally {
    loading.value = false
  }
}

// 加载目录（该用户可见的全部文章标题）
const loadOverview = async () => {
  try {
    overviewLoading.value = true
    const response = await blogApi.getBlogList({ page: 1, size: 1000, status: filters.status, categoryId: filters.categoryId, keyword: filters.keyword })
    if (response.data.success) {
      overview.value = (response.data.data.records || []).map(b => ({ id: b.id, title: b.title }))
    }
  } catch (e) {
    console.error('加载目录失败:', e)
  } finally {
    overviewLoading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await blogApi.getCategoryList()
    if (response.data.success) {
      categories.value = response.data.data
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 作者名称由组合式提供

// 筛选条件变化处理
const handleFilterChange = () => {
  pagination.page = 1
  loadBlogs()
  loadOverview()
}

// 重置筛选条件
const resetFilters = () => {
  Object.assign(filters, {
    status: '',
    categoryId: '',
    keyword: ''
  })
  pagination.page = 1
  loadBlogs()
  loadOverview()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadBlogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadBlogs()
}

// 查看博客详情
const viewBlog = (blogId) => {
  router.push(`/blog/${blogId}`)
}

// 编辑博客
const editBlog = (blogId) => {
  router.push(`/blog/edit/${blogId}`)
}

// 发布草稿
const publishDraft = async (blogId) => {
  try {
    await ElMessageBox.confirm('确定要发布这篇草稿吗？', '确认发布', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await blogApi.updateBlogStatus(blogId, 'published')
    
    if (response.data.success) {
      ElMessage.success('发布成功')
      loadBlogs()
      loadOverview()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 删除博客
const deleteBlog = async (blogId) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇博客吗？此操作不可恢复！', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await blogApi.deleteBlog(blogId)
    
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadBlogs()
      loadOverview()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 跳转到创建博客页面
const goToCreateBlog = () => {
  router.push('/blog/create')
}

// 检查是否为作者
const isAuthor = (authorId) => {
  return isLoggedIn() && getCurrentUserId() === authorId
}

// 页面初始化
onMounted(() => {
  loadCategories()
  loadBlogs()
  loadOverview()
})
</script>

<style scoped>
.blog-container {
  /* 让内容列在网格中占满可用宽度，避免被强制居中变窄 */
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 0 20px;
  position: relative;
  /* 为底部分页预留空间 */
  padding-bottom: 80px;
}

/* 上下空间更紧凑，让整体靠上 */
.main-content {
  padding-top: 20px; /* 减少顶部间距，让内容更靠上 */
}

.blog-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
}

.overview-sidebar {
  padding: 12px;
  border-radius: 10px;
  position: sticky;
  top: 80px; /* 调整与新的padding-top对齐 */
  /* 高度撑满可视区，给滚动容器留足空间 */
  max-height: calc(100vh - 120px); /* 调整高度适应新布局 */
  /* 弱化边框与阴影，使侧栏更融入背景 */
  border: none !important;
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(8px);
  /* 仅保留底部分隔线，避免突兀的边框 */
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.overview-header {
  color: #ffffff;
  font-weight: 600;
  margin-bottom: 10px;
}

.overview-scroll {
  height: calc(100vh - 140px);
}

.overview-list { list-style: none; padding: 0; margin: 0; }

.overview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
}

.overview-item:hover { background: rgba(255, 255, 255, 0.15); }
.overview-icon { opacity: 0.9; }
.overview-item .title { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 去掉原标题区域 */

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(10px);
}

.filter-left, .filter-right {
  display: flex;
  align-items: center;
}

.blog-list {
  margin-bottom: 0;
  /* 移除固定高度和滚动，让内容自然展示，分页控制显示数量 */
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 固定2列布局，确保每页4个卡片能完整显示 */
  gap: 15px; /* 减小间距，节省空间 */
}

.blog-card {
  transition: all 0.3s ease;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.blog-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.15);
}

.blog-card-content {
  padding: 15px; /* 减少内边距，让卡片更紧凑 */
}

.blog-header {
  margin-bottom: 15px;
}

.blog-title-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.blog-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.4;
  flex: 1;
}

.status-tag {
  flex-shrink: 0;
}

.blog-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
}

.blog-category {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.blog-date {
  color: rgba(255, 255, 255, 0.6);
}

.blog-excerpt {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
  margin-bottom: 15px;
  font-size: 0.9rem;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 减少显示行数，让卡片更紧凑 */
  line-clamp: 2; /* 标准属性，兼容性 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.blog-author {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.blog-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 300px;
}

/* 美化分页组件内部样式 */
.pagination-container :deep(.el-pagination) {
  --el-pagination-font-size: 14px;
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: rgba(255, 255, 255, 0.9);
  --el-pagination-border-radius: 6px;
  --el-pagination-button-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-button-disabled-color: rgba(255, 255, 255, 0.4);
  --el-pagination-button-disabled-bg-color: transparent;
  --el-pagination-hover-color: #ffffff;
  --el-pagination-hover-bg-color: rgba(255, 255, 255, 0.2);
}

.pagination-container :deep(.el-pagination .btn-prev),
.pagination-container :deep(.el-pagination .btn-next) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pagination .btn-prev:hover),
.pagination-container :deep(.el-pagination .btn-next:hover) {
  background: rgba(255, 255, 255, 0.25);
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.pagination-container :deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pagination .el-pager li:hover) {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  transform: translateY(-1px);
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background: rgba(255, 255, 255, 0.3);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.4);
  font-weight: 600;
}

.pagination-container :deep(.el-pagination .el-pagination__total) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.pagination-container :deep(.el-pagination .el-pagination__jump) {
  color: rgba(255, 255, 255, 0.8);
}

.pagination-container :deep(.el-pagination .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
}

.pagination-container :deep(.el-pagination .el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
}

@media (max-width: 768px) {
  .blog-layout { grid-template-columns: 1fr; }
  .overview-sidebar { display: none; }
  .filter-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-left, .filter-right {
    width: 100%;
    justify-content: center;
  }
  
  .blog-grid {
    grid-template-columns: 1fr;
  }
  
  .blog-card-content {
    padding: 15px;
  }
  
  .blog-title {
    font-size: 1.1rem;
  }
  
  .blog-actions {
    flex-direction: column;
    gap: 5px;
  }
  
  /* 移动端分页样式优化 */
  .pagination-container {
    bottom: 10px;
    left: 10px;
    right: 10px;
    transform: none;
    min-width: auto;
    padding: 10px 15px;
    border-radius: 20px;
  }
  
  .pagination-container :deep(.el-pagination) {
    --el-pagination-font-size: 12px;
  }
  
  .pagination-container :deep(.el-pagination .btn-prev),
  .pagination-container :deep(.el-pagination .btn-next),
  .pagination-container :deep(.el-pagination .el-pager li) {
    min-width: 32px;
    height: 32px;
    line-height: 30px;
    font-size: 12px;
  }
}
</style>