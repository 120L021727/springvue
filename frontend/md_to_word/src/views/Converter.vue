<template>
  <div style="padding: 40px; max-width: 1200px; margin: auto;">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-input
          type="textarea"
          v-model="markdown"
          :rows="20"
          placeholder="请输入 Markdown 内容"
        />
        <el-button type="primary" @click="convert" style="margin-top: 10px;">转换为 Word</el-button>
      </el-col>
      <el-col :span="12">
        <el-input
          type="textarea"
          :rows="20"
          :value="wordText"
          placeholder="Word 文档内容（预览）"
          readonly
        />
        <el-button
          type="success"
          :disabled="!wordBlob"
          @click="downloadWord"
          style="margin-top: 10px;"
        >下载 Word 文件</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const markdown = ref('')
const wordText = ref('')
const wordBlob = ref(null)

const convert = async () => {
  if (!markdown.value.trim()) {
    ElMessage.error('请输入 Markdown 内容')
    return
  }
  try {
    const res = await axios.post(
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
    ElMessage.success('转换成功，请下载 Word 文件')
  } catch (e) {
    ElMessage.error('转换失败')
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
</script>

<style scoped>
.el-input {
  width: 100%;
}
</style>