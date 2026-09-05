<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的积分</h2>
      <el-button type="primary" size="small" @click="$router.push('/ranking')">
        <el-icon><Trophy /></el-icon> 积分排行
      </el-button>
    </div>

    <div class="points-summary">
      <div class="points-big">
        <span class="points-num">{{ totalPoints }}</span>
        <span class="points-unit">分</span>
      </div>
      <div class="points-rank">当前排名：第 {{ rank }} 位</div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-title">积分变动趋势</div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-title">积分构成</div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
    </div>

    <div class="section-card">
      <h4 class="section-title">
        积分获取规则
        <el-button size="small" type="primary" @click="rulesExpanded = !rulesExpanded">
          {{ rulesExpanded ? '收起' : '展开' }}
          <el-icon><component :is="rulesExpanded ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
        </el-button>
      </h4>
      <el-collapse-transition>
        <div v-show="rulesExpanded">
          <el-table :data="rulesData" stripe size="small">
            <el-table-column prop="category" label="类别" width="100" />
            <el-table-column prop="name" label="项目" />
            <el-table-column prop="points" label="积分" width="100">
              <template #default="{ row }">
                <span class="gold-text">+{{ row.points }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="说明" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>
      </el-collapse-transition>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.sourceType" placeholder="来源类型" clearable style="width: 130px" size="small">
        <el-option label="社会服务" value="SERVICE" />
        <el-option label="荣誉奖励" value="HONOR" />
        <el-option label="技能证书" value="CERT" />
        <el-option label="管理员调整" value="ADJUST" />
      </el-select>
      <el-date-picker
        v-model="query.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        value-format="YYYY-MM-DD"
        size="small"
        style="width: 240px"
      />
      <el-button type="primary" size="small" @click="fetchData">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="sourceType" label="来源类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="sourceTagType(row.sourceType)">
              {{ sourceLabel(row.sourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="points" label="积分变动" width="100">
          <template #default="{ row }">
            <span :class="row.points > 0 ? 'gold-text' : 'danger-text'">
              {{ row.points > 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" width="80" />
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        size="small"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 12px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getPointsDetail, getStudentHome } from '@/api/studentApp'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const studentStore = useStudentStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const totalPoints = ref(0)
const rank = ref(0)
const pointsTrend = ref([])
const pointsComposition = ref([])
const rulesExpanded = ref(false)

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const query = reactive({ page: 1, size: 10, sourceType: '', dateRange: null })

const rulesData = [
  { category: '社会服务', name: '志愿服务/社会实践活动', points: '2/小时', desc: '按实际服务时长计算，每小时2积分' },
  { category: '社会服务', name: '征兵宣讲/国防教育', points: '3/小时', desc: '参与征兵宣讲或国防教育活动' },
  { category: '社会服务', name: '军训带训', points: '4/小时', desc: '担任新生军训教官或助教' },
  { category: '受奖情况', name: '一等功', points: '100', desc: '服役期间荣立一等功' },
  { category: '受奖情况', name: '二等功', points: '60', desc: '服役期间荣立二等功' },
  { category: '受奖情况', name: '三等功', points: '30', desc: '服役期间荣立三等功' },
  { category: '受奖情况', name: '优秀士兵（含嘉奖）', points: '20', desc: '获评优秀士兵（含嘉奖）' },
  { category: '受奖情况', name: '嘉奖', points: '10', desc: '获得部队嘉奖' },
  { category: '技能证书', name: '军训教官证', points: '5', desc: '取得军训教官证' },
  { category: '技能证书', name: '急救证', points: '10', desc: '取得急救证' },
  { category: '技能证书', name: '驾驶证C1', points: '15', desc: '取得驾驶证C1' },
  { category: '技能证书', name: '电工证', points: '15', desc: '取得电工证' },
  { category: '技能证书', name: '中式烹调师', points: '15', desc: '取得中式烹调师证书' },
  { category: '技能证书', name: '计算机等级', points: '20', desc: '取得计算机等级证书' },
  { category: '技能证书', name: '英语等级', points: '20', desc: '取得英语等级证书' },
  { category: '技能证书', name: '无人机CAAC执照', points: '20', desc: '取得无人机CAAC执照' },
  { category: '技能证书', name: '心理咨询师', points: '25', desc: '取得心理咨询师证书' },
  { category: '技能证书', name: '消防设施操作员', points: '25', desc: '取得消防设施操作员证书' },
  { category: '技能证书', name: '初级会计职称', points: '25', desc: '取得初级会计职称' },
  { category: '技能证书', name: '教师资格证', points: '25', desc: '取得教师资格证' },
  { category: '技能证书', name: '法律职业资格证', points: '30', desc: '取得法律职业资格证' }
]

function sourceLabel(type) {
  const map = { SERVICE: '社会服务', HONOR: '受奖情况', CERT: '技能证书', ADJUST: '管理员调整', CERT_REVOKE: '证书撤销' }
  return map[type] || type
}

function sourceTagType(type) {
  const map = { SERVICE: 'success', HONOR: 'warning', CERT: '', ADJUST: 'info', CERT_REVOKE: 'danger' }
  return map[type] || 'info'
}

function initCharts() {
  if (!trendChartRef.value || !pieChartRef.value) return
  if (trendChart) trendChart.dispose()
  if (pieChart) pieChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  pieChart = echarts.init(pieChartRef.value)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#909399' }
    },
    series: [{
      data: [],
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#c4a35a', width: 3 },
      itemStyle: { color: '#c4a35a' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(196,163,90,0.3)' },
          { offset: 1, color: 'rgba(196,163,90,0.02)' }
        ])
      }
    }]
  })

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 分 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#909399', fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['50%', '45%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: []
    }]
  })
}

function updateTrendChart(data) {
  if (!trendChart || !data || data.length === 0) return
  const months = ['1月', '2月', '3月', '4月', '5月', '6月']
  const labels = data.map((_, i) => months[i] || (i + 1) + '月')
  trendChart.setOption({
    xAxis: { data: labels.slice(-data.length) },
    series: [{ data: data }]
  })
}

function updatePieChart(data) {
  if (!pieChart || !data || data.length === 0) return
  pieChart.setOption({ series: [{ data: data }] })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPointsDetail(studentStore.studentId, query)
    tableData.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取积分明细失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const res = await getStudentHome(studentStore.studentId)
    if (res.data) {
      totalPoints.value = res.data.totalPoints
      rank.value = res.data.rank
      if (res.data.pointsTrend) {
        pointsTrend.value = res.data.pointsTrend
        updateTrendChart(res.data.pointsTrend)
      }
      if (res.data.pointsComposition) {
        pointsComposition.value = res.data.pointsComposition
        updatePieChart(res.data.pointsComposition)
      }
    }
  } catch (e) {
    ElMessage.error('获取积分统计失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchData()
  fetchStats()
  nextTick(() => {
    initCharts()
  })
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.points-summary {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fef9f0, #fdf5e6);
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.points-big {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.points-num {
  font-size: 36px;
  font-weight: 700;
  font-family: 'Courier New', 'Consolas', monospace;
  color: var(--color-gold);
}

.points-unit {
  font-size: 16px;
  color: var(--color-text-secondary);
}

.points-rank {
  font-size: 13px;
  color: var(--color-text-secondary);
  background: #f5f7fa;
  padding: 4px 12px;
  border-radius: 8px;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-card);
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.chart-box {
  width: 100%;
  height: 240px;
}

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-card);
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.danger-text {
  color: #e74c3c;
  font-weight: 600;
}
</style>