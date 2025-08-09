<template>
  <LayoutBase>
    <div class="main-content">
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
              <!-- 使用Markdown渲染 -->
              <div class="markdown-content" v-html="renderedContent"></div>
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
  </LayoutBase>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Share } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'
import { blogApi } from '@/utils/blogApi'
import { formatDate } from '@/utils/blogUtils'
import { useAuthorCache } from '@/composables/useAuthorCache'
import service from '@/utils/request'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, getCurrentUserId } = useAuth()
const { ensureAuthors, getAuthorName } = useAuthorCache()

// 响应式数据
const loading = ref(true)
const blog = ref(null)
const categories = ref([])
const author = ref(null)

// 计算属性
const isAuthor = computed(() => {
  if (!blog.value || !isLoggedIn()) return false
  return getCurrentUserId() === blog.value.authorId
})

const renderedContent = computed(() => {
  if (!blog.value || !blog.value.content) return ''
  return marked(blog.value.content)
})

// 生命周期
onMounted(() => {
  loadCategories()
  loadBlogDetail()
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

const loadBlogDetail = async () => {
  const blogId = route.params.id
  if (!blogId) {
    ElMessage.error('博客ID无效')
    return
  }
  
  loading.value = true
  try {
    const response = await blogApi.getBlogDetail(blogId)
    if (response.data.success) {
      blog.value = response.data.data
      // 加载作者信息
      await ensureAuthors([blog.value.authorId])
    } else {
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
.blog-detail-container {
  max-width: 800px;
  margin: 0 auto;
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
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin: 0 0 20px 0;
  line-height: 1.3;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
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
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.blog-category {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
}

.blog-date {
  color: rgba(255, 255, 255, 0.8);
}

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
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
}

.markdown-content {
  /* Markdown样式 */
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  color: white;
  margin-top: 30px;
  margin-bottom: 15px;
  font-weight: 600;
}

.markdown-content h1 {
  font-size: 1.8rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 10px;
}

.markdown-content h2 {
  font-size: 1.5rem;
}

.markdown-content h3 {
  font-size: 1.3rem;
}

.markdown-content p {
  margin-bottom: 15px;
  line-height: 1.8;
}

.markdown-content ul,
.markdown-content ol {
  margin-bottom: 15px;
  padding-left: 20px;
}

.markdown-content li {
  margin-bottom: 8px;
}

.markdown-content blockquote {
  border-left: 4px solid rgba(255, 255, 255, 0.3);
  padding-left: 20px;
  margin: 20px 0;
  font-style: italic;
  color: rgba(255, 255, 255, 0.8);
}

.markdown-content code {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}

.markdown-content pre {
  background: rgba(0, 0, 0, 0.3);
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
}

.markdown-content pre code {
  background: none;
  padding: 0;
  color: rgba(255, 255, 255, 0.9);
}

.markdown-content a {
  color: #4CAF50;
  text-decoration: none;
}

.markdown-content a:hover {
  text-decoration: underline;
}

.markdown-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
}

.markdown-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.markdown-content th,
.markdown-content td {
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 12px;
  text-align: left;
}

.markdown-content th {
  background: rgba(255, 255, 255, 0.1);
  font-weight: 600;
}

.blog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.blog-tags {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tag-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

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