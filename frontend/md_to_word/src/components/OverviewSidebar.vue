<template>
  <aside class="overview-sidebar glass-effect" v-loading="loading">
    <div class="overview-header">目录</div>
    <el-scrollbar class="overview-scroll" ref="scrollRef">
      <ul class="overview-list">
        <li v-for="cat in overviewTree" :key="cat.id" class="overview-category">
          <div class="category-header" @click="toggleCategory(cat.id)">
            <el-icon class="category-icon"><Folder /></el-icon>
            <span class="category-title">{{ cat.name }}</span>
            <span class="category-count">{{ cat.children.length }}</span>
            <el-icon class="expand-icon" :class="{ open: isCategoryOpen(cat.id) }"><ArrowDown /></el-icon>
          </div>
          <ul class="category-children" v-show="isCategoryOpen(cat.id)">
            <li
              v-for="item in cat.children"
              :key="item.id"
              class="overview-item"
              :class="{ active: String(item.id) === String(activeId) }"
              @click.stop="onItemClick(item.id)"
            >
              <el-icon class="overview-icon"><Document /></el-icon>
              <span class="title" :title="item.title">{{ item.title }}</span>
            </li>
          </ul>
        </li>
      </ul>
    </el-scrollbar>
  </aside>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Folder, ArrowDown } from '@element-plus/icons-vue'
import { blogApi } from '@/utils/blogApi'

const props = defineProps({
  filters: { type: Object, default: () => ({}) },
  activeId: { type: [String, Number], default: null },
  rememberScroll: { type: Boolean, default: true },
  storageKey: { type: String, default: 'overviewScrollTop' }
})

const emit = defineEmits(['select'])
const router = useRouter()

const loading = ref(false)
const categories = ref([])
const overview = ref([])
const openCategoryMap = reactive({})
const scrollRef = ref(null)

const categoryNameById = computed(() => {
  const idToName = {}
  ;(categories.value || []).forEach((c) => { if (c && c.id != null) idToName[c.id] = c.name })
  return idToName
})

const overviewTree = computed(() => {
  const groups = {}
  ;(overview.value || []).forEach((b) => {
    const key = b.categoryId ?? 'uncategorized'
    if (!groups[key]) groups[key] = []
    groups[key].push(b)
  })
  return Object.keys(groups).map((key) => ({
    id: key,
    name: key === 'uncategorized' ? '未分类' : (categoryNameById.value[key] || `分类 ${key}`),
    children: groups[key].slice().sort((a, b) => String(a.title||'').localeCompare(String(b.title||''), 'zh-Hans-CN'))
  })).sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh-Hans-CN'))
})

const isCategoryOpen = (id) => openCategoryMap[id] !== false
const toggleCategory = (id) => { openCategoryMap[id] = !isCategoryOpen(id) }
const initOpenState = () => { overviewTree.value.forEach(cat => { if (openCategoryMap[cat.id] === undefined) openCategoryMap[cat.id] = true }) }

const saveScrollPosition = () => {
  if (!props.rememberScroll) return
  try {
    const wrapEl = scrollRef.value?.wrapRef
    if (wrapEl && typeof wrapEl.scrollTop === 'number') {
      sessionStorage.setItem(props.storageKey, String(wrapEl.scrollTop))
    }
    // 同步当前筛选，便于详情页还原
    sessionStorage.setItem('blogOverviewFilters', JSON.stringify(props.filters || {}))
  } catch (_) {}
}

const restoreScroll = () => {
  if (!props.rememberScroll) return
  requestAnimationFrame(() => {
    try {
      const wrapEl = scrollRef.value?.wrapRef
      const savedTop = Number(sessionStorage.getItem(props.storageKey) || '0')
      if (wrapEl && !Number.isNaN(savedTop)) wrapEl.scrollTop = savedTop
    } catch (_) {}
  })
}

const onItemClick = async (id) => {
  console.log('OverviewSidebar: 点击文章项', id)
  saveScrollPosition()
  // 发射事件给父组件，同时直接跳转（确保跳转生效）
  emit('select', id)
  console.log('OverviewSidebar: 准备跳转到', `/blog/${id}`)
  
  try {
    // 强制路由跳转，即使是同一个路由组件
    await router.push(`/blog/${id}`)
    console.log('OverviewSidebar: 跳转完成')
  } catch (error) {
    console.error('OverviewSidebar: 跳转失败', error)
  }
}

const loadCategories = async () => {
  try {
    const response = await blogApi.getCategoryList()
    if (response.data?.success) categories.value = response.data.data || []
  } catch (e) { /* ignore */ }
}

const loadOverview = async () => {
  try {
    loading.value = true
    // 使用传入的筛选条件，或从sessionStorage恢复
    let params = { page: 1, size: 1000 }
    if (Object.keys(props.filters).length > 0) {
      params = { ...params, ...props.filters }
    } else {
      // 尝试从sessionStorage恢复筛选条件
      try {
        const cached = sessionStorage.getItem('blogOverviewFilters')
        if (cached) {
          const parsed = JSON.parse(cached)
          params = { ...params, ...parsed }
        }
      } catch (_) {}
    }

    console.log('OverviewSidebar: 加载目录，参数:', params)
    const response = await blogApi.getBlogList(params)
    if (response.data?.success) {
      overview.value = (response.data.data.records || []).map(b => ({ 
        id: b.id, 
        title: b.title, 
        categoryId: b.categoryId ?? 'uncategorized' 
      }))
      console.log('OverviewSidebar: 目录加载完成，条目数:', overview.value.length)
      console.log('OverviewSidebar: 目录树数据:', overviewTree.value)
      initOpenState()
      restoreScroll()
    }
  } catch (e) { 
    console.error('加载目录失败:', e)
  } finally { 
    loading.value = false 
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadOverview()])
})

watch(() => props.filters, () => { loadOverview() }, { deep: true })
</script>

<style scoped>
.overview-sidebar {
  padding: 12px;
  border-radius: 10px;
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 120px);
  border: none !important;
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.overview-header {
  color: #ffffff;
  font-weight: 600;
  margin-bottom: 10px;
}

.overview-scroll {
  height: calc(100vh - 140px);
}

.overview-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.overview-item, .overview-category .category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  user-select: none; /* 防止文本选择干扰点击 */
  position: relative; /* 确保点击区域正确 */
}

.overview-item:hover, .overview-category .category-header:hover {
  background: rgba(255, 255, 255, 0.15);
}

.overview-icon {
  opacity: 0.9;
}

.overview-item .title {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.overview-item.active {
  background: rgba(255, 255, 255, 0.2);
  font-weight: 500;
}

.overview-category {
  margin-bottom: 6px;
}

.overview-category .category-header {
  justify-content: space-between;
}

.overview-category .category-title {
  flex: 1;
  font-weight: 600;
}

.overview-category .category-count {
  font-size: 12px;
  opacity: 0.8;
  margin-right: 6px;
}

.overview-category .expand-icon {
  transition: transform 0.2s ease;
}

.overview-category .expand-icon.open {
  transform: rotate(180deg);
}

.overview-category .category-children {
  list-style: none;
  padding-left: 26px;
  margin: 6px 0 4px;
}
</style>
