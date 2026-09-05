<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <h2>个人画像</h2>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/resume-create')">
          制作简历
        </el-button>
      </div>
    </div>

    <template v-if="data">
      <!-- 头部卡片 -->
      <div class="portrait-header">
        <div class="portrait-avatar">
          <el-icon :size="42"><UserFilled /></el-icon>
        </div>
        <div class="portrait-info">
          <div class="name-row">
            <span class="name">{{ data.basic?.name }}</span>
            <el-tag :type="levelTag(data.points?.level)" size="small" class="level-badge">
              {{ data.points?.level }}级
            </el-tag>
          </div>
          <p class="meta">{{ data.basic?.studentNo }} · {{ data.basic?.college }} · {{ data.basic?.major }}</p>
        </div>
      </div>

      <!-- 数据卡片 -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-value gold">{{ data.points?.total || 0 }}</div>
          <div class="stat-label">总积分</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ data.honors?.length || 0 }}</div>
          <div class="stat-label">荣誉</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ data.certs?.length || 0 }}</div>
          <div class="stat-label">证书</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ totalServiceHours }}h</div>
          <div class="stat-label">服务时长</div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="section-card" v-if="data.basic">
        <h4 class="section-title">基本信息</h4>
        <div class="basic-grid">
          <div class="basic-item" v-if="data.basic.gender"><span>性别</span><b>{{ data.basic.gender }}</b></div>
          <div class="basic-item" v-if="data.basic.ethnicity"><span>民族</span><b>{{ data.basic.ethnicity }}</b></div>
          <div class="basic-item" v-if="data.basic.birthDate"><span>出生年月</span><b>{{ data.basic.birthDate }}</b></div>
          <div class="basic-item" v-if="data.basic.nativePlace"><span>籍贯</span><b>{{ data.basic.nativePlace }}</b></div>
          <div class="basic-item" v-if="data.basic.politicalStatus"><span>政治面貌</span><b>{{ data.basic.politicalStatus }}</b></div>
          <div class="basic-item" v-if="data.basic.retireDate"><span>退役时间</span><b>{{ data.basic.retireDate }}</b></div>
        </div>
      </div>

      <!-- 雷达图 -->
      <div class="section-card">
        <h4 class="section-title">能力评估</h4>
        <div ref="radarRef" class="radar-chart"></div>
      </div>

      <!-- 服役经历 -->
      <div class="section-card" v-if="data.military">
        <h4 class="section-title">服役经历</h4>
        <div class="timeline-item">
          <div class="tl-dot"></div>
          <div class="tl-content">
            <div class="tl-title">{{ data.military.branchName }}{{ data.military.leaderPostName ? ' · ' + data.military.leaderPostName : '' }}</div>
            <div class="tl-time">{{ data.military.startDate }} ~ {{ data.military.endDate }} · {{ data.military.serviceYears || '--' }}</div>
            <div class="tl-extra" v-if="data.military.position">职务：{{ data.military.position }}</div>
          </div>
        </div>
      </div>

      <!-- 受奖情况 -->
      <div class="section-card" v-if="data.honors?.length">
        <h4 class="section-title">受奖情况</h4>
        <div class="list-item" v-for="h in data.honors" :key="h.name">
          <span class="item-name">{{ h.name }}</span>
          <el-tag size="small" type="info" class="item-category">{{ h.categoryName || '荣誉' }}</el-tag>
          <span class="item-date">{{ h.awardDate }}</span>
          <span class="item-points gold">{{ h.points > 0 ? '+' + h.points : h.points }}</span>
        </div>
      </div>

      <!-- 证书列表 -->
      <div class="section-card" v-if="data.certs?.length">
        <h4 class="section-title">技能证书</h4>
        <div class="list-item" v-for="c in data.certs" :key="c.name">
          <span class="item-name">{{ c.name }}</span>
          <span class="item-date">{{ c.obtainDate }}{{ c.validUntil ? ' ~ ' + c.validUntil : '' }}</span>
          <span class="item-points gold">+{{ c.points }}</span>
        </div>
      </div>

      <!-- 社会服务 -->
      <div class="section-card" v-if="data.services?.length">
        <h4 class="section-title">社会服务</h4>
        <div class="list-item" v-for="s in data.services" :key="s.title">
          <span class="item-name">{{ s.title }}</span>
          <span class="item-date">{{ s.serviceDate }} · {{ s.hours }}h</span>
          <span class="item-points gold">+{{ s.points }}</span>
        </div>
      </div>

      <!-- 学业表现 -->
      <div class="section-card" v-if="data.academic?.length">
        <h4 class="section-title">学业表现</h4>
        <div class="list-item" v-for="a in data.academic" :key="a.semester">
          <span class="item-name">{{ a.semester }}</span>
          <span class="item-date">GPA: {{ a.gpa || '--' }}{{ a.scholarship ? ' · ' + a.scholarship : '' }}</span>
          <span class="item-points gold">+{{ a.points }}</span>
        </div>
      </div>

      <!-- 就业意向 -->
      <div class="section-card" v-if="data.intention?.jobTypes?.length">
        <h4 class="section-title">就业意向</h4>
        <div class="tag-row">
          <el-tag v-for="j in data.intention.jobTypes" :key="j" size="small" type="info">{{ j }}</el-tag>
        </div>
      </div>
    </template>

    <el-empty v-if="!loading && !data" description="暂无画像数据" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import { getPortrait } from '@/api/studentApp'
import * as echarts from 'echarts'

const router = useRouter()
const studentStore = useStudentStore()
const loading = ref(false)
const data = ref(null)
const radarRef = ref(null)

const totalServiceHours = computed(() => {
  return (data.value?.services || []).reduce((sum, s) => sum + (s.hours || 0), 0)
})

function levelTag(level) {
  const map = { A: 'danger', B: 'warning', C: 'primary', D: 'info' }
  return map[level] || 'info'
}

function renderRadar() {
  if (!radarRef.value || !data.value) return
  const honors = data.value.honors?.length || 0
  const certs = data.value.certs?.length || 0
  const svcHours = totalServiceHours.value || 0
  const totalPts = data.value.points?.total || 0
  const acadCount = data.value.academic?.length || 0

  // 简单评分模型
  const leadScore = Math.min(100, honors * 20 + svcHours * 2)
  const execScore = Math.min(100, totalPts * 0.6 + honors * 15)
  const teamScore = Math.min(100, svcHours * 3 + acadCount * 15)
  const skillScore = Math.min(100, certs * 20 + acadCount * 10)
  const respondScore = Math.min(100, honors * 15 + svcHours * 2)
  const respScore = Math.min(100, totalPts * 0.5 + svcHours)

  const chart = echarts.init(radarRef.value)
  chart.setOption({
    radar: {
      indicator: [
        { name: '领导力', max: 100 },
        { name: '执行力', max: 100 },
        { name: '团队协作', max: 100 },
        { name: '专业技能', max: 100 },
        { name: '应急响应', max: 100 },
        { name: '责任心', max: 100 }
      ],
      center: ['50%', '55%'],
      radius: '70%'
    },
    series: [{
      type: 'radar',
      data: [{ value: [leadScore, execScore, teamScore, skillScore, respondScore, respScore], name: '能力评估',
        areaStyle: { color: 'rgba(196,163,90,0.25)' },
        lineStyle: { color: '#c4a35a', width: 2 },
        itemStyle: { color: '#c4a35a' }
      }]
    }]
  })
}

async function fetchPortrait() {
  loading.value = true
  try {
    const res = await getPortrait(studentStore.studentId)
    data.value = res.data
    await nextTick()
    renderRadar()
  } catch (e) {
    ElMessage.error('获取画像失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPortrait()
})
</script>

<style scoped>
.portrait-header {
  background: #fff;
  border-radius: var(--radius);
  padding: 28px 32px;
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.portrait-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563EB, #c4a35a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.portrait-info .name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.portrait-info .name {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}
.level-badge { font-weight: 700; }
.portrait-info .meta {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px;
  text-align: center;
  box-shadow: var(--shadow-card);
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}
.stat-value.gold { color: #c4a35a; }
.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.section-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
  margin-bottom: 16px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0 0 14px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}
.radar-chart {
  width: 100%;
  height: 350px;
}

.timeline-item {
  display: flex;
  gap: 12px;
}
.tl-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c4a35a;
  margin-top: 6px;
  flex-shrink: 0;
}
.tl-title {
  font-weight: 600;
  color: var(--color-primary);
  font-size: 14px;
}
.tl-time {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}
.tl-extra {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed var(--color-border);
}
.list-item:last-child { border-bottom: none; }
.item-name {
  flex: 1;
  font-size: 14px;
  color: var(--color-primary);
}
.item-date {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 0 16px;
}
.item-points {
  font-weight: 600;
  font-size: 14px;
}

.tag-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.gold { color: #c4a35a; }

.basic-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 24px;
}
.basic-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed var(--color-border);
  font-size: 13px;
}
.basic-item span {
  color: var(--color-text-secondary);
}
.basic-item b {
  color: var(--color-primary);
  font-weight: 600;
}
.item-category {
  margin: 0 8px;
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .basic-grid { grid-template-columns: 1fr 1fr; }
}
</style>
