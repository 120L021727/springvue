<template>
  <el-dropdown trigger="click" @command="handleLogout">
    <div class="avatar-container">
      <el-avatar 
        :size="32" 
        :src="avatarUrl" 
        icon="UserFilled"
      />
      <div class="username">{{ getDisplayName() }}</div>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { SwitchButton } from '@element-plus/icons-vue'
import { computed } from 'vue'

const userStore = useUserStore()
const router = useRouter()

/**
 * 计算头像URL，确保是完整的URL
 */
const avatarUrl = computed(() => {
  const userPic = userStore.user?.userPic
  if (!userPic) return ''
  if (userPic.startsWith('http')) return userPic
  return `http://localhost:8080${userPic}`
})

/**
 * 获取用户显示名称
 * 优先返回昵称，如果没有昵称则返回用户名
 * @returns {string} 用户显示名称
 */
const getDisplayName = () => {
  if (userStore.user) {
    return userStore.user.nickname || userStore.user.username || '用户'
  }
  return '用户'
}

/**
 * 处理退出登录
 * 清除用户状态并跳转到登录页面
 */
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
/**
 * 头像容器样式
 * 垂直排列头像和用户名
 */
.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

/**
 * 头像样式
 * 添加白色边框和圆形裁剪
 */
.avatar {
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  overflow: hidden;
}

/**
 * 用户名样式
 * 白色文字，添加阴影效果提高可读性
 */
.username {
  color: white;
  font-weight: 500;
  margin-top: 8px;
  font-size: 14px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

/**
 * Element Plus 下拉菜单样式覆盖
 * 自定义下拉菜单的外观
 */
:deep(.el-dropdown-menu) {
  min-width: 120px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}
</style> 