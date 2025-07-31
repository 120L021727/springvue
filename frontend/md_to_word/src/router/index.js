import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
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
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
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
  // 如果需要认证
  if (to.meta.requiresAuth) {
    // 检查JWT令牌是否存在
    const token = sessionStorage.getItem('jwt-token')
    if (!token) {
      // 没有令牌，重定向到登录页
      next('/login')
      return
    }
    
    // 有令牌，验证是否有效
    const userStore = useUserStore()
    const isValid = await userStore.checkAuth()
    
    if (isValid) {
      next() // 继续访问
    } else {
      next('/login') // 令牌无效，重定向到登录页
    }
  } else {
    // 不需要认证的页面
    // 如果已登录且访问登录页，重定向到首页
    if (to.path === '/login' && sessionStorage.getItem('jwt-token')) {
      next('/home') // 修改为跳转到home页面
    } else {
      next()
    }
  }
})

export default router