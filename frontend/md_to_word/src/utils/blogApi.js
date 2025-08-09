/**
 * 博客相关API服务
 * 统一管理博客相关的API调用，提供统一的接口访问方式
 * 
 * 功能说明：
 * - 封装所有博客相关的HTTP请求
 * - 提供统一的错误处理机制
 * - 支持参数验证和格式化
 * - 便于维护和扩展
 */

import service from './request'

/**
 * 博客API服务类
 * 提供博客文章的增删改查功能
 */
class BlogApiService {
  /**
   * 获取博客列表
   * 
   * 功能说明：
   * - 支持分页查询
   * - 支持按状态筛选（draft/published）
   * - 支持按分类筛选
   * - 支持关键词搜索（标题和内容）
   * - 支持按作者筛选
   * 
   * @param {Object} params 查询参数
   * @param {number} params.page 页码，默认1
   * @param {number} params.size 每页大小，默认10
   * @param {string} params.status 状态筛选，可选值：draft/published
   * @param {number} params.categoryId 分类ID，可选
   * @param {string} params.keyword 关键词，可选
   * @param {number} params.authorId 作者ID，可选
   * @returns {Promise} API响应，包含博客列表和分页信息
   */
  static getBlogList(params = {}) {
    const { page = 1, size = 10, status, categoryId, keyword, authorId } = params
    return service.get('/api/blog/list', {
      params: { page, size, status, categoryId, keyword, authorId }
    })
  }

  /**
   * 获取博客详情
   * 
   * 功能说明：
   * - 根据博客ID获取完整的博客信息
   * - 包含博客的标题、内容、分类、作者等信息
   * - 权限验证由后端处理
   * 
   * @param {number} id 博客ID
   * @returns {Promise} API响应，包含博客详细信息
   */
  static getBlogDetail(id) {
    return service.get(`/api/blog/${id}`)
  }

  /**
   * 创建博客
   * 
   * 功能说明：
   * - 创建新的博客文章
   * - 自动设置当前用户为作者
   * - 支持设置标题、内容、分类、状态等
   * - 数据验证由后端处理
   * 
   * @param {Object} blogData 博客数据
   * @param {string} blogData.title 博客标题
   * @param {string} blogData.content 博客内容
   * @param {number} blogData.categoryId 分类ID，可选
   * @param {string} blogData.status 状态，可选值：draft/published
   * @returns {Promise} API响应，包含创建结果
   */
  static createBlog(blogData) {
    return service.post('/api/blog', blogData)
  }

  /**
   * 更新博客
   * 
   * 功能说明：
   * - 更新现有博客的信息
   * - 只有作者可以更新自己的博客
   * - 支持更新标题、内容、分类、状态等
   * - 权限验证由后端处理
   * 
   * @param {number} id 博客ID
   * @param {Object} blogData 博客数据
   * @param {string} blogData.title 博客标题
   * @param {string} blogData.content 博客内容
   * @param {number} blogData.categoryId 分类ID，可选
   * @param {string} blogData.status 状态，可选值：draft/published
   * @returns {Promise} API响应，包含更新结果
   */
  static updateBlog(id, blogData) {
    return service.put(`/api/blog/${id}`, blogData)
  }

  /**
   * 删除博客
   * 
   * 功能说明：
   * - 删除指定的博客文章
   * - 只有作者可以删除自己的博客
   * - 执行物理删除操作
   * - 权限验证由后端处理
   * 
   * @param {number} id 博客ID
   * @returns {Promise} API响应，包含删除结果
   */
  static deleteBlog(id) {
    return service.delete(`/api/blog/${id}`)
  }

  /**
   * 更新博客状态
   * 
   * 功能说明：
   * - 更新博客的发布状态
   * - 支持草稿发布（draft -> published）
   * - 支持撤回发布（published -> draft）
   * - 只有作者可以修改博客状态
   * 
   * @param {number} id 博客ID
   * @param {string} status 新状态，只能是draft或published
   * @returns {Promise} API响应，包含状态更新结果
   */
  static updateBlogStatus(id, status) {
    return service.patch(`/api/blog/${id}/status?status=${status}`)
  }
}

/**
 * 分类API服务类
 * 提供博客分类的管理功能
 */
class CategoryApiService {
  /**
   * 获取分类列表
   * 
   * 功能说明：
   * - 获取所有可用的博客分类
   * - 按排序字段升序排列
   * - 包含分类的基本信息（ID、名称、描述等）
   * 
   * @returns {Promise} API响应，包含分类列表
   */
  static getCategoryList() {
    return service.get('/api/category/list')
  }

  /**
   * 获取分类详情
   * 
   * 功能说明：
   * - 根据分类ID获取分类的详细信息
   * - 包含分类的名称、描述、排序值等
   * 
   * @param {number} id 分类ID
   * @returns {Promise} API响应，包含分类详细信息
   */
  static getCategoryDetail(id) {
    return service.get(`/api/category/${id}`)
  }

  /**
   * 创建分类
   * 
   * 功能说明：
   * - 创建新的博客分类
   * - 验证分类名称的唯一性
   * - 自动设置排序值（如果未指定）
   * 
   * @param {Object} categoryData 分类数据
   * @param {string} categoryData.name 分类名称
   * @param {string} categoryData.description 分类描述，可选
   * @param {number} categoryData.sortOrder 排序值，可选
   * @returns {Promise} API响应，包含创建结果
   */
  static createCategory(categoryData) {
    return service.post('/api/category', categoryData)
  }

  /**
   * 更新分类
   * 
   * 功能说明：
   * - 更新现有分类的信息
   * - 验证分类名称的唯一性（排除当前分类）
   * - 支持更新名称、描述、排序值等
   * 
   * @param {number} id 分类ID
   * @param {Object} categoryData 分类数据
   * @param {string} categoryData.name 分类名称
   * @param {string} categoryData.description 分类描述，可选
   * @param {number} categoryData.sortOrder 排序值，可选
   * @returns {Promise} API响应，包含更新结果
   */
  static updateCategory(id, categoryData) {
    return service.put(`/api/category/${id}`, categoryData)
  }

  /**
   * 删除分类
   * 
   * 功能说明：
   * - 删除指定的博客分类
   * - 如果分类下有文章，则不允许删除
   * - 执行物理删除操作
   * 
   * @param {number} id 分类ID
   * @returns {Promise} API响应，包含删除结果
   */
  static deleteCategory(id) {
    return service.delete(`/api/category/${id}`)
  }
}

/**
 * 用户API服务类
 * 提供用户信息查询功能
 */
class UserApiService {
  /**
   * 获取用户信息
   * 
   * 功能说明：
   * - 根据用户ID获取用户的基本信息
   * - 包含用户名、昵称、头像等
   * - 用于显示博客作者信息
   * 
   * @param {number} id 用户ID
   * @returns {Promise} API响应，包含用户详细信息
   */
  static getUserInfo(id) {
    return service.get(`/api/user/${id}`)
  }
}

export { BlogApiService, CategoryApiService, UserApiService } 