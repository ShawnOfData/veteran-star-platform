<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>

    <div class="opp-grid" v-loading="loading">
      <div v-for="item in tableData" :key="item.id" class="opp-card" @click="openDetail(item)">
        <div class="fav-btn active" @click.stop="handleUnfavorite(item, $event)">
          <el-icon><StarFilled /></el-icon>
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
            <span>{{ item.unitName || '—' }}</span>
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
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && tableData.length === 0" description="暂无收藏机会" />

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
          <el-descriptions-item label="发布单位">{{ currentOpp.unitName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="薪资待遇">{{ currentOpp.salaryRange ? salaryLabel(currentOpp.salaryRange) : '面议' }}</el-descriptions-item>
          <el-descriptions-item label="所在地区">{{ currentOpp.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentOpp.contactName || currentOpp.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentOpp.endTime || currentOpp.deadline || '长期有效' }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentOpp.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="报名人数">{{ currentOpp.applyCount || 0 }} 人</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>详细描述</h4>
          <p v-if="currentOpp.description" v-html="currentOpp.description"></p>
          <p v-else class="text-muted">暂无描述</p>
        </div>

        <div class="detail-section" v-if="currentOpp.requirements || currentOpp.demandCount">
          <h4>报名要求</h4>
          <p v-if="currentOpp.requirements" v-html="currentOpp.requirements"></p>
          <p v-else>需求人数：{{ currentOpp.demandCount }} 人</p>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="danger" :loading="unfavoriting" @click="handleUnfavoriteFromDetail">
          <el-icon><StarFilled /></el-icon> 取消收藏
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import { StarFilled, View, User } from '@element-plus/icons-vue'
import { getMyFavorites, unfavoriteOpportunity, getOpportunityList } from '@/api/studentApp'

const studentStore = useStudentStore()

const query = reactive({
  page: 1,
  size: 12
})
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const currentOpp = ref(null)
const unfavoriting = ref(false)

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
    const res = await getMyFavorites(studentStore.studentId, query)
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total || 0
    } else if (Array.isArray(data)) {
      tableData.value = data
      total.value = data.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取收藏列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function openDetail(opp) {
  currentOpp.value = opp
  detailVisible.value = true
}

async function handleUnfavorite(opp, event) {
  if (unfavoriting.value) return
  unfavoriting.value = true
  try {
    await unfavoriteOpportunity(studentStore.studentId, opp.id)
    ElMessage.success('已取消收藏')
    const idx = tableData.value.findIndex(item => item.id === opp.id)
    if (idx !== -1) {
      tableData.value.splice(idx, 1)
      total.value--
    }
    if (currentOpp.value?.id === opp.id) {
      detailVisible.value = false
      currentOpp.value = null
    }
  } catch (e) {
    ElMessage.error('操作失败：' + (e.message || '网络异常'))
  } finally {
    unfavoriting.value = false
  }
}

async function handleUnfavoriteFromDetail() {
  if (currentOpp.value) {
    await handleUnfavorite(currentOpp.value, null)
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
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.opp-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
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
  padding: 16px;
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
