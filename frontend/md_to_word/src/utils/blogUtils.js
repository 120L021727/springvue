/**
 * 博客相关工具函数
 * 提供博客相关的通用功能，用于数据处理和格式化
 * 
 * 功能说明：
 * - 日期格式化
 * - 文本处理（摘要生成、Markdown处理）
 * - 数据查找和映射
 * - 参数构建和验证
 * - 状态文本转换
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
 * 根据作者ID获取作者名称
 * 
 * 功能说明：
 * - 在作者信息缓存中查找指定ID的作者
 * - 返回作者昵称，如果找不到返回默认格式
 * - 用于在博客列表中显示作者信息
 * 
 * @param {number} authorId 作者ID
 * @param {Object} authors 作者信息缓存，key为作者ID，value为作者信息对象
 * @returns {string} 作者昵称，找不到返回"用户{ID}"格式
 */
export const getAuthorName = (authorId, authors = {}) => {
  if (!authorId) return ''
  const author = authors[authorId]
  return author ? author.nickname : `用户${authorId}`
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
 * @param {string} filters.status 状态筛选，可选
 * @param {number} filters.categoryId 分类ID，可选
 * @param {string} filters.keyword 关键词，可选
 * @param {number} filters.authorId 作者ID，可选
 * @returns {Object} 构建好的查询参数对象
 */
export const buildBlogQueryParams = (filters = {}) => {
  const { page = 1, size = 10, status, categoryId, keyword, authorId } = filters
  const params = { page, size }
  
  // 只添加有值的参数
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
 * - 检查必填字段是否为空
 * - 返回验证结果和错误信息
 * - 用于前端表单验证
 * 
 * @param {Object} blogData 博客数据对象
 * @param {string} blogData.title 博客标题
 * @param {string} blogData.content 博客内容
 * @param {number} blogData.authorId 作者ID
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export const validateBlogData = (blogData) => {
  if (!blogData.title || !blogData.title.trim()) {
    return { valid: false, message: '博客标题不能为空' }
  }
  
  if (!blogData.content || !blogData.content.trim()) {
    return { valid: false, message: '博客内容不能为空' }
  }
  
  if (!blogData.authorId) {
    return { valid: false, message: '作者ID不能为空' }
  }
  
  return { valid: true, message: '' }
}

/**
 * 获取博客状态显示文本
 * 
 * 功能说明：
 * - 将状态值转换为中文显示文本
 * - 支持draft和published状态
 * - 未知状态返回原值
 * 
 * @param {string} status 状态值，支持draft/published
 * @returns {string} 中文状态文本
 */
export const getStatusText = (status) => {
  const statusMap = {
    'draft': '草稿',
    'published': '已发布'
  }
  return statusMap[status] || status
}

/**
 * 获取博客状态标签类型
 * 
 * 功能说明：
 * - 根据状态值返回对应的Element Plus标签类型
 * - 用于设置标签的颜色样式
 * - draft状态使用warning（橙色），published状态使用success（绿色）
 * 
 * @param {string} status 状态值，支持draft/published
 * @returns {string} Element Plus标签类型（warning/success/info）
 */
export const getStatusTagType = (status) => {
  const typeMap = {
    'draft': 'warning',
    'published': 'success'
  }
  return typeMap[status] || 'info'
} 