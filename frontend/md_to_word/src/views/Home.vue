<template>
  <LayoutBase>
    <div class="main-content">
      <!-- 英雄区域 -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">欢迎来到坤坤的网站</h1>
          <p class="hero-subtitle">一个集工具、博客、学习于一体的综合平台</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="goToTools">
              <el-icon><Tools /></el-icon>
              开始使用工具
            </el-button>
            <el-button size="large" @click="goToBlog">
              <el-icon><Document /></el-icon>
              浏览博客
            </el-button>
          </div>
        </div>
      </section>

      <!-- 特色功能区域 -->
      <section class="features-section">
        <div class="responsive-container">
          <h2 class="section-title">平台特色</h2>
          <div class="responsive-grid">
            <div class="feature-card glass-effect">
              <div class="feature-icon">
                <el-icon size="40"><Tools /></el-icon>
              </div>
              <h3>实用工具</h3>
              <p>提供各种实用的在线工具，包括文档转换、格式处理等，让工作更高效。</p>
            </div>
            <div class="feature-card glass-effect">
              <div class="feature-icon">
                <el-icon size="40"><Document /></el-icon>
              </div>
              <h3>技术博客</h3>
              <p>分享技术心得、学习笔记和项目经验，与大家一起成长进步。</p>
            </div>
            <div class="feature-card glass-effect">
              <div class="feature-icon">
                <el-icon size="40"><User /></el-icon>
              </div>
              <h3>用户友好</h3>
              <p>简洁美观的界面设计，流畅的用户体验，让使用变得更加简单。</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 最新动态区域 -->
      <section class="news-section">
        <div class="responsive-container">
          <h2 class="section-title">最新动态</h2>
          <div class="responsive-grid">
            <div class="news-card glass-effect">
              <div class="news-date">2024-01-15</div>
              <h3>Markdown转Word工具上线</h3>
              <p>全新的文档转换工具，支持Markdown格式转换为Word文档，保持格式完整。</p>
            </div>
            <div class="news-card glass-effect">
              <div class="news-date">2024-01-10</div>
              <h3>网站全新改版</h3>
              <p>采用现代化的设计风格，提供更好的用户体验和更丰富的功能。</p>
            </div>
            <div class="news-card glass-effect">
              <div class="news-date">2024-01-05</div>
              <h3>博客系统升级</h3>
              <p>博客系统进行全面升级，支持更多格式和更好的阅读体验。</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </LayoutBase>
</template>

<script setup>
/**
 * 首页组件
 * 展示网站欢迎信息、特色功能和最新动态
 */

import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tools, Document, User } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'

const router = useRouter()

/**
 * 跳转到工具页面
 * 导航到需要登录的工具页面
 */
const goToTools = () => {
  router.push('/tools')
}

/**
 * 跳转到博客页面
 * 导航到需要登录的博客页面
 */
const goToBlog = () => {
  const token = sessionStorage.getItem('jwt-token')
  if (!token) {
    ElMessage.info('请先登录后再访问博客页面')
    router.push('/login')
  } else {
    router.push('/blog')
  }
}
</script>

<style scoped>
/**
 * 首页特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */



/* 英雄区域 */
.hero-section {
  padding: 60px 20px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-actions .el-button {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 25px;
}

/* 特色功能区域 */
.features-section {
  padding: 40px 20px;
  margin: 20px 20px;
}

.section-title {
  text-align: center;
  font-size: 2.2rem;
  font-weight: 600;
  margin-bottom: 40px;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 15px;
  transition: all 0.3s ease;
  color: white;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.feature-icon {
  margin-bottom: 15px;
  color: #ffffff;
}

.feature-card h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 12px;
  color: white;
}

.feature-card p {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  font-size: 14px;
}

/* 最新动态区域 */
.news-section {
  padding: 40px 20px;
  margin: 20px 20px;
}

.news-card {
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  color: white;
}

.news-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.news-date {
  color: #ffffff;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
  opacity: 0.8;
}

.news-card h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: white;
}

.news-card p {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  font-size: 14px;
}
</style>