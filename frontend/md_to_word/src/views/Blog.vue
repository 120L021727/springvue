<template>
  <div class="page-background">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 背景 -->
    <div class="background-image"></div>
    <div class="background-overlay"></div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="blog-container">
        <h1 class="page-title">技术博客</h1>
        <p class="page-subtitle">分享技术心得，记录学习历程</p>
        
        <div class="blog-grid">
          <el-card 
            class="blog-card glass-effect" 
            shadow="hover"
            v-for="blog in blogs"
            :key="blog.id"
          >
            <div class="blog-image">
              <img :src="blog.image" :alt="blog.title" />
            </div>
            <div class="blog-content">
              <div class="blog-meta">
                <span class="blog-date">{{ blog.date }}</span>
                <span class="blog-category">{{ blog.category }}</span>
              </div>
              <h3 class="blog-title">{{ blog.title }}</h3>
              <p class="blog-excerpt">{{ blog.excerpt }}</p>
              <div class="blog-footer">
                <el-button type="primary" size="small" text>
                  阅读全文
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[6, 12, 24]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import TopNavbar from '@/components/TopNavbar.vue'

// 分页数据
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(18)

// 模拟博客数据
const blogs = ref([
  {
    id: 1,
    title: 'Vue 3 Composition API 深度解析',
    excerpt: '深入探讨Vue 3 Composition API的设计理念、使用方法和最佳实践，帮助你更好地理解和使用Vue 3。',
    date: '2024-01-15',
    category: '前端开发',
    image: 'https://via.placeholder.com/300x200/667eea/ffffff?text=Vue+3'
  },
  {
    id: 2,
    title: 'Spring Boot 微服务架构实践',
    excerpt: '从零开始构建Spring Boot微服务应用，包括服务注册、配置管理、负载均衡等核心概念。',
    date: '2024-01-12',
    category: '后端开发',
    image: 'https://via.placeholder.com/300x200/764ba2/ffffff?text=Spring+Boot'
  },
  {
    id: 3,
    title: 'Docker 容器化部署指南',
    excerpt: '详细介绍Docker的基本概念、常用命令和实际部署案例，让你的应用部署更加简单高效。',
    date: '2024-01-10',
    category: 'DevOps',
    image: 'https://via.placeholder.com/300x200/f093fb/ffffff?text=Docker'
  },
  {
    id: 4,
    title: 'TypeScript 高级类型系统',
    excerpt: '探索TypeScript的高级类型特性，包括条件类型、映射类型、模板字面量类型等。',
    date: '2024-01-08',
    category: '前端开发',
    image: 'https://via.placeholder.com/300x200/4facfe/ffffff?text=TypeScript'
  },
  {
    id: 5,
    title: 'Redis 缓存策略与优化',
    excerpt: '学习Redis的各种缓存策略，包括缓存穿透、缓存雪崩、缓存击穿的解决方案。',
    date: '2024-01-05',
    category: '数据库',
    image: 'https://via.placeholder.com/300x200/43e97b/ffffff?text=Redis'
  },
  {
    id: 6,
    title: 'Git 工作流最佳实践',
    excerpt: '介绍Git Flow、GitHub Flow等主流工作流，以及团队协作中的Git使用技巧。',
    date: '2024-01-03',
    category: '工具使用',
    image: 'https://via.placeholder.com/300x200/fa709a/ffffff?text=Git'
  }
])

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  // 这里可以重新加载数据
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 这里可以重新加载数据
}
</script>

<style scoped>
/**
 * 博客页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */



.blog-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}



.blog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.blog-card {
  border-radius: 15px;
  overflow: hidden;
  transition: all 0.3s ease;
  /* 移除白色背景，使用透明效果 */
}

.blog-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.blog-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.blog-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.blog-card:hover .blog-image img {
  transform: scale(1.05);
}

.blog-content {
  padding: 20px;
}

.blog-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.blog-date {
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.blog-category {
  color: #4CAF50;
  background: rgba(76, 175, 80, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.blog-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 10px;
  color: white;
  line-height: 1.4;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.blog-excerpt {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-footer {
  display: flex;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 分页组件样式调整 */
:deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-pagination .el-pager li) {
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: rgba(76, 175, 80, 0.3);
  color: white;
  border: 1px solid rgba(76, 175, 80, 0.5);
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination .el-pagination__total),
:deep(.el-pagination .el-pagination__sizes) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination .el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination .el-select .el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .blog-container {
    padding: 0 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .blog-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .blog-card {
    margin: 0 10px;
  }
}
</style> 