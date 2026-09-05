<!--
  App.vue — 根组件
  仅包含 router-view，由路由控制页面切换
-->
<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['Home', 'Profile', 'Settings']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script setup>
// 根组件无需额外逻辑
</script>

<style>
/* 全局样式在 global.css 中定义 */
</style>
