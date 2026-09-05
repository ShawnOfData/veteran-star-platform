<template>
  <div class="page-container">
    <div class="page-header">
      <h2>机会广场</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索机会" clearable style="width: 200px" />
      <el-select v-model="query.type" placeholder="机会类型" clearable style="width: 140px">
        <el-option label="就业岗位" value="job" />
        <el-option label="实习机会" value="intern" />
        <el-option label="创业扶持" value="startup" />
        <el-option label="培训课程" value="training" />
        <el-option label="政策福利" value="policy" />
      </el-select>
      <el-select v-model="query.salaryRange" placeholder="薪资范围" clearable style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="3k以下" value="below3k" />
        <el-option label="3k-6k" value="3k-6k" />
        <el-option label="6k-10k" value="6k-10k" />
        <el-option label="10k-15k" value="10k-15k" />
        <el-option label="15k以上" value="above15k" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <div class="opp-grid" v-loading="loading">
      <div v-for="item in tableData" :key="item.id" class="opp-card" @click="openDetail(item)">
        <div class="fav-btn" :class="{ active: favorited && currentOpp?.id === item.id }" @click.stop="toggleFavorite(item)">
          <el-icon><StarFilled v-if="item.favorited" /><Star v-else /></el-icon>
        </div>

        <div v-if="item.coverUrl" class="opp-cover">
          <img :src="item.coverUrl" alt="封面" />
        </div>
        <div v-else class="opp-cover opp-cover-placeholder">
          <span>暂无封面</span>
        </div>

        <div class="opp-body">
          <div class="opp-header-row">
            <el-tag size="small" :type="typeTag(item.type)">{{ typeLabel(item.type) }}</el-tag>
            <span v-if="item.salaryRange" class="salary-tag">{{ salaryLabel(item.salaryRange) }}</span>
          </div>

          <h4 class="opp-title" :title="item.title">{{ item.title }}</h4>

          <div class="opp-meta">
            <el-icon size="14"><User /></el-icon>
            <span>{{ item.unitName || item.organization || '—' }}</span>
            <el-icon size="14" style="margin-left: 8px"><Location /></el-icon>
            <span>{{ item.address || '—' }}</span>
          </div>

          <div class="opp-tags" v-if="item.tags && item.tags.length">
            <el-tag v-for="tag in item.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>

          <div class="opp-footer">
            <div class="opp-stats">
              <span class="opp-stat">
                <el-icon><View /></el-icon> {{ item.viewCount || 0 }}
              </span>
              <span class="opp-stat">
                <el-icon><User /></el-icon> {{ item.applyCount || 0 }}
              </span>
            </div>
            <span class="opp-deadline" v-if="item.deadline">
              <el-icon size="12"><Timer /></el-icon> 截止 {{ item.deadline }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && tableData.length === 0" description="暂无机会" />

    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[12, 24, 48]"
      layout="total, prev, pager, next"
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 20px; justify-content: center;"
    />

    <el-dialog v-model="detailVisible" :title="currentOpp?.title" width="640px" destroy-on-close top="5vh">
      <template v-if="currentOpp">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="类型">
            <el-tag size="small" :type="typeTag(currentOpp.type)">{{ typeLabel(currentOpp.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布单位">{{ currentOpp.unitName || currentOpp.organization || '—' }}</el-descriptions-item>
          <el-descriptions-item label="薪资待遇">{{ currentOpp.salaryRange ? salaryLabel(currentOpp.salaryRange) : '面议' }}</el-descriptions-item>
          <el-descriptions-item label="所在地区">{{ currentOpp.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentOpp.contact || '—' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentOpp.deadline || '长期有效' }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentOpp.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="报名人数">{{ currentOpp.applyCount || 0 }} 人</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>详细描述</h4>
          <p v-if="currentOpp.description" style="white-space: pre-wrap;">{{ currentOpp.description }}</p>
          <p v-else class="text-muted">暂无描述</p>
        </div>

        <div class="detail-section" v-if="currentOpp.requirements">
          <h4>报名要求</h4>
          <p style="white-space: pre-wrap;">{{ currentOpp.requirements }}</p>
        </div>

        <div class="detail-section" v-if="currentOpp.tags && currentOpp.tags.length">
          <h4>标签</h4>
          <div class="opp-tags">
            <el-tag v-for="tag in currentOpp.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          :type="favorited ? 'warning' : 'default'"
          :loading="favoriting"
          @click="toggleFavorite(currentOpp)"
        >
          {{ favorited ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button type="primary" @click="handleApply" :disabled="applied">
          {{ applied ? '已报名' : '立即报名' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, View, User, Location, Timer } from '@element-plus/icons-vue'
import {
  getOpportunityList,
  applyOpportunity,
  favoriteOpportunity,
  unfavoriteOpportunity,
  incrementViewCount
} from '@/api/studentApp'

const studentStore = useStudentStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentOpp = ref(null)
const applied = ref(false)
const favorited = ref(false)
const favoriting = ref(false)

const query = reactive({
  keyword: '',
  type: '',
  salaryRange: '',
  page: 1,
  size: 12
})

function typeLabel(type) {
  const map = { job: '就业岗位', intern: '实习机会', startup: '创业扶持', training: '培训课程', policy: '政策福利' }
  return map[type] || type
}

function typeTag(type) {
  const map = { job: 'success', intern: '', startup: 'warning', training: 'info', policy: 'danger' }
  return map[type] || 'info'
}

function salaryLabel(range) {
  const map = { below3k: '3k以下', '3k-6k': '3k-6k', '6k-10k': '6k-10k', '10k-15k': '10k-15k', above15k: '15k以上' }
  return map[range] || range
}

async function fetchData() {
  loading.value = true
  try {
    // eslint-disable-next-line no-unused-vars
    const { salaryRange, ...params } = query
    const res = await getOpportunityList({ ...params, studentId: studentStore.studentId })
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records.map(item => ({
        ...item,
        favorited: item.isFavorited || false
      }))
      total.value = data.total || 0
    } else if (Array.isArray(data)) {
      tableData.value = data.map(item => ({ ...item, favorited: item.isFavorited || false }))
      total.value = data.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取机会列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

async function openDetail(opp) {
  currentOpp.value = opp
  applied.value = false
  favorited.value = opp.favorited || false
  detailVisible.value = true

  try {
    await incrementViewCount(opp.id)
  } catch {
    // view count increment failure is non-critical
  }
}

async function toggleFavorite(opp) {
  if (favoriting.value) return
  favoriting.value = true
  try {
    if (opp.favorited) {
      await unfavoriteOpportunity(studentStore.studentId, opp.id)
      opp.favorited = false
      if (currentOpp.value?.id === opp.id) {
        favorited.value = false
      }
      ElMessage.success('已取消收藏')
    } else {
      await favoriteOpportunity(studentStore.studentId, opp.id)
      opp.favorited = true
      if (currentOpp.value?.id === opp.id) {
        favorited.value = true
      }
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error('操作失败：' + (e.message || '网络异常'))
  } finally {
    favoriting.value = false
  }
}

async function handleApply() {
  try {
    await applyOpportunity({
      opportunityId: currentOpp.value.id,
      studentId: studentStore.studentId,
      remark: ''
    })
    ElMessage.success('报名成功')
    applied.value = true
    if (currentOpp.value.applyCount !== undefined) {
      currentOpp.value.applyCount++
    }
  } catch (e) {
    ElMessage.error('报名失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.opp-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.opp-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.opp-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.opp-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
  display: block;
}

.opp-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.opp-cover-placeholder {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 14px;
}

.opp-body {
  padding: 20px;
}

.opp-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.salary-tag {
  font-size: 12px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.opp-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.opp-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.opp-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.opp-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.opp-stats {
  display: flex;
  gap: 16px;
}

.opp-stat {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.opp-deadline {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 2px;
}

.fav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1;
  font-size: 18px;
  transition: transform 0.2s;
  color: #c0c4cc;
}

.fav-btn:hover {
  transform: scale(1.15);
}

.fav-btn.active {
  color: #f56c6c;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ebeef5;
}

.detail-section p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
}

.text-muted {
  color: #c0c4cc !important;
}
</style>
