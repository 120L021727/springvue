<template>
  <LayoutBase>
    <div class="main-content">
      <div class="blog-edit-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">{{ isEdit ? '编辑博客' : '创建博客' }}</h1>
          <p class="page-subtitle">{{ isEdit ? '修改您的博客内容' : '分享您的技术心得' }}</p>
        </div>

        <!-- 编辑表单 -->
        <div class="edit-form">
          <el-form 
            ref="formRef" 
            :model="blogForm" 
            :rules="formRules"
            label-width="100px"
            class="blog-form"
          >
            <!-- 标题 -->
            <el-form-item label="标题" prop="title">
              <el-input 
                v-model="blogForm.title" 
                placeholder="请输入博客标题"
                maxlength="200"
                show-word-limit
                size="large"
              />
            </el-form-item>

            <!-- 分类 -->
            <el-form-item label="分类" prop="categoryId">
              <div class="category-selector">
                <el-select 
                  v-model="blogForm.categoryId" 
                  placeholder="请选择分类"
                  clearable
                  filterable
                  style="width: 100%;"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id" 
                    :label="category.name" 
                    :value="category.id"
                  />
                </el-select>
                <el-button 
                  type="primary" 
                  @click="showCreateCategory = true"
                  size="default"
                  style="margin-left: 10px;"
                >
                  新建分类
                </el-button>
              </div>
            </el-form-item>

            <!-- 内容（富文本） -->
            <el-form-item label="内容" prop="contentHtml">
              <!-- 云端版本 -->
              <!-- <RichTextEditor v-model="blogForm.contentHtml" :minHeight="600" /> -->
              <!-- 本地化部署版本 -->
              <RichTextEditorLocal v-model="blogForm.contentHtml" :minHeight="600" />
            </el-form-item>

            <!-- 状态选择移除：由下方按钮决定提交为草稿或发布 -->

            <!-- 操作按钮 -->
            <el-form-item>
              <div class="form-actions">
                <el-button @click="goBack" size="large">取消</el-button>
                <el-button @click="saveAsDraft" size="large">保存草稿</el-button>
                <el-button 
                  type="primary" 
                  @click="publishBlog" 
                  size="large"
                  :loading="saving"
                >
                  {{ isEdit ? '更新博客' : '发布博客' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 预览区域 -->
        <div class="preview-section" v-if="showPreview">
          <div class="preview-header">
            <h3>预览效果</h3>
            <el-button @click="showPreview = false" size="small">关闭预览</el-button>
          </div>
          <div class="preview-content" v-html="renderedPreview"></div>
        </div>
      </div>
    </div>
  </LayoutBase>

  <!-- 创建分类对话框 -->
  <el-dialog
    v-model="showCreateCategory"
    title="创建新分类"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="categoryFormRef"
      :model="categoryForm"
      :rules="categoryRules"
      label-width="80px"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input
          v-model="categoryForm.name"
          placeholder="请输入分类名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="分类描述" prop="description">
        <el-input
          v-model="categoryForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入分类描述（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showCreateCategory = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitCreateCategory"
          :loading="creatingCategory"
        >
          创建
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'
import service from '@/utils/request'
// 使用云端版本：RichTextEditor；
// 如需本地化部署版本，改为引入 RichTextEditorLocal 并替换组件标签即可
// import RichTextEditor from '@/components/RichTextEditor.vue'
import RichTextEditorLocal from '@/components/RichTextEditorLocal.vue'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, getCurrentUserId } = useAuth()

// 响应式数据
const formRef = ref(null)
const categoryFormRef = ref(null)
const saving = ref(false)
const creatingCategory = ref(false)
const showCreateCategory = ref(false)
const showPreview = ref(false)
const categories = ref([])
const blogForm = ref({
  title: '',
  content: '',
  categoryId: null,
  status: 'draft'
})

// 创建分类表单
const categoryForm = ref({
  name: '',
  description: ''
})

// 分类表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在1到50个字符', trigger: 'blur' }
  ]
}

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入博客标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在1到200个字符', trigger: 'blur' }
  ],
  contentHtml: [
    { required: true, message: '请输入博客内容', trigger: 'change' }
  ]
}

// 计算属性
const isEdit = computed(() => {
  return route.params.id !== undefined
})

const renderedPreview = computed(() => '')

// 生命周期
onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadBlogDetail()
  }
})

// 方法定义
const loadCategories = async () => {
  try {
    const response = await service.get('/api/category/list')
    if (response.data.success) {
      categories.value = response.data.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 提交创建分类
const submitCreateCategory = async () => {
  try {
    // 验证表单
    await categoryFormRef.value.validate()
    
    creatingCategory.value = true
    
    const response = await service.post('/api/category', categoryForm.value)
    
    if (response.data.success) {
      ElMessage.success('分类创建成功')
      
      // 重新加载分类列表
      await loadCategories()
      
      // 设置新创建的分类为选中状态
      const newCategory = categories.value.find(cat => cat.name === categoryForm.value.name)
      if (newCategory) {
        blogForm.value.categoryId = newCategory.id
      }
      
      // 关闭对话框并重置表单
      showCreateCategory.value = false
      categoryForm.value = { name: '', description: '' }
    }
  } catch (error) {
    console.error('创建分类失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('创建分类失败')
    }
  } finally {
    creatingCategory.value = false
  }
}

const loadBlogDetail = async () => {
  const blogId = route.params.id
  try {
    const response = await service.get(`/api/blog/${blogId}`)
    if (response.data.success) {
      const blog = response.data.data
      blogForm.value = {
        title: blog.title,
        contentHtml: blog.contentHtml || '',
        categoryId: blog.categoryId,
        status: blog.status
      }
    }
  } catch (error) {
    console.error('加载博客详情失败:', error)
    ElMessage.error('加载博客详情失败')
  }
}

// Markdown 工具不再需要，留空以避免报错

const saveBlog = async () => {
  if (!isLoggedIn()) {
    ElMessage.error('请先登录')
    return
  }
  
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }
  
  saving.value = true
  try {
    const data = {
      ...blogForm.value,
      authorId: getCurrentUserId()
    }
    
    let response
    if (isEdit.value) {
      response = await service.put(`/api/blog/${route.params.id}`, data)
    } else {
      response = await service.post('/api/blog', data)
    }
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '博客更新成功' : '博客创建成功')
      router.push('/blog')
    }
  } catch (error) {
    console.error('保存博客失败:', error)
    ElMessage.error('保存博客失败')
  } finally {
    saving.value = false
  }
}

const saveAsDraft = async () => {
  blogForm.value.status = 'draft'
  await saveBlog()
}

const publishBlog = async () => {
  blogForm.value.status = 'published'
  await saveBlog()
}

const goBack = () => {
  router.push('/blog')
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}
</script>

<style scoped>
.blog-edit-container {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.edit-form {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 40px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 30px;
}

.blog-form {
  color: white;
}

.editor-container {
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.content-editor {
  border: none;
}

.content-editor :deep(.el-textarea__inner) {
  background: transparent;
  border: none;
  color: white;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  padding: 20px;
}

.content-editor :deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.preview-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 30px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: white;
}

.preview-content {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.8;
  font-size: 1rem;
}

.preview-content h1,
.preview-content h2,
.preview-content h3,
.preview-content h4,
.preview-content h5,
.preview-content h6 {
  color: white;
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: 600;
}

.preview-content h1 {
  font-size: 1.6rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 8px;
}

.preview-content h2 {
  font-size: 1.4rem;
}

.preview-content h3 {
  font-size: 1.2rem;
}

.preview-content p {
  margin-bottom: 12px;
  line-height: 1.7;
}

.preview-content ul,
.preview-content ol {
  margin-bottom: 12px;
  padding-left: 20px;
}

.preview-content li {
  margin-bottom: 6px;
}

.preview-content blockquote {
  border-left: 4px solid rgba(255, 255, 255, 0.3);
  padding-left: 15px;
  margin: 15px 0;
  font-style: italic;
  color: rgba(255, 255, 255, 0.8);
}

.preview-content code {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}

.preview-content pre {
  background: rgba(0, 0, 0, 0.3);
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 15px 0;
}

.preview-content pre code {
  background: none;
  padding: 0;
  color: rgba(255, 255, 255, 0.9);
}

.preview-content a {
  color: #4CAF50;
  text-decoration: none;
}

.preview-content a:hover {
  text-decoration: underline;
}

.preview-content img {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 15px 0;
}

.preview-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.preview-content th,
.preview-content td {
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 10px;
  text-align: left;
}

.preview-content th {
  background: rgba(255, 255, 255, 0.1);
  font-weight: 600;
}

/* 表单样式调整 */
:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

/* 分类选择器样式 */
.category-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-selector .el-select {
  flex: 1;
}

/* 对话框样式 */
:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px;
}

:deep(.el-dialog__title) {
  color: #333;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 20px;
  color: #333;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding: 15px 20px;
}

/* 对话框内的表单样式 */
:deep(.el-dialog .el-form-item__label) {
  color: #333;
}

:deep(.el-dialog .el-input__wrapper) {
  background: white;
  border: 1px solid #dcdfe6;
  color: #333;
}

:deep(.el-dialog .el-textarea__inner) {
  background: white;
  border: 1px solid #dcdfe6;
  color: #333;
}

:deep(.el-dialog .el-input__inner) {
  color: #333;
}

:deep(.el-dialog .el-input__inner::placeholder) {
  color: #c0c4cc;
}

:deep(.el-dialog .el-textarea__inner::placeholder) {
  color: #c0c4cc;
}

/* 主表单的字体颜色 */
.blog-form :deep(.el-input__inner) {
  color: white;
}

.blog-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.blog-form :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.blog-form :deep(.el-radio__label) {
  color: rgba(255, 255, 255, 0.9);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .blog-edit-container {
    padding: 0 15px;
  }
  
  .edit-form {
    padding: 20px;
  }
  
  .editor-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
}
</style> 