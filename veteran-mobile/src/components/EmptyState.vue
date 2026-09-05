<!--
  EmptyState.vue — 空状态占位组件
  用于列表无数据时展示
-->
<template>
  <div class="empty-state">
    <van-empty :description="description">
      <template v-if="$slots.default">
        <slot />
      </template>
    </van-empty>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  }
})
</script>

<style scoped>
.empty-state {
  padding: 20px 0;
}
</style>
