import { ref } from 'vue'
import { blogApi } from '@/utils/blogApi'

// 简单的作者信息缓存，跨组件共享
const authorCache = ref(new Map())

export function useAuthorCache() {
  /**
   * 按需批量拉取作者信息并写入缓存
   * @param {number[]} authorIds 作者ID数组
   */
  const ensureAuthors = async (authorIds = []) => {
    const unique = [...new Set(authorIds)].filter(id => id != null)
    const need = unique.filter(id => !authorCache.value.has(id))
    for (const id of need) {
      try {
        const res = await blogApi.getUserInfo(id)
        if (res.data?.success) {
          authorCache.value.set(id, res.data.data)
        }
      } catch (e) {
        authorCache.value.set(id, { id, nickname: `用户${id}` })
      }
    }
  }

  /**
   * 获取作者昵称
   * @param {number} authorId 作者ID
   * @returns {string} 作者昵称
   */
  const getAuthorName = (authorId) => {
    const a = authorCache.value.get(authorId)
    return a ? a.nickname : `用户${authorId}`
  }

  return { ensureAuthors, getAuthorName }
}


