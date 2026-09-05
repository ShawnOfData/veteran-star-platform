<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 管理员账号 -->
      <el-tab-pane label="管理员账号" name="admins">
        <div style="padding: 16px 0;">
          <el-button type="primary" size="small" @click="openAdminDialog">新增管理员</el-button>
          <el-table :data="adminList" stripe size="small" style="margin-top: 12px;">
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="row.role === 'super' ? 'danger' : 'primary'" size="small">
                  {{ row.role === 'super' ? '超级管理员' : '普通管理员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="lastLogin" label="最后登录" width="180" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <span class="text-btn" @click="openAdminDialog(row)">编辑</span>
                <span class="text-btn danger" @click="handleDeleteAdmin(row)">删除</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 字典管理 -->
      <el-tab-pane label="字典管理" name="dict">
        <div style="padding: 16px 0;">
          <div class="filter-bar" style="padding: 0 0 12px; box-shadow: none; display: flex; gap: 8px; align-items: center;">
            <el-select v-model="dictTypeFilter" placeholder="字典类型" style="width: 160px;" size="small" @change="fetchDicts">
              <el-option label="兵种" value="branch" />
              <el-option label="荣誉" value="honor" />
              <el-option label="证书" value="cert" />
              <el-option label="服务岗位" value="post-type" />
              <el-option label="就业岗位" value="job-type" />
            </el-select>
            <el-button type="primary" size="small" @click="openDictDialog">新增条目</el-button>
            <el-button type="primary" size="small" @click="handleRefreshDict" style="margin-left: auto;">刷新字典缓存</el-button>
          </div>
          <el-table :data="dictPageList" stripe size="small">
            <el-table-column prop="code" label="编码" width="140" />
            <el-table-column prop="name" label="名称" width="160" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <span class="text-btn" @click="openDictDialog(row)">编辑</span>
                <span class="text-btn danger" @click="handleDeleteDict(row)">删除</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="dictPage"
            :page-size="10"
            :total="dictList.length"
            layout="total, prev, pager, next"
            size="small"
            style="margin-top:12px"
          />
        </div>
      </el-tab-pane>

      <!-- 操作日志 -->
      <el-tab-pane label="操作日志" name="logs">
        <div style="padding: 16px 0;">
          <div class="filter-bar" style="padding: 0 0 12px; box-shadow: none;">
            <el-input v-model="logQuery.operator" placeholder="操作人" clearable style="width: 140px" size="small" />
            <el-date-picker
              v-model="logQuery.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 240px"
            />
            <el-button type="primary" size="small" @click="fetchLogs">查询</el-button>
          </div>
          <el-table :data="logList" stripe size="small">
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="action" label="操作" width="160" />
            <el-table-column prop="method" label="方法" min-width="200" />
            <el-table-column prop="ip" label="IP地址" width="140" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <el-pagination
            v-model:current-page="logQuery.page"
            v-model:page-size="logQuery.size"
            :total="logTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            size="small"
            style="margin-top:12px"
            @size-change="fetchLogs"
            @current-change="fetchLogs"
          />
        </div>
      </el-tab-pane>

      <!-- 系统配置 -->
      <el-tab-pane label="系统配置" name="sysconfig">
        <div style="padding: 16px 0;">
          <el-form v-loading="sysConfigLoading" :model="sysConfigForm" label-width="120px" class="config-form">
            <el-form-item label="站点名称">
              <el-input v-model="sysConfigForm.site_name" style="max-width: 400px" />
            </el-form-item>
            <el-form-item label="站点Logo URL">
              <el-input v-model="sysConfigForm.site_logo" style="max-width: 400px" placeholder="输入Logo图片URL" />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="sysConfigForm.site_version" style="max-width: 200px" />
            </el-form-item>
            <el-form-item label="AI API密钥">
              <el-input v-model="sysConfigForm.ai_api_key" type="password" show-password style="max-width: 400px" placeholder="AI服务API密钥" />
            </el-form-item>
            <el-form-item label="AI API地址">
              <el-input v-model="sysConfigForm.ai_api_url" style="max-width: 400px" placeholder="AI服务地址" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="sysConfigForm.contact_phone" style="max-width: 300px" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="sysConfigForm.contact_email" style="max-width: 300px" />
            </el-form-item>
            <el-form-item label="服务站地址">
              <el-input v-model="sysConfigForm.contact_address" style="max-width: 500px" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveSysConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 管理员弹窗 -->
    <el-dialog v-model="adminDialogVisible" :title="adminDialogTitle" width="420px" destroy-on-close>
      <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" :disabled="isEditAdmin" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEditAdmin ? '' : 'password'">
          <el-input v-model="adminForm.password" type="password" :placeholder="isEditAdmin ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="adminForm.role" style="width: 100%">
            <el-option label="普通管理员" value="admin" />
            <el-option label="超级管理员" value="super" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdmin">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典条目弹窗 -->
    <el-dialog v-model="dictDialogVisible" :title="dictDialogTitle" width="420px" destroy-on-close>
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="80px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="dictForm.code" :disabled="isEditDict" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="dictForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dictForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDict">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDictBranch, getDictHonor, getDictCert, getDictPostType, getDictJobType, refreshDictCache, addDictItem, updateDictItem, deleteDictItem } from '@/api/dict'
import { getAdminList, createAdmin, updateAdmin, deleteAdmin } from '@/api/auth'
import { getOperationLogs } from '@/api/operationLog'
import { getSysConfig, updateSysConfig } from '@/api/sysConfig'

const activeTab = ref('admins')

// ==================== 管理员账号 ====================
const adminList = ref([])
const adminDialogVisible = ref(false)
const adminDialogTitle = ref('新增管理员')
const isEditAdmin = ref(false)
const editAdminId = ref(null)
const adminFormRef = ref(null)
const adminForm = reactive({ username: '', password: '', role: 'admin' })
const adminRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function openAdminDialog(row) {
  if (row) {
    isEditAdmin.value = true
    editAdminId.value = row.id
    adminDialogTitle.value = '编辑管理员'
    adminForm.username = row.username
    adminForm.password = ''
    adminForm.role = row.role || 'admin'
  } else {
    isEditAdmin.value = false
    editAdminId.value = null
    adminDialogTitle.value = '新增管理员'
    adminForm.username = ''
    adminForm.password = ''
    adminForm.role = 'admin'
  }
  adminDialogVisible.value = true
}

async function submitAdmin() {
  const valid = await adminFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEditAdmin.value) {
      await updateAdmin(editAdminId.value, { username: adminForm.username, password: adminForm.password || undefined, role: adminForm.role })
    } else {
      await createAdmin({ username: adminForm.username, password: adminForm.password, role: adminForm.role })
    }
    ElMessage.success(isEditAdmin.value ? '管理员已更新' : '管理员已创建')
    adminDialogVisible.value = false
    fetchAdmins()
  } catch (e) {
    ElMessage.error((isEditAdmin.value ? '更新' : '创建') + '管理员失败：' + (e.message || '网络异常'))
  }
}

async function handleDeleteAdmin(row) {
  await ElMessageBox.confirm(`确定要删除管理员"${row.username}"吗？`, '确认删除', { type: 'warning', confirmButtonText: '确定' })
  try {
    await deleteAdmin(row.id)
    ElMessage.success('已删除')
    fetchAdmins()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除管理员失败：' + (e.message || '网络异常'))
    }
  }
}

async function fetchAdmins() {
  try {
    const res = await getAdminList()
    adminList.value = res.data || []
  } catch (e) {
    ElMessage.error('获取管理员列表失败：' + (e.message || '网络异常'))
  }
}

// ==================== 字典管理 ====================
const dictTypeFilter = ref('branch')
const dictList = ref([])
const dictPage = ref(1)
const dictPageList = computed(() => {
  const start = (dictPage.value - 1) * 10
  return dictList.value.slice(start, start + 10)
})
const dictDialogVisible = ref(false)
const dictDialogTitle = ref('新增条目')
const isEditDict = ref(false)
const editDictId = ref(null)
const dictFormRef = ref(null)
const dictForm = reactive({ code: '', name: '', sortOrder: 0 })
const dictRules = {
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

const dictTypeLabelMap = { 'branch': '兵种', 'honor': '荣誉', 'cert': '证书', 'post-type': '服务岗位', 'job-type': '就业岗位' }

function openDictDialog(row) {
  if (row) {
    isEditDict.value = true
    editDictId.value = row.id
    dictDialogTitle.value = '编辑条目'
    dictForm.code = row.code
    dictForm.name = row.name
    dictForm.sortOrder = row.sortOrder || 0
  } else {
    isEditDict.value = false
    editDictId.value = null
    dictDialogTitle.value = '新增条目'
    dictForm.code = ''
    dictForm.name = ''
    dictForm.sortOrder = 0
  }
  dictDialogVisible.value = true
}

async function submitDict() {
  const valid = await dictFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEditDict.value) {
      await updateDictItem(dictTypeFilter.value, editDictId.value, { ...dictForm })
    } else {
      await addDictItem(dictTypeFilter.value, { ...dictForm })
    }
    ElMessage.success(isEditDict.value ? '条目已更新' : '条目已新增')
    dictDialogVisible.value = false
    fetchDicts()
  } catch (e) {
    ElMessage.error((isEditDict.value ? '更新' : '新增') + '条目失败：' + (e.message || '网络异常'))
  }
}

async function handleDeleteDict(row) {
  await ElMessageBox.confirm(`确定要删除"${row.name}"吗？`, '确认删除', { type: 'warning', confirmButtonText: '确定' })
  try {
    await deleteDictItem(dictTypeFilter.value, row.id)
    ElMessage.success('已删除')
    fetchDicts()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败：' + (e.message || '网络异常'))
    }
  }
}

async function handleRefreshDict() {
  try {
    await refreshDictCache()
    ElMessage.success('字典缓存已刷新')
  } catch (e) {
    ElMessage.error('刷新字典缓存失败：' + (e.message || '网络异常'))
  }
}

async function fetchDicts() {
  try {
    const type = dictTypeFilter.value
    let res
    switch (type) {
      case 'branch': res = await getDictBranch(); break
      case 'honor': res = await getDictHonor(); break
      case 'cert': res = await getDictCert(); break
      case 'post-type': res = await getDictPostType(); break
      case 'job-type': res = await getDictJobType(); break
    }
    dictList.value = res.data || []
    dictPage.value = 1
  } catch (e) {
    dictList.value = []
    ElMessage.error('获取字典数据失败：' + (e.message || '网络异常'))
  }
}

// ==================== 操作日志 ====================
const logList = ref([])
const logTotal = ref(0)
const logQuery = reactive({ operator: '', dateRange: null, page: 1, size: 10 })

async function fetchLogs() {
  try {
    const params = {
      page: logQuery.page,
      size: logQuery.size
    }
    if (logQuery.operator) params.operator = logQuery.operator
    if (logQuery.dateRange && logQuery.dateRange.length === 2) {
      params.startDate = logQuery.dateRange[0]
      params.endDate = logQuery.dateRange[1]
    }
    const res = await getOperationLogs(params)
    logList.value = res.data.records || []
    logTotal.value = res.data.total || 0
  } catch (e) {
    logList.value = []
    logTotal.value = 0
    ElMessage.error('获取操作日志失败：' + (e.message || '网络异常'))
  }
}

// ==================== 系统配置 ====================
const sysConfigLoading = ref(false)
const sysConfigForm = reactive({
  site_name: '',
  site_logo: '',
  site_version: '',
  ai_api_key: '',
  ai_api_url: '',
  contact_phone: '',
  contact_email: '',
  contact_address: ''
})

async function fetchSysConfig() {
  sysConfigLoading.value = true
  try {
    const res = await getSysConfig()
    const list = res.data || []
    for (const item of list) {
      if (sysConfigForm.hasOwnProperty(item.configKey)) {
        sysConfigForm[item.configKey] = item.configValue || ''
      }
    }
  } catch (e) {
    ElMessage.error('获取系统配置失败：' + (e.message || '网络异常'))
  } finally {
    sysConfigLoading.value = false
  }
}

async function handleSaveSysConfig() {
  try {
    const configs = Object.entries(sysConfigForm).map(([key, value]) => ({
      configKey: key,
      configValue: value || ''
    }))
    await updateSysConfig(configs)
    ElMessage.success('系统配置已保存')
  } catch (e) {
    ElMessage.error('保存系统配置失败：' + (e.message || '网络异常'))
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchAdmins()
  fetchDicts()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.config-form {
  max-width: 600px;
}
.text-btn {
  color: var(--color-primary, #409eff);
  cursor: pointer;
  font-size: 13px;
  margin-right: 12px;
}
.text-btn.danger {
  color: #f56c6c;
}
.text-btn:hover {
  opacity: 0.8;
}
</style>
