/**
 * 博客相关工具函数
 * 提供博客相关的通用功能，用于数据处理和格式化
 * 
 * 功能说明：
 * - 日期格式化
 * - 文本处理（摘要生成、Markdown处理）
 * - 数据查找和映射
 * - 参数构建和验证
 */

/**
 * 格式化日期
 * 
 * 功能说明：
 * - 将日期字符串转换为中文格式
 * - 显示格式：年月日（如：2024年1月1日）
 * - 处理无效日期，返回空字符串
 * 
 * @param {string} dateString 日期字符串，支持ISO格式或其他标准格式
 * @returns {string} 格式化后的中文日期，无效日期返回空字符串
 */
export const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * 获取博客摘要
 * 
 * 功能说明：
 * - 从博客内容中提取纯文本摘要
 * - 移除Markdown标记符号（#*`）
 * - 将换行符替换为空格
 * - 限制摘要长度，超出部分用省略号表示
 * 
 * @param {string} content 博客内容，支持Markdown格式
 * @param {number} maxLength 最大长度，默认100个字符
 * @returns {string} 处理后的摘要文本
 */
export const getExcerpt = (content, maxLength = 100) => {
  if (!content) return ''
  // 移除Markdown标记，获取纯文本
  const text = content.replace(/[#*`]/g, '').replace(/\n/g, ' ')
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

/**
 * 根据分类ID获取分类名称
 * 
 * 功能说明：
 * - 在分类列表中查找指定ID的分类
 * - 返回分类名称，如果找不到返回空字符串
 * - 用于在博客列表中显示分类名称
 * 
 * @param {number} categoryId 分类ID
 * @param {Array} categories 分类列表，每个元素包含id和name字段
 * @returns {string} 分类名称，找不到返回空字符串
 */
export const getCategoryName = (categoryId, categories = []) => {
  if (!categoryId) return ''
  const category = categories.find(c => c.id === categoryId)
  return category ? category.name : ''
}

/**
 * 构建博客查询参数
 * 
 * 功能说明：
 * - 将筛选条件对象转换为API查询参数
 * - 设置默认的分页参数
 * - 过滤掉空值和undefined
 * - 用于统一处理查询参数的构建
 * 
 * @param {Object} filters 筛选条件对象
 * @param {number} filters.page 页码，默认1
 * @param {number} filters.size 每页大小，默认10
 * @param {string} filters.status 状态筛选，可选值：draft/published
 * @param {number} filters.categoryId 分类ID，可选
 * @param {string} filters.keyword 关键词，可选
 * @param {number} filters.authorId 作者ID，可选
 * @returns {Object} 处理后的查询参数对象
 */
export const buildBlogQueryParams = (filters = {}) => {
  const { page = 1, size = 10, status, categoryId, keyword, authorId } = filters
  
  const params = { page, size }
  
  // 只添加非空值
  if (status) params.status = status
  if (categoryId) params.categoryId = categoryId
  if (keyword) params.keyword = keyword
  if (authorId) params.authorId = authorId
  
  return params
}

/**
 * 验证博客数据
 * 
 * 功能说明：
 * - 验证博客数据的完整性
 * - 检查必填字段是否存在
 * - 验证字段长度和格式
 * - 返回验证结果和错误信息
 * 
 * @param {Object} blogData 博客数据对象
 * @param {string} blogData.title 博客标题
 * @param {string} blogData.content 博客内容
 * @param {string} blogData.status 博客状态，可选值：draft/published
 * @returns {Object} 验证结果，包含isValid和errors字段
 */
export const validateBlogData = (blogData) => {
  const errors = []
  
  if (!blogData.title || blogData.title.trim().length === 0) {
    errors.push('博客标题不能为空')
  } else if (blogData.title.length > 100) {
    errors.push('博客标题不能超过100个字符')
  }
  
  if (!blogData.content || blogData.content.trim().length === 0) {
    errors.push('博客内容不能为空')
  }
  
  if (blogData.status && !['draft', 'published'].includes(blogData.status)) {
    errors.push('博客状态只能是草稿或已发布')
  }
  
  return {
    isValid: errors.length === 0,
    errors
  }
} 