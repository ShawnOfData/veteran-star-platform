<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据看板</h2>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">退役学生总数</span>
          <el-icon class="stat-icon"><User /></el-icon>
        </div>
        <div class="stat-value">{{ stats.totalStudents }}</div>
        <div class="stat-sub">在校 {{ stats.activeStudents }} 人</div>
      </div>

      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">积分中位数</span>
          <el-icon class="stat-icon"><Medal /></el-icon>
        </div>
        <div class="stat-value">{{ stats.medianPoints }}</div>
        <div class="stat-sub">最高 {{ stats.maxPoints }} 分</div>
      </div>

      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">本月服务次数</span>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </div>
        <div class="stat-value">{{ stats.monthlyServices }}</div>
        <div class="stat-sub">累计 {{ stats.totalServices }} 次</div>
      </div>

      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">待审核记录</span>
          <el-icon class="stat-icon"><Warning /></el-icon>
        </div>
        <div class="stat-value gold-text">{{ stats.pendingReviews }}</div>
        <div class="stat-sub">需尽快处理</div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-title">积分分布</div>
        <div ref="pointsChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">各学院退役人数</div>
        <div ref="collegeChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="bottom-row">
      <div class="table-card" style="flex: 1;">
        <div class="chart-title" style="padding: 16px 0;">近期服务记录</div>
        <el-table :data="recentRecords" stripe size="small">
          <el-table-column prop="studentName" label="学生" />
          <el-table-column prop="activityType" label="活动类型" />
          <el-table-column prop="durationHours" label="时长(h)" width="80" />
          <el-table-column prop="serviceDate" label="日期" width="110" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'" size="small">
                {{ row.status === 1 ? '已通过' : row.status === 0 ? '待审' : '已驳回' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getDashboardStats, getServiceRecords } from '@/api/points'

const pointsChartRef = ref(null)
const collegeChartRef = ref(null)
let pointsChart = null
let collegeChart = null

const stats = reactive({
  totalStudents: 0,
  activeStudents: 0,
  medianPoints: 0,
  maxPoints: 0,
  monthlyServices: 0,
  totalServices: 0,
  pendingReviews: 0,
  pointsDistribution: [],
  collegeDistribution: []
})

const recentRecords = ref([])

function initCharts() {
  if (!pointsChartRef.value || !collegeChartRef.value) return
  pointsChart = echarts.init(pointsChartRef.value)
  collegeChart = echarts.init(collegeChartRef.value)

  // 先设基本配色，数据稍后填充
  pointsChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: ['0-20', '21-40', '41-60', '61-80', '81-100', '100+'],
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#909399' }
    },
    series: [{
      data: [],
      type: 'bar',
      barWidth: 28,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3a6e33' },
          { offset: 1, color: '#2d5a27' }
        ])
      }
    }]
  })

  collegeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#909399', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [],
      color: ['#2d5a27', '#3a6e33', '#4a8a40', '#c4a35a', '#8b9dc3', '#5b8c5a', '#7a9a7a']
    }]
  })
}

function updatePointsChart(data) {
  if (!pointsChart || !data || data.length !== 6) return
  pointsChart.setOption({ series: [{ data: data }] })
}

function updateCollegeChart(data) {
  if (!collegeChart || !data || data.length === 0) return
  collegeChart.setOption({ series: [{ data: data }] })
}

async function fetchData() {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      Object.assign(stats, res.data)
      // 图表数据到达后更新
      if (res.data.pointsDistribution) {
        updatePointsChart(res.data.pointsDistribution)
      }
      if (res.data.collegeDistribution) {
        updateCollegeChart(res.data.collegeDistribution)
      }
    }
  } catch (e) {
    ElMessage.error('获取统计数据失败：' + (e.message || '网络异常'))
  }

  try {
    const recRes = await getServiceRecords({ page: 1, size: 5 })
    recentRecords.value = recRes.data?.records || []
  } catch (e) {
    ElMessage.error('获取近期记录失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  initCharts()
  fetchData()
})

onUnmounted(() => {
  pointsChart?.dispose()
  collegeChart?.dispose()
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-sub {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow-card);
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 12px;
}

.chart-box {
  width: 100%;
  height: 280px;
}

.bottom-row {
  display: flex;
  gap: 20px;
}
</style>