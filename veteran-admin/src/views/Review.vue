<template>
  <div class="page-container">
    <div class="page-header">
      <h2>审核管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="fetchData">
      <el-tab-pane label="服役经历" name="SERVICE_EXPERIENCE">
        <el-table :data="seItems" stripe size="small" v-loading="loading">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="detail.branchName" label="军兵种" min-width="120" />
          <el-table-column prop="detail.leaderPostName" label="骨干职务" min-width="140" show-overflow-tooltip />
          <el-table-column label="入伍~退役时间" min-width="180">
            <template #default="{ row }">{{ toYM(row.detail.startDate) }} ~ {{ toYM(row.detail.endDate) }}</template>
          </el-table-column>
          <el-table-column prop="detail.serviceYears" label="年限" width="60" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handlePass(row, 'SERVICE_EXPERIENCE')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row, 'SERVICE_EXPERIENCE')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && seItems.length === 0" description="暂无待审服役经历" />
      </el-tab-pane>

      <el-tab-pane label="受奖情况" name="HONOR">
        <el-table :data="honorItems" stripe size="small" v-loading="loading">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="detail.honorName" label="表彰奖励" min-width="150" />
          <el-table-column prop="detail.honorCategoryName" label="荣誉类别" width="100" />
          <el-table-column label="受奖时间" width="100">
            <template #default="{ row }">{{ toYM(row.detail.awardDate) }}</template>
          </el-table-column>
          <el-table-column prop="detail.pointsAwarded" label="积分" width="60" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handlePass(row, 'HONOR')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row, 'HONOR')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && honorItems.length === 0" description="暂无待审受奖情况" />
      </el-tab-pane>

      <el-tab-pane label="技能证书" name="SKILL">
        <el-table :data="skillItems" stripe size="small" v-loading="loading">
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="detail.certName" label="证书类型" width="120" />
          <el-table-column prop="detail.certNo" label="证书编号" width="150" />
          <el-table-column prop="detail.obtainDate" label="获取日期" width="110" />
          <el-table-column prop="detail.validUntil" label="有效期" width="110" />
          <el-table-column prop="detail.pointsAwarded" label="积分" width="60" />
          <el-table-column label="证明材料" width="90">
            <template #default="{ row }">
              <el-link v-if="row.detail.proofUrl" :href="row.detail.proofUrl" target="_blank" type="primary" :underline="false">查看</el-link>
              <span v-else class="text-muted">无</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handlePass(row, 'SKILL')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row, 'SKILL')">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && skillItems.length === 0" description="暂无待审证书" />
      </el-tab-pane>
    </el-tabs>

    <!-- 驳回原因弹窗 -->
    <el-dialog v-model="rejectDialog.visible" title="驳回原因" width="400px">
      <el-input
        v-model="rejectDialog.reason"
        type="textarea"
        :rows="3"
        placeholder="请输入驳回原因"
      />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingReviews, reviewServiceExperience, reviewHonor, reviewSkill } from '@/api/review'

const activeTab = ref('SERVICE_EXPERIENCE')
const loading = ref(false)
const allItems = ref([])

const seItems = ref([])
const honorItems = ref([])
const skillItems = ref([])

const rejectDialog = reactive({
  visible: false,
  item: null,
  type: '',
  reason: ''
})

// 年月展示：后端存当月1日(YYYY-MM-DD)，审核列表只展示 YYYY-MM
function toYM(value) {
  return value ? String(value).substring(0, 7) : '--'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPendingReviews()
    allItems.value = res.data || []
    groupItems()
  } catch (e) {
    ElMessage.error('获取待审项失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function groupItems() {
  seItems.value = allItems.value.filter(i => i.type === 'SERVICE_EXPERIENCE')
  honorItems.value = allItems.value.filter(i => i.type === 'HONOR')
  skillItems.value = allItems.value.filter(i => i.type === 'SKILL')
}

function handlePass(item, type) {
  ElMessageBox.confirm('确定通过该审核项？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const api = { SERVICE_EXPERIENCE: reviewServiceExperience, HONOR: reviewHonor, SKILL: reviewSkill }
      await api[type]({ id: item.id, status: 1 })
      ElMessage.success('已通过')
      fetchData()
    } catch (e) {
      ElMessage.error('操作失败：' + (e.message || '网络异常'))
    }
  }).catch(() => {})
}

function handleReject(item, type) {
  rejectDialog.item = item
  rejectDialog.type = type
  rejectDialog.reason = ''
  rejectDialog.visible = true
}

async function confirmReject() {
  if (!rejectDialog.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    const api = { SERVICE_EXPERIENCE: reviewServiceExperience, HONOR: reviewHonor, SKILL: reviewSkill }
    await api[rejectDialog.type]({
      id: rejectDialog.item.id,
      status: 2,
      rejectReason: rejectDialog.reason.trim()
    })
    ElMessage.success('已驳回')
    rejectDialog.visible = false
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
