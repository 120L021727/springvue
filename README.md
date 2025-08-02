# MdToWord 项目

## 项目概述

MdToWord是一个基于Spring Boot + Vue 3的Markdown转Word在线工具，采用现代化的前后端分离架构，提供安全可靠的用户认证和优秀的用户体验。

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
    │   ├── stores/            # 状态管理
    │   ├── router/            # 路由配置
    │   └── utils/             # 工具函数
    └── package.json
```

## 核心功能

### 🔐 用户认证系统
- **Spring Security表单登录** - 标准化的认证流程
- **JWT无状态认证** - 安全可靠的Token机制
- **智能路由守卫** - 前端自动权限控制
- **记住原始页面** - 登录后自动跳转

### 📝 Markdown转换
- **在线转换** - 实时Markdown转Word
- **预览功能** - 即时查看转换效果
- **文件下载** - 一键下载转换结果
- **格式保持** - 完整保留文档格式

### 🎨 用户体验
- **自定义界面** - 美观的毛玻璃效果
- **响应式设计** - 适配各种设备
- **友好提示** - 清晰的操作反馈
- **流畅动画** - 提升交互体验

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
http://localhost:5173
```

## 安全特性

- ✅ Spring Security标准认证
- ✅ JWT无状态Token
- ✅ 密码加密存储
- ✅ 跨域安全配置
- ✅ 统一异常处理
- ✅ 请求权限控制

## 用户体验优化

### 智能路由守卫
- 未登录自动跳转登录页
- Token过期自动清理并提示
- 记住原始目标页面

### 友好提示系统
- 信息提示（蓝色）
- 警告提示（橙色）
- 错误提示（红色）
- 成功提示（绿色）

### 状态管理
- 自动检查用户状态
- 前后端状态同步
- 优雅的错误处理

## API接口

### 用户相关
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录（Spring Security）
- `GET /api/user/current` - 获取当前用户
- `POST /api/user/logout` - 用户退出

### 转换相关
- `POST /api/converter/markdown-to-word` - Markdown转Word
- `GET /api/converter/health` - 健康检查

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

**MdToWord** - 让Markdown转Word变得简单高效 🚀 