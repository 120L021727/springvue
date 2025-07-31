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
        autocomplete="off"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            autocomplete="new-username"
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
            autocomplete="new-password"
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
  router.push('/home') // 修改为跳转到home页面
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
    ElMessage.error(error.message || '操作失败')
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
  /* 替换原来的渐变背景为图片背景 */
  background: url('/background.png') center center/cover no-repeat;
  /* 添加深色遮罩层，确保文字可读性 */
  background-attachment: fixed;
  position: relative;
  padding: 20px;
}

/* 添加遮罩层提高可读性（可选） */
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3); /* 30%透明度的黑色遮罩 */
  z-index: 1;
}

/* 确保卡片和标签在遮罩层之上 */
.login-card,
.brand-tag {
  position: relative;
  z-index: 2;
}

/* 右上角品牌标签 */
.brand-tag {
  position: absolute;
  top: 20px;
  right: 30px;
  background: rgba(255, 255, 255, 0.95); /* 增加透明度确保可见性 */
  color: #1976d2;
  padding: 8px 16px;
  border-radius: 15px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(25, 118, 210, 0.2);
  z-index: 2;
}

.login-card {
  width: 100%;
  max-width: 480px;
  background: transparent !important; /* 改为完全透明 */
  backdrop-filter: blur(15px); /* 增加模糊效果 */
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2); /* 增强阴影 */
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.2); /* 添加细边框增强层次 */
}

/* 其他样式保持不变 */
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

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-bottom: 20px;
}

/* 让Element Plus卡片透明 */
:deep(.el-card) {
  background: transparent !important;
  border: none !important;
}

:deep(.el-card__header) {
  background: transparent !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  padding: 25px 20px 15px;
}

:deep(.el-card__body) {
  background: transparent !important;
  padding: 20px 30px 30px;
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
</style>