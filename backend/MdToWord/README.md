# MdToWord 后端项目

## 项目概述

这是一个基于Spring Boot + Spring Security的Markdown转Word工具后端服务，采用表单登录 + JWT认证的混合认证模式。

## 技术栈

- **Spring Boot 3.x** - 主框架
- **Spring Security** - 安全框架
- **JWT** - 无状态认证
- **MyBatis Plus** - ORM框架
- **MySQL** - 数据库

## 架构设计

### 认证流程

1. **表单登录** - 用户通过前端表单提交用户名密码到 `/api/user/login`
2. **Spring Security认证** - 后端使用Spring Security的标准认证流程
3. **JWT生成** - 认证成功后生成JWT Token返回给前端
4. **后续请求** - 前端在后续请求中携带JWT Token进行无状态认证

### 包结构

```
com.example.mdtoword/
├── config/                 # 配置类
│   └── SecurityConfig.java # Spring Security配置
├── security/              # 安全相关组件
│   ├── filter/            # 过滤器
│   │   └── JwtAuthenticationFilter.java
│   ├── handler/           # 认证处理器
│   │   ├── CustomAuthenticationSuccessHandler.java
│   │   └── CustomAuthenticationFailureHandler.java
│   ├── service/           # 安全服务
│   │   └── UserDetailsServiceImpl.java
│   └── util/              # 安全工具
│       └── JwtUtil.java
├── controller/            # 控制器
│   ├── UserController.java
│   └── ConverterController.java
├── service/              # 业务服务
│   ├── UserService.java
│   └── ConverterService.java
├── pojo/                 # 实体类
│   ├── User.java
│   └── Result.java
├── exception/            # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
└── mapper/              # 数据访问层
    └── UserMapper.java
```

## 核心功能

### 1. 用户认证

- **注册**: `POST /api/user/register`
- **登录**: `POST /api/user/login` (Spring Security表单登录)
- **获取当前用户**: `GET /api/user/current`
- **退出登录**: `POST /api/user/logout`

### 2. Markdown转换

- **转换接口**: `POST /api/converter/markdown-to-word`
- **健康检查**: `GET /api/converter/health`

## 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mdtoword
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### JWT配置

```yaml
jwt:
  secret: your-secret-key-at-least-256-bits-long
  expiration: 86400  # 24小时
```

### 跨域配置

前端地址: `http://localhost:5173`

## 启动说明

1. 确保MySQL数据库已启动并创建数据库
2. 修改 `application.yml` 中的数据库配置
3. 运行 `MdToWordApplication.java`
4. 服务将在 `http://localhost:8080` 启动

## 安全特性

- ✅ Spring Security表单登录
- ✅ JWT无状态认证
- ✅ 密码加密存储
- ✅ 跨域安全配置
- ✅ 统一异常处理
- ✅ 请求权限控制

## 前端集成

前端使用Vue.js + Element Plus，通过表单提交到Spring Security登录端点，保持自定义样式的同时利用Spring Security的标准认证机制。 