# Blog和Category API文档

## 概述

本文档详细描述了Blog和Category模块的所有API接口，包括请求参数、响应格式、错误处理等。

## 基础信息

- **Base URL**: `http://localhost:8080`
- **认证方式**: JWT Token (Bearer Token)
- **响应格式**: JSON
- **字符编码**: UTF-8

## 通用响应格式

```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体数据
  }
}
```

## 错误响应格式

```json
{
  "success": false,
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 1. Category API

### 1.1 获取分类列表

**接口地址**: `GET /api/category/list`

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "获取分类列表成功",
  "data": [
    {
      "id": 1,
      "name": "技术分享",
      "description": "技术相关的文章分享",
      "sortOrder": 1,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ]
}
```

### 1.2 获取分类详情

**接口地址**: `GET /api/category/{id}`

**路径参数**:
- `id` (Integer): 分类ID

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "获取分类详情成功",
  "data": {
    "id": 1,
    "name": "技术分享",
    "description": "技术相关的文章分享",
    "sortOrder": 1,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

### 1.3 创建分类

**接口地址**: `POST /api/category`

**请求头**:
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**请求体**:
```json
{
  "name": "新分类",
  "description": "分类描述",
  "sortOrder": 1
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "分类创建成功",
  "data": "分类创建成功"
}
```

### 1.4 更新分类

**接口地址**: `PUT /api/category/{id}`

**路径参数**:
- `id` (Integer): 分类ID

**请求头**:
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**请求体**:
```json
{
  "name": "更新后的分类名",
  "description": "更新后的描述",
  "sortOrder": 2
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "分类更新成功",
  "data": "分类更新成功"
}
```

### 1.5 删除分类

**接口地址**: `DELETE /api/category/{id}`

**路径参数**:
- `id` (Integer): 分类ID

**请求头**:
- `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "分类删除成功",
  "data": "分类删除成功"
}
```

## 2. Blog API

### 2.1 获取博客列表

**接口地址**: `GET /api/blog/list`

**查询参数**:
- `page` (Integer, 可选): 页码，默认1
- `size` (Integer, 可选): 每页大小，默认10，最大100
- `status` (String, 可选): 状态筛选，可选值：`published`, `draft`
- `categoryId` (Integer, 可选): 分类ID筛选
- `keyword` (String, 可选): 关键词搜索

**请求示例**:
```
GET /api/blog/list?page=1&size=10&status=published&categoryId=1&keyword=Vue
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "查询博客列表成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Vue3 组合式API详解",
        "content": "# Vue3 组合式API详解\n\n## 什么是组合式API？...",
        "summary": "详细介绍Vue3组合式API的使用方法和最佳实践",
        "categoryId": 1,
        "authorId": 3,
        "status": "published",
        "viewCount": 156,
        "tags": null,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2.2 获取博客详情

**接口地址**: `GET /api/blog/{id}`

**路径参数**:
- `id` (Integer): 博客ID

**权限说明**:
- 已发布的博客：所有人可访问
- 草稿博客：只有作者可访问

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "获取博客详情成功",
  "data": {
    "id": 1,
    "title": "Vue3 组合式API详解",
    "content": "# Vue3 组合式API详解\n\n## 什么是组合式API？...",
    "summary": "详细介绍Vue3组合式API的使用方法和最佳实践",
    "categoryId": 1,
    "authorId": 3,
    "status": "published",
    "viewCount": 156,
    "tags": null,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

### 2.3 创建博客

**接口地址**: `POST /api/blog`

**请求头**:
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**请求体**:
```json
{
  "title": "新博客标题",
  "content": "# 新博客内容\n\n这是博客的Markdown内容...",
  "summary": "博客摘要",
  "categoryId": 1,
  "status": "draft"
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "博客创建成功",
  "data": "博客创建成功"
}
```

### 2.4 更新博客

**接口地址**: `PUT /api/blog/{id}`

**路径参数**:
- `id` (Integer): 博客ID

**请求头**:
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**权限说明**: 只有作者可以更新自己的博客

**请求体**:
```json
{
  "title": "更新后的博客标题",
  "content": "# 更新后的博客内容\n\n这是更新后的Markdown内容...",
  "summary": "更新后的博客摘要",
  "categoryId": 1,
  "status": "published"
}
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "博客更新成功",
  "data": "博客更新成功"
}
```

### 2.5 删除博客

**接口地址**: `DELETE /api/blog/{id}`

**路径参数**:
- `id` (Integer): 博客ID

**请求头**:
- `Authorization: Bearer {token}`

**权限说明**: 只有作者可以删除自己的博客

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "博客删除成功",
  "data": "博客删除成功"
}
```

### 2.6 更新博客状态

**接口地址**: `PATCH /api/blog/{id}/status`

**路径参数**:
- `id` (Integer): 博客ID

**查询参数**:
- `status` (String): 新状态，可选值：`draft`, `published`

**请求头**:
- `Authorization: Bearer {token}`

**权限说明**: 只有作者可以更新自己博客的状态

**请求示例**:
```
PATCH /api/blog/1/status?status=published
```

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "博客状态更新成功",
  "data": "博客状态更新成功"
}
```

## 3. User API

### 3.1 获取用户信息

**接口地址**: `GET /api/user/{userId}`

**路径参数**:
- `userId` (Integer): 用户ID

**响应示例**:
```json
{
  "success": true,
  "code": 200,
  "message": "获取用户信息成功",
  "data": {
    "id": 3,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

## 4. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 5. 常见错误响应

### 5.1 参数验证错误

```json
{
  "success": false,
  "code": 400,
  "message": "博客标题不能为空, 博客内容不能为空",
  "data": null
}
```

### 5.2 权限不足

```json
{
  "success": false,
  "code": 403,
  "message": "您没有权限更新此博客",
  "data": null
}
```

### 5.3 资源不存在

```json
{
  "success": false,
  "code": 404,
  "message": "博客不存在",
  "data": null
}
```

### 5.4 业务逻辑错误

```json
{
  "success": false,
  "code": 400,
  "message": "无效的状态值，只能是 draft 或 published",
  "data": null
}
```

## 6. 前端调用示例

### 6.1 获取博客列表

```javascript
// 获取已发布的博客
const response = await service.get('/api/blog/list', {
  params: {
    page: 1,
    size: 10,
    status: 'published'
  }
});

// 获取所有博客（包括草稿）
const response = await service.get('/api/blog/list', {
  params: {
    page: 1,
    size: 10
  }
});

// 按分类筛选
const response = await service.get('/api/blog/list', {
  params: {
    page: 1,
    size: 10,
    categoryId: 1
  }
});

// 关键词搜索
const response = await service.get('/api/blog/list', {
  params: {
    page: 1,
    size: 10,
    keyword: 'Vue'
  }
});
```

### 6.2 创建博客

```javascript
const blogData = {
  title: '新博客标题',
  content: '# 新博客内容\n\n这是博客的Markdown内容...',
  summary: '博客摘要',
  categoryId: 1,
  status: 'draft'
};

const response = await service.post('/api/blog', blogData);
```

### 6.3 发布草稿

```javascript
const response = await service.patch(`/api/blog/${blogId}/status?status=published`);
```

### 6.4 获取用户信息

```javascript
const response = await service.get(`/api/user/${userId}`);
```

## 7. 注意事项

1. **认证要求**: 除了查看已发布的博客和分类列表，其他操作都需要JWT认证
2. **权限控制**: 博客的编辑、删除、状态更新等操作只有作者本人可以执行
3. **参数验证**: 所有输入参数都会进行验证，不符合要求会返回400错误
4. **分页限制**: 每页大小最大为100，超过会自动调整为100
5. **状态管理**: 博客状态只能是 `draft` 或 `published`
6. **错误处理**: 所有错误都会返回统一的错误格式，便于前端处理

## 8. 性能优化建议

1. **分页查询**: 始终使用分页查询，避免一次性加载大量数据
2. **索引优化**: 数据库已为常用查询字段创建索引
3. **缓存策略**: 考虑对分类列表等不常变化的数据进行缓存
4. **请求合并**: 前端可以考虑合并多个请求，减少网络开销
5. **懒加载**: 对于博客内容等大文本，可以考虑懒加载策略 