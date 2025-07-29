<template>
  <div class="login-container">
    <!-- 右上角标签 -->
    <div class="brand-tag">
      坤坤的小工具
    </div>
    
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            class="login-button"
          >
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>
        
        <div class="toggle-section">
          <el-button
            type="text"
            @click="toggleMode"
            class="toggle-button"
          >
            {{ isLogin ? '没有账号？点击注册' : '已有账号？点击登录' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const isLogin = ref(true)
const loading = ref(false)
const loginFormRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 切换登录/注册模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  loginFormRef.value?.resetFields()
}

// 处理登录
const handleLogin = async () => {
  await userStore.login(loginForm.username, loginForm.password)
  ElMessage.success('登录成功')
  router.push('/converter')
}

// 处理注册
const handleRegister = async () => {
  await userStore.register(loginForm.username, loginForm.password)
  ElMessage.success('注册成功，请登录')
  isLogin.value = true
}

// 提交处理
const handleSubmit = async () => {
  if (!loginFormRef.value) return
  
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    if (isLogin.value) {
      await handleLogin()
    } else {
      await handleRegister()
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%); /* 淡蓝色渐变背景 */
  padding: 20px;
  position: relative;
}

/* 右上角品牌标签 */
.brand-tag {
  position: absolute;
  top: 20px;
  right: 30px;
  background: rgba(255, 255, 255, 0.9);
  color: #1976d2;
  padding: 8px 16px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(25, 118, 210, 0.2);
}

.login-card {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  color: #333;
  font-weight: 600;
  margin-bottom: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.toggle-section {
  text-align: center;
  margin-top: 10px;
}

.toggle-button {
  color: #1976d2 !important;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.toggle-button:hover {
  color: #1565c0 !important;
  text-decoration: underline;
}

/* 登录按钮样式 */
.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px; /* 增加字间距 */
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  color: #555;
  font-weight: 500;
  font-size: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  height: 45px;
}

:deep(.el-input__inner) {
  font-size: 16px;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(25, 118, 210, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #1565c0 0%, #1e88e5 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(25, 118, 210, 0.4);
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-card__header) {
  padding: 25px 20px 15px;
}

:deep(.el-card__body) {
  padding: 20px 30px 30px;
}
</style>