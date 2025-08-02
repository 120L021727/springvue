<template>
  <div class="converter-page">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div style="padding: 60px; max-width: 1600px; margin: 0 auto;">
        <el-card shadow="hover">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px;">
            <div>
              <h2 style="font-size: 2.2rem; margin: 0;">md转换word工具</h2>
              <p style="margin: 5px 0 0 0; color: #666; font-size: 14px;">
                欢迎使用，{{ getUsername() }}
              </p>
            </div>
            <el-button type="primary" @click="goToTools" size="small">返回工具页</el-button>
          </div>
          <el-row :gutter="30" style="align-items: stretch;">
            <el-col :span="12" style="display: flex; flex-direction: column; height: 500px;">
              <el-divider>md内容</el-divider>
              <div class="input-container">
                <el-input
                  type="textarea"
                  v-model="markdown"
                  placeholder="请输入 Markdown 内容"
                  style="width: 100%; height: 100%; font-size: 18px;"
                />
              </div>
              <el-button type="primary" @click="convert" style="margin-top: 30px; width: 100%; font-size: 18px;">转换为 Word</el-button>
            </el-col>
            <el-col :span="12" style="display: flex; flex-direction: column; height: 500px;">
              <el-divider>Word格式预览</el-divider>
              <div
                class="preview-container"
                contenteditable="true"
                v-html="wordPreviewHtml"
                style="font-size: 18px;"
              ></div>
              <el-button
                type="success"
                :disabled="!wordBlob"
                @click="downloadWord"
                style="margin-top: 30px; width: 100%; font-size: 18px;"
              >下载 Word 文件</el-button>
            </el-col>
          </el-row>
          <el-row style="margin-top: 30px;">
            <el-col :span="24">
              <el-button type="warning" @click="clearAll" style="width: 100%; font-size: 18px;">清空内容</el-button>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import service from '@/utils/request'
import mammoth from 'mammoth'
import TopNavbar from '@/components/TopNavbar.vue'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { getUsername, autoInitUser } = useAuth()

const markdown = ref('')
const wordBlob = ref(null)
const wordPreviewHtml = ref('')

// 自动初始化用户状态
autoInitUser()

// 返回工具页
const goToTools = () => {
  router.push('/tools')
}

const convert = async () => {
  if (!markdown.value.trim()) {
    ElMessage.error('请输入 Markdown 内容')
    return
  }
  
  try {
    const res = await service.post(
      '/api/converter/markdown-to-word',
      markdown.value,
      {
        responseType: 'blob',
        headers: {
          'Content-Type': 'text/plain'
        }
      }
    )
    
    wordBlob.value = res.data
    const arrayBuffer = await res.data.arrayBuffer()
    const result = await mammoth.convertToHtml({ arrayBuffer })
    wordPreviewHtml.value = result.value
    ElMessage.success('转换成功，请下载 Word 文件')
  } catch (e) {
    // 错误已在拦截器中处理
    console.error('转换失败:', e)
  }
}

const downloadWord = () => {
  if (!wordBlob.value) return
  const url = window.URL.createObjectURL(wordBlob.value)
  const a = document.createElement('a')
  a.href = url
  a.download = 'converted.docx'
  a.click()
  window.URL.revokeObjectURL(url)
}

const clearAll = () => {
  markdown.value = ''
  wordPreviewHtml.value = ''
  wordBlob.value = null
  ElMessage.success('内容已清空')
}
</script>

<style scoped>
.converter-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.main-content {
  padding-top: 60px; /* 为顶部导航栏留出空间 */
}

.input-container, .preview-container {
  flex: 1;
  min-height: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
  background-color: #fff;
  overflow-y: auto;
  box-sizing: border-box;
}

.input-container :deep(.el-textarea) {
  height: 100%;
}

.input-container :deep(.el-textarea__inner) {
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
  height: 100% !important;
  resize: none !important;
  font-size: 18px !important;
}

.preview-container {
  outline: none;
  font-size: 18px;
}
</style>