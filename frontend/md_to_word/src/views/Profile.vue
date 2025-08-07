<template>
  <div class="page-background">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 背景 -->
    <div class="background-image"></div>
    <div class="background-overlay"></div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="profile-container">
        <h1 class="page-title">个人信息</h1>
        <p class="page-subtitle">管理您的账户信息</p>
        
        <div class="profile-grid">
          <!-- 基本信息卡片 -->
          <div class="profile-card glass-effect">
            <div class="card-header">
              <h2>基本信息</h2>
            </div>
            <div class="card-content">
              <div class="avatar-section">
                <div class="avatar-container">
                  <el-avatar 
                    :size="80" 
                    :src="userInfo.avatar || ''" 
                    icon="UserFilled"
                  />
                  <div class="avatar-upload-overlay">
                    <el-upload
                      ref="avatarUploadRef"
                      :action="uploadAction"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :before-upload="beforeAvatarUpload"
                      :on-success="handleAvatarSuccess"
                      :on-error="handleAvatarError"
                      class="avatar-uploader"
                    >
                      <el-icon class="upload-icon"><Upload /></el-icon>
                    </el-upload>
                  </div>
                </div>
                <div class="avatar-info">
                  <h3>{{ getDisplayName() }}</h3>
                  <p>用户ID: {{ userInfo.id }}</p>
                </div>
              </div>
              
              <el-form :model="userInfo" :rules="basicInfoRules" ref="basicInfoFormRef" label-width="80px" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveBasicInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 安全设置卡片 -->
          <div class="profile-card glass-effect">
            <div class="card-header">
              <h2>安全设置</h2>
            </div>
            <div class="card-content">
              <div class="security-item">
                <div class="security-info">
                  <h4>修改密码</h4>
                  <p>定期更换密码可以提高账户安全性</p>
                </div>
                <el-button type="primary" @click="showChangePassword = true">
                  修改密码
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 账户统计卡片 -->
          <div class="profile-card glass-effect">
            <div class="card-header">
              <h2>账户统计</h2>
            </div>
            <div class="card-content">
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-number">{{ stats.loginCount }}</div>
                  <div class="stat-label">登录次数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ stats.lastLogin }}</div>
                  <div class="stat-label">最后登录</div>
                </div>
                <div class="stat-item">
                  <div class="stat-number">{{ stats.createdAt }}</div>
                  <div class="stat-label">注册时间</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="400px"
      class="password-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 个人信息页面组件
 * 展示和管理用户个人信息、安全设置等
 * 
 * @author 坤坤
 * @since 2024-01-01
 */

import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import TopNavbar from '@/components/TopNavbar.vue'
import { useRouter } from 'vue-router'
import { Upload } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

/**
 * 处理头像URL，确保是完整的URL
 * @param {string} avatarUrl 头像URL
 * @returns {string} 完整的头像URL
 */
const getFullAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) return ''
  if (avatarUrl.startsWith('http')) return avatarUrl
  return `http://localhost:8080${avatarUrl}`
}

// 用户信息
const userInfo = reactive({
  id: '1001',
  username: 'admin',
  email: 'admin@example.com',
  nickname: '管理员',
  avatar: ''
})

// 账户统计
const stats = reactive({
  loginCount: 156,
  lastLogin: '2024-01-15 14:30',
  createdAt: '2023-06-01'
})

// 修改密码相关
const showChangePassword = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 基本信息表单引用
const basicInfoFormRef = ref()

// 头像上传相关
const avatarUploadRef = ref()
const uploadAction = 'http://localhost:8080/api/file/upload-avatar'
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${sessionStorage.getItem('jwt-token')}`
}))

// 基本信息验证规则
const basicInfoRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { 
      pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, 
      message: '请输入正确的邮箱格式', 
      trigger: 'blur' 
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在1-20个字符之间', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === passwordForm.currentPassword) {
          callback(new Error('新密码不能与当前密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 保存基本信息
 * 调用后端API保存用户的邮箱和昵称信息
 */
const saveBasicInfo = async () => {
  try {
    // 1. 表单验证
    await basicInfoFormRef.value.validate()
    
    // 2. 调用后端API保存用户信息
    const response = await fetch('/api/user/info', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${sessionStorage.getItem('jwt-token')}`
      },
      body: JSON.stringify({
        nickname: userInfo.nickname,
        email: userInfo.email
      })
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      ElMessage.success('基本信息保存成功')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    console.error('保存基本信息异常:', error)
    
    if (error.name === 'ValidationError') {
      // 表单验证失败
      ElMessage.error('请检查输入信息是否正确')
    } else if (error.response?.status === 401) {
      // 未授权，可能是token过期
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('保存失败，请稍后重试')
    }
  }
}

/**
 * 修改密码
 * 调用后端API修改密码，成功后直接跳转登录页面
 * 
 * 设计说明：
 * 1. 密码修改成功后，旧的JWT token立即失效
 * 2. 为了安全考虑，用户必须使用新密码重新登录
 * 3. 避免用户困惑，不提供"稍后登录"选项
 * 4. 确保所有需要认证的操作都失效，直到重新登录
 */
const changePassword = async () => {
  try {
    // 1. 表单验证
    await passwordFormRef.value.validate()
    
    // 2. 构建请求参数
    const params = new URLSearchParams({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    // 3. 调用后端API修改密码
    const response = await fetch('/api/user/change-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${sessionStorage.getItem('jwt-token')}`
      },
      body: params
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      // 4. 密码修改成功
      ElMessage.success(result.data.message || '密码修改成功')
      
      // 5. 关闭对话框
      showChangePassword.value = false
      
      // 6. 清空表单
      Object.assign(passwordForm, {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      
      // 7. 检查是否需要重新登录
      if (result.data.needRelogin) {
        // 显示提示信息
        ElMessage.info('密码修改成功，即将跳转到登录页面')
        
        // 延迟一秒后跳转，让用户看到提示信息
        setTimeout(() => {
          // 清除本地存储的token和用户信息
          sessionStorage.removeItem('jwt-token')
          userStore.logout()
          
          // 跳转到登录页面
          router.push('/login')
          
          ElMessage.success('请使用新密码重新登录')
        }, 1000)
      }
    } else {
      // 密码修改失败
      ElMessage.error(result.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码异常:', error)
    
    if (error.name === 'ValidationError') {
      // 表单验证失败
      ElMessage.error('请检查输入信息是否正确')
    } else if (error.response?.status === 401) {
      // 未授权，可能是token过期
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      // 其他错误
      ElMessage.error('修改失败，请稍后重试')
    }
  }
}

// 页面加载时获取用户信息
onMounted(async () => {
  try {
    // 获取用户详细信息
    await loadUserInfo()
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
})

/**
 * 加载用户详细信息
 * 从后端API获取当前登录用户的详细信息
 */
const loadUserInfo = async () => {
  try {
    const response = await fetch('/api/user/current', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${sessionStorage.getItem('jwt-token')}`
      }
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      // 使用工具函数处理头像URL
      const avatarUrl = getFullAvatarUrl(result.data.userPic)
      
      // 更新用户信息
      Object.assign(userInfo, {
        id: result.data.id,
        username: result.data.username,
        email: result.data.email || '',
        nickname: result.data.nickname || '',
        avatar: avatarUrl
      })
    } else {
      throw new Error(result.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息异常:', error)
    if (error.message.includes('未登录') || error.message.includes('认证')) {
      // 用户未登录或token过期
      userStore.logout()
      router.push('/login')
    }
    throw error
  }
}

/**
 * 获取显示名称
 * 优先显示昵称，如果昵称为空则显示用户名
 */
const getDisplayName = () => {
  return userInfo.nickname || userInfo.username;
}

/**
 * 头像上传前的验证
 * @param {File} file 上传的文件
 * @returns {boolean} 是否允许上传
 */
const beforeAvatarUpload = (file) => {
  // 检查文件大小（100MB）
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('头像文件大小不能超过 100MB！')
    return false
  }

  return true
}

/**
 * 头像上传成功处理
 * @param {Object} response 服务器响应
 */
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    // 使用工具函数处理头像URL
    const fullAvatarUrl = getFullAvatarUrl(response.data)
    
    // 更新用户信息中的头像（显示用完整URL）
    userInfo.avatar = fullAvatarUrl
    
    // 更新store中的用户信息（存储相对路径）
    if (userStore.user) {
      userStore.user = {
        ...userStore.user,
        userPic: response.data  // 存储相对路径
      }
    }
    
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

/**
 * 头像上传失败处理
 * @param {Error} error 错误信息
 */
const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败，请重试')
}
</script>

<style scoped>
/**
 * 个人信息页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */



.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}



.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.profile-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.card-header {
  padding: 20px 20px 0;
}

.card-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.card-content {
  padding: 20px;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.avatar-container {
  position: relative;
  margin-right: 20px;
  display: inline-block;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.avatar-container:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-uploader {
  width: 100%;
  height: 100%;
}

.avatar-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: none;
}

.avatar-uploader :deep(.el-upload-list) {
  display: none;
}

.upload-icon {
  font-size: 24px;
  color: white;
  transition: transform 0.3s ease;
}

.avatar-upload-overlay:hover .upload-icon {
  transform: scale(1.1);
}

.avatar-info {
  margin-left: 20px;
}

.avatar-info h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: white;
}

.avatar-info p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.profile-form {
  margin-top: 20px;
}

.profile-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.profile-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.profile-form :deep(.el-input__inner) {
  color: white;
}

.profile-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.security-item:last-child {
  border-bottom: none;
}

.security-info {
  flex: 1;
}

.security-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: white;
}

.security-info p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
  color: #4CAF50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
}

/* 对话框样式 */
:deep(.password-dialog .el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.password-dialog .el-dialog__header) {
  background: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.password-dialog .el-dialog__title) {
  color: #333;
  font-weight: 600;
}

:deep(.password-dialog .el-dialog__body) {
  padding: 30px 20px;
}

:deep(.password-dialog .el-form-item__label) {
  color: #333;
  font-weight: 500;
}

:deep(.password-dialog .el-input__wrapper) {
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.password-dialog .el-input__inner) {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 0 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .profile-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .profile-card {
    margin: 0 10px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-info {
    margin-left: 0;
    margin-top: 15px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 