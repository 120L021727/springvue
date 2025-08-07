<template>
  <div class="top-navbar">
    <div class="navbar-container">
      <!-- 左侧品牌标签 -->
      <div class="brand-section">
        <BrandTag text="坤坤的网站" />
      </div>

      <!-- 中间导航菜单 -->
      <div class="nav-section">
        <el-menu
          :default-active="activeIndex"
          mode="horizontal"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.9)"
          active-text-color="#ffffff"
          class="nav-menu"
          :collapse="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="tools">
            <el-icon><Tools /></el-icon>
            <span>工具</span>
          </el-menu-item>
          <el-menu-item index="blog">
            <el-icon><Document /></el-icon>
            <span>博客</span>
          </el-menu-item>
          <el-menu-item index="about">
            <el-icon><InfoFilled /></el-icon>
            <span>关于</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 搜索栏 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索..."
          class="search-input"
          size="small"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 右侧用户信息 -->
      <div class="user-section">
        <el-dropdown trigger="click" @command="handleUserCommand">
          <div class="user-info">
            <el-avatar 
              :size="32" 
              :src="avatarUrl" 
              icon="UserFilled"
            />
            <span class="username">{{ getUsername() }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-if="isLoggedIn()" 
                command="profile"
                :disabled="!isLoggedIn()"
              >
                <el-icon><User /></el-icon>
                <span>个人信息</span>
              </el-dropdown-item>
              <el-dropdown-item 
                v-if="isLoggedIn()" 
                command="logout"
                :disabled="!isLoggedIn()"
              >
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
              <el-dropdown-item 
                v-if="!isLoggedIn()" 
                command="login"
                :disabled="isLoggedIn()"
              >
                <el-icon><User /></el-icon>
                <span>未登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuth } from '@/composables/useAuth'
import { ElMessage } from 'element-plus'
import { 
  House, 
  Tools, 
  Document, 
  InfoFilled,
  Search, 
  ArrowDown, 
  User, 
  SwitchButton 
} from '@element-plus/icons-vue'
import BrandTag from './BrandTag.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 使用useAuth composable来自动初始化用户状态
const { autoInitUser, isLoggedIn, getUsername } = useAuth()

// 自动初始化用户状态
autoInitUser()

const searchKeyword = ref('')

/**
 * 计算头像URL，确保是完整的URL
 */
const avatarUrl = computed(() => {
  const userPic = userStore.user?.userPic
  if (!userPic) return ''
  if (userPic.startsWith('http')) return userPic
  return `http://localhost:8080${userPic}`
})

// 根据当前路由确定激活的菜单项
const activeIndex = computed(() => {
  const path = route.path
  if (path === '/') return 'home'
  if (path === '/tools' || path === '/converter') return 'tools'
  if (path === '/blog') return 'blog'
  if (path === '/about') return 'about'
  return 'home'
})



// 处理菜单选择
const handleMenuSelect = (index) => {
  switch (index) {
    case 'home':
      router.push('/')
      break
    case 'tools':
      if (!isLoggedIn()) {
        ElMessage.info('请先登录后再访问工具页面')
        router.push('/login')
      } else {
        router.push('/tools')
      }
      break
    case 'blog':
      if (!isLoggedIn()) {
        ElMessage.info('请先登录后再访问博客页面')
        router.push('/login')
      } else {
        router.push('/blog')
      }
      break
    case 'about':
      router.push('/about')
      break
  }
}

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'login':
      if (!isLoggedIn()) {
        router.push('/login')
      }
      break
    case 'logout':
      if (isLoggedIn()) {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      }
      break
    case 'profile':
      if (isLoggedIn()) {
        router.push('/profile')
      }
      break
  }
}
</script>

<style scoped>
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--navbar-bg, rgba(255, 255, 255, 0.95));
  backdrop-filter: blur(15px);
  border-bottom: var(--navbar-border, 1px solid rgba(0, 0, 0, 0.1));
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.brand-section {
  flex-shrink: 0;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border: none;
  background: transparent;
  display: flex;
  justify-content: center;
  width: 100%;
}

.nav-menu :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  border-bottom: none;
  color: var(--menu-text-color, #333);
  font-weight: 600;
  transition: all 0.3s ease;
  margin: 0 10px;
  display: flex;
  align-items: center;
}

.nav-menu :deep(.el-menu-item:hover) {
  background-color: var(--menu-hover-bg, rgba(25, 118, 210, 0.1));
  color: var(--menu-hover-color, #1976d2);
}

.nav-menu :deep(.el-menu-item.is-active) {
  background-color: var(--menu-active-bg, rgba(25, 118, 210, 0.15));
  color: var(--menu-active-color, #1976d2);
  border-bottom: var(--menu-active-border, 2px solid #1976d2);
}

.nav-menu :deep(.el-menu-item .el-icon) {
  margin-right: 5px;
}

.nav-menu :deep(.el-menu-item span) {
  display: inline-block;
}

.search-section {
  flex-shrink: 0;
  margin: 0 20px;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--search-bg, rgba(0, 0, 0, 0.05));
  border: 1px solid var(--search-border, rgba(0, 0, 0, 0.1));
  border-radius: 20px;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: var(--search-hover-border, #1976d2);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--search-focus-border, #1976d2);
  box-shadow: 0 0 0 2px var(--search-focus-shadow, rgba(25, 118, 210, 0.2));
}

.search-input :deep(.el-input__inner) {
  color: var(--search-text, #333);
}

.search-input :deep(.el-input__inner::placeholder) {
  color: var(--search-placeholder, rgba(0, 0, 0, 0.6));
}

.search-input :deep(.el-input__prefix) {
  color: var(--search-prefix, rgba(0, 0, 0, 0.8));
}

.user-section {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  color: var(--user-text, #333);
}

.user-info:hover {
  background: var(--user-hover-bg, rgba(0, 0, 0, 0.05));
}

.username {
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
}

/* 未登录状态的样式 */
.user-info.not-logged-in {
  opacity: 0.6;
  cursor: not-allowed;
}

.user-info.not-logged-in:hover {
  background: transparent;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
}

:deep(.el-dropdown-menu__item.is-disabled) {
  color: #c0c4cc;
  cursor: not-allowed;
}

:deep(.el-dropdown-menu__item.is-disabled:hover) {
  background-color: transparent;
}
</style> 