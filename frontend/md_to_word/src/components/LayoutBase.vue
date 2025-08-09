<template>
  <div :class="withBackground ? 'page-background' : ''">
    <TopNavbar />

    <div v-if="withBackground" class="background-image"></div>
    <div v-if="withBackground" class="background-overlay"></div>

    <div v-if="$slots.center" class="layout-center">
      <slot name="center" />
    </div>

    <div class="main-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import TopNavbar from '@/components/TopNavbar.vue'

const props = defineProps({
  withBackground: { type: Boolean, default: true }
})
</script>

<style scoped>
.layout-center {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 60px; /* 顶部导航栏高度，避免被遮挡 */
  z-index: 10;
}
</style>


