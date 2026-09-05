<template>
  <div class="page-container">
    <div class="rank-container">
      <!-- 顶部工具栏 -->
      <div class="header-actions">
        <h2 class="page-title">积分排行榜</h2>
        <div class="filter-group">
          <el-select v-model="filter.college" placeholder="按学院筛选" clearable size="small" style="width: 160px">
            <el-option label="计算机学院" value="计算机学院" />
            <el-option label="电子信息学院" value="电子信息学院" />
            <el-option label="机械工程学院" value="机械工程学院" />
            <el-option label="经济管理学院" value="经济管理学院" />
          </el-select>
          <el-select v-model="filter.grade" placeholder="按年级筛选" clearable size="small" style="width: 140px">
            <el-option label="2022级" value="2022" />
            <el-option label="2023级" value="2023" />
            <el-option label="2024级" value="2024" />
            <el-option label="2025级" value="2025" />
          </el-select>
          <button class="btn-search" @click="fetchData">查询</button>
        </div>
      </div>

      <!-- 前三名领奖台 -->
      <div class="podium-container" v-if="rankingData.length >= 3">
        <div class="podium-card second-place" @click="openDetail(rankingData[1])">
          <div class="badge silver">2</div>
          <div class="avatar">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="user-name">{{ rankingData[1].name }}</div>
          <div class="user-dept">{{ rankingData[1].college }}</div>
          <div class="score">{{ rankingData[1].totalPoints }} 分</div>
        </div>
        <div class="podium-card first-place" @click="openDetail(rankingData[0])">
          <div class="badge gold">1</div>
          <div class="avatar crown">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <div class="user-name">{{ rankingData[0].name }}</div>
          <div class="user-dept">{{ rankingData[0].college }}</div>
          <div class="score">{{ rankingData[0].totalPoints }} 分</div>
        </div>
        <div class="podium-card third-place" @click="openDetail(rankingData[2])">
          <div class="badge bronze">3</div>
          <div class="avatar">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="user-name">{{ rankingData[2].name }}</div>
          <div class="user-dept">{{ rankingData[2].college }}</div>
          <div class="score">{{ rankingData[2].totalPoints }} 分</div>
        </div>
      </div>

      <!-- 我的排名 -->
      <div class="my-rank-card" v-if="myRank">
        <div class="my-rank-left">
          <span class="my-rank-badge">#{{ myRank.rank }}</span>
          <span class="my-rank-name">{{ myRank.name }}</span>
        </div>
        <span class="my-rank-score">{{ myRank.totalPoints }} 分</span>
      </div>

      <!-- 排行榜表格 -->
      <div class="table-wrapper">
        <el-table :data="rankingData" stripe v-loading="loading" @row-click="openDetail" class="rank-table">
          <el-table-column label="排名" width="72" align="center">
            <template #default="{ row, $index }">
              <span class="rank-num" :class="'rank-' + ($index + 1)">{{ $index + 1 + (query.page - 1) * query.size }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="90" />
          <el-table-column prop="studentNo" label="学号" width="130" />
          <el-table-column prop="college" label="学院" min-width="140" show-overflow-tooltip />
          <el-table-column prop="grade" label="年级" width="76" />
          <el-table-column prop="totalPoints" label="总积分" width="90" sortable>
            <template #default="{ row }">
              <strong>{{ row.totalPoints }}</strong>
            </template>
          </el-table-column>
          <el-table-column prop="servicePoints" label="服务积分" width="90" />
          <el-table-column prop="honorPoints" label="荣誉积分" width="90" />
          <el-table-column prop="certPoints" label="证书积分" width="90" />
          <el-table-column label="操作" width="72" fixed="right">
            <template #default="{ row }">
              <span class="action-link" @click.stop="openDetail(row)">详情</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          size="small"
          class="rank-pagination"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>

      <!-- 积分详情弹窗 -->
      <el-dialog v-model="detailVisible" title="积分详情" width="500px" destroy-on-close>
        <template v-if="currentStudent">
          <div class="detail-header">
            <div class="detail-avatar">
              <el-icon :size="40"><UserFilled /></el-icon>
            </div>
            <div class="detail-info">
              <h3>{{ currentStudent.name }}</h3>
              <p>{{ currentStudent.studentNo }} | {{ currentStudent.college }} · {{ currentStudent.grade }}级</p>
            </div>
          </div>
          <div class="detail-points-grid">
            <div class="detail-points-item">
              <span class="dpi-label">总积分</span>
              <span class="dpi-value primary">{{ currentStudent.totalPoints }}</span>
            </div>
            <div class="detail-points-item">
              <span class="dpi-label">服务积分</span>
              <span class="dpi-value">{{ currentStudent.servicePoints }}</span>
            </div>
            <div class="detail-points-item">
              <span class="dpi-label">荣誉积分</span>
              <span class="dpi-value">{{ currentStudent.honorPoints }}</span>
            </div>
            <div class="detail-points-item">
              <span class="dpi-label">证书积分</span>
              <span class="dpi-value">{{ currentStudent.certPoints }}</span>
            </div>
          </div>
          <div ref="detailChartRef" class="detail-chart-box"></div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getRankingList, getStudentHome } from '@/api/studentApp'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const studentStore = useStudentStore()

const loading = ref(false)
const rankingData = ref([])
const total = ref(0)
const myRank = ref(null)
const detailVisible = ref(false)
const currentStudent = ref(null)
const detailChartRef = ref(null)
let detailChart = null

const query = reactive({ page: 1, size: 20 })
const filter = reactive({ college: '', grade: '' })

function openDetail(row) {
  currentStudent.value = row
  detailVisible.value = true
  nextTick(() => {
    initDetailChart()
  })
}

function initDetailChart() {
  if (!detailChartRef.value) return
  if (detailChart) detailChart.dispose()
  detailChart = echarts.init(detailChartRef.value)
  detailChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['服务积分', '荣誉积分', '证书积分'], bottom: 0, textStyle: { color: '#909399', fontSize: 12 } },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: ['2月', '3月', '4月', '5月'],
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      { name: '服务积分', type: 'bar', stack: 'total', data: [8, 16, 20, 8], itemStyle: { color: '#67c23a' }, barWidth: 24 },
      { name: '荣誉积分', type: 'bar', stack: 'total', data: [14, 0, 0, 12], itemStyle: { color: '#e6a23c' }, barWidth: 24 },
      { name: '证书积分', type: 'bar', stack: 'total', data: [0, 8, 0, 0], itemStyle: { color: '#409eff' }, barWidth: 24 }
    ]
  })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRankingList({ ...query, ...filter })
    rankingData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    rankingData.value = []
    total.value = 0
    ElMessage.error('获取排行榜失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }

  try {
    const homeRes = await getStudentHome(studentStore.studentId)
    if (homeRes.data) {
      myRank.value = {
        rank: homeRes.data.rank,
        name: studentStore.studentName || '当前用户',
        totalPoints: homeRes.data.totalPoints
      }
    }
  } catch (e) {
    ElMessage.error('获取个人排名失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* ===== 外层容器 ===== */
.rank-container {
  max-width: 1200px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  padding: 32px 36px;
}

/* ===== 顶部工具栏 ===== */
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-group :deep(.el-select) {
  --el-select-border-color-hover: #2563EB;
}
.filter-group :deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: none;
}
.filter-group :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #2563EB;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.btn-search {
  background-image: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  color: #fff;
  border: none;
  padding: 8px 22px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
  line-height: 1;
  height: 32px;
}
.btn-search:hover {
  background-image: linear-gradient(135deg, #2563EB 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.35);
  opacity: 0.95;
}
.btn-search:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
}

/* ===== 前三名领奖台 ===== */
.podium-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
  align-items: end;
}

.podium-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.podium-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(37, 99, 235, 0.12);
}

.first-place {
  border: 2px solid #ffd700;
  padding: 42px 20px;
  background: linear-gradient(to bottom, #ffffff, #fffdf0);
}
.first-place:hover {
  border-color: #f0d000;
}

/* 徽章 */
.badge {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  margin-bottom: 14px;
  font-size: 15px;
}
.badge.gold   { background: #ffd700; box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4); }
.badge.silver { background: #c0c0c0; box-shadow: 0 4px 12px rgba(192, 192, 192, 0.35); }
.badge.bronze { background: #cd7f32; box-shadow: 0 4px 12px rgba(205, 127, 50, 0.35); }

/* 头像 */
.avatar {
  width: 56px;
  height: 56px;
  background: #f0f2f5;
  border-radius: 50%;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.avatar.crown {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #fef9f0, #fdf5e6);
  border: 2px solid #ffd700;
  color: #ffd700;
}

.user-name {
  font-size: 16px;
  color: #0F172A;
  font-weight: 700;
  margin-bottom: 4px;
}
.user-dept {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}
.score {
  font-size: 18px;
  font-weight: 700;
  color: #2563EB;
}

/* ===== 我的排名 ===== */
.my-rank-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 22px;
  background: linear-gradient(135deg, #f0f7ff, #e8f4fd);
  border-radius: 12px;
  border: 1px solid #d0e3f7;
  margin-bottom: 24px;
}
.my-rank-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.my-rank-badge {
  font-size: 18px;
  font-weight: 700;
  color: #2563EB;
}
.my-rank-name {
  font-size: 14px;
  color: #0F172A;
  font-weight: 500;
}
.my-rank-score {
  font-size: 18px;
  font-weight: 700;
  color: #2563EB;
}

/* ===== 表格 ===== */
.table-wrapper {
  overflow-x: auto;
}

.rank-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: #fafafa;
  font-size: 14px;
}
.rank-table :deep(th.el-table__cell) {
  background: #fafafa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 8px;
  border-bottom: 2px solid #e4e7ed !important;
}
.rank-table :deep(td.el-table__cell) {
  padding: 14px 8px;
  border-bottom: 1px solid #f0f0f0;
  color: #444;
}
.rank-table :deep(.el-table__row:hover td) {
  background: #fafdff !important;
}

/* 排名数字 */
.rank-num {
  font-weight: 700;
  display: inline-block;
  width: 28px;
  text-align: center;
  font-size: 14px;
  color: #606266;
}
.rank-num.rank-1 { color: #ffd700; font-size: 16px; }
.rank-num.rank-2 { color: #c0c0c0; font-size: 15px; }
.rank-num.rank-3 { color: #cd7f32; font-size: 15px; }

.rank-table :deep(.el-table__body tr:nth-child(1) .rank-num) { color: #ffd700; font-size: 16px; }
.rank-table :deep(.el-table__body tr:nth-child(2) .rank-num) { color: #c0c0c0; font-size: 15px; }
.rank-table :deep(.el-table__body tr:nth-child(3) .rank-num) { color: #cd7f32; font-size: 15px; }

.action-link {
  color: #2563EB;
  text-decoration: none;
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}
.action-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.rank-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

/* ===== 详情弹窗 ===== */
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.detail-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #0F172A;
}
.detail-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.detail-points-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.detail-points-item {
  text-align: center;
  padding: 14px 8px;
  background: #fafafa;
  border-radius: 10px;
}
.dpi-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.dpi-value {
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
}
.dpi-value.primary { color: #2563EB; }

.detail-chart-box {
  width: 100%;
  height: 200px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .rank-container { padding: 20px; }
  .podium-container { grid-template-columns: 1fr; }
  .first-place { padding: 28px 20px; order: -1; }
  .header-actions { flex-direction: column; align-items: flex-start; }
  .filter-group { width: 100%; }
  .filter-group :deep(.el-select) { flex: 1; }
}
</style>