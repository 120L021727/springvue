/**
 * 应用程序入口文件
 * 负责初始化Vue应用、配置插件和全局组件
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/main.css'  // 引入全局样式文件
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建Vue应用实例
const app = createApp(App)

// 创建Pinia状态管理实例
const pinia = createPinia()

// 配置Pinia持久化插件，用于保存用户状态到localStorage
pinia.use(piniaPluginPersistedstate)

// 注册全局插件
app.use(pinia)        // 状态管理
app.use(router)       // 路由管理
app.use(ElementPlus)  // UI组件库

// 全局注册Element Plus图标组件
// 这样可以在任何组件中直接使用图标，无需单独导入
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载应用到DOM
app.mount('#app')