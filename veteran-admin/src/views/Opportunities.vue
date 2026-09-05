<template>
  <div class="page-container">
    <div class="page-header">
      <h2>机会管理</h2>
      <el-button type="primary" @click="openCreateDialog">发布机会</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.title" placeholder="标题关键词" clearable style="width: 160px" />
      <el-select v-model="query.type" placeholder="机会类型" clearable style="width: 130px">
        <el-option label="就业岗位" value="job" />
        <el-option label="实习机会" value="intern" />
        <el-option label="创业扶持" value="startup" />
        <el-option label="培训课程" value="training" />
        <el-option label="政策福利" value="policy" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已关闭" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              style="width: 50px; height: 50px"
              fit="cover"
              :preview-src-list="[row.coverUrl]"
              preview-teleported
            />
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="unitName" label="发布单位" width="140" show-overflow-tooltip />
        <el-table-column prop="salaryRange" label="薪资范围" width="120" />
        <el-table-column prop="demandCount" label="需求人数" width="80" align="center" />
        <el-table-column prop="currentApplied" label="已报名" width="70" align="center" />
        <el-table-column prop="viewCount" label="浏览量" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'"
              size="small"
            >
              {{ row.status === 1 ? '已发布' : row.status === 0 ? '草稿' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <span class="text-btn" @click="openEditDialog(row)">编辑</span>
            <span v-if="row.status === 0" class="text-btn" @click="handlePublish(row)">发布</span>
            <span v-if="row.status === 1" class="text-btn danger" @click="handleClose(row)">关闭</span>
            <span class="text-btn danger" @click="handleDelete(row)">删除</span>
            <span class="text-btn" @click="openApplicationDialog(row)">
              <el-icon style="vertical-align: -0.15em; margin-right: 2px"><Document /></el-icon>报名管理
            </span>
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

    <!-- 机会表单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入机会标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%" @change="onTypeChange">
            <el-option label="就业岗位" value="job" />
            <el-option label="实习机会" value="intern" />
            <el-option label="创业扶持" value="startup" />
            <el-option label="培训课程" value="training" />
            <el-option label="政策福利" value="policy" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布单位" prop="unitName">
          <el-input v-model="form.unitName" placeholder="请输入发布单位名称" />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverUrl">
          <el-upload
            :auto-upload="true"
            :action="uploadCoverUrl"
            :headers="uploadHeaders"
            :on-success="handleCoverSuccess"
            :on-error="handleCoverError"
            :before-upload="beforeCoverUpload"
            accept="image/png,image/jpeg,image/jpg,image/gif"
            :show-file-list="false"
          >
            <div class="cover-preview" v-if="form.coverUrl">
              <el-image
                :src="form.coverUrl"
                fit="cover"
                style="width: 200px; height: 120px; border-radius: 4px"
              />
              <el-button type="danger" size="small" circle @click="form.coverUrl = ''" style="margin-left: 8px; margin-top: 8px">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传封面图</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入机会描述" />
        </el-form-item>
        <!-- 任职要求：就业/实习/培训 -->
        <el-form-item v-if="showField('requirements')" label="任职要求" prop="requirements">
          <el-input v-model="form.requirements" type="textarea" :rows="3" placeholder="请输入任职要求" />
        </el-form-item>
        <!-- 地址：就业/实习/培训 -->
        <el-form-item v-if="showField('address')" label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="16">
          <!-- 薪资/费用：就业/实习/培训 -->
          <el-col v-if="showField('salaryRange')" :span="12">
            <el-form-item label="薪资范围" prop="salaryRange">
              <el-input v-model="form.salaryRange" placeholder="例如：8K-15K" />
            </el-form-item>
          </el-col>
          <!-- 需求人数：就业/实习/创业/培训（政策无） -->
          <el-col v-if="showField('demandCount')" :span="12">
            <el-form-item label="需求人数" prop="demandCount">
              <el-input-number v-model="form.demandCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 联系人/电话：就业/实习/创业/培训（政策无） -->
        <el-row v-if="showField('contact')" :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
          <div class="form-tip">多个标签用逗号分隔</div>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报名管理弹窗 -->
    <el-dialog v-model="appDialog.visible" title="报名管理" width="800px" destroy-on-close>
      <template #header>
        <span>报名管理 - {{ appDialog.opportunityTitle }}</span>
      </template>
      <el-table :data="appDialog.list" stripe v-loading="appDialog.loading">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentStudentId" label="学号" width="120" />
        <el-table-column prop="studentPhone" label="联系电话" width="120" />
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
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">驳回</el-button>
            </template>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="appDialog.page"
        v-model:page-size="appDialog.size"
        :total="appDialog.total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchApplications"
        @current-change="fetchApplications"
        style="margin-top: 16px"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Plus, Delete } from '@element-plus/icons-vue'
import {
  getOpportunityPage, createOpportunity, updateOpportunity,
  deleteOpportunity, publishOpportunity, closeOpportunity,
  getApplicationPage, reviewApplication
} from '@/api/opportunity'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('发布机会')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const uploadCoverUrl = '/api/admin/opportunity/upload-cover'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

const query = reactive({
  title: '',
  type: '',
  status: null,
  page: 1,
  size: 10
})

const form = reactive({
  title: '',
  type: 'job',
  unitName: '',
  coverUrl: '',
  description: '',
  requirements: '',
  address: '',
  salaryRange: '',
  startTime: '',
  endTime: '',
  demandCount: 0,
  contactName: '',
  contactPhone: '',
  tags: '',
  priority: 0
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const appDialog = reactive({
  visible: false,
  opportunityTitle: '',
  opportunityId: null,
  loading: false,
  list: [],
  total: 0,
  page: 1,
  size: 10
})

function typeLabel(type) {
  const map = { job: '就业岗位', intern: '实习机会', startup: '创业扶持', training: '培训课程', policy: '政策福利' }
  return map[type] || type
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getOpportunityPage(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    ElMessage.error('获取机会列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.title = ''
  query.type = ''
  query.status = null
  query.page = 1
  fetchData()
}

// 各类型对应的可见字段配置
const typeFields = {
  job:      ['requirements', 'address', 'salaryRange', 'demandCount', 'contact'],
  intern:   ['requirements', 'address', 'salaryRange', 'demandCount', 'contact'],
  startup:  ['demandCount', 'contact'],
  training: ['requirements', 'address', 'demandCount', 'contact'],
  policy:   []
}

function showField(name) {
  return typeFields[form.type]?.includes(name) ?? true
}

function onTypeChange() {
  // 切换类型时清空不显示的字段值
  if (!showField('requirements')) form.requirements = ''
  if (!showField('address')) form.address = ''
  if (!showField('salaryRange')) form.salaryRange = ''
  if (!showField('demandCount')) form.demandCount = 0
  if (!showField('contact')) {
    form.contactName = ''
    form.contactPhone = ''
  }
  // 清除表单验证状态
  formRef.value?.clearValidate()
}

function resetForm() {
  form.title = ''
  form.type = 'job'
  form.unitName = ''
  form.coverUrl = ''
  form.description = ''
  form.requirements = ''
  form.address = ''
  form.salaryRange = ''
  form.startTime = ''
  form.endTime = ''
  form.demandCount = 0
  form.contactName = ''
  form.contactPhone = ''
  form.tags = ''
  form.priority = 0
}

function handleCoverSuccess(response) {
  if (response.code === 200) {
    form.coverUrl = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleCoverError() {
  ElMessage.error('封面上传失败，请重试')
}

function beforeCoverUpload(file) {
  const ok = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif'].includes(file.type)
  if (!ok) ElMessage.error('仅支持 png/jpg/gif 格式')
  return ok
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '发布机会'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑机会'
  form.title = row.title || ''
  form.type = row.type || 'job'
  form.unitName = row.unitName || ''
  form.coverUrl = row.coverUrl || ''
  form.description = row.description || ''
  form.requirements = row.requirements || ''
  form.address = row.address || ''
  form.salaryRange = row.salaryRange || ''
  form.startTime = row.startTime || ''
  form.endTime = row.endTime || ''
  form.demandCount = row.demandCount ?? 0
  form.contactName = row.contactName || ''
  form.contactPhone = row.contactPhone || ''
  form.tags = row.tags || ''
  form.priority = row.priority ?? 0
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateOpportunity(editId.value, form)
      ElMessage.success('机会已更新')
    } else {
      await createOpportunity(form)
      ElMessage.success('机会已创建')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error((isEdit.value ? '更新' : '创建') + '机会失败：' + (e.message || '网络异常'))
  }
}

async function handlePublish(row) {
  await ElMessageBox.confirm(`确认发布"${row.title}"吗？`, '发布确认', { confirmButtonText: '确认发布' })
  try {
    await publishOpportunity(row.id)
    ElMessage.success('已发布')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('发布失败：' + (e.message || '网络异常'))
    }
  }
}

async function handleClose(row) {
  await ElMessageBox.confirm(`确认关闭"${row.title}"吗？`, '关闭确认', { confirmButtonText: '确认关闭', type: 'warning' })
  try {
    await closeOpportunity(row.id)
    ElMessage.success('已关闭')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('关闭失败：' + (e.message || '网络异常'))
    }
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除"${row.title}"吗？`, '确认删除', { type: 'warning', confirmButtonText: '确定' })
  try {
    await deleteOpportunity(row.id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败：' + (e.message || '网络异常'))
    }
  }
}

// 报名管理
function openApplicationDialog(row) {
  appDialog.opportunityId = row.id
  appDialog.opportunityTitle = row.title
  appDialog.page = 1
  appDialog.visible = true
  fetchApplications()
}

async function fetchApplications() {
  appDialog.loading = true
  try {
    const res = await getApplicationPage({
      opportunityId: appDialog.opportunityId,
      page: appDialog.page,
      size: appDialog.size
    })
    appDialog.list = res.data.records || []
    appDialog.total = res.data.total || 0
  } catch (e) {
    appDialog.list = []
    ElMessage.error('获取报名列表失败：' + (e.message || '网络异常'))
  } finally {
    appDialog.loading = false
  }
}

async function handleApprove(row) {
  await ElMessageBox.confirm(`确定通过"${row.studentName}"的报名申请吗？`, '审核确认', { confirmButtonText: '确定', type: 'info' })
  try {
    await reviewApplication(row.id, 2)
    ElMessage.success('已通过')
    fetchApplications()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('操作失败：' + (e.message || '网络异常'))
    }
  }
}

async function handleReject(row) {
  await ElMessageBox.confirm(`确定驳回"${row.studentName}"的报名申请吗？`, '审核确认', { confirmButtonText: '确定', type: 'warning' })
  try {
    await reviewApplication(row.id, 3)
    ElMessage.success('已驳回')
    fetchApplications()
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

.text-btn {
  color: #409eff;
  cursor: pointer;
  margin-right: 12px;
  font-size: 13px;
  white-space: nowrap;
}

.text-btn:hover {
  color: #66b1ff;
}

.text-btn.danger {
  color: #f56c6c;
}

.text-btn.danger:hover {
  color: #f89898;
}

.text-muted {
  color: #999;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.upload-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  margin-top: 8px;
  cursor: pointer;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.cover-preview {
  display: flex;
  align-items: flex-start;
}
</style>
