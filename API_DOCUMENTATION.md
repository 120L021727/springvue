# cjkweb API 接口文档

## 概述

- **项目名称**: cjkweb - 坤坤的网站服务
- **基础URL**: `http://localhost:8080`
- **认证方式**: JWT Token (Bearer Token)
- **数据格式**: JSON (UTF-8编码)
- **文件上传**: multipart/form-data

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "success": true
}
```

## 状态码说明

| HTTP状态码 | 业务状态码 | 说明 |
|------------|------------|------|
| 200 | 200 | 请求成功 |
| 400 | 400 | 请求参数错误或业务逻辑错误 |
| 401 | 401 | 未授权，需要登录或Token无效 |
| 403 | 403 | 禁止访问，权限不足 |
| 404 | 404 | 资源不存在 |
| 500 | 500 | 服务器内部错误 |

---

## 1. 用户管理接口

### 1.1 用户注册

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/user/register` |
| **接口描述** | 注册新用户账户 |
| **认证要求** | 无需认证 |
| **请求方式** | POST |
| **Content-Type** | application/x-www-form-urlencoded |

#### 请求参数

| 参数名 | 类型 | 必填 | 长度限制 | 说明 |
|--------|------|------|----------|------|
| username | String | 是 | 3-20字符 | 用户名，系统唯一标识 |
| password | String | 是 | 6-50字符 | 登录密码，会被加密存储 |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/user/register" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=123456"
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": "注册成功",
  "success": true
}
```

**错误响应 (400)**
```json
{
  "code": 400,
  "message": "用户名已存在",
  "success": false
}
```

---

### 1.2 用户登录

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/user/login` |
| **接口描述** | 用户登录认证，返回JWT Token |
| **认证要求** | 无需认证 |
| **请求方式** | POST |
| **Content-Type** | application/x-www-form-urlencoded |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（明文） |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/user/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=testuser&password=123456"
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "testuser",
      "email": "testuser@example.com",
      "userPic": null,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  },
  "success": true
}
```

**错误响应 (401)**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "success": false
}
```

---

### 1.3 获取当前用户信息

| 项目 | 说明 |
|------|------|
| **接口地址** | `GET /api/user/current` |
| **接口描述** | 获取当前登录用户的详细信息 |
| **认证要求** | 需要JWT Token |
| **请求方式** | GET |

#### 请求头

| 参数名 | 类型 | 必填 | 格式 | 说明 |
|--------|------|------|------|------|
| Authorization | String | 是 | Bearer {token} | JWT认证Token |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/user/current" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "userPic": "/api/file/avatar/abc123.jpg",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "success": true
}
```

**错误响应 (401)**
```json
{
  "code": 401,
  "message": "未登录",
  "success": false
}
```

---

### 1.4 更新用户信息

| 项目 | 说明 |
|------|------|
| **接口地址** | `PUT /api/user/info` |
| **接口描述** | 更新当前登录用户的基本信息 |
| **认证要求** | 需要JWT Token |
| **请求方式** | PUT |
| **Content-Type** | application/json |

#### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |
| Content-Type | String | 是 | application/json |

#### 请求参数

| 参数名 | 类型 | 必填 | 长度限制 | 格式要求 | 说明 |
|--------|------|------|----------|----------|------|
| nickname | String | 是 | 1-20字符 | 任意字符 | 用户昵称 |
| email | String | 是 | 最大100字符 | 邮箱格式 | 邮箱地址 |

#### 请求示例

```bash
curl -X PUT "http://localhost:8080/api/user/info" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "新昵称",
    "email": "newemail@example.com"
  }'
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "信息更新成功",
  "data": "信息更新成功",
  "success": true
}
```

**错误响应 (400)**
```json
{
  "code": 400,
  "message": "昵称长度必须在1-20个字符之间",
  "success": false
}
```

---

### 1.5 修改密码

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/user/change-password` |
| **接口描述** | 修改当前登录用户的密码 |
| **认证要求** | 需要JWT Token |
| **请求方式** | POST |
| **Content-Type** | application/x-www-form-urlencoded |

#### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

#### 请求参数

| 参数名 | 类型 | 必填 | 长度限制 | 说明 |
|--------|------|------|----------|------|
| currentPassword | String | 是 | 6-50字符 | 当前密码 |
| newPassword | String | 是 | 6-50字符 | 新密码 |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/user/change-password" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "currentPassword=123456&newPassword=654321"
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "message": "密码修改成功，请重新登录",
    "needRelogin": true
  },
  "success": true
}
```

**错误响应 (400)**
```json
{
  "code": 400,
  "message": "密码修改失败，请检查当前密码是否正确",
  "success": false
}
```

---

### 1.6 用户退出登录

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/user/logout` |
| **接口描述** | 用户退出登录 |
| **认证要求** | 需要JWT Token |
| **请求方式** | POST |

#### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/user/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "退出成功",
  "data": "退出成功",
  "success": true
}
```

---

## 2. 文档转换接口

### 2.1 Markdown转Word

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/converter/markdown-to-word` |
| **接口描述** | 将Markdown文本内容转换为Word文档 |
| **认证要求** | 需要JWT Token |
| **请求方式** | POST |
| **Content-Type** | application/json |

#### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |
| Content-Type | String | 是 | application/json |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| markdownContent | String | 是 | Markdown文本内容 |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/converter/markdown-to-word" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '"# 文档标题\n\n## 章节标题\n\n这是正文内容。\n\n### 子章节\n\n- 列表项1\n- 列表项2\n\n| 表头1 | 表头2 |\n|-------|-------|\n| 内容1 | 内容2 |"'
```

#### 响应说明

- **Content-Type**: `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- **Content-Disposition**: `attachment; filename=converted.docx`
- **响应体**: Word文档的字节数组

#### 错误响应 (400/500)

```json
{
  "code": 400,
  "message": "Markdown content cannot be null or empty",
  "success": false
}
```

---

### 2.2 健康检查

| 项目 | 说明 |
|------|------|
| **接口地址** | `GET /api/converter/health` |
| **接口描述** | 检查文档转换服务是否正常运行 |
| **认证要求** | 无需认证 |
| **请求方式** | GET |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/converter/health"
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "Converter service is running",
  "success": true
}
```

---

## 3. 文件管理接口

### 3.1 上传头像

| 项目 | 说明 |
|------|------|
| **接口地址** | `POST /api/file/upload-avatar` |
| **接口描述** | 上传用户头像文件 |
| **认证要求** | 需要JWT Token |
| **请求方式** | POST |
| **Content-Type** | multipart/form-data |

#### 请求头

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |
| Content-Type | String | 是 | multipart/form-data |

#### 请求参数

| 参数名 | 类型 | 必填 | 大小限制 | 格式要求 | 说明 |
|--------|------|------|----------|----------|------|
| file | File | 是 | 最大100MB | 图片格式 | 头像文件 |

#### 请求示例

```bash
curl -X POST "http://localhost:8080/api/file/upload-avatar" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@avatar.jpg"
```

#### 响应示例

**成功响应 (200)**
```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": "/api/file/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
  "success": true
}
```

**错误响应 (400)**
```json
{
  "code": 400,
  "message": "文件大小不能超过100MB",
  "success": false
}
```

---

### 3.2 访问头像

| 项目 | 说明 |
|------|------|
| **接口地址** | `GET /api/file/avatar/{fileName}` |
| **接口描述** | 获取用户头像文件 |
| **认证要求** | 无需认证 |
| **请求方式** | GET |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | String | 是 | 文件名（路径参数） |

#### 请求示例

```bash
curl -X GET "http://localhost:8080/api/file/avatar/550e8400-e29b-41d4-a716-446655440000.jpg"
```

#### 响应说明

- **Content-Type**: `image/jpeg`
- **Content-Disposition**: `inline; filename="550e8400-e29b-41d4-a716-446655440000.jpg"`
- **响应体**: 图片文件的字节流

#### 错误响应

- **404**: 文件不存在
- **400**: 文件路径格式错误

---

## 4. 数据模型

### 4.1 User 用户模型

| 字段名 | 类型 | 数据库字段 | 说明 | 验证规则 |
|--------|------|------------|------|----------|
| id | Integer | id | 用户唯一标识 | 主键，自增 |
| username | String | username | 用户名（登录名） | 3-20字符，唯一 |
| password | String | password | 密码（加密存储） | 6-50字符，JSON中不返回 |
| nickname | String | nickname | 昵称（显示名） | 1-20字符 |
| email | String | email | 邮箱地址 | 邮箱格式验证 |
| userPic | String | user_pic | 头像文件路径 | 可选 |
| createTime | LocalDateTime | create_time | 账户创建时间 | 自动填充 |
| updateTime | LocalDateTime | update_time | 最后更新时间 | 自动填充 |

**JSON示例**:
```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "email": "test@example.com",
  "userPic": "/api/file/avatar/abc123.jpg",
  "createTime": "2024-01-01T10:00:00",
  "updateTime": "2024-01-01T10:00:00"
}
```

### 4.2 Result 响应模型

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| code | Integer | HTTP状态码 | 200, 400, 401, 500 |
| message | String | 操作结果描述 | "操作成功", "用户名已存在" |
| data | Object | 响应数据 | 可以是任何类型的数据 |
| success | Boolean | 操作是否成功 | true, false |

**JSON示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "success": true
}
```

---

## 5. 错误码说明

### 5.1 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 5.2 业务错误码

| 错误场景 | 错误码 | 错误消息 | 处理方式 |
|----------|--------|----------|----------|
| 用户名已存在 | 400 | "用户名已存在" | 提示用户更换用户名 |
| 密码长度不足 | 400 | "密码长度必须在6-50个字符之间" | 提示用户符合密码要求 |
| 邮箱格式错误 | 400 | "邮箱格式不正确" | 提示用户输入正确邮箱 |
| 文件过大 | 400 | "文件大小不能超过100MB" | 提示用户压缩文件 |
| 内容为空 | 400 | "Markdown content cannot be null or empty" | 提示用户输入内容 |
| 用户名或密码错误 | 401 | "用户名或密码错误" | 提示用户检查凭据 |
| Token无效 | 401 | "未登录" | 引导用户重新登录 |
| 用户未登录 | 401 | "用户未登录" | 引导用户登录 |
| 权限不足 | 403 | "权限不足" | 提示用户权限不足 |
| 文件不存在 | 404 | 标准404响应 | 显示404页面 |
| 系统内部错误 | 500 | "服务器内部错误" | 记录日志，显示错误页面 |

---

## 6. 认证说明

### 6.1 JWT Token使用

- **获取Token**: 通过登录接口获取
- **使用方式**: 在请求头中携带 `Authorization: Bearer {token}`
- **Token格式**: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- **有效期**: 24小时
- **算法**: HMAC-SHA256

### 6.2 公开接口

以下接口无需认证即可访问：

| 接口地址 | 说明 |
|----------|------|
| `POST /api/user/register` | 用户注册 |
| `POST /api/user/login` | 用户登录 |
| `GET /api/converter/health` | 健康检查 |
| `GET /api/file/avatar/{fileName}` | 访问头像 |

### 6.3 需要认证的接口

除公开接口外，所有其他接口都需要有效的JWT Token：

| 接口地址 | 说明 |
|----------|------|
| `GET /api/user/current` | 获取当前用户信息 |
| `PUT /api/user/info` | 更新用户信息 |
| `POST /api/user/change-password` | 修改密码 |
| `POST /api/user/logout` | 退出登录 |
| `POST /api/converter/markdown-to-word` | Markdown转Word |
| `POST /api/file/upload-avatar` | 上传头像 |

---

## 7. 部署信息

### 7.1 环境要求

| 项目 | 要求 |
|------|------|
| Java版本 | JDK 17+ |
| 数据库 | MySQL 8.0+ |
| 构建工具 | Maven 3.6+ |
| 内存 | 最少2GB，推荐4GB+ |
| 磁盘 | 最少1GB可用空间 |

### 7.2 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| 数据库连接 | MySQL连接配置 | localhost:3306/springvue_db |
| 文件上传路径 | 头像存储路径 | ./uploads/avatars/ |
| 文件大小限制 | 最大文件大小 | 100MB |
| JWT密钥 | Token签名密钥 | myVeryLongSecretKeyThatIsAtLeast256BitsLong |
| JWT过期时间 | Token有效期 | 86400秒（24小时） |



---

## 8. 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| 1.0.0 | 2025-08-01 | 初始版本，包含用户管理、文档转换、文件管理功能 |

---
 