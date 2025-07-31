<template>
  <div class="home-container">
    <!-- 用户头像菜单 -->
    <div class="user-profile">
      <el-dropdown trigger="click">
        <div class="avatar-container">
          <div class="avatar">
            <!-- 如果有头像就显示头像，否则显示默认头像 -->
            <el-avatar 
              :size="50" 
              :src="userStore.user?.avatar || ''" 
              icon="UserFilled"
            ></el-avatar>
          </div>
          <div class="username">{{ userStore.user?.username || '用户' }}</div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <span>用户信息</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="logout">
              <span style="color: #f56c6c">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 品牌标签 -->
    <div class="brand-tag">
      坤坤的小工具
    </div>
    
    <!-- 工具卡片区域 -->
    <div class="tools-container">
      <h1 class="welcome-title">欢迎使用坤坤的小工具箱</h1>
      <div class="tools-grid">
        <el-card 
          class="tool-card" 
          shadow="hover"
          @click="router.push('/converter')"
        >
          <div class="tool-icon">
            <el-icon :size="40"><Document /></el-icon>
          </div>
          <h2 class="tool-title">Markdown转Word</h2>
          <p class="tool-desc">轻松将Markdown格式转换为Word文档</p>
        </el-card>
        
        <!-- 可以在这里添加更多工具卡片 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 使用与登录页相同的背景 */
  background: url('/background.png') center center/cover no-repeat;
  background-attachment: fixed;
  position: relative;
  padding: 80px 20px 40px;
}

/* 背景遮罩层 */
.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3); /* 30%透明度的黑色遮罩 */
  z-index: 1;
}

/* 用户头像区域 */
.user-profile {
  position: absolute;
  top: 20px;
  right: 30px;
  z-index: 3;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.avatar {
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  overflow: hidden;
}

.username {
  color: white;
  font-weight: 500;
  margin-top: 8px;
  font-size: 14px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

/* 右上角品牌标签 */
.brand-tag {
  position: absolute;
  top: 20px;
  left: 30px;
  background: rgba(255, 255, 255, 0.95);
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

/* 工具容器 */
.tools-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  margin-top: 60px;
}

.welcome-title {
  text-align: center;
  color: white;
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

.tools-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
}

.tool-card {
  width: 280px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  background: rgba(255, 255, 255, 0.9) !important;
  border-radius: 15px;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2) !important;
}

.tool-icon {
  color: #1976d2;
  margin-bottom: 15px;
}

.tool-title {
  font-size: 1.4rem;
  margin: 10px 0;
  color: #333;
}

.tool-desc {
  color: #666;
  font-size: 14px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-dropdown-menu) {
  min-width: 120px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

:deep(.el-card) {
  overflow: hidden;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border: none;
}

:deep(.el-card__body) {
  padding: 0;
  height: 100%;
}
</style>