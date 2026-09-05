<!--
  Portrait.vue — 个人画像
  雷达图能力分析 + 基本信息卡片 + AI 分析
-->
<template>
  <div class="portrait-page">
    <van-nav-bar title="个人画像" left-arrow @click-left="$router.back()" />

    <LoadingState v-if="loading" height="400px" />

    <template v-else>
      <!-- 基本信息 -->
      <div class="info-card">
        <div class="info-avatar">
          <van-icon name="user-o" size="32" color="#fff" />
        </div>
        <div class="info-text">
          <h2>{{ portrait.basic?.name || '-' }}</h2>
          <p>{{ portrait.basic?.college }} · {{ portrait.basic?.major }} · {{ portrait.basic?.grade }}</p>
        </div>
      </div>

      <!-- 基本信息扩展 -->
      <van-cell-group inset title="基本信息">
        <van-cell title="学号" :value="portrait.basic?.studentNo || '-'" />
        <van-cell title="性别" :value="portrait.basic?.gender || '-'" />
        <van-cell title="民族" :value="portrait.basic?.ethnicity || '-'" />
        <van-cell title="出生年月" :value="portrait.basic?.birthDate || '-'" />
        <van-cell title="籍贯" :value="portrait.basic?.nativePlace || '-'" />
        <van-cell title="政治面貌" :value="portrait.basic?.politicalStatus || '-'" />
        <van-cell title="退役时间" :value="portrait.basic?.retireDate || '-'" />
      </van-cell-group>

      <!-- 服役经历 -->
      <van-cell-group inset title="服役经历" v-if="portrait.military">
        <van-cell title="军兵种" :value="portrait.military.branchName || '-'" />
        <van-cell title="骨干职务" :value="portrait.military.leaderPostName || '-'" />
        <van-cell title="服役时间" :value="(portrait.military.startDate || '-') + ' ~ ' + (portrait.military.endDate || '-')" />
        <van-cell title="服役年限" :value="portrait.military.serviceYears || '-'" />
      </van-cell-group>

      <!-- 受奖情况 -->
      <van-cell-group inset title="受奖情况" v-if="portrait.honors && portrait.honors.length">
        <van-cell v-for="(h, i) in portrait.honors" :key="i" :title="h.name || '-'" :label="'受奖时间: ' + (h.awardDate || '-')">
          <template #value>
            <span class="gold-text">+{{ h.points }}</span>
            <van-tag type="primary" size="mini" style="margin-left:4px">{{ h.categoryName || '未分类' }}</van-tag>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 能力雷达图 -->
      <div class="chart-section">
        <h3 class="section-title">能力分析</h3>
        <div ref="chartRef" class="radar-chart"></div>
      </div>

      <!-- 积分构成 -->
      <van-cell-group inset title="积分构成">
        <van-cell title="服务积分" :value="(portrait.points?.servicePoints || 0) + '分'">
          <template #right-icon><span class="bar bar-service" :style="{ width: barWidth(portrait.points?.servicePoints) }"></span></template>
        </van-cell>
        <van-cell title="荣誉积分" :value="(portrait.points?.honorPoints || 0) + '分'">
          <template #right-icon><span class="bar bar-honor" :style="{ width: barWidth(portrait.points?.honorPoints) }"></span></template>
        </van-cell>
        <van-cell title="证书积分" :value="(portrait.points?.certPoints || 0) + '分'">
          <template #right-icon><span class="bar bar-cert" :style="{ width: barWidth(portrait.points?.certPoints) }"></span></template>
        </van-cell>
        <van-cell title="总积分" :value="(portrait.points?.total || 0) + '分'">
          <template #value><span class="total-score">{{ portrait.points?.total || 0 }}</span></template>
        </van-cell>
      </van-cell-group>

      <!-- AI 分析 -->
      <van-cell-group inset title="AI 综合分析" v-if="aiAnalysis">
        <van-cell title="综合评级">
          <template #value><van-tag :type="riskTagType(aiAnalysis.riskScore)" size="medium">{{ riskLabel(aiAnalysis.riskScore) }}</van-tag></template>
        </van-cell>
        <van-cell title="技能画像" :label="aiAnalysis.skillProfile" />
        <van-cell title="推荐岗位" :label="aiAnalysis.recommendJob || '-'" />
        <van-cell title="建议" :label="aiAnalysis.suggestion || '-'" />
      </van-cell-group>
      <van-cell-group inset v-else>
        <van-cell>
          <template #title>
            <van-button plain block size="small" @click="fetchAIAnalysis" :loading="aiLoading">
              获取 AI 分析
            </van-button>
          </template>
        </van-cell>
      </van-cell-group>
    </template>
  </div>
</template>

<script setup>
/**
 * 个人画像页
 * - 获取学生画像数据
 * - ECharts 雷达图展示能力分布
 * - AI 综合分析
 */
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getPortrait, getAIAnalysis } from '@/api/studentApp'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

echarts.use([RadarChart, TooltipComponent, LegendComponent, CanvasRenderer])

const store = useStudentStore()
const portrait = ref({})
const aiAnalysis = ref(null)
const loading = ref(true)
const aiLoading = ref(false)
const chartRef = ref(null)
let chartInstance = null

onMounted(async () => {
  try {
    const res = await getPortrait(store.studentId)
    portrait.value = res.data || {}
    await nextTick()
    renderChart()
  } catch (e) { /* 拦截器处理 */ }
  finally { loading.value = false }
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

/** 渲染雷达图 */
function renderChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)

  const p = portrait.value.points || {}
  const maxVal = Math.max(p.servicePoints || 0, p.honorPoints || 0, p.certPoints || 0, 100)

  chartInstance.setOption({
    tooltip: {},
    radar: {
      indicator: [
        { name: '服务积分', max: maxVal },
        { name: '荣誉积分', max: maxVal },
        { name: '证书积分', max: maxVal },
        { name: '申请活跃', max: maxVal },
        { name: '综合评价', max: maxVal }
      ],
      radius: '65%',
      axisName: { color: '#666', fontSize: 12 }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [
          p.servicePoints || 0,
          p.honorPoints || 0,
          p.certPoints || 0,
          portrait.value.applyCount || 0,
          p.total || 0
        ],
        name: '能力分布',
        areaStyle: { color: 'rgba(37, 99, 235, 0.2)' },
        lineStyle: { color: '#2563EB' },
        itemStyle: { color: '#2563EB' }
      }]
    }]
  })
}

/** 获取 AI 分析 */
async function fetchAIAnalysis() {
  aiLoading.value = true
  try {
    const res = await getAIAnalysis(store.studentId)
    aiAnalysis.value = res.data
  } catch (e) { /* 拦截器处理 */ }
  finally { aiLoading.value = false }
}

/** 积分条宽度 */
function barWidth(points) {
  const max = Math.max(portrait.value.points?.total || 100, 100)
  return Math.min((points / max) * 100, 100) + 'px'
}

/** 风险等级标签 */
function riskLabel(score) {
  if (score === undefined || score === null) return '未评估'
  if (score < 30) return '优秀'
  if (score < 60) return '良好'
  if (score < 80) return '一般'
  return '需关注'
}
function riskTagType(score) {
  if (score === undefined || score === null) return 'default'
  if (score < 30) return 'success'
  if (score < 60) return 'primary'
  if (score < 80) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.portrait-page { background: var(--color-bg-page); min-height: 100vh; padding-bottom: 20px; }

/* 信息卡片 */
.info-card {
  background: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  padding: 20px 16px;
  display: flex;
  align-items: center;
  color: #fff;
}
.info-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}
.info-text h2 { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.info-text p { font-size: 13px; opacity: 0.8; }

/* 雷达图 */
.chart-section {
  background: #fff;
  margin: 8px 12px;
  border-radius: 12px;
  padding: 16px;
}
.section-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.radar-chart { width: 100%; height: 260px; }

/* 积分条 */
.bar {
  display: inline-block;
  height: 8px;
  border-radius: 4px;
  margin-left: 8px;
}
.bar-service { background: linear-gradient(90deg, #409eff, #2563EB); }
.bar-honor { background: linear-gradient(90deg, #ffd700, #f5a623); }
.bar-cert { background: linear-gradient(90deg, #67c23a, #4eaa2f); }
.total-score { font-size: 18px; font-weight: 700; color: var(--color-primary); }
.gold-text { color: var(--color-gold, #f5a623); font-weight: 600; font-size: 13px; }
</style>
