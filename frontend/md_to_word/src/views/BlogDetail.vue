<template>
  <LayoutBase>
    <div class="main-content">
      <div class="blog-layout">
                <!-- 目录栏（常驻，公共组件） -->
        <OverviewSidebar
          :active-id="route.params.id"
          :remember-scroll="true"
          storage-key="overviewScrollTop"
          @select="handleOverviewSelect"
        />

        <div class="blog-detail-container">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        
        <div v-else-if="blog" class="blog-content">
          <!-- 返回按钮 -->
          <div class="back-button">
            <el-button @click="goBack" type="text" size="large">
              <el-icon><ArrowLeft /></el-icon>
              返回博客列表
            </el-button>
          </div>
          
          <!-- 博客头部信息 -->
          <div class="blog-header">
            <div class="title-section">
              <h1 class="blog-title">{{ blog.title }}</h1>
              <!-- 状态标识 -->
              <el-tag 
                v-if="blog.status === 'draft'" 
                type="warning" 
                size="large"
                class="status-tag"
              >
                草稿
              </el-tag>
            </div>
            <div class="blog-meta">
              <div class="meta-left">
                <span class="blog-category" v-if="getCategoryName(blog.categoryId)">
                  {{ getCategoryName(blog.categoryId) }}
                </span>
                <span class="blog-date">{{ formatDate(blog.createTime) }}</span>
                <span class="blog-author">
                  <el-avatar :size="20" icon="UserFilled" />
                  作者 {{ author ? author.nickname : blog.authorId }}
                </span>
              </div>
              
              <!-- 作者操作按钮 -->
              <div class="meta-right" v-if="isAuthor">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="editBlog"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small"
                  @click="deleteBlog"
                >
                  删除
                </el-button>
                <el-button 
                  v-if="blog.status === 'draft'"
                  type="success" 
                  size="small"
                  @click="publishBlog"
                >
                  发布
                </el-button>
                <el-button 
                  v-else
                  type="warning" 
                  size="small"
                  @click="unpublishBlog"
                >
                  撤回
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 博客内容 -->
          <div class="blog-body">
            <div class="content-wrapper">
              <!-- 富文本渲染 -->
              <div class="post-content" v-html="blog.contentHtml"></div>
            </div>
          </div>
          
          <!-- 博客底部 -->
          <div class="blog-footer">
            <div class="blog-tags" v-if="blog.tags && blog.tags.length">
              <span class="tag-label">标签：</span>
              <el-tag 
                v-for="tag in blog.tags" 
                :key="tag"
                size="small"
                style="margin-right: 8px;"
              >
                {{ tag }}
              </el-tag>
            </div>
            
            <div class="blog-actions">
              <el-button type="primary" @click="shareBlog">
                <el-icon><Share /></el-icon>
                分享
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 博客不存在 -->
        <div v-else class="not-found">
          <el-empty description="博客不存在或已被删除">
            <el-button type="primary" @click="goBack">返回博客列表</el-button>
          </el-empty>
        </div>
        </div>
      </div>
    </div>
  </LayoutBase>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Share } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import OverviewSidebar from '@/components/OverviewSidebar.vue'
import { useAuth } from '@/composables/useAuth'
import { blogApi } from '@/utils/blogApi'
import { formatDate } from '@/utils/blogUtils'
import { useAuthorCache } from '@/composables/useAuthorCache'
import service from '@/utils/request'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, getCurrentUserId } = useAuth()
const { ensureAuthors, getAuthorName } = useAuthorCache()

// 响应式数据
const loading = ref(true)
const blog = ref(null)
const categories = ref([])
const author = ref(null)
// 目录功能已移至 OverviewSidebar 组件

// 计算属性
const isAuthor = computed(() => {
  if (!blog.value || !isLoggedIn()) return false
  return getCurrentUserId() === blog.value.authorId
})

// 富文本模式：直接使用 contentHtml 展示，无需 markdown 解析

// 监听路由参数变化，实现同组件内的文章切换
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      console.log('BlogDetail: 路由参数变化，从', oldId, '到', newId)
      loadBlogDetail()
    }
  }
)

// 生命周期
onMounted(() => {
  loadCategories()
  loadBlogDetail()
  // 目录由 OverviewSidebar 组件自行加载
})

// 方法定义
const loadCategories = async () => {
  try {
    const response = await blogApi.getCategoryList()
    if (response.data.success) {
      categories.value = response.data.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 目录相关逻辑已移至 OverviewSidebar 组件

const loadBlogDetail = async () => {
  const blogId = route.params.id
  console.log('BlogDetail: 开始加载博客详情，ID:', blogId)
  
  if (!blogId) {
    ElMessage.error('博客ID无效')
    return
  }
  
  loading.value = true
  try {
    const response = await blogApi.getBlogDetail(blogId)
    if (response.data.success) {
      blog.value = response.data.data
      console.log('BlogDetail: 博客详情加载成功:', blog.value.title)
      // 加载作者信息
      await ensureAuthors([blog.value.authorId])
    } else {
      console.error('BlogDetail: 博客不存在，响应:', response.data)
      ElMessage.error('博客不存在')
    }
  } catch (error) {
    console.error('加载博客详情失败:', error)
    ElMessage.error('加载博客详情失败')
  } finally {
    loading.value = false
  }
}

const loadAuthorInfo = async () => {}

const goBack = () => {
  router.push('/blog')
}

const handleOverviewSelect = (id) => {
  console.log('BlogDetail: 收到目录选择事件', id)
  // 目录点击处理，跳转到对应详情页
  router.push(`/blog/${id}`)
}

const editBlog = () => {
  router.push(`/blog/edit/${blog.value.id}`)
}

const deleteBlog = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇博客吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await service.delete(`/api/blog/${blog.value.id}`)
    if (response.data.success) {
      ElMessage.success('博客删除成功')
      goBack()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除博客失败:', error)
      ElMessage.error('删除博客失败')
    }
  }
}

const publishBlog = async () => {
  try {
    const response = await service.patch(`/api/blog/${blog.value.id}/status?status=published`)
    if (response.data.success) {
      ElMessage.success('博客发布成功')
      loadBlogDetail() // 重新加载博客信息
    }
  } catch (error) {
    console.error('发布博客失败:', error)
    ElMessage.error('发布博客失败')
  }
}

const unpublishBlog = async () => {
  try {
    const response = await service.patch(`/api/blog/${blog.value.id}/status?status=draft`)
    if (response.data.success) {
      ElMessage.success('博客已撤回为草稿')
      loadBlogDetail() // 重新加载博客信息
    }
  } catch (error) {
    console.error('撤回博客失败:', error)
    ElMessage.error('撤回博客失败')
  }
}

const shareBlog = () => {
  // 复制当前页面URL到剪贴板
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const getCategoryName = (categoryId) => {
  if (!categoryId) return ''
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : ''
}

// 统一使用 blogUtils.formatDate
</script>

<style scoped>
.main-content {
  padding-top: 20px; /* 与博客列表页对齐 */
}

.blog-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr); /* 与博客列表页完全一致 */
  gap: 20px;
  align-items: start;
}

/* 目录样式已移至 OverviewSidebar 组件 */

.blog-detail-container {
  width: 100%;
  max-width: none; /* 移除最大宽度限制，让内容充分利用空间 */
  margin: 0;
  padding: 0 20px;
}

.loading-container {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 30px;
  backdrop-filter: blur(10px);
}

.back-button {
  margin-bottom: 20px;
}

.blog-content {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  max-width: 1000px; /* 限制正文最大宽度以保持可读性 */
  margin: 0;
}

.blog-header {
  margin-bottom: 40px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.blog-title {
  font-size: 2.2rem;
  font-weight: 700;
  color: #111827; /* gray-900 */
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.status-tag {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 500;
  border-radius: 8px;
  padding: 8px 15px;
  font-size: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.blog-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #6b7280; /* gray-500 */
  font-size: 0.95rem;
}

.blog-category {
  background: #f3f4f6; /* gray-100 */
  color: #374151; /* gray-700 */
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  border: 1px solid #e5e7eb; /* gray-200 */
}

.blog-date { color: #9ca3af; /* gray-400 */ }

.blog-author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-right {
  display: flex;
  gap: 10px;
}

.blog-body {
  margin-bottom: 40px;
}

.content-wrapper {
  line-height: 1.8;
  color: #374151; /* gray-700 */
  font-size: 1.05rem;
}

/* 富文本正文样式 - 适用于 contentHtml 渲染 */
.post-content {
  line-height: 1.8;
  color: #24292f; /* GitHub-like */
  font-size: 1.05rem;
}
.post-content img { max-width: 100%; height: auto; border-radius: 6px; margin: 12px 0; display: block; }
.post-content a { color: #2563eb; text-decoration: none; }
.post-content a:hover { text-decoration: underline; }
.post-content h1, .post-content h2, .post-content h3, .post-content h4, .post-content h5, .post-content h6 {
  color: #111827; margin-top: 1.6em; margin-bottom: 0.8em; font-weight: 700;
}
.post-content h1 { font-size: 1.8rem; border-bottom: 1px solid #e5e7eb; padding-bottom: 0.4em; }
.post-content h2 { font-size: 1.5rem; }
.post-content h3 { font-size: 1.25rem; }
.post-content p { margin: 0 0 1em 0; }
.post-content ul, .post-content ol { margin: 0 0 1em 1.25em; }
.post-content li { margin: 0.25em 0; }
.post-content blockquote { border-left: 4px solid #e5e7eb; padding-left: 1em; margin: 1em 0; color: #4b5563; }
.post-content code { background: #f3f4f6; padding: 2px 6px; border-radius: 4px; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; font-size: 0.9rem; }
.post-content pre { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; border: 1px solid #e5e7eb; }
.post-content pre code { background: transparent; padding: 0; color: #111827; }
.post-content table { width: 100%; border-collapse: collapse; margin: 1em 0; }
.post-content th, .post-content td { border: 1px solid #e5e7eb; padding: 10px; }

/* Markdown 样式已移除，现在使用富文本 contentHtml */

.blog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 30px;
  border-top: 1px solid #e5e7eb;
}

.blog-tags {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag-label { color: #6b7280; font-size: 0.9rem; }

.blog-actions {
  display: flex;
  gap: 10px;
}

.not-found {
  text-align: center;
  padding: 60px 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .blog-detail-container {
    padding: 0 15px;
  }
  
  .blog-content {
    padding: 20px;
  }
  
  .blog-title {
    font-size: 1.8rem;
  }
  
  .blog-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .meta-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .blog-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style> 