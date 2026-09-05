<template>
  <div class="page-container">
    <div class="page-header">
      <h2>报名管理</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="机会标题关键词" clearable style="width: 180px" />
      <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="2" />
        <el-option label="已驳回" :value="3" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="opportunityTitle" label="机会标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentStudentId" label="学号" width="130" />
        <el-table-column prop="studentPhone" label="手机号" width="130" />
        <el-table-column prop="applyTime" label="报名时间" width="160" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'warning' : row.status === 2 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 0 ? '待审核' : row.status === 2 ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleReview(row, 2)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row, 3)">驳回</el-button>
            </template>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplicationPage, reviewApplication } from '@/api/opportunity'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  status: null,
  page: 1,
  size: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getApplicationPage(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    ElMessage.error('获取报名列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = null
  query.page = 1
  fetchData()
}

async function handleReview(row, status) {
  const actionText = status === 2 ? '通过' : '驳回'
  await ElMessageBox.confirm(
    `确定${actionText}"${row.studentName}"的报名申请吗？`,
    '审核确认',
    { confirmButtonText: '确定', type: status === 2 ? 'info' : 'warning' }
  )
  try {
    await reviewApplication(row.id, status)
    ElMessage.success(`已${actionText}`)
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('操作失败：' + (e.message || '网络异常'))
    }
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

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.text-muted {
  color: #999;
  font-size: 12px;
}
</style>
