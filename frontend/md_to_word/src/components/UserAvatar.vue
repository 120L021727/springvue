<template>
  <!-- 用户头像下拉菜单组件 -->
  <el-dropdown trigger="click">
    <!-- 头像和用户名显示区域 -->
    <div class="avatar-container">
      <div class="avatar">
        <el-avatar 
          :size="size || 50" 
          :src="userStore.user?.avatar || ''" 
          icon="UserFilled"
        ></el-avatar>
      </div>
      <div class="username">{{ userStore.user?.username || '用户' }}</div>
    </div>
    
    <!-- 下拉菜单内容 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item>
          <span>用户信息</span>
        </el-dropdown-item>
        <el-dropdown-item divided @click="handleLogout">
          <span style="color: #f56c6c">退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
/**
 * 用户头像组件
 * 显示用户头像、用户名和下拉菜单，支持登出功能
 */

import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

// 定义组件属性
const props = defineProps({
  /**
   * 头像大小
   * @type {number}
   * @default 50
   */
  size: {
    type: Number,
    default: 50
  }
})

const router = useRouter()
const userStore = useUserStore()

/**
 * 处理用户登出
 * 清除用户状态并跳转到登录页
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