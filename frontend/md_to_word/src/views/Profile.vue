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
                <el-avatar 
                  :size="80" 
                  :src="userInfo.avatar || ''" 
                  icon="UserFilled"
                />
                <div class="avatar-info">
                  <h3>{{ userInfo.username }}</h3>
                  <p>用户ID: {{ userInfo.id }}</p>
                </div>
              </div>
              
              <el-form :model="userInfo" label-width="80px" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="昵称">
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
              
              <div class="security-item">
                <div class="security-info">
                  <h4>账户状态</h4>
                  <p>当前状态：正常</p>
                </div>
                <el-tag type="success">正常</el-tag>
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
                <div class="stat-item">
                  <div class="stat-number">{{ stats.status }}</div>
                  <div class="stat-label">账户状态</div>
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
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
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
 */

import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import TopNavbar from '@/components/TopNavbar.vue'

const userStore = useUserStore()

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
  createdAt: '2023-06-01',
  status: '正常'
})

// 修改密码相关
const showChangePassword = ref(false)
const passwordFormRef = ref()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
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

// 保存基本信息
const saveBasicInfo = async () => {
  try {
    // 这里调用API保存用户信息
    ElMessage.success('基本信息保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

// 修改密码
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    // 这里调用API修改密码
    ElMessage.success('密码修改成功')
    showChangePassword.value = false
    // 清空表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    ElMessage.error('密码修改失败，请重试')
  }
}

// 页面加载时获取用户信息
onMounted(async () => {
  try {
    // 这里可以调用API获取用户详细信息
    // const userData = await userStore.getUserInfo()
    // Object.assign(userInfo, userData)
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
})
</script>

<style scoped>
/**
 * 个人信息页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */

/**
 * 主要内容区域样式
 */
.main-content {
  padding-top: 80px; /* 为顶部导航栏留出空间 */
  padding-bottom: 40px;
  position: relative;
  z-index: 1;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 10px;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.page-subtitle {
  text-align: center;
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40px;
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
  grid-template-columns: repeat(2, 1fr);
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