<!--
  OpportunityDetail.vue — 就业机会详情
  详情卡片 + 申请按钮 + 收藏
-->
<template>
  <div class="detail-page">
    <van-nav-bar title="机会详情" left-arrow @click-left="$router.back()" />

    <LoadingState v-if="loading" height="300px" />
    <template v-else-if="detail">
      <!-- 封面图 -->
      <div v-if="detail.coverUrl" class="detail-cover">
        <img :src="detail.coverUrl" alt="封面" />
      </div>

      <!-- 标题区 -->
      <div class="detail-header">
        <h1 class="detail-title">{{ detail.title }}</h1>
        <div class="detail-tags">
          <van-tag v-if="detail.type" plain type="primary">{{ typeLabel(detail.type) }}</van-tag>
          <van-tag v-if="detail.postType" plain type="success">{{ detail.postType }}</van-tag>
          <van-tag v-if="detail.jobType" plain type="warning">{{ detail.jobType }}</van-tag>
        </div>
      </div>

      <!-- 薪资 + 公司 -->
      <van-cell-group inset>
        <van-cell title="薪资" v-if="detail.salary">
          <template #value><span class="salary-text">{{ detail.salary }}</span></template>
        </van-cell>
        <van-cell title="公司" :value="detail.company || '-'" v-if="detail.company" />
        <van-cell title="地点" :value="detail.location || '-'" v-if="detail.location" />
        <van-cell title="联系人" :value="detail.contactPerson || '-'" v-if="detail.contactPerson" />
        <van-cell title="联系方式" :value="detail.contact || '-'" v-if="detail.contact" />
        <van-cell title="浏览量" :value="(detail.viewCount || 0) + '次'" />
        <van-cell title="发布时间" :value="formatTime(detail.publishTime)" v-if="detail.publishTime" />
      </van-cell-group>

      <!-- 职位描述 -->
      <div class="desc-section">
        <h3 class="desc-title">职位描述</h3>
        <p class="desc-content">{{ detail.description || '暂无描述' }}</p>
      </div>
    </template>
    <EmptyState v-else description="机会不存在或已下架" />

    <!-- 底部操作栏 -->
    <van-action-bar v-if="detail">
      <van-action-bar-icon icon="star-o" text="收藏" @click="toggleFavorite" v-if="!favorited" />
      <van-action-bar-icon icon="star" text="已收藏" color="#ffd21e" @click="toggleFavorite" v-else />
      <van-action-bar-icon icon="phone-o" text="联系" @click="callContact" v-if="detail?.contact" />
      <van-action-bar-button type="primary" text="立即申请" @click="handleApply" :disabled="applied" />
    </van-action-bar>
  </div>
</template>

<script setup>
/**
 * 机会详情页
 * - 获取详情数据
 * - 增加浏览数
 * - 收藏/取消收藏
 * - 立即申请
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getOpportunityById, incrementViewCount, applyOpportunity, favoriteOpportunity, unfavoriteOpportunity } from '@/api/studentApp'
import { showToast, showConfirmDialog } from 'vant'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const store = useStudentStore()
const detail = ref(null)
const loading = ref(true)
const favorited = ref(false)
const applied = ref(false)

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getOpportunityById(id)
    detail.value = res.data
    if (detail.value) {
      favorited.value = detail.value.favorited || false
      applied.value = detail.value.applied || false
      // 异步增加浏览数
      incrementViewCount(id).catch(() => {})
    }
  } catch (e) { /* 拦截器处理 */ }
  finally { loading.value = false }
})

/** 收藏/取消收藏 */
async function toggleFavorite() {
  try {
    if (favorited.value) {
      await unfavoriteOpportunity(store.studentId, detail.value.id)
      favorited.value = false
      showToast('已取消收藏')
    } else {
      await favoriteOpportunity(store.studentId, detail.value.id)
      favorited.value = true
      showToast({ message: '收藏成功', type: 'success' })
    }
  } catch (e) { /* 拦截器处理 */ }
}

/** 申请机会 */
function handleApply() {
  showConfirmDialog({
    title: '确认申请',
    message: `确定要申请「${detail.value.title}」吗？`
  }).then(async () => {
    try {
      await applyOpportunity({
        studentId: store.studentId,
        opportunityId: detail.value.id
      })
      applied.value = true
      showToast({ message: '申请成功', type: 'success' })
    } catch (e) { /* 拦截器处理 */ }
  }).catch(() => {})
}

/** 拨打电话 */
function callContact() {
  if (detail.value?.contact) {
    window.location.href = 'tel:' + detail.value.contact
  }
}

function typeLabel(type) {
  return { 1: '全职', 2: '兼职', 3: '实习', 4: '校园招聘' }[type] || '其他'
}
function formatTime(time) {
  return time ? (time.length > 10 ? time.slice(0, 10) : time) : '-'
}
</script>

<style scoped>
.detail-page {
  background: var(--color-bg-page);
  min-height: 100vh;
  padding-bottom: 60px;
}

.detail-cover {
  width: 100%;
  height: 200px;
  background: #f7f8fa;
}
.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.detail-header {
  background: #fff;
  padding: 16px;
  margin-bottom: 8px;
}
.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}
.detail-tags {
  display: flex;
  gap: 4px;
}
.salary-text {
  color: var(--color-danger);
  font-weight: 600;
  font-size: 16px;
}

.desc-section {
  background: #fff;
  margin-top: 8px;
  padding: 16px;
}
.desc-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
.desc-content {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
