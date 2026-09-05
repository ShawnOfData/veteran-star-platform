<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学生管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增学生</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.studentNo" placeholder="学号" clearable style="width: 140px" />
      <el-input v-model="query.name" placeholder="姓名" clearable style="width: 140px" />
      <el-select v-model="query.college" placeholder="学院" clearable style="width: 140px">
        <el-option label="信息学院" value="信息学院" />
        <el-option label="机械学院" value="机械学院" />
        <el-option label="经管学院" value="经管学院" />
      </el-select>
      <el-select v-model="query.grade" placeholder="年级" clearable style="width: 120px">
        <el-option label="2022级" value="2022" />
        <el-option label="2023级" value="2023" />
        <el-option label="2024级" value="2024" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="在校" :value="1" />
        <el-option label="毕业" :value="2" />
        <el-option label="休学" :value="3" />
      </el-select>
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="college" label="学院" width="120" />
        <el-table-column prop="major" label="专业" width="140" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="totalPoints" label="累计积分" width="100" sortable>
          <template #default="{ row }">
            <span class="gold-text">{{ row.totalPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
              {{ row.status === 1 ? '在校' : row.status === 2 ? '毕业' : '休学' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <span class="text-btn" @click="openDetail(row)">详情</span>
            <span class="text-btn" @click="openEditDialog(row)">编辑</span>
            <span class="text-btn danger" @click="handleDelete(row)">删除</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-select v-model="form.ethnicity" filterable allow-create placeholder="选择民族" style="width: 100%">
                <el-option v-for="n in ethnicityOptions" :key="n" :label="n" :value="n" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生年月">
              <el-date-picker v-model="form.birthDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="form.nativePlace" placeholder="如：河北省石家庄市" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-select v-model="form.politicalStatus" placeholder="选择政治面貌" style="width: 100%">
                <el-option v-for="p in politicalStatusOptions" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学院" prop="college">
              <el-input v-model="form.college" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-input v-model="form.grade" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学时间">
              <el-date-picker v-model="form.enrollDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退役时间">
              <el-date-picker v-model="form.retireDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在校" :value="1" />
                <el-option label="毕业" :value="2" />
                <el-option label="休学" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="学生详情" size="640px">
      <template v-if="currentStudent">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentStudent.gender || '-' }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ currentStudent.ethnicity || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出生年月">{{ toYM(currentStudent.birthDate) }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ currentStudent.nativePlace || '-' }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ currentStudent.politicalStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ currentStudent.college }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ currentStudent.grade }}</el-descriptions-item>
          <el-descriptions-item label="入学时间">{{ toYM(currentStudent.enrollDate) }}</el-descriptions-item>
          <el-descriptions-item label="退役时间">{{ toYM(currentStudent.retireDate) }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentStudent.phone }}</el-descriptions-item>
          <el-descriptions-item label="累计积分">
            <span class="gold-text">{{ currentStudent.totalPoints }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentStudent.status === 1 ? 'success' : 'info'" size="small">
              {{ currentStudent.status === 1 ? '在校' : '毕业' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 12px; color: var(--color-primary);">服役经历</h4>
        <el-table :data="serviceExperiences" size="small" stripe>
          <el-table-column prop="branchName" label="军兵种" />
          <el-table-column label="骨干职务">
            <template #default="{ row }">{{ row.leaderPostName || '-' }}</template>
          </el-table-column>
          <el-table-column label="入伍时间" width="100">
            <template #default="{ row }">{{ toYM(row.startDate) }}</template>
          </el-table-column>
          <el-table-column label="退役时间" width="100">
            <template #default="{ row }">{{ toYM(row.endDate) }}</template>
          </el-table-column>
          <el-table-column prop="serviceYears" label="年限" width="70" />
        </el-table>

        <h4 style="margin: 20px 0 12px; color: var(--color-primary);">受奖情况</h4>
        <el-table :data="honors" size="small" stripe>
          <el-table-column prop="honorName" label="表彰奖励" min-width="120" />
          <el-table-column label="荣誉类别" width="100">
            <template #default="{ row }">{{ row.honorCategoryName || '-' }}</template>
          </el-table-column>
          <el-table-column label="受奖时间" width="100">
            <template #default="{ row }">{{ toYM(row.awardDate) }}</template>
          </el-table-column>
          <el-table-column prop="pointsAwarded" label="积分" width="80" />
        </el-table>

        <h4 style="margin: 20px 0 12px; color: var(--color-primary);">技能证书</h4>
        <el-table :data="skills" size="small" stripe>
          <el-table-column prop="certName" label="证书类型" min-width="120" />
          <el-table-column prop="certNo" label="证书编号" />
          <el-table-column prop="obtainDate" label="获得日期" width="120" />
          <el-table-column prop="pointsAwarded" label="积分" width="80" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStudentPage, getStudentById, createStudent, updateStudent, deleteStudent,
  getServiceExperiences, getHonors, getSkills
} from '@/api/student'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const detailVisible = ref(false)
const currentStudent = ref(null)
const serviceExperiences = ref([])
const honors = ref([])
const skills = ref([])

const query = reactive({
  studentNo: '',
  name: '',
  college: '',
  grade: '',
  status: null,
  page: 1,
  size: 10
})

const form = reactive({
  studentNo: '',
  name: '',
  gender: '',
  ethnicity: '',
  birthDate: '',
  nativePlace: '',
  politicalStatus: '',
  college: '',
  major: '',
  grade: '',
  enrollDate: '',
  retireDate: '',
  phone: '',
  status: 1
})

// 民族/政治面貌选项
const ethnicityOptions = [
  '汉族', '满族', '蒙古族', '回族', '藏族', '壮族', '维吾尔族', '苗族', '彝族', '土家族',
  '布依族', '侗族', '瑶族', '白族', '朝鲜族', '哈尼族', '哈萨克族', '黎族', '傣族', '畲族',
  '傈僳族', '仡佬族', '东乡族', '高山族', '拉祜族', '水族', '佤族', '纳西族', '羌族', '土族'
]
const politicalStatusOptions = ['中共党员', '中共预备党员', '共青团员', '群众']

// 年月展示：后端存当月1日(YYYY-MM-DD)，前端只展示 YYYY-MM
function toYM(value) {
  return value ? String(value).substring(0, 7) : '-'
}

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStudentPage(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    tableData.value = []
    ElMessage.error('获取学生列表失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.studentNo = ''
  query.name = ''
  query.college = ''
  query.grade = ''
  query.status = null
  query.page = 1
  fetchData()
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增学生'
  Object.assign(form, {
    studentNo: '', name: '', gender: '', ethnicity: '', birthDate: '',
    nativePlace: '', politicalStatus: '', college: '', major: '', grade: '',
    enrollDate: '', retireDate: '', phone: '', status: 1
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑学生'
  Object.assign(form, {
    studentNo: row.studentNo,
    name: row.name,
    gender: row.gender || '',
    ethnicity: row.ethnicity || '',
    birthDate: toYM(row.birthDate),
    nativePlace: row.nativePlace || '',
    politicalStatus: row.politicalStatus || '',
    college: row.college || '',
    major: row.major || '',
    grade: row.grade || '',
    enrollDate: toYM(row.enrollDate),
    retireDate: toYM(row.retireDate),
    phone: '',
    status: row.status
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateStudent(editId.value, form)
      ElMessage.success('学生档案已更新')
    } else {
      await createStudent(form)
      ElMessage.success('学生档案已创建')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error((isEdit.value ? '更新' : '创建') + '学生失败：' + (e.message || '网络异常'))
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除学生"${row.name}"吗？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  try {
    await deleteStudent(row.id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败：' + (e.message || '网络异常'))
    }
  }
}

async function openDetail(row) {
  currentStudent.value = row
  detailVisible.value = true
  try {
    const [seRes, skillsRes] = await Promise.all([
      getServiceExperiences(row.id),
      getSkills(row.id)
    ])
    serviceExperiences.value = seRes.data || []
    skills.value = skillsRes.data || []

    if (serviceExperiences.value.length > 0) {
      const honorRes = await getHonors(serviceExperiences.value[0].id)
      honors.value = honorRes.data || []
    } else {
      honors.value = []
    }
  } catch (e) {
    serviceExperiences.value = []
    honors.value = []
    skills.value = []
    ElMessage.error('获取学生详情失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchData()
})
</script>