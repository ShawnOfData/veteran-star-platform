<!--
  Home.vue — 首页
  用户信息卡片 + 积分统计 + 快捷入口 + 最近申请
-->
<template>
  <div class="home-page page-with-tabbar">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-avatar">
        <van-icon name="user-o" size="32" color="#fff" />
      </div>
      <div class="user-info">
        <h2>{{ store.studentName }}</h2>
        <p>{{ store.studentNo }}</p>
      </div>
      <div class="user-rank" v-if="homeData">
        <span class="rank-num">{{ homeData.rank || '-' }}</span>
        <span class="rank-label">排名</span>
      </div>
    </div>

    <!-- 积分统计 -->
    <div class="stats-grid" v-if="homeData">
      <div class="stat-item" @click="router.push('/points')">
        <span class="stat-num">{{ homeData.totalPoints || 0 }}</span>
        <span class="stat-label">总积分</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ homeData.serviceCount || 0 }}</span>
        <span class="stat-label">服务次数</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ homeData.honorCount || 0 }}</span>
        <span class="stat-label">荣誉数</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ homeData.certCount || 0 }}</span>
        <span class="stat-label">证书数</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">快捷入口</span>
      </div>
      <van-grid :column-num="4" :border="false" square>
        <van-grid-item icon="medal-o" text="积分" @click="router.push('/points')" />
        <van-grid-item icon="chart-trending-o" text="排名" @click="router.push('/ranking')" />
        <van-grid-item icon="records" text="简历" @click="router.push('/resume')" />
        <van-grid-item icon="browsing-history-o" text="画像" @click="router.push('/portrait')" />
        <van-grid-item icon="star-o" text="机会" @click="router.push('/opportunities')" />
        <van-grid-item icon="orders-o" text="申请" @click="router.push('/applications')" />
        <van-grid-item icon="like-o" text="收藏" @click="router.push('/favorites')" />
        <van-grid-item icon="setting-o" text="设置" @click="router.push('/settings')" />
      </van-grid>
    </div>

    <!-- 最近申请 -->
    <div class="section">
      <div class="section-header">
        <span class="section-title">最近申请</span>
        <van-button plain hairline size="mini" @click="router.push('/applications')">更多</van-button>
      </div>
      <LoadingState v-if="loading" height="120px" />
      <EmptyState v-else-if="!homeData?.applications?.length" description="暂无申请记录" />
      <div v-else class="app-list">
        <div
          v-for="app in homeData.applications.slice(0, 3)"
          :key="app.id"
          class="app-item"
          @click="router.push('/opportunities/' + app.opportunityId)"
        >
          <div class="app-info">
            <p class="app-title">{{ app.opportunityTitle || '未知机会' }}</p>
            <p class="app-date">{{ app.applyTime || '-' }}</p>
          </div>
          <van-tag :type="statusTagType(app.status)" size="medium">
            {{ statusLabel(app.status) }}
          </van-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 首页
 * 调用 getStudentHome 获取首页数据
 * 展示用户信息、积分统计、快捷入口、最近申请
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getStudentHome } from '@/api/studentApp'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()
const store = useStudentStore()
const homeData = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getStudentHome(store.studentId)
    homeData.value = res.data
  } catch (e) {
    // 拦截器已处理错误
  } finally {
    loading.value = false
  }
})

/** 申请状态标签类型 */
function statusTagType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'default'
}

/** 申请状态文字 */
function statusLabel(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}
</script>

<style scoped>
.home-page {
  background: var(--color-bg-page);
  padding-bottom: 60px;
}

/* 用户卡片 */
.user-card {
  background: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  padding: 20px 16px;
  display: flex;
  align-items: center;
  color: #fff;
}
.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}
.user-info {
  flex: 1;
}
.user-info h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}
.user-info p {
  font-size: 13px;
  opacity: 0.8;
}
.user-rank {
  text-align: center;
}
.rank-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #c4a35a;
}
.rank-label {
  font-size: 12px;
  opacity: 0.8;
}

/* 积分统计 */
.stats-grid {
  display: flex;
  background: #fff;
  margin: -12px 12px 0;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 1;
}
.stat-item {
  flex: 1;
  text-align: center;
}
.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}
.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 通用 section */
.section {
  margin-top: 16px;
  background: #fff;
  padding: 12px 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 8px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

/* 申请列表 */
.app-list {
  padding: 0 16px;
}
.app-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.app-item:last-child {
  border-bottom: none;
}
.app-title {
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.app-date {
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
