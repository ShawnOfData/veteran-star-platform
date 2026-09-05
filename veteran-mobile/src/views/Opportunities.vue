<!--
  Opportunities.vue — 就业机会列表
  搜索 + 筛选 + 卡片列表 + 下拉刷新/上拉加载 + 收藏
-->
<template>
  <div class="opportunities-page page-with-tabbar">
    <!-- 搜索栏 -->
    <van-sticky>
      <van-search
        v-model="keyword"
        placeholder="搜索职位、公司"
        shape="round"
        @search="onSearch"
      >
        <template #action>
          <span @click="showFilter = true">筛选</span>
        </template>
      </van-search>
    </van-sticky>

    <!-- 机会列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          v-for="item in list"
          :key="item.id"
          class="opp-card"
          @click="goDetail(item.id)"
        >
          <!-- 封面图 -->
          <div v-if="item.coverUrl" class="card-cover">
            <img :src="item.coverUrl" alt="封面" />
          </div>
          <div v-else class="card-cover card-cover-placeholder">
            <van-icon name="photo-o" size="28" color="#c8c9cc" />
            <span>暂无封面</span>
          </div>

          <!-- 卡片头部 -->
          <div class="card-header">
            <h3 class="card-title">{{ item.title }}</h3>
            <van-icon
              :name="item.favorited ? 'star' : 'star-o'"
              :color="item.favorited ? '#ffd21e' : '#969799'"
              size="20"
              @click.stop="toggleFavorite(item)"
            />
          </div>

          <!-- 标签行 -->
          <div class="card-tags">
            <van-tag v-if="item.type" plain type="primary" size="medium">{{ typeLabel(item.type) }}</van-tag>
            <van-tag v-if="item.postType" plain type="success" size="medium">{{ item.postType }}</van-tag>
            <van-tag v-if="item.jobType" plain type="warning" size="medium">{{ item.jobType }}</van-tag>
          </div>

          <!-- 公司 + 薪资 -->
          <div class="card-info">
            <span class="company" v-if="item.company">
              <van-icon name="shop-o" />{{ item.company }}
            </span>
            <span class="salary" v-if="item.salary">{{ item.salary }}</span>
          </div>

          <!-- 底部信息 -->
          <div class="card-footer">
            <span v-if="item.location" class="footer-item">
              <van-icon name="location-o" />{{ item.location }}
            </span>
            <span class="footer-item">
              <van-icon name="eye-o" />{{ item.viewCount || 0 }}
            </span>
            <span class="footer-time">{{ formatTime(item.publishTime) }}</span>
          </div>
        </div>

        <EmptyState v-if="!loading && !list.length" description="暂无就业机会" />
      </van-list>
    </van-pull-refresh>

    <!-- 筛选弹窗 -->
    <van-popup
      v-model:show="showFilter"
      position="right"
      :style="{ width: '80%', height: '100%' }"
    >
      <div class="filter-panel">
        <van-nav-bar title="筛选条件" />
        <van-cell-group>
          <van-field label="职位类型" is-link readonly :model-value="filterPostType || '不限'" @click="showPostTypePicker = true" />
          <van-field label="工作类型" is-link readonly :model-value="filterJobType || '不限'" @click="showJobTypePicker = true" />
        </van-cell-group>
        <div class="filter-actions">
          <van-button block plain style="margin-bottom: 8px" @click="resetFilter">重置</van-button>
          <van-button block type="primary" @click="applyFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 筛选选择器 -->
    <van-popup v-model:show="showPostTypePicker" position="bottom" round>
      <van-picker
        :columns="postTypeColumns"
        @confirm="(e) => { filterPostType = e.selectedValues[0] || ''; showPostTypePicker = false }"
        @cancel="showPostTypePicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showJobTypePicker" position="bottom" round>
      <van-picker
        :columns="jobTypeColumns"
        @confirm="(e) => { filterJobType = e.selectedValues[0] || ''; showJobTypePicker = false }"
        @cancel="showJobTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
/**
 * 就业机会列表页
 * - 搜索关键词
 * - 筛选（职位类型、工作类型）
 * - 下拉刷新 + 上拉加载
 * - 收藏/取消收藏
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getOpportunityList, favoriteOpportunity, unfavoriteOpportunity, getDictPostType, getDictJobType } from '@/api/studentApp'
import { showToast } from 'vant'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const store = useStudentStore()

// 列表数据
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10

// 搜索/筛选
const keyword = ref('')
const showFilter = ref(false)
const filterPostType = ref('')
const filterJobType = ref('')
const showPostTypePicker = ref(false)
const showJobTypePicker = ref(false)

// 字典
const postTypeDict = ref([])
const jobTypeDict = ref([])
const postTypeColumns = ref([])
const jobTypeColumns = ref([])

onMounted(async () => {
  try {
    const [pt, jt] = await Promise.all([getDictPostType(), getDictJobType()])
    postTypeDict.value = pt.data || []
    jobTypeDict.value = jt.data || []
    postTypeColumns.value = [postTypeDict.value.map(d => d.name || d.value || d)]
    jobTypeColumns.value = [jobTypeDict.value.map(d => d.name || d.value || d)]
  } catch (e) { /* 字典加载失败不影响主流程 */ }
})

/** 加载列表 */
async function onLoad() {
  try {
    const res = await getOpportunityList({
      page: page.value,
      size: pageSize,
      keyword: keyword.value || undefined,
      postType: filterPostType.value || undefined,
      jobType: filterJobType.value || undefined,
      studentId: store.studentId
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

/** 下拉刷新 */
function onRefresh() {
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
}

/** 搜索 */
function onSearch() {
  list.value = []
  page.value = 1
  finished.value = false
  onLoad()
}

/** 应用筛选 */
function applyFilter() {
  showFilter.value = false
  onSearch()
}

/** 重置筛选 */
function resetFilter() {
  filterPostType.value = ''
  filterJobType.value = ''
  showFilter.value = false
  onSearch()
}

/** 跳转详情 */
function goDetail(id) {
  router.push('/opportunities/' + id)
}

/** 收藏/取消收藏 */
async function toggleFavorite(item) {
  try {
    if (item.favorited) {
      await unfavoriteOpportunity(store.studentId, item.id)
      item.favorited = false
      showToast('已取消收藏')
    } else {
      await favoriteOpportunity(store.studentId, item.id)
      item.favorited = true
      showToast({ message: '收藏成功', type: 'success' })
    }
  } catch (e) { /* 拦截器处理 */ }
}

/** 机会类型标签 */
function typeLabel(type) {
  const map = { 1: '全职', 2: '兼职', 3: '实习', 4: '校园招聘' }
  return map[type] || '其他'
}

/** 格式化时间 */
function formatTime(time) {
  if (!time) return ''
  return time.length > 10 ? time.slice(0, 10) : time
}
</script>

<style scoped>
.opportunities-page {
  background: var(--color-bg-page);
  padding-bottom: 60px;
  min-height: 100vh;
}

/* 机会卡片 */
.opp-card {
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  flex: 1;
  margin-right: 8px;
  line-height: 1.4;
}
.card-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.card-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.company {
  font-size: 13px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.salary {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-danger);
}
.card-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
.footer-item {
  display: flex;
  align-items: center;
  gap: 2px;
}
.footer-time {
  margin-left: auto;
}

/* 筛选面板 */
.filter-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.filter-actions {
  padding: 16px;
  margin-top: auto;
}
</style>
