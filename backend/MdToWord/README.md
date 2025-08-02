# 坤坤的网站 - 后端项目

## 项目概述

坤坤的网站后端项目基于Spring Boot 3.x构建，采用Spring Security + JWT的认证架构，提供安全可靠的API服务，支持用户管理、文档转换等功能。

## 技术栈

- **Spring Boot 3.x** - 主框架
- **Spring Security** - 安全框架
- **JWT** - 无状态认证
- **MyBatis Plus** - ORM框架
- **MySQL** - 数据库
- **Maven** - 构建工具

## 项目结构

```
src/main/java/com/example/mdtoword/
├── config/                    # 配置类
│   └── SecurityConfig.java    # Spring Security配置
├── security/                  # 安全相关组件
│   ├── filter/               # 过滤器
│   │   └── JwtAuthenticationFilter.java
│   ├── handler/              # 处理器
│   │   ├── CustomAuthenticationSuccessHandler.java
│   │   └── CustomAuthenticationFailureHandler.java
│   ├── service/              # 安全服务
│   │   └── UserDetailsServiceImpl.java
│   └── util/                 # 工具类
│       └── JwtUtil.java
├── controller/               # 控制器
│   ├── UserController.java   # 用户控制器
│   └── ConverterController.java # 转换控制器
├── service/                  # 业务服务
│   ├── UserService.java      # 用户服务接口
│   └── impl/                 # 服务实现
│       └── UserServiceImpl.java
├── mapper/                   # 数据访问层
│   └── UserMapper.java       # 用户数据访问
├── pojo/                     # 实体类
│   ├── User.java            # 用户实体
│   └── Result.java          # 统一响应结果
├── exception/                # 异常处理
│   ├── BusinessException.java # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
└── MdToWordApplication.java  # 应用启动类
```

## 核心功能

### 🔐 用户认证系统
- **Spring Security表单登录** - 标准化的认证流程
- **JWT无状态认证** - 安全可靠的Token机制
- **自定义认证处理器** - 灵活的登录成功/失败处理
- **用户状态管理** - 完整的用户生命周期管理

### 📝 文档转换
- **Markdown转Word** - 在线文档转换服务
- **格式保持** - 完整保留文档格式
- **文件处理** - 安全的文件上传下载
- **转换模板** - 可定制的转换模板

### 🛡️ 安全特性
- **密码加密** - BCrypt密码加密
- **跨域配置** - 安全的CORS设置
- **统一异常处理** - 友好的错误响应
- **权限控制** - 细粒度的访问控制

## API接口

### 用户相关接口

#### 用户注册
```
POST /api/user/register
Content-Type: application/x-www-form-urlencoded

参数:
- username: 用户名
- password: 密码

响应:
{
  "success": true,
  "message": "注册成功",
  "data": null
}
```

#### 用户登录
```
POST /api/user/login
Content-Type: application/x-www-form-urlencoded

参数:
- username: 用户名
- password: 密码

响应:
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "jwt_token_string",
    "user": {
      "id": 1,
      "username": "testuser"
    }
  }
}
```

#### 获取当前用户
```
GET /api/user/current
Authorization: Bearer {jwt_token}

响应:
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser"
  }
}
```

#### 用户退出
```
POST /api/user/logout
Authorization: Bearer {jwt_token}

响应:
{
  "success": true,
  "message": "退出成功",
  "data": null
}
```

### 转换相关接口

#### Markdown转Word
```
POST /api/converter/markdown-to-word
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}

参数:
- file: Markdown文件

响应:
{
  "success": true,
  "message": "转换成功",
  "data": {
    "downloadUrl": "/api/converter/download/{filename}"
  }
}
```

#### 健康检查
```
GET /api/converter/health

响应:
{
  "success": true,
  "message": "服务正常",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

## 安全配置

### Spring Security配置
- 启用表单登录
- 配置JWT过滤器
- 设置公开接口
- 配置CORS策略

### JWT配置
- Token有效期：24小时
- 签名算法：HS512
- 密钥：配置文件管理

### 密码加密
- 算法：BCrypt
- 强度：12

## 数据库设计

### 用户表 (user)
```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

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
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时
```

### 服务器配置
```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

## 启动说明

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤
1. 配置数据库连接
2. 创建数据库和表
3. 启动应用
```bash
./mvnw spring-boot:run
```

### 访问地址
```
http://localhost:8080
```

## 开发指南

### 添加新接口
1. 创建Controller类
2. 定义Service接口和实现
3. 配置数据访问层
4. 添加异常处理

### 安全配置
1. 在SecurityConfig中配置权限
2. 添加JWT过滤器
3. 配置认证处理器

### 异常处理
1. 创建自定义异常类
2. 在GlobalExceptionHandler中处理
3. 返回统一的Result格式

## 部署说明

### 打包
```bash
./mvnw clean package
```

### 运行
```bash
java -jar target/mdtoword-0.0.1-SNAPSHOT.jar
```

### 生产环境配置
- 修改数据库连接
- 配置JWT密钥
- 设置日志级别
- 配置跨域策略

## 监控和日志

### 健康检查
- 应用状态监控
- 数据库连接检查
- 服务可用性验证

### 日志配置
- 使用SLF4J + Logback
- 分级日志记录
- 文件滚动存储

## 性能优化

### 数据库优化
- 索引优化
- 连接池配置
- 查询优化

### 缓存策略
- 用户信息缓存
- 转换结果缓存
- 静态资源缓存

---

**坤坤的网站后端** - 安全可靠的Spring Boot应用 🚀 