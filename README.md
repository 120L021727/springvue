# 坤坤的网站

## 项目概述

坤坤的网站是一个现代化的综合平台，集工具、博客、学习于一体。采用Spring Boot + Vue 3的前后端分离架构，提供安全可靠的用户认证和优秀的用户体验。

## 技术架构

### 后端技术栈
- **Spring Boot 3.x** - 主框架
- **Spring Security** - 安全框架
- **JWT** - 无状态认证
- **MyBatis Plus** - ORM框架
- **MySQL** - 数据库

### 前端技术栈
- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI组件库
- **Vite** - 构建工具
- **Axios** - HTTP客户端

## 项目结构

```
springvue/
├── backend/MdToWord/          # 后端项目
│   ├── src/main/java/
│   │   └── com/example/mdtoword/
│   │       ├── config/        # 配置类
│   │       ├── security/      # 安全相关组件
│   │       ├── controller/    # 控制器
│   │       ├── service/       # 业务服务
│   │       ├── pojo/          # 实体类
│   │       ├── exception/     # 异常处理
│   │       └── mapper/        # 数据访问层
│   └── src/main/resources/
│       └── application.yml    # 配置文件
└── frontend/md_to_word/       # 前端项目
    ├── src/
    │   ├── views/             # 页面组件
    │   ├── components/        # 公共组件
    │   ├── composables/       # 组合式函数
    │   ├── stores/            # 状态管理
    │   ├── router/            # 路由配置
    │   └── utils/             # 工具函数
    └── package.json
```

## 核心功能

### 🏠 首页展示
- **欢迎页面** - 美观的首页设计
- **平台特色** - 展示网站核心功能
- **最新动态** - 网站更新信息
- **快速导航** - 便捷的功能入口

### 🔧 工具中心
- **Markdown转Word** - 在线文档转换工具
- **实时预览** - 即时查看转换效果
- **文件下载** - 一键下载转换结果
- **格式保持** - 完整保留文档格式

### 📝 博客系统
- **技术分享** - 技术心得和项目经验
- **学习笔记** - 知识积累和总结
- **文章管理** - 分类和标签系统
- **阅读体验** - 舒适的阅读界面

### 🔐 用户系统
- **Spring Security表单登录** - 标准化的认证流程
- **JWT无状态认证** - 安全可靠的Token机制
- **智能路由守卫** - 前端自动权限控制
- **记住原始页面** - 登录后自动跳转
- **状态自动恢复** - 页面刷新后保持登录状态

### 🎨 界面设计
- **固定导航栏** - 统一的顶部导航
- **毛玻璃效果** - 现代化的视觉设计
- **响应式布局** - 适配各种设备
- **背景图片** - 美观的页面背景
- **透明卡片** - 优雅的内容展示

## 页面结构

### 主要页面
- **首页** (`/`) - 网站欢迎页面，展示平台特色
- **工具页** (`/tools`) - 各种实用工具集合
- **博客页** (`/blog`) - 技术博客和文章
- **登录页** (`/login`) - 用户登录和注册
- **转换工具** (`/converter`) - Markdown转Word工具

### 导航功能
- **品牌标签** - "坤坤的网站"品牌展示
- **导航菜单** - 首页、工具、博客、关于
- **搜索功能** - 全站内容搜索
- **用户信息** - 登录状态和用户操作

## 认证流程

### 1. 用户登录
```
用户输入 → 前端表单 → Spring Security → JWT生成 → 返回Token
```

### 2. 请求认证
```
前端请求 → JWT过滤器 → 验证Token → 设置认证信息 → 处理请求
```

### 3. 权限控制
```
路由守卫 → 检查Token → 验证有效性 → 允许/拒绝访问
```

### 4. 状态恢复
```
页面刷新 → 检查Token → 自动验证 → 恢复用户状态
```

## 快速开始

### 后端启动
1. 配置数据库
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mdtoword
    username: root
    password: your_password
```

2. 启动应用
```bash
cd backend/MdToWord
./mvnw spring-boot:run
```

### 前端启动
1. 安装依赖
```bash
cd frontend/md_to_word
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 访问应用
```
http://localhost:5174
```

## 安全特性

- ✅ Spring Security标准认证
- ✅ JWT无状态Token
- ✅ 密码加密存储
- ✅ 跨域安全配置
- ✅ 统一异常处理
- ✅ 请求权限控制
- ✅ 自动状态恢复

## 用户体验优化

### 智能路由守卫
- 未登录自动跳转登录页
- Token过期自动清理并提示
- 记住原始目标页面
- 友好的提示信息

### 状态管理
- 自动检查用户状态
- 前后端状态同步
- 页面刷新后状态恢复
- 优雅的错误处理

### 界面优化
- 禁用浏览器自动填充
- 毛玻璃视觉效果
- 响应式设计
- 流畅的动画效果

## API接口

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录（Spring Security）
- `GET /api/user/current` - 获取当前用户
- `POST /api/user/logout` - 用户退出

### 转换相关
- `POST /api/converter/markdown-to-word` - Markdown转Word
- `GET /api/converter/health` - 健康检查

## 组件化设计

### 公共组件
- **TopNavbar** - 顶部导航栏组件
- **BrandTag** - 品牌标签组件
- **UserAvatar** - 用户头像组件

### 组合式函数
- **useAuth** - 用户认证相关逻辑
- **useUserStore** - 用户状态管理

### 工具函数
- **request.js** - HTTP请求封装
- **common.js** - 通用工具函数

## 样式系统

### 公共样式类
- `.page-background` - 页面背景样式
- `.glass-effect` - 毛玻璃效果
- `.responsive-container` - 响应式容器
- `.responsive-grid` - 响应式网格

### 设计特色
- 统一的背景图片
- 透明的毛玻璃卡片
- 现代化的导航栏
- 优雅的动画效果

## 开发指南

### 添加新功能
1. 后端：创建Controller、Service、Mapper
2. 前端：创建Vue组件、配置路由
3. 测试：验证功能完整性

### 部署说明
1. 后端：打包为JAR文件部署
2. 前端：构建静态文件部署
3. 配置：设置正确的API地址

## 浏览器兼容性

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 许可证

MIT License

## 贡献指南

欢迎提交Issue和Pull Request来改进项目！

---

**坤坤的网站** - 一个集工具、博客、学习于一体的综合平台 🚀 