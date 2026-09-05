<!--
  MyApplications.vue — 我的申请
  状态筛选 + 申请列表 + 取消申请
-->
<template>
  <div class="my-apps-page">
    <van-nav-bar title="我的申请" left-arrow @click-left="$router.back()" />

    <!-- 状态筛选 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部" />
      <van-tab title="待审核" />
      <van-tab title="已通过" />
      <van-tab title="已拒绝" />
    </van-tabs>

    <!-- 申请列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div
          v-for="item in list"
          :key="item.id"
          class="app-card"
          @click="goDetail(item.opportunityId)"
        >
          <div class="card-top">
            <h3 class="card-title">{{ item.opportunityTitle || '未知机会' }}</h3>
            <van-tag :type="statusType(item.status)" size="medium">{{ statusLabel(item.status) }}</van-tag>
          </div>
          <div class="card-info">
            <span v-if="item.company"><van-icon name="shop-o" />{{ item.company }}</span>
            <span v-if="item.salary" class="salary">{{ item.salary }}</span>
          </div>
          <div class="card-bottom">
            <span class="apply-time">申请时间：{{ formatTime(item.applyTime) }}</span>
            <van-button
              v-if="item.status === 0"
              plain
              hairline
              size="mini"
              type="danger"
              @click.stop="handleCancel(item)"
            >取消申请</van-button>
          </div>
        </div>
        <EmptyState v-if="!loading && !list.length" description="暂无申请记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
/**
 * 我的申请页
 * - 状态筛选（全部/待审核/已通过/已拒绝）
 * - 分页加载
 * - 取消申请
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getMyApplications, cancelApplication } from '@/api/studentApp'
import { showToast, showConfirmDialog } from 'vant'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const store = useStudentStore()
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const activeTab = ref(0)

const statusMap = [null, 0, 1, 2] // tab索引到status的映射

async function onLoad() {
  try {
    const status = statusMap[activeTab.value]
    const res = await getMyApplications(store.studentId, {
      page: page.value,
      size: pageSize,
      status: status !== null ? status : undefined
    })
    const records = res.data?.records || res.data?.list || []
    list.value.push(...records)
    const total = res.data?.total || 0
    if (list.value.length >= total || records.length < pageSize) {
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

function onTabChange() {
  onRefresh()
}

function goDetail(id) {
  if (id) router.push('/opportunities/' + id)
}

function handleCancel(item) {
  showConfirmDialog({
    title: '取消申请',
    message: `确定取消「${item.opportunityTitle}」的申请吗？`
  }).then(async () => {
    try {
      await cancelApplication(item.id, store.studentId)
      showToast({ message: '已取消', type: 'success' })
      onRefresh()
    } catch (e) { /* 拦截器处理 */ }
  }).catch(() => {})
}

function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'default' }
function statusLabel(s) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知' }
function formatTime(t) { return t ? (t.length > 10 ? t.slice(0, 10) : t) : '-' }
</script>

<style scoped>
.my-apps-page { background: var(--color-bg-page); min-height: 100vh; }

.app-card {
  background: #fff;
  margin: 8px 12px;
  border-radius: 12px;
  padding: 14px;
  box-shadow: var(--shadow-sm);
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  flex: 1;
  margin-right: 8px;
}
.card-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.salary { color: var(--color-danger); font-weight: 600; }
.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.apply-time { font-size: 12px; color: var(--color-text-secondary); }
</style>
