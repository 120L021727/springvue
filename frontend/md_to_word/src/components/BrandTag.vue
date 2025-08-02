<template>
  <!-- 品牌标签组件 -->
  <div class="brand-tag" :style="customStyle">
    {{ text || '坤坤的网站' }}
  </div>
</template>

<script setup>
/**
 * 品牌标签组件
 * 用于显示网站品牌名称，支持自定义文本和样式
 */

// 定义组件属性
const props = defineProps({
  /**
   * 品牌文本内容
   * @type {string}
   * @default '坤坤的网站'
   */
  text: {
    type: String,
    default: '坤坤的网站'
  },
  /**
   * 自定义样式对象
   * @type {Object}
   * @default () => ({})
   */
  customStyle: {
    type: Object,
    default: () => ({})
  }
})
</script>

<style scoped>
/**
 * 品牌标签样式
 * 使用毛玻璃效果和圆角设计，确保文字正常横排显示
 */
.brand-tag {
  background: rgba(255, 255, 255, 0.95);  /* 半透明白色背景 */
  color: #1976d2;                         /* 品牌蓝色文字 */
  padding: 8px 16px;                      /* 内边距 */
  border-radius: 15px;                    /* 圆角 */
  font-size: 14px;                        /* 字体大小 */
  font-weight: 500;                       /* 字体粗细 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2); /* 阴影效果 */
  backdrop-filter: blur(10px);            /* 毛玻璃效果 */
  border: 1px solid rgba(25, 118, 210, 0.2); /* 边框 */
  z-index: 2;                             /* 层级 */
  
  /* 确保文字正常横排显示 */
  writing-mode: horizontal-tb;
  text-orientation: mixed;
  white-space: nowrap;                    /* 防止文字换行 */
  display: inline-block;                  /* 行内块级元素 */
}
</style> 