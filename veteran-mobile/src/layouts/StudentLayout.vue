<!--
  StudentLayout.vue — 主布局
  底部 Tabbar（首页/档案/我的）+ 路由出口
-->
<template>
  <div class="layout">
    <main class="layout__main">
      <router-view />
    </main>

    <van-tabbar v-model="activeTab" route active-color="#2563EB" inactive-color="#646566">
      <van-tabbar-item to="/home" icon="wap-home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="contact-o">档案</van-tabbar-item>
      <van-tabbar-item to="/settings" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
/**
 * 主布局组件
 * - 顶部路由出口渲染子页面
 * - 底部 Tabbar 三项导航（首页/档案/我的）
 */
import { ref } from 'vue'

const activeTab = ref(0)
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.layout__main {
  min-height: 100vh;
  padding-bottom: 50px;
  /* 安全区适配 */
  padding-bottom: calc(50px + env(safe-area-inset-bottom, 0));
}
</style>
