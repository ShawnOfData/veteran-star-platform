<template>
  <div class="page-container">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="openEditDialog">
        <el-icon style="margin-right: 4px"><Plus /></el-icon>发布公告
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-item">
        <div class="stat-value">{{ total }}</div>
        <div class="stat-label">全部公告</div>
        <div class="stat-icon-box all">
          <el-icon size="22"><WarningFilled /></el-icon>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.published }}</div>
        <div class="stat-label">已发布</div>
        <div class="stat-icon-box published">
          <el-icon size="22"><SuccessFilled /></el-icon>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.draft }}</div>
        <div class="stat-label">草稿</div>
        <div class="stat-icon-box draft">
          <el-icon size="22"><EditPen /></el-icon>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.closed }}</div>
        <div class="stat-label">已关闭</div>
        <div class="stat-icon-box closed">
          <el-icon size="22"><CircleCloseFilled /></el-icon>
        </div>
      </div>
    </div>

    <div class="table-card">
      <!-- 筛选栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索公告标题…" clearable style="width: 220px" size="small"
            @clear="fetchData" @keyup.enter="fetchData">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="small" @click="fetchData">查询</el-button>
        </div>
        <div class="toolbar-right">
          <el-tooltip content="刷新" placement="top">
            <el-button size="small" circle @click="fetchData">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="list" stripe v-loading="loading" :header-cell-style="{ background: '#f8fafb', color: '#1a2332', fontWeight: 600, borderBottom: '2px solid #e8ecf0' }"
        @expand-change="handleExpand">
        <el-table-column type="expand" width="36">
          <template #default="{ row }">
            <div class="expanded-content">
              <div class="expand-label">公告内容</div>
              <div class="expand-text">{{ row.content }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="#" width="56" align="center" />
        <el-table-column label="标题" min-width="160">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="priority-dot" :class="'p' + row.priority"></span>
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="内容预览" min-width="200">
          <template #default="{ row }">
            <span class="content-preview">{{ row.content?.slice(0, 50) }}{{ row.content?.length > 50 ? '…' : '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="86" align="center">
          <template #default="{ row }">
            <span class="priority-badge" :class="'p' + row.priority">
              {{ row.priority === 2 ? '紧急' : row.priority === 1 ? '重要' : '普通' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="'s' + row.status">
              {{ row.status === 1 ? '已发布' : row.status === 0 ? '草稿' : '已关闭' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="168" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <span class="action-btn edit" @click="openEditDialog(row)">
                <el-icon><Edit /></el-icon> 编辑
              </span>
              <span v-if="row.status !== 1" class="action-btn publish" @click="handlePublish(row)">
                <el-icon><Upload /></el-icon> 发布
              </span>
              <span v-if="row.status === 1" class="action-btn close" @click="handleClose(row)">
                <el-icon><CircleClose /></el-icon> 关闭
              </span>
              <span class="action-btn delete" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="640px" destroy-on-close
      :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="announce-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit
            :prefix-icon="'Edit'" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容…" maxlength="2000"
            show-word-limit />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="优先级" label-width="80px">
            <el-select v-model="form.priority" style="width: 100%">
              <el-option :value="0" label="普通">
                <span style="display: flex; align-items: center; gap: 6px;">
                  <span class="priority-dot p0"></span> 普通
                </span>
              </el-option>
              <el-option :value="1" label="重要">
                <span style="display: flex; align-items: center; gap: 6px;">
                  <span class="priority-dot p1"></span> 重要
                </span>
              </el-option>
              <el-option :value="2" label="紧急">
                <span style="display: flex; align-items: center; gap: 6px;">
                  <span class="priority-dot p2"></span> 紧急
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" label-width="80px">
            <el-radio-group v-model="form.status" class="status-radio-group">
              <el-radio-button :value="0">存草稿</el-radio-button>
              <el-radio-button :value="1">立即发布</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            {{ isEdit ? '保存修改' : '创建公告' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncementPage, createAnnouncement, updateAnnouncement, deleteAnnouncement, publishAnnouncement, closeAnnouncement } from '@/api/announcement'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

const stats = reactive({ published: 0, draft: 0, closed: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ title: '', content: '', priority: 0, status: 1 })
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAnnouncementPage({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    const data = res.data
    list.value = data?.records || []
    total.value = data?.total || 0
    // 统计
    const all = data?.records || []
    stats.published = all.filter(i => i.status === 1).length
    stats.draft = all.filter(i => i.status === 0).length
    stats.closed = all.filter(i => i.status === 2).length
  } catch (e) {
    ElMessage.error('获取公告列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function handleExpand(row, expandedRows) {
  // 展开时重新统计
}

function openEditDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.title = row.title
    form.content = row.content
    form.priority = row.priority
    form.status = row.status
  } else {
    isEdit.value = false
    editId.value = null
    form.title = ''
    form.content = ''
    form.priority = 0
    form.status = 1
  }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement(editId.value, { title: form.title, content: form.content, priority: form.priority, status: form.status })
      ElMessage.success('公告已更新')
    } else {
      await createAnnouncement({ title: form.title, content: form.content, priority: form.priority, status: form.status })
      ElMessage.success('公告已创建')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error((isEdit.value ? '更新' : '创建') + '公告失败：' + (e.message || '网络异常'))
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await publishAnnouncement(row.id)
    ElMessage.success('公告「' + row.title + '」已发布')
    fetchData()
  } catch (e) {
    ElMessage.error('发布失败：' + (e.message || '网络异常'))
  }
}

async function handleClose(row) {
  try {
    await closeAnnouncement(row.id)
    ElMessage.success('公告「' + row.title + '」已关闭')
    fetchData()
  } catch (e) {
    ElMessage.error('关闭失败：' + (e.message || '网络异常'))
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告「${row.title}」吗？此操作不可撤回。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', distinguishCancelAndClose: true }
    )
    await deleteAnnouncement(row.id)
    ElMessage.success('已删除公告「' + row.title + '」')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败：' + (e.message || '网络异常'))
    }
  }
}

onMounted(fetchData)
</script>

<style scoped>
/* ---- 统计卡片 ---- */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-item {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
  position: relative;
  overflow: hidden;
  transition: transform .2s, box-shadow .2s;
}
.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a2332;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.stat-icon-box {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: .85;
}
.stat-icon-box.all     { background: rgba(26,35,50,.08); color: #1a2332; }
.stat-icon-box.published { background: rgba(103,194,58,.12); color: #67c23a; }
.stat-icon-box.draft   { background: rgba(64,158,255,.10); color: #409eff; }
.stat-icon-box.closed  { background: rgba(144,147,153,.10); color: #909399; }

/* ---- 表格工具栏 ---- */
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* ---- 标题列 ---- */
.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.priority-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.priority-dot.p0 { background: #409eff; }
.priority-dot.p1 { background: #e6a23c; }
.priority-dot.p2 { background: #f56c6c; box-shadow: 0 0 6px rgba(245,108,108,.5); }
.title-text {
  font-weight: 500;
  color: #1a2332;
}

/* ---- 内容预览 ---- */
.content-preview {
  color: #909399;
  font-size: 13px;
}

/* ---- 优先级标签 ---- */
.priority-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  letter-spacing: .5px;
}
.priority-badge.p0 { background: rgba(64,158,255,.10); color: #409eff; }
.priority-badge.p1 { background: rgba(230,162,60,.12); color: #c47a1a; }
.priority-badge.p2 { background: rgba(245,108,108,.12); color: #f56c6c; }

/* ---- 状态标签 ---- */
.status-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
  letter-spacing: .5px;
}
.status-badge.s1 { background: rgba(103,194,58,.12); color: #67c23a; }
.status-badge.s0 { background: rgba(144,147,153,.10); color: #909399; }
.status-badge.s2 { background: rgba(245,108,108,.10); color: #f56c6c; }

/* ---- 操作按钮 ---- */
.action-group {
  display: flex;
  gap: 0;
  align-items: center;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  padding: 4px 10px;
  cursor: pointer;
  transition: all .15s;
  border-radius: 4px;
  white-space: nowrap;
}
.action-btn.edit   { color: #409eff; }
.action-btn.edit:hover { background: rgba(64,158,255,.08); }
.action-btn.publish { color: #67c23a; }
.action-btn.publish:hover { background: rgba(103,194,58,.08); }
.action-btn.close  { color: #e6a23c; }
.action-btn.close:hover  { background: rgba(230,162,60,.08); }
.action-btn.delete { color: #909399; }
.action-btn.delete:hover { color: #f56c6c; background: rgba(245,108,108,.08); }

/* ---- 展开内容 ---- */
.expanded-content {
  padding: 12px 24px 16px 56px;
  background: #fafbfc;
}
.expand-label {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 6px;
  letter-spacing: 1px;
}
.expand-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}

/* ---- 弹窗表单 ---- */
.announce-form { margin-top: 8px; }
.form-row {
  display: flex;
  gap: 20px;
}
.form-row :deep(.el-form-item) {
  flex: 1;
}
.status-radio-group :deep(.el-radio-button__inner) {
  padding: 8px 18px;
  font-size: 13px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
