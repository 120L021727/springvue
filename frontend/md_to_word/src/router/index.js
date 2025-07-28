import { createRouter, createWebHistory } from 'vue-router'
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  
  if (to.meta.requiresAuth && !user) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && user) {
    // 已登录但访问登录页，跳转到转换器页面
    next('/converter')
  } else {
    next()
  }
})

export default router