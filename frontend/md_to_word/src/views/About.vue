<template>
  <div class="page-background">
    <!-- 顶部导航栏 -->
    <TopNavbar />
    
    <!-- 背景 -->
    <div class="background-image"></div>
    <div class="background-overlay"></div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="responsive-container">
        <div class="about-content glass-effect">
          <h1 class="about-title">关于坤坤的网站</h1>
          
          <div class="about-section">
            <h2>项目简介</h2>
            <p>坤坤的网站是一个现代化的综合平台，集工具、博客、学习于一体。我们致力于为用户提供优质的在线服务和知识分享平台。</p>
          </div>
          
          <div class="about-section">
            <h2>核心功能</h2>
            <ul class="feature-list">
              <li><strong>实用工具</strong> - 提供各种在线工具，包括文档转换、格式处理等</li>
              <li><strong>技术博客</strong> - 分享技术心得、学习笔记和项目经验</li>
              <li><strong>用户友好</strong> - 简洁美观的界面设计，流畅的用户体验</li>
              <li><strong>安全可靠</strong> - 采用现代化的安全技术，保护用户数据</li>
            </ul>
          </div>
          
          <div class="about-section">
            <h2>技术栈</h2>
            <div class="tech-stack">
              <div class="tech-item">
                <h3>前端技术</h3>
                <p>Vue 3, Element Plus, Vite, Pinia</p>
              </div>
              <div class="tech-item">
                <h3>后端技术</h3>
                <p>Spring Boot, Spring Security, JWT, MyBatis Plus</p>
              </div>
              <div class="tech-item">
                <h3>数据库</h3>
                <p>MySQL</p>
              </div>
            </div>
          </div>
          
          <div class="about-section">
            <h2>联系我们</h2>
            <p>如果您有任何问题或建议，欢迎通过以下方式联系我们：</p>
            <div class="contact-info">
              <p>📧 邮箱：contact@example.com</p>
              <p>🌐 网站：www.example.com</p>
              <p>📱 微信：example_wechat</p>
            </div>
          </div>
          
          <div class="about-section">
            <h2>更新日志</h2>
            <div class="update-log">
              <div class="update-item">
                <span class="update-date">2024-01-15</span>
                <span class="update-content">网站全新改版，采用现代化设计</span>
              </div>
              <div class="update-item">
                <span class="update-date">2024-01-10</span>
                <span class="update-content">新增Markdown转Word工具</span>
              </div>
              <div class="update-item">
                <span class="update-date">2024-01-05</span>
                <span class="update-content">博客系统上线</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 关于页面组件
 * 展示网站信息、技术栈、联系方式等
 */

import TopNavbar from '@/components/TopNavbar.vue'
</script>

<style scoped>
/**
 * 关于页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */



.about-content {
  padding: 40px;
  border-radius: 20px;
  margin: 20px 0;
  color: white;
}

.about-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 40px;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.about-section {
  margin-bottom: 30px;
}

.about-section h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: white;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
  padding-bottom: 8px;
}

.about-section p {
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 15px;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  padding: 8px 0;
  color: rgba(255, 255, 255, 0.9);
  position: relative;
  padding-left: 20px;
}

.feature-list li::before {
  content: "✓";
  position: absolute;
  left: 0;
  color: #4CAF50;
  font-weight: bold;
}

.tech-stack {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 15px;
}

.tech-item {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.tech-item h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 10px;
  color: white;
}

.tech-item p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.contact-info {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.contact-info p {
  margin: 8px 0;
  color: rgba(255, 255, 255, 0.9);
}

.update-log {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.update-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.update-item:last-child {
  border-bottom: none;
}

.update-date {
  font-weight: 600;
  color: #4CAF50;
  min-width: 100px;
}

.update-content {
  color: rgba(255, 255, 255, 0.9);
  flex: 1;
  margin-left: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-content {
    padding: 20px;
    margin: 10px;
  }
  
  .about-title {
    font-size: 2rem;
  }
  
  .tech-stack {
    grid-template-columns: 1fr;
  }
  
  .update-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .update-date {
    margin-bottom: 5px;
  }
  
  .update-content {
    margin-left: 0;
  }
}
</style> 