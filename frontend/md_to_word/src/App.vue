<script setup>
import { onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'

// 全局初始化用户状态：仅一次，避免各页面重复初始化
const { initUserState } = useAuth()
onMounted(() => initUserState())
</script>

<template>
  <!-- 路由视图容器，根据当前路由渲染对应的页面组件 -->
  <router-view />
</template>

<style scoped>
/**
 * 全局样式重置和基础设置
 * 注意：这里使用scoped，但全局样式应该在assets/main.css中定义
 */

/* 重置默认样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 设置根元素高度和字体 */
html, body {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', SimSun, sans-serif;
}
</style>