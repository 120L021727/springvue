# MdToWord 前端项目

## 项目概述

这是一个基于Vue 3 + Element Plus的Markdown转Word工具前端应用，采用现代化的UI设计和优秀的用户体验，经过重构优化，具有清晰的架构和可维护性。

## 技术栈

- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI组件库
- **Vite** - 构建工具
- **Axios** - HTTP客户端

## 项目架构

### 📁 目录结构

```
src/
├── assets/           # 静态资源
├── components/       # 公共组件
│   ├── UserAvatar.vue    # 用户头像组件
│   ├── BrandTag.vue      # 品牌标签组件
│   └── PageLayout.vue    # 页面布局组件
├── composables/      # 组合式函数
│   ├── useAuth.js        # 用户认证相关
│   └── index.js          # 统一导出
├── router/          # 路由配置
├── stores/          # 状态管理
├── utils/           # 工具函数
│   ├── request.js       # HTTP请求封装
│   └── common.js        # 通用工具函数
├── views/           # 页面组件
│   ├── Login.vue    # 登录页面
│   ├── Home.vue     # 主页
│   └── Converter.vue # 转换工具
├── App.vue          # 根组件
└── main.js          # 入口文件
```

## 核心特性

### 🔧 组件化架构
- **UserAvatar** - 可复用的用户头像组件
- **BrandTag** - 统一的品牌标签组件
- **PageLayout** - 标准化的页面布局组件

### 🎯 组合式函数 (Composables)
- **useAuth** - 统一的用户认证逻辑
- 自动用户状态检查
- 简化的用户信息获取

### 🛠️ 工具函数
- **request.js** - HTTP请求封装
- **common.js** - 通用工具函数集合
  - 文件大小格式化
  - 防抖/节流函数
  - 深拷贝
  - 格式验证等

### 🎨 样式优化
- 统一的背景和布局
- 可复用的样式组件
- 响应式设计

## 功能特性

### 🔐 用户认证
- **Spring Security表单登录** - 与后端无缝集成
- **JWT无状态认证** - 安全可靠
- **智能路由守卫** - 自动跳转和权限控制
- **记住原始页面** - 登录后自动跳转到目标页面

### 🎨 用户体验
- **自定义登录界面** - 美观的毛玻璃效果
- **友好提示信息** - 清晰的用户反馈
- **响应式设计** - 适配不同屏幕尺寸
- **流畅动画效果** - 提升交互体验

### 🔧 核心功能
- **Markdown转Word** - 在线转换工具
- **实时预览** - 即时查看转换效果
- **文件下载** - 一键下载转换结果
- **用户管理** - 注册、登录、退出

## 重构优化亮点

### 1. 组件复用
```vue
<!-- 使用公共组件 -->
<PageLayout>
  <UserAvatar />
  <BrandTag />
  <!-- 页面内容 -->
</PageLayout>
```

### 2. 逻辑简化
```javascript
// 使用Composable简化用户状态管理
const { getUsername, autoInitUser } = useAuth()
autoInitUser() // 自动初始化用户状态
```

### 3. 工具函数
```javascript
import { formatFileSize, debounce, isValidEmail } from '@/utils/common'

// 格式化文件大小
formatFileSize(1024) // "1 KB"

// 防抖函数
const debouncedSearch = debounce(search, 300)

// 邮箱验证
isValidEmail('test@example.com') // true
```

## 启动说明

1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 访问应用
```
http://localhost:5173
```

## 构建部署

```bash
# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

## 与后端集成

### API配置
- **基础URL**: `http://localhost:8080`
- **认证方式**: JWT Bearer Token
- **跨域支持**: 已配置CORS

### 认证流程
1. 用户输入用户名密码
2. 前端表单提交到Spring Security登录端点
3. 后端验证并返回JWT Token
4. 前端保存Token，后续请求自动携带

## 开发指南

### 添加新页面
1. 在 `src/views/` 创建Vue组件
2. 使用 `PageLayout` 组件作为基础布局
3. 在 `src/router/index.js` 添加路由配置
4. 设置 `meta.requiresAuth` 控制访问权限

### 添加新组件
1. 在 `src/components/` 创建组件
2. 使用 `defineProps` 定义属性
3. 使用 `scoped` 样式避免冲突

### 添加新工具函数
1. 在 `src/utils/common.js` 添加函数
2. 添加详细的JSDoc注释
3. 导出函数供其他模块使用

### 状态管理
- 使用Pinia进行状态管理
- 用户信息存储在 `userStore`
- Token存储在 `sessionStorage`

### 样式规范
- 使用Element Plus组件库
- 自定义样式采用scoped方式
- 响应式设计优先
- 使用CSS变量管理主题色

## 性能优化

### 1. 组件懒加载
```javascript
// 路由懒加载
const Home = () => import('@/views/Home.vue')
```

### 2. 防抖节流
```javascript
// 搜索防抖
const debouncedSearch = debounce(searchFunction, 300)
```

### 3. 图片优化
- 使用WebP格式
- 图片懒加载
- 适当的图片尺寸

## 浏览器兼容性

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 许可证

MIT License
