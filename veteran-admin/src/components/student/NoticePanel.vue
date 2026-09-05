<template>
  <div class="panel-card">
    <div class="panel-header">
      <h3>通知公告</h3>
      <span class="panel-more">更多 →</span>
    </div>

    <div v-if="list.length" class="ann-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="ann-item"
        @click="openDetail(item)"
      >
        <div class="ann-left">
          <span
            class="ann-tag"
            :class="item.priority === 2 ? 'red' : item.priority === 1 ? 'gold' : 'blue'"
          >
            {{ item.priority === 2 ? '重要' : item.priority === 1 ? '通知' : '公告' }}
          </span>
          <span class="ann-title">{{ item.title }}</span>
        </div>
        <span class="ann-time">{{ item.createTime?.slice(5, 10) }}</span>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg class="icon-svg" viewBox="0 0 24 24"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
      </div>
      <div class="empty-title">暂无公告</div>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="selected?.title" width="560px" destroy-on-close append-to-body>
      <div class="ann-detail">
        <div class="ann-detail-meta">
          <span
            class="ann-tag"
            :class="selected?.priority === 2 ? 'red' : selected?.priority === 1 ? 'gold' : 'blue'"
          >
            {{ selected?.priority === 2 ? '重要' : selected?.priority === 1 ? '通知' : '公告' }}
          </span>
          <span class="ann-detail-time">{{ selected?.createTime }}</span>
        </div>
        <div class="ann-detail-content">{{ selected?.content }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  list: { type: Array, default: () => [] }
})

const dialogVisible = ref(false)
const selected = ref(null)

function openDetail(item) {
  selected.value = item
  dialogVisible.value = true
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
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.panel-header h3 { font-size: 16px; font-weight: 600; color: #0F172A; }
.panel-more { font-size: 12px; color: var(--color-military); cursor: pointer; }

.ann-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0; border-bottom: 1px solid #f0f2f5;
  cursor: pointer; transition: background 0.2s;
}
.ann-item:last-child { border-bottom: none; }
.ann-item:hover .ann-title { color: var(--color-military); }
.ann-left { display: flex; align-items: center; gap: 10px; min-width: 0; flex: 1; }
.ann-tag { font-size: 10px; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; }
.ann-tag.blue { background: #eff6ff; color: #2563EB; }
.ann-tag.gold { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
.ann-tag.red { background: #fef2f2; color: #e74c3c; }
.ann-title {
  font-size: 13px; color: #303133;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  transition: color 0.2s;
}
.ann-time { font-size: 12px; color: #c0c4cc; flex-shrink: 0; margin-left: 12px; }

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

/* 详情 */
.ann-detail-meta {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.ann-detail-time { font-size: 13px; color: #909399; }
.ann-detail-content { font-size: 14px; color: #303133; line-height: 1.8; white-space: pre-wrap; }

.icon-svg {
  width: 22px; height: 22px; fill: none; stroke: currentColor;
  stroke-width: 2; stroke-linecap: round; stroke-linejoin: round;
}
</style>
