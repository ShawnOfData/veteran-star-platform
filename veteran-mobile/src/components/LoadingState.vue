<!--
  LoadingState.vue — 加载状态组件
  用于数据加载中展示
-->
<template>
  <div class="loading-state" :style="{ minHeight: height }">
    <van-loading :type="type" :color="color" :size="size" vertical>
      {{ text }}
    </van-loading>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '加载中...' },
  size: { type: String, default: '32px' },
  color: { type: String, default: '#2563EB' },
  type: { type: String, default: 'spinner' },
  height: { type: String, default: '200px' }
})
</script>

<style scoped>
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
