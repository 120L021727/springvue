/**
 * 路由配置模块
 * 定义应用的路由规则和导航守卫
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import Tools from '@/views/Tools.vue'
import Blog from '@/views/Blog.vue'
import BlogDetail from '@/views/BlogDetail.vue'
import BlogEdit from '@/views/BlogEdit.vue'
import CategoryManagement from '@/views/CategoryManagement.vue'
import About from '@/views/About.vue'
import Converter from '@/views/Converter.vue'
import Profile from '@/views/Profile.vue'

/**
 * 路由配置数组
 * 定义应用的所有路由规则
 */
const routes = [
  { path: '/', name: 'Home', component: Home },                                    // 首页
  { path: '/login', name: 'Login', component: Login },                            // 登录页
  { path: '/tools', name: 'Tools', component: Tools, meta: { requiresAuth: true } }, // 工具页（需要认证）
  { path: '/blog', name: 'Blog', component: Blog, meta: { requiresAuth: true } }, // 博客页（需要认证）
  { path: '/blog/:id', name: 'BlogDetail', component: BlogDetail },               // 博客详情页（公开）
  { path: '/blog/create', name: 'BlogCreate', component: BlogEdit, meta: { requiresAuth: true } }, // 创建博客（需要认证）
  { path: '/blog/edit/:id', name: 'BlogEdit', component: BlogEdit, meta: { requiresAuth: true } }, // 编辑博客（需要认证）
  { path: '/category-management', name: 'CategoryManagement', component: CategoryManagement, meta: { requiresAuth: true } }, // 分类管理页（需要认证）
  { path: '/about', name: 'About', component: About },                            // 关于页
  { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } }, // 个人信息页（需要认证）
  { path: '/converter', name: 'Converter', component: Converter, meta: { requiresAuth: true } } // 转换工具页（需要认证）
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(), // 使用HTML5 History模式
  routes
})

/**
 * 全局前置守卫
 * 在路由跳转前进行权限检查和用户状态验证
 */
router.beforeEach(async (to, from, next) => {
  // 检查目标路由是否需要认证
  if (to.meta.requiresAuth) {
    const token = sessionStorage.getItem('jwt-token')
    
    // 如果没有token，保存目标路径并跳转到登录页
    if (!token) {
      sessionStorage.setItem('redirectAfterLogin', to.fullPath)
      ElMessage.info('请先登录后再访问此页面')
      next('/login')
      return
    }

    // 有token，验证token有效性
    const userStore = useUserStore()
    const isValid = await userStore.checkAuth()

    if (isValid) {
      // token有效，允许访问
      next()
    } else {
      // token无效，清除token并跳转到登录页
      sessionStorage.removeItem('jwt-token')
      sessionStorage.setItem('redirectAfterLogin', to.fullPath)
      ElMessage.warning('登录已过期，请重新登录')
      next('/login')
    }
  } else {
    // 不需要认证的路由
    if (to.path === '/login' && sessionStorage.getItem('jwt-token')) {
      // 已登录用户访问登录页，重定向到首页
      next('/')
    } else {
      // 正常访问
      next()
    }
  }
})

export default router