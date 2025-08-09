<template>
  <LayoutBase>
    <div class="main-content">
      <div class="tools-container">
        <h1 class="welcome-title">坤坤的工具箱</h1>
        <p class="welcome-subtitle">Hi, {{ getUsername() }}！选择您需要的工具开始使用吧</p>
        
        <div class="tools-grid">
          <el-card 
            class="tool-card" 
            shadow="hover"
            @click="router.push('/converter')"
          >
            <div class="tool-icon">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <h2 class="tool-title">Markdown转Word</h2>
            <p class="tool-desc">轻松将Markdown格式转换为Word文档，保持格式完整</p>
          </el-card>
          
          <!-- 可以在这里添加更多工具卡片 -->
          <el-card 
            class="tool-card coming-soon" 
            shadow="hover"
          >
            <div class="tool-icon">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
            <h2 class="tool-title">图片压缩工具</h2>
            <p class="tool-desc">在线压缩图片，减小文件大小，保持图片质量</p>
            <div class="coming-soon-badge">即将上线</div>
          </el-card>
          
          <el-card 
            class="tool-card coming-soon" 
            shadow="hover"
          >
            <div class="tool-icon">
              <el-icon :size="40"><Link /></el-icon>
            </div>
            <h2 class="tool-title">短链接生成</h2>
            <p class="tool-desc">将长链接转换为短链接，方便分享和传播</p>
            <div class="coming-soon-badge">即将上线</div>
          </el-card>
          
          <el-card 
            class="tool-card coming-soon" 
            shadow="hover"
          >
            <div class="tool-icon">
              <el-icon :size="40"><Calendar /></el-icon>
            </div>
            <h2 class="tool-title">时间计算器</h2>
            <p class="tool-desc">计算日期差、时间转换等实用时间工具</p>
            <div class="coming-soon-badge">即将上线</div>
          </el-card>
        </div>
      </div>
    </div>
  </LayoutBase>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Document, Picture, Link, Calendar } from '@element-plus/icons-vue'
import LayoutBase from '@/components/LayoutBase.vue'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { getUsername, autoInitUser } = useAuth()

// 自动初始化用户状态
autoInitUser()
</script>

<style scoped>
/**
 * 工具页面特定样式
 * 使用main.css中的公共样式类，只保留特定样式
 */



.tools-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-title {
  text-align: center;
  color: white;
  font-size: 2.5rem;
  margin-bottom: 10px;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

.welcome-subtitle {
  text-align: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
  margin-bottom: 40px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.tools-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
}

.tool-card {
  width: 280px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  background: rgba(255, 255, 255, 0.9) !important;
  border-radius: 15px;
  position: relative;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2) !important;
}

.tool-card.coming-soon {
  cursor: not-allowed;
  opacity: 0.7;
}

.tool-card.coming-soon:hover {
  transform: none;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1) !important;
}

.tool-icon {
  color: #1976d2;
  margin-bottom: 15px;
}

.tool-title {
  font-size: 1.4rem;
  margin: 10px 0;
  color: #333;
}

.tool-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.coming-soon-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ff9800;
  color: white;
  padding: 4px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-card) {
  overflow: hidden;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border: none;
}

:deep(.el-card__body) {
  padding: 0;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 2rem;
  }
  
  .welcome-subtitle {
    font-size: 1rem;
  }
  
  .tools-grid {
    gap: 20px;
  }
  
  .tool-card {
    width: 100%;
    max-width: 300px;
  }
}
</style> 