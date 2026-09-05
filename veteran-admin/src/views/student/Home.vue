<template>
  <div class="home-container">
    <!-- 顶部欢迎 Banner -->
    <WelcomeBanner
      :student-name="studentStore.studentName"
      :total-points="stats.totalPoints"
      :rank="stats.rank"
      :current-date="currentDate"
      :level-info="levelInfo"
    />

    <!-- 快捷服务入口 -->
    <QuickMenu />

    <!-- 数据概览卡片 -->
    <StatsCard
      :total-points="stats.totalPoints"
      :service-count="stats.serviceCount"
      :total-hours="stats.totalHours"
      :honor-count="stats.honorCount"
      :cert-count="stats.certCount"
      :level-info="levelInfo"
    />

    <!-- 业务动态区域 -->
    <div class="middle-section fade-in">
      <ApplicationTimeline :applications="myApplications" />
      <NoticePanel :list="announceList" />
    </div>

    <!-- 辅助信息区域 -->
    <div class="bottom-section fade-in">
      <PolicyPanel />
      <FaqPanel />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getStudentHome } from '@/api/studentApp'
import { getActiveAnnouncements } from '@/api/announcement'
import { calcLevel } from '@/utils/pointsLevel'
import WelcomeBanner from '@/components/student/WelcomeBanner.vue'
import QuickMenu from '@/components/student/QuickMenu.vue'
import StatsCard from '@/components/student/StatsCard.vue'
import ApplicationTimeline from '@/components/student/ApplicationTimeline.vue'
import NoticePanel from '@/components/student/NoticePanel.vue'
import PolicyPanel from '@/components/student/PolicyPanel.vue'
import FaqPanel from '@/components/student/FaqPanel.vue'

const studentStore = useStudentStore()

const announceList = ref([])
const currentDate = ref('')
const myApplications = ref([])

const stats = reactive({
  totalPoints: 0,
  rank: 0,
  serviceCount: 0,
  totalHours: 0,
  honorCount: 0,
  certCount: 0
})

// 等级信息：基于总积分前端计算（不改后端接口）
const levelInfo = computed(() => calcLevel(stats.totalPoints))

function formatCurrentDate() {
  const now = new Date()
  const weeks = ['日', '一', '二', '三', '四', '五', '六']
  currentDate.value = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weeks[now.getDay()]}`
}

async function fetchData() {
  try {
    const res = await getStudentHome(studentStore.studentId)
    if (res.data) {
      Object.assign(stats, res.data)
      if (res.data.applications) {
        myApplications.value = res.data.applications.slice(0, 4)
      }
    }
  } catch { /* 静默 */ }

  try {
    const annRes = await getActiveAnnouncements()
    announceList.value = (annRes.data || []).slice(0, 4)
  } catch { /* 静默 */ }
}

onMounted(() => { formatCurrentDate(); fetchData() })
</script>

<style scoped>
.home-container { padding: 0 24px 24px; }

/* ===== 业务动态区域：申请进度 + 通知公告（55% : 45%） ===== */
.middle-section {
  display: grid;
  grid-template-columns: 55fr 45fr;
  gap: 16px;
  margin-bottom: 24px;
}

/* ===== 辅助信息区域：政策 + FAQ ===== */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* ===== Fade In ===== */
.fade-in { animation: fadeIn 0.5s ease; }
.fade-in:nth-child(2) { animation-delay: 0.15s; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 响应式 ===== */
@media (max-width: 1440px) {
  .qmenu-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 1200px) {
  .middle-section, .bottom-section { grid-template-columns: 1fr; }
}
</style>
