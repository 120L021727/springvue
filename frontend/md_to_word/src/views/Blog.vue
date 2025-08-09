<template>
  <LayoutBase>
    <div class="main-content">
      <div class="blog-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">坤坤的博客</h1>
          <p class="page-subtitle">分享技术心得，记录学习历程</p>
        </div>

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
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </LayoutBase>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Edit } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'
import { blogApi } from '@/utils/blogApi'
import { formatDate, getExcerpt, getCategoryName } from '@/utils/blogUtils'

const router = useRouter()
const { isLoggedIn, getCurrentUserId } = useAuth()

// 响应式数据
const loading = ref(false)
const blogs = ref([])
const categories = ref([])
const total = ref(0)

// 作者信息缓存
const authorCache = ref(new Map())

// 筛选条件
const filters = reactive({
  status: '',
  categoryId: '',
  keyword: ''
})

// 分页配置
const pagination = reactive({
  page: 1,
  size: 10
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
      await loadAuthorsInfo()
    }
  } catch (error) {
    console.error('加载博客列表失败:', error)
    ElMessage.error('加载博客列表失败')
  } finally {
    loading.value = false
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

// 加载作者信息（按需加载并缓存）
const loadAuthorsInfo = async () => {
  // 获取所有不重复的作者ID
  const authorIds = [...new Set(blogs.value.map(blog => blog.authorId))]
  
  // 只加载还没有缓存的作者信息
  const uncachedIds = authorIds.filter(id => !authorCache.value.has(id))
  
  for (const authorId of uncachedIds) {
    try {
      const response = await blogApi.getUserInfo(authorId)
      if (response.data.success) {
        authorCache.value.set(authorId, response.data.data)
      }
    } catch (error) {
      console.warn(`用户ID ${authorId} 不存在或无法访问`)
      // 设置默认值，避免重复请求
      authorCache.value.set(authorId, { id: authorId, nickname: `用户${authorId}` })
    }
  }
}

// 获取作者名称
const getAuthorName = (authorId) => {
  if (!authorId) return ''
  const author = authorCache.value.get(authorId)
  return author ? author.nickname : `用户${authorId}`
}

// 筛选条件变化处理
const handleFilterChange = () => {
  pagination.page = 1
  loadBlogs()
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
})
</script>

<style scoped>
.blog-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

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
  margin-bottom: 40px;
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
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
  padding: 20px;
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
  line-height: 1.6;
  margin-bottom: 20px;
  font-size: 0.95rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
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
  margin-top: 40px;
}

@media (max-width: 768px) {
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
}
</style>