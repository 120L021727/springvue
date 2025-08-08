<template>
  <div class="page-background">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 背景 -->
    <div class="background-image"></div>
    <div class="background-overlay"></div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="category-management-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">分类管理</h1>
          <p class="page-subtitle">管理博客分类，组织您的内容</p>
        </div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <el-button 
            type="primary" 
            @click="showCreateDialog = true"
            size="large"
          >
            <el-icon><Plus /></el-icon>
            新建分类
          </el-button>
        </div>

        <!-- 分类列表 -->
        <div class="category-list">
          <el-table 
            :data="categories" 
            v-loading="loading"
            style="width: 100%"
            :header-cell-style="{ background: 'rgba(255, 255, 255, 0.1)', color: 'white' }"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="分类名称" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="editCategory(scope.row)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteCategory(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>

  <!-- 创建/编辑分类对话框 -->
  <el-dialog
    v-model="showCreateDialog"
    :title="isEdit ? '编辑分类' : '新建分类'"
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
      
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="categoryForm.sortOrder"
          :min="0"
          :max="999"
          placeholder="数字越小越靠前"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitCategory"
          :loading="submitting"
        >
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TopNavbar from '@/components/TopNavbar.vue'
import service from '@/utils/request'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const categories = ref([])
const categoryFormRef = ref(null)
const editingCategory = ref(null)

const categoryForm = ref({
  name: '',
  description: '',
  sortOrder: 0
})

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在1到50个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 计算属性
const isEdit = computed(() => {
  return editingCategory.value !== null
})

// 生命周期
onMounted(() => {
  loadCategories()
})

// 方法定义
const loadCategories = async () => {
  loading.value = true
  try {
    const response = await service.get('/api/category/list')
    if (response.data.success) {
      categories.value = response.data.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const editCategory = (category) => {
  editingCategory.value = category
  categoryForm.value = {
    name: category.name,
    description: category.description || '',
    sortOrder: category.sortOrder || 0
  }
  showCreateDialog.value = true
}

const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await service.delete(`/api/category/${category.id}`)
    if (response.data.success) {
      ElMessage.success('分类删除成功')
      await loadCategories()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

const submitCategory = async () => {
  try {
    await categoryFormRef.value.validate()
    submitting.value = true
    
    let response
    if (isEdit.value) {
      response = await service.put(`/api/category/${editingCategory.value.id}`, categoryForm.value)
    } else {
      response = await service.post('/api/category', categoryForm.value)
    }
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '分类更新成功' : '分类创建成功')
      showCreateDialog.value = false
      await loadCategories()
      resetForm()
    }
  } catch (error) {
    console.error('保存分类失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存分类失败')
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingCategory.value = null
  categoryForm.value = {
    name: '',
    description: '',
    sortOrder: 0
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.category-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.action-bar {
  margin-bottom: 30px;
  display: flex;
  justify-content: flex-end;
}

.category-list {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  padding: 30px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 表格样式 */
:deep(.el-table) {
  background: transparent;
  color: white;
}

:deep(.el-table th) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-table td) {
  background: transparent;
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-table--border) {
  border: 1px solid rgba(255, 255, 255, 0.2);
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

:deep(.el-dialog .el-input-number .el-input__wrapper) {
  background: white;
  border: 1px solid #dcdfe6;
  color: #333;
}

:deep(.el-dialog .el-input-number .el-input__inner) {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .category-management-container {
    padding: 0 15px;
  }
  
  .category-list {
    padding: 20px;
  }
  
  .action-bar {
    justify-content: center;
  }
}
</style> 