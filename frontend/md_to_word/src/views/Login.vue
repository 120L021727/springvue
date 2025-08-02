<template>
  <div class="page-background">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 背景 -->
    <div class="background-image"></div>
    <div class="background-overlay"></div>
    
    <!-- 登录框 -->
    <div class="login-container">
      <el-card class="glass-effect" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>{{ isLogin ? '用户登录' : '用户注册' }}</h2>
          </div>
        </template>
        
        <!-- 登录表单 -->
        <el-form
          v-if="isLogin"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="80px"
          size="large"
          autocomplete="off"
          @submit.prevent="handleLogin"
        >
          <!-- 隐藏的输入框，用于禁用浏览器自动填充 -->
          <input type="text" style="display:none" autocomplete="username" />
          <input type="password" style="display:none" autocomplete="current-password" />
          
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
              autocomplete="off"
              :readonly="false"
              @focus="handleInputFocus"
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
              :readonly="false"
              @focus="handleInputFocus"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 注册表单 -->
        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
          size="large"
          autocomplete="off"
          @submit.prevent="handleRegister"
        >
          <!-- 隐藏的输入框，用于禁用浏览器自动填充 -->
          <input type="text" style="display:none" autocomplete="new-username" />
          <input type="password" style="display:none" autocomplete="new-password" />
          
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
              autocomplete="off"
              :readonly="false"
              @focus="handleInputFocus"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
              autocomplete="new-password"
              :readonly="false"
              @focus="handleInputFocus"
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              class="login-button"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="toggle-section">
          <el-button
            type="text"
            @click="toggleMode"
            class="toggle-button"
          >
            {{ isLogin ? '没有账号？点击注册' : '已有账号？点击登录' }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录/注册页面组件
 * 提供用户登录和注册功能，支持表单验证和Spring Security集成
 */

import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import axios from 'axios'
import TopNavbar from '@/components/TopNavbar.vue'

const router = useRouter()
const userStore = useUserStore()

// ==================== 响应式数据 ====================
/**
 * 当前模式：true为登录，false为注册
 */
const isLogin = ref(true)

/**
 * 加载状态：控制按钮loading效果
 */
const loading = ref(false)

/**
 * 表单引用：用于表单验证和重置
 */
const loginFormRef = ref()
const registerFormRef = ref()

// ==================== 表单数据 ====================
/**
 * 登录表单数据
 */
const loginForm = reactive({
  username: '',
  password: ''
})

/**
 * 注册表单数据
 */
const registerForm = reactive({
  username: '',
  password: ''
})

// ==================== 表单验证规则 ====================
/**
 * 登录表单验证规则
 */
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

/**
 * 注册表单验证规则
 */
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// ==================== 生命周期钩子 ====================
/**
 * 组件挂载时的处理
 * 禁用浏览器的自动填充功能
 */
onMounted(() => {
  // 延迟执行，确保DOM已完全渲染
  setTimeout(() => {
    // 查找所有输入框并禁用自动填充
    const inputs = document.querySelectorAll('input[type="text"], input[type="password"]')
    inputs.forEach(input => {
      input.setAttribute('autocomplete', 'off')
      input.setAttribute('data-form-type', 'other')
    })
  }, 100)
})

// ==================== 方法定义 ====================
/**
 * 处理输入框焦点事件
 * 用于禁用浏览器的自动填充功能
 */
const handleInputFocus = (event) => {
  // 延迟设置readonly为false，防止浏览器自动填充
  setTimeout(() => {
    event.target.readOnly = false
  }, 100)
}

/**
 * 切换登录/注册模式
 * 切换时自动重置表单字段
 */
const toggleMode = () => {
  isLogin.value = !isLogin.value
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
}

/**
 * 处理登录 - 使用Spring Security表单登录
 * 通过FormData模拟表单提交，与Spring Security集成
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  // 表单验证
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    // 创建FormData对象，模拟表单提交
    const formData = new FormData()
    formData.append('username', loginForm.username)
    formData.append('password', loginForm.password)
    
    // 发送POST请求到Spring Security的登录端点
    const response = await axios.post('/api/user/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    
    if (response.data.success) {
      // 保存Token和用户信息到本地存储
      const { token, user } = response.data.data
      sessionStorage.setItem('jwt-token', token)
      userStore.user = user
      
      ElMessage.success('登录成功')
      
      // 检查是否有保存的原始路径，实现登录后跳转到原页面
      const redirectPath = sessionStorage.getItem('redirectAfterLogin')
      if (redirectPath) {
        sessionStorage.removeItem('redirectAfterLogin')
        router.push(redirectPath)
      } else {
        router.push('/')
      }
    }
  } catch (error) {
    // 显示具体的错误信息或默认错误提示
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 处理用户注册
 * 调用用户store的注册方法，成功后切换到登录模式
 */
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  // 表单验证
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    await userStore.register(registerForm.username, registerForm.password)
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
    registerFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/**
 * 登录页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */

/**
 * 登录容器样式
 * 绝对定位居中，为顶部导航栏留出空间
 */
.login-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 480px;
  padding: 0 20px;
  z-index: 10;
  margin-top: 30px; /* 为顶部导航栏留出空间 */
}

/**
 * 卡片头部样式
 * 居中显示标题
 */
.card-header {
  text-align: center;
  margin-bottom: 25px;
}

.card-header h2 {
  color: #1976d2;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

/**
 * 登录按钮样式
 * 渐变背景，悬停效果，全宽设计
 */
.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 10px;
  background: linear-gradient(135deg, #1976d2, #42a5f5);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(25, 118, 210, 0.3);
}

/**
 * 切换模式区域样式
 * 居中显示切换按钮
 */
.toggle-section {
  text-align: center;
  margin-top: 20px;
}

/**
 * 切换按钮样式
 * 文字链接样式，悬停效果
 */
.toggle-button {
  color: #1976d2;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.toggle-button:hover {
  color: #42a5f5;
  text-decoration: underline;
}
</style>