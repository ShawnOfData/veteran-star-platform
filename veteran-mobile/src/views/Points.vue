<!--
  Points.vue — 积分明细
  积分总览卡片 + 明细列表（下拉刷新+上拉加载）
-->
<template>
  <div class="points-page">
    <van-nav-bar title="积分明细" left-arrow @click-left="$router.back()" />

    <!-- 积分总览 -->
    <div class="points-summary">
      <div class="summary-circle">
        <span class="summary-num">{{ totalPoints }}</span>
        <span class="summary-label">总积分</span>
      </div>
      <div class="summary-stats">
        <div class="stat"><span class="stat-num">{{ serviceCount }}</span><span class="stat-label">服务次数</span></div>
        <div class="stat"><span class="stat-num">{{ honorCount }}</span><span class="stat-label">荣誉数</span></div>
      </div>
    </div>

    <!-- 明细列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <van-cell
          v-for="item in list"
          :key="item.id"
          :title="item.reason"
          :label="item.createTime"
        >
          <template #value>
            <span :class="item.points > 0 ? 'points-add' : 'points-sub'">
              {{ item.points > 0 ? '+' : '' }}{{ item.points }}
            </span>
          </template>
        </van-cell>
        <EmptyState v-if="!loading && !list.length" description="暂无积分记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
/**
 * 积分明细页
 * 使用 Vant PullRefresh + List 实现下拉刷新和上拉加载
 */
import { ref, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getPointsDetail } from '@/api/studentApp'
import EmptyState from '@/components/EmptyState.vue'

const store = useStudentStore()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 20

const totalPoints = ref(0)
const serviceCount = ref(0)
const honorCount = ref(0)

onMounted(() => {
  // 首次加载积分总览
  fetchSummary()
})

async function fetchSummary() {
  try {
    const res = await getPointsDetail(store.studentId, { page: 1, size: 1 })
    // 从响应中提取总积分等信息
    if (res.data) {
      totalPoints.value = res.data.totalPoints || res.data.total || 0
      serviceCount.value = res.data.serviceCount || 0
      honorCount.value = res.data.honorCount || 0
    }
  } catch (e) { /* 拦截器处理 */ }
}

async function onLoad() {
  try {
    const res = await getPointsDetail(store.studentId, { page: page.value, size: pageSize })
    const records = res.data?.records || res.data?.list || res.data || []
    if (Array.isArray(records)) {
      list.value.push(...records)
    }
    if (list.value.length >= (res.data?.total || 0) || records.length < pageSize) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
}
</script>

<style scoped>
.points-page { background: var(--color-bg-page); min-height: 100vh; }

/* 积分总览 */
.points-summary {
  background: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  padding: 20px 16px;
  display: flex;
  align-items: center;
  color: #fff;
}
.summary-circle {
  text-align: center;
  margin-right: 24px;
}
.summary-num {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #c4a35a;
}
.summary-label {
  font-size: 12px;
  opacity: 0.8;
}
.summary-stats {
  flex: 1;
  display: flex;
}
.summary-stats .stat {
  flex: 1;
  text-align: center;
}
.summary-stats .stat-num {
  display: block;
  font-size: 18px;
  font-weight: 600;
}
.summary-stats .stat-label {
  font-size: 12px;
  opacity: 0.8;
}

/* 积分明细 */
.points-add { color: var(--color-success); font-weight: 600; }
.points-sub { color: var(--color-danger); font-weight: 600; }
</style>
