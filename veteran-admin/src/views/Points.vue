<template>
  <div class="page-container">
    <div class="page-header">
      <h2>积分管理</h2>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">待审核记录</span>
          <el-icon class="stat-icon"><Clock /></el-icon>
        </div>
        <div class="stat-value gold-text">{{ stats.pendingCount }}</div>
        <div class="stat-sub">需尽快处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">今日已审核</span>
          <el-icon class="stat-icon"><Select /></el-icon>
        </div>
        <div class="stat-value">{{ stats.todayReviewed }}</div>
        <div class="stat-sub">通过 {{ stats.todayApproved }} / 驳回 {{ stats.todayRejected }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">本月发放积分</span>
          <el-icon class="stat-icon"><Medal /></el-icon>
        </div>
        <div class="stat-value">{{ stats.monthlyPoints }}</div>
        <div class="stat-sub">累计 {{ stats.totalPoints }} 分</div>
      </div>
      <div class="stat-card">
        <div class="stat-top">
          <span class="stat-label">积分规则数</span>
          <el-icon class="stat-icon"><Setting /></el-icon>
        </div>
        <div class="stat-value">{{ stats.ruleCount }}</div>
        <div class="stat-sub">
          <span class="text-btn" @click="activeTab = 'rules'">查看规则</span>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="服务记录审核" name="review">
        <div class="filter-bar" style="box-shadow: none; padding: 0 0 12px;">
          <el-input v-model="query.studentName" placeholder="学生姓名" clearable style="width: 140px" size="small" />
          <el-select v-model="query.status" placeholder="审核状态" clearable style="width: 130px" size="small">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            size="small"
            style="width: 240px"
          />
          <el-button type="primary" size="small" @click="fetchData">查询</el-button>
          <el-button size="small" @click="resetQuery">重置</el-button>
          <template v-if="selectedRows.length > 0">
            <el-divider direction="vertical" />
            <span style="font-size:13px;color:#606266;">已选 {{ selectedRows.length }} 项</span>
            <el-button type="success" size="small" @click="batchApprove">批量通过</el-button>
            <el-button type="danger" size="small" @click="batchReject">批量驳回</el-button>
          </template>
        </div>

        <el-table
          :data="tableData"
          stripe
          v-loading="loading"
          @selection-change="handleSelectionChange"
          ref="tableRef"
        >
          <el-table-column type="selection" width="45" :selectable="row => row.status === 0" />
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="activityType" label="活动类型" width="120" />
          <el-table-column prop="activityDesc" label="活动描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="durationHours" label="时长(h)" width="80" />
          <el-table-column prop="pointsApplied" label="申请积分" width="90">
            <template #default="{ row }">
              <span class="gold-text">{{ row.pointsApplied }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="serviceDate" label="服务日期" width="110" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'" size="small">
                {{ row.status === 1 ? '已通过' : row.status === 0 ? '待审核' : '已驳回' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <span class="text-btn" @click="handleApprove(row)">通过</span>
                <span class="text-btn danger" @click="handleReject(row)">驳回</span>
              </template>
              <span class="text-btn" @click="openDetail(row)">详情</span>
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
          @size-change="fetchData"
          @current-change="fetchData"
          style="margin-top: 12px; justify-content: flex-end;"
        />
      </el-tab-pane>

      <el-tab-pane label="积分明细总览" name="detail">
        <div class="filter-bar" style="box-shadow: none; padding: 0 0 12px;">
          <el-input v-model="detailQuery.studentName" placeholder="学生姓名" clearable style="width: 140px" size="small" />
          <el-select v-model="detailQuery.reasonType" placeholder="来源类型" clearable style="width: 130px" size="small">
            <el-option label="社会服务" value="SERVICE" />
            <el-option label="荣誉奖励" value="HONOR" />
            <el-option label="技能证书" value="CERT" />
            <el-option label="管理员调整" value="ADJUST" />
          </el-select>
          <el-date-picker
            v-model="detailQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            size="small"
            style="width: 240px"
          />
          <el-button type="primary" size="small" @click="fetchDetailData">查询</el-button>
          <el-button type="warning" size="small" @click="openAdjustDialog">手动调整积分</el-button>
        </div>

        <el-table :data="detailData" stripe v-loading="detailLoading" size="small">
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="reasonType" label="来源类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="reasonTagType(row.reasonType)">
                {{ reasonLabel(row.reasonType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="200" show-overflow-tooltip />
          <el-table-column prop="changeValue" label="积分变动" width="100">
            <template #default="{ row }">
              <span :class="row.changeValue > 0 ? 'gold-text' : 'danger-text'">
                {{ row.changeValue > 0 ? '+' : '' }}{{ row.changeValue }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>

        <el-pagination
          v-model:current-page="detailQuery.page"
          v-model:page-size="detailQuery.size"
          :total="detailTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          size="small"
          @size-change="fetchDetailData"
          @current-change="fetchDetailData"
          style="margin-top: 12px; justify-content: flex-end;"
        />
      </el-tab-pane>

      <el-tab-pane label="积分规则" name="rules">
        <div style="padding: 8px 0;">
          <el-tabs v-model="ruleTab" type="card">
            <el-tab-pane label="荣誉积分规则" name="honor">
              <el-table :data="honorRules" stripe size="small">
                <el-table-column prop="code" label="代码" width="100" />
                <el-table-column prop="name" label="荣誉名称" />
                <el-table-column prop="defaultPoints" label="默认积分" width="120">
                  <template #default="{ row }">
                    <template v-if="editingCode === row.code">
                      <el-input-number v-model="editPoints" :min="0" :max="100" size="small" style="width: 100px" />
                      <el-button type="primary" size="small" style="margin-left: 8px;" @click="saveRule(row)">保存</el-button>
                      <el-button size="small" @click="editingCode = ''">取消</el-button>
                    </template>
                    <template v-else>
                      <span class="gold-text">{{ row.defaultPoints }}</span>
                      <span class="text-btn" style="margin-left: 8px;" @click="startEdit(row)">修改</span>
                    </template>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="证书积分规则" name="cert">
              <el-table :data="certRules" stripe size="small">
                <el-table-column prop="code" label="代码" width="100" />
                <el-table-column prop="name" label="证书名称" />
                <el-table-column prop="defaultPoints" label="默认积分" width="120">
                  <template #default="{ row }">
                    <template v-if="editingCode === row.code">
                      <el-input-number v-model="editPoints" :min="0" :max="100" size="small" style="width: 100px" />
                      <el-button type="primary" size="small" style="margin-left: 8px;" @click="saveRule(row)">保存</el-button>
                      <el-button size="small" @click="editingCode = ''">取消</el-button>
                    </template>
                    <template v-else>
                      <span class="gold-text">{{ row.defaultPoints }}</span>
                      <span class="text-btn" style="margin-left: 8px;" @click="startEdit(row)">修改</span>
                    </template>
                  </template>
                </el-table-column>
                <el-table-column prop="validityMonths" label="有效期(月)" width="100" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="420px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因（选填）" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchRejectVisible" title="批量驳回" width="420px">
      <p style="margin-bottom: 12px; color: #606266;">将对 {{ selectedRows.length }} 条记录执行驳回操作</p>
      <el-input v-model="batchRejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因（选填）" />
      <template #footer>
        <el-button @click="batchRejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchReject">确认批量驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustVisible" title="手动调整积分" width="460px" destroy-on-close>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="80px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="adjustForm.studentId" placeholder="请选择学生" filterable style="width: 100%">
            <el-option v-for="s in studentOptions" :key="s.id" :label="`${s.name} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="type">
          <el-radio-group v-model="adjustForm.type">
            <el-radio value="add">增加积分</el-radio>
            <el-radio value="deduct">扣减积分</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数" prop="points">
          <el-input-number v-model="adjustForm.points" :min="1" :max="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="2" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust">确认调整</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="积分详情" size="500px">
      <template v-if="currentRecord">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="学生">{{ currentRecord.studentName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentRecord.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="活动类型">{{ currentRecord.activityType }}</el-descriptions-item>
          <el-descriptions-item label="活动描述">{{ currentRecord.activityDesc }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentRecord.durationHours }} 小时</el-descriptions-item>
          <el-descriptions-item label="申请积分">
            <span class="gold-text">{{ currentRecord.pointsApplied }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ currentRecord.serviceDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRecord.status === 1 ? 'success' : currentRecord.status === 0 ? 'warning' : 'danger'" size="small">
              {{ currentRecord.status === 1 ? '已通过' : currentRecord.status === 0 ? '待审核' : '已驳回' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.reviewComment" label="审核意见">{{ currentRecord.reviewComment }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceRecords, reviewServiceRecord } from '@/api/points'
import { getDictHonor, getDictCert } from '@/api/dict'
import { getStudentPage } from '@/api/student'

const activeTab = ref('review')
const ruleTab = ref('honor')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const tableRef = ref(null)

const rejectVisible = ref(false)
const rejectReason = ref('')
const currentRejectRow = ref(null)

const batchRejectVisible = ref(false)
const batchRejectReason = ref('')

const detailVisible = ref(false)
const currentRecord = ref(null)

const detailLoading = ref(false)
const detailData = ref([])
const detailTotal = ref(0)

const adjustVisible = ref(false)
const adjustFormRef = ref(null)
const adjustForm = reactive({ studentId: null, type: 'add', points: 1, remark: '' })
const adjustRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  remark: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}
const studentOptions = ref([])

const honorRules = ref([])
const certRules = ref([])
const editingCode = ref('')
const editPoints = ref(0)

const stats = reactive({
  pendingCount: 0,
  todayReviewed: 0,
  todayApproved: 0,
  todayRejected: 0,
  monthlyPoints: 0,
  totalPoints: 0,
  ruleCount: 0
})

const query = reactive({
  studentName: '',
  status: null,
  dateRange: null,
  page: 1,
  size: 10
})

const detailQuery = reactive({
  studentName: '',
  reasonType: '',
  dateRange: null,
  page: 1,
  size: 10
})

function reasonLabel(type) {
  const map = { SERVICE: '社会服务', HONOR: '荣誉奖励', CERT: '技能证书', ADJUST: '管理员调整', CERT_REVOKE: '证书撤销' }
  return map[type] || type
}

function reasonTagType(type) {
  const map = { SERVICE: 'success', HONOR: 'warning', CERT: '', ADJUST: 'info', CERT_REVOKE: 'danger' }
  return map[type] || 'info'
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.dateRange) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange
    const res = await getServiceRecords(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    tableData.value = []
    ElMessage.error('获取服务记录失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.studentName = ''
  query.status = null
  query.dateRange = null
  query.page = 1
  fetchData()
}

async function handleApprove(row) {
  await ElMessageBox.confirm(`确认通过"${row.studentName}"的积分申请吗？`, '审核通过', {
    confirmButtonText: '确认通过', type: 'success'
  })
  try {
    await reviewServiceRecord({ recordId: row.id, status: 1 })
    ElMessage.success('已通过审核')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('审核操作失败：' + (e.message || '网络异常'))
    }
  }
}

function handleReject(row) {
  currentRejectRow.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  try {
    await reviewServiceRecord({
      recordId: currentRejectRow.value.id,
      status: 2,
      comment: rejectReason.value
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('驳回操作失败：' + (e.message || '网络异常'))
  }
}

async function batchApprove() {
  const names = selectedRows.value.map(r => r.studentName).join('、')
  await ElMessageBox.confirm(`确认批量通过以下 ${selectedRows.value.length} 条申请？\n${names}`, '批量审核', {
    confirmButtonText: '确认通过', type: 'success'
  })
  try {
    let success = 0, fail = 0
    for (const row of selectedRows.value) {
      try {
        await reviewServiceRecord({ recordId: row.id, status: 1 })
        success++
      } catch { fail++ }
    }
    if (fail > 0) {
      ElMessage.warning(`批量审核完成：成功 ${success} 条，失败 ${fail} 条`)
    } else {
      ElMessage.success(`已批量通过 ${success} 条`)
    }
    selectedRows.value = []
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('批量审核操作异常：' + (e.message || '网络异常'))
    }
  }
}

function batchReject() {
  batchRejectReason.value = ''
  batchRejectVisible.value = true
}

async function confirmBatchReject() {
  try {
    let success = 0, fail = 0
    for (const row of selectedRows.value) {
      try {
        await reviewServiceRecord({ recordId: row.id, status: 2, comment: batchRejectReason.value })
        success++
      } catch { fail++ }
    }
    if (fail > 0) {
      ElMessage.warning(`批量驳回完成：成功 ${success} 条，失败 ${fail} 条`)
    } else {
      ElMessage.success(`已批量驳回 ${success} 条`)
    }
    batchRejectVisible.value = false
    selectedRows.value = []
    fetchData()
  } catch (e) {
    ElMessage.error('批量驳回操作异常：' + (e.message || '网络异常'))
  }
}

function openDetail(row) {
  currentRecord.value = row
  detailVisible.value = true
}

async function fetchDetailData() {
  detailLoading.value = true
  try {
    const params = { ...detailQuery, page: 1, size: 20 }
    if (params.dateRange) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange
    // TODO: 后端需提供全局积分明细查询接口
    detailData.value = []
    detailTotal.value = 0
  } catch (e) {
    detailData.value = []
    detailTotal.value = 0
    ElMessage.error('获取积分明细失败：' + (e.message || '网络异常'))
  } finally {
    detailLoading.value = false
  }
}

async function openAdjustDialog() {
  try {
    const res = await getStudentPage({ page: 1, size: 100 })
    studentOptions.value = (res.data?.records || []).map(s => ({
      id: s.id,
      name: s.name,
      studentNo: s.studentNo
    }))
  } catch {
    studentOptions.value = []
  }
  adjustForm.studentId = null
  adjustForm.type = 'add'
  adjustForm.points = 1
  adjustForm.remark = ''
  adjustVisible.value = true
}

async function confirmAdjust() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  // TODO: 后端需提供积分调整接口 POST /admin/social/points/adjust
  ElMessage.warning('积分调整功能开发中，请通过服务记录审核调整积分')
  adjustVisible.value = false
}

function startEdit(row) {
  editingCode.value = row.code
  editPoints.value = row.defaultPoints
}

function saveRule(row) {
  row.defaultPoints = editPoints.value
  editingCode.value = ''
  ElMessage.success('积分规则已更新')
}

async function fetchRules() {
  try {
    const [honorRes, certRes] = await Promise.all([getDictHonor(), getDictCert()])
    honorRules.value = honorRes.data || []
    certRules.value = certRes.data || []
    stats.ruleCount = honorRules.value.length + certRules.value.length
  } catch (e) {
    ElMessage.error('获取积分规则失败：' + (e.message || '网络异常'))
    honorRules.value = []
    certRules.value = []
    stats.ruleCount = 0
  }
}

onMounted(() => {
  fetchData()
  fetchRules()
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

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-tab-pane) {
  padding-top: 8px;
}
</style>