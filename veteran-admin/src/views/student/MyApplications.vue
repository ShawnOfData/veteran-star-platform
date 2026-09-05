<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的报名</h2>
    </div>

    <div class="table-card" v-loading="loading">
      <el-table :data="tableData" stripe style="width: 100%" v-if="tableData.length > 0">
        <el-table-column label="机会标题" min-width="180">
          <template #default="{ row }">
            <span class="opp-title-link">{{ row.opportunityTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.opportunityType)">{{ typeLabel(row.opportunityType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="170">
          <template #default="{ row }">
            {{ row.applyTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140">
          <template #default="{ row }">
            <span v-if="row.remark">{{ row.remark }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消报名
            </el-button>
            <el-button
              type="primary"
              size="small"
              plain
              @click="viewOpportunity(row)"
            >
              查看机会
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无报名记录" />
    </div>

    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, prev, pager, next"
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 20px; justify-content: center;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, cancelApplication } from '@/api/studentApp'

const studentStore = useStudentStore()

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ page: 1, size: 10 })

function typeLabel(type) {
  const map = { job: '就业岗位', intern: '实习机会', startup: '创业扶持', training: '培训课程', policy: '政策福利' }
  return map[type] || type
}

function typeTag(type) {
  const map = { job: 'success', intern: '', startup: 'warning', training: 'info', policy: 'danger' }
  return map[type] || 'info'
}

function statusLabel(status) {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] !== undefined ? map[status] : '未知'
}

function statusTag(status) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyApplications(studentStore.studentId, query.value)
    const data = res.data
    if (data && data.records) {
      tableData.value = data.records
      total.value = data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    tableData.value = []
    total.value = 0
    ElMessage.error('获取报名记录失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

async function handleCancel(app) {
  try {
    await ElMessageBox.confirm('确定要取消该报名吗？', '取消报名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelApplication(app.id, studentStore.studentId)
    ElMessage.success('已取消报名')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败：' + (e.message || '网络异常'))
    }
  }
}

function viewOpportunity(app) {
  window.open(`/opportunities?id=${app.opportunityId}`, '_blank')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.opp-title-link {
  font-weight: 600;
  color: #409eff;
}

.text-muted {
  color: #c0c4cc;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: var(--shadow-card);
  padding: 20px;
  margin-top: 20px;
}
</style>
