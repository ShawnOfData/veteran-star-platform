<template>
  <div class="panel-card">
    <div class="panel-header">
      <h3>申请进度</h3>
      <span class="panel-more" @click="$router.push('/my-applications')">查看全部 →</span>
    </div>

    <el-timeline v-if="applications.length" class="tl">
      <el-timeline-item
        v-for="(item, i) in applications"
        :key="i"
        :type="timelineType(item.status)"
        :hollow="false"
      >
        <div class="tl-title">{{ item.title || '报名申请' }}</div>
        <div class="tl-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</div>
      </el-timeline-item>
    </el-timeline>

    <!-- 空状态：引导探索机会 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg class="icon-svg" viewBox="0 0 24 24"><path d="M21 21l-4.35-4.35"/><circle cx="11" cy="11" r="7"/></svg>
      </div>
      <div class="empty-title">暂无申请记录</div>
      <div class="empty-desc">去机会广场探索更多机会</div>
      <el-button type="primary" size="small" round @click="$router.push('/opportunities')">
        去机会广场
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  applications: { type: Array, default: () => [] }
})

function statusClass(status) {
  if (status === 2) return 'green'
  if (status === 1) return 'blue'
  return 'gray'
}

function statusText(status) {
  if (status === 2) return '已通过 ✓'
  if (status === 1) return '审核中'
  return '未提交'
}

function timelineType(status) {
  if (status === 2) return 'success'
  if (status === 1) return 'primary'
  return 'info'
}
</script>

<style scoped>
.panel-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #edf1f7;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04);
  padding: 20px 24px;
}
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { font-size: 16px; font-weight: 600; color: #0F172A; }
.panel-more { font-size: 12px; color: var(--color-military); cursor: pointer; }

.tl { padding-left: 4px; }
.tl-title { font-size: 14px; font-weight: 500; color: #303133; }
.tl-status { font-size: 12px; margin-top: 2px; }
.tl-status.blue { color: #2563EB; }
.tl-status.green { color: #10B981; }
.tl-status.gray { color: #d1d5db; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 32px 0; gap: 6px; text-align: center;
}
.empty-icon {
  width: 52px; height: 52px; border-radius: 50%;
  background: #f1f5f9; color: #94a3b8;
  display: flex; align-items: center; justify-content: center; margin-bottom: 6px;
}
.empty-title { font-size: 14px; font-weight: 500; color: #64748b; }
.empty-desc { font-size: 12px; color: #94a3b8; margin-bottom: 10px; }

.icon-svg {
  width: 22px; height: 22px; fill: none; stroke: currentColor;
  stroke-width: 2; stroke-linecap: round; stroke-linejoin: round;
}
</style>
