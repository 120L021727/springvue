import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Login from '@/views/Login.vue'
import Converter from '@/views/Converter.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/converter',
    name: 'Converter',
    component: Converter,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth) {
    // 需要认证的页面
    if (userStore.isLoggedIn) {
      // 已登录，验证后端状态
      const isValid = await userStore.checkAuth()
      if (isValid) {
        next()
      } else {
        next('/login')
      }
    } else {
      // 未登录，跳转登录
      next('/login')
    }
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    // 已登录访问登录页，验证状态
    const isValid = await userStore.checkAuth()
    if (isValid) {
      next('/converter')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router