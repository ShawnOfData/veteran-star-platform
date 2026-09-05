<!--
  MyFavorites.vue — 我的收藏
  收藏列表 + 取消收藏 + 跳转详情
-->
<template>
  <div class="my-fav-page">
    <van-nav-bar title="我的收藏" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div
          v-for="item in list"
          :key="item.id"
          class="fav-card"
          @click="goDetail(item.opportunityId || item.id)"
        >
          <div v-if="item.coverUrl" class="card-cover">
            <img :src="item.coverUrl" alt="封面" />
          </div>
          <div v-else class="card-cover card-cover-placeholder">
            <van-icon name="photo-o" size="28" color="#c8c9cc" />
            <span>暂无封面</span>
          </div>
          <div class="card-top">
            <h3 class="card-title">{{ item.title || item.opportunityTitle }}</h3>
            <van-icon
              name="star"
              color="#ffd21e"
              size="20"
              @click.stop="handleUnfavorite(item)"
            />
          </div>
          <div class="card-info">
            <span v-if="item.company"><van-icon name="shop-o" />{{ item.company }}</span>
            <span v-if="item.salary" class="salary">{{ item.salary }}</span>
          </div>
          <div class="card-bottom">
            <span v-if="item.location" class="footer-item"><van-icon name="location-o" />{{ item.location }}</span>
            <span class="footer-time">{{ formatTime(item.favoriteTime || item.publishTime) }}</span>
          </div>
        </div>
        <EmptyState v-if="!loading && !list.length" description="暂无收藏" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
/**
 * 我的收藏页
 * - 分页加载收藏列表
 * - 取消收藏
 * - 跳转详情
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getMyFavorites, unfavoriteOpportunity } from '@/api/studentApp'
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

async function onLoad() {
  try {
    const res = await getMyFavorites(store.studentId, { page: page.value, size: pageSize })
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

function goDetail(id) {
  if (id) router.push('/opportunities/' + id)
}

function handleUnfavorite(item) {
  showConfirmDialog({
    title: '取消收藏',
    message: `确定取消收藏「${item.title || item.opportunityTitle}」吗？`
  }).then(async () => {
    try {
      const oppId = item.opportunityId || item.id
      await unfavoriteOpportunity(store.studentId, oppId)
      list.value = list.value.filter(i => i !== item)
      showToast({ message: '已取消收藏', type: 'success' })
    } catch (e) { /* 拦截器处理 */ }
  }).catch(() => {})
}

function formatTime(t) { return t ? (t.length > 10 ? t.slice(0, 10) : t) : '-' }
</script>

<style scoped>
.my-fav-page { background: var(--color-bg-page); min-height: 100vh; }

.fav-card {
  background: #fff;
  margin: 8px 12px;
  border-radius: 12px;
  padding: 14px;
  box-shadow: var(--shadow-sm);
}
.card-cover {
  width: 100%;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
  background: #f7f8fa;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.card-cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #c8c9cc;
  font-size: 12px;
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
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.footer-time { margin-left: auto; }
</style>
