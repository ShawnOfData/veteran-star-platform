<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人档案</h2>
      <div class="header-actions">
        <el-button @click="router.push('/portrait')">我的画像</el-button>
        <el-button @click="router.push('/settings')">设置</el-button>
        <el-button type="primary" @click="editMode = !editMode">
          {{ editMode ? '取消编辑' : '编辑档案' }}
        </el-button>
      </div>
    </div>

    <div class="profile-card">
      <div class="profile-avatar">
        <el-icon :size="48"><UserFilled /></el-icon>
      </div>
      <div class="profile-basic">
        <h3>{{ profile.name }}</h3>
        <p>{{ profile.studentNo }} | {{ profile.college }} · {{ profile.major }}</p>
      </div>
    </div>

    <div class="section-card">
      <h4 class="section-title">基本信息</h4>
      <el-form :model="profile" label-width="100px" :disabled="!editMode">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="profile.studentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="profile.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="profile.gender">
                <el-radio value="男">男</el-radio>
                <el-radio value="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族">
              <el-select v-model="profile.ethnicity" filterable allow-create placeholder="请选择民族" style="width: 100%">
                <el-option v-for="n in ethnicityOptions" :key="n" :label="n" :value="n" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生年月">
              <el-date-picker v-model="profile.birthDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="籍贯">
              <el-input v-model="profile.nativePlace" placeholder="如：河北省石家庄市" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌">
              <el-select v-model="profile.politicalStatus" placeholder="请选择政治面貌" style="width: 100%">
                <el-option
                  v-for="p in politicalStatusOptions"
                  :key="p"
                  :label="p"
                  :value="p"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学院">
              <el-input v-model="profile.college" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="profile.major" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级">
              <el-input v-model="profile.grade" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学时间">
              <el-date-picker v-model="profile.enrollDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退役时间">
              <el-date-picker v-model="profile.retireDate" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="editMode" class="drawer-footer">
          <el-button @click="editMode = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </div>
      </el-form>
    </div>

    <!-- 服役经历 -->
    <div class="section-card">
      <div class="section-header">
        <h4 class="section-title">服役经历</h4>
        <el-button type="primary" size="small" @click="seDialog.visible = true">
          <el-icon><Plus /></el-icon> 提交申请
        </el-button>
      </div>
      <el-table :data="seList" stripe size="small" v-loading="loading">
        <el-table-column prop="branchName" label="军兵种" width="110" />
        <el-table-column prop="leaderPostName" label="骨干职务" min-width="140" show-overflow-tooltip />
        <el-table-column prop="startDate" label="入伍时间" width="100" />
        <el-table-column prop="endDate" label="退役时间" width="100" />
        <el-table-column prop="serviceYears" label="年限" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 受奖情况 -->
    <div class="section-card">
      <div class="section-header">
        <h4 class="section-title">受奖情况</h4>
        <el-button type="primary" size="small" @click="honorDialog.visible = true">
          <el-icon><Plus /></el-icon> 提交申请
        </el-button>
      </div>
      <el-table :data="honorList" stripe size="small" v-loading="loading">
        <el-table-column prop="honorName" label="表彰奖励" min-width="120" />
        <el-table-column prop="honorCategoryName" label="荣誉类别" width="100" />
        <el-table-column prop="awardDate" label="受奖时间" width="100" />
        <el-table-column prop="pointsAwarded" label="积分" width="70">
          <template #default="{ row }">
            <span class="gold-text">+{{ row.pointsAwarded }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 技能证书 -->
    <div class="section-card">
      <div class="section-header">
        <h4 class="section-title">技能证书</h4>
        <el-button type="primary" size="small" @click="skillDialog.visible = true">
          <el-icon><Plus /></el-icon> 提交申请
        </el-button>
      </div>
      <el-table :data="skillList" stripe size="small" v-loading="loading">
        <el-table-column prop="certName" label="证书类型" min-width="100" />
        <el-table-column prop="certNo" label="证书编号" min-width="120" />
        <el-table-column prop="obtainDate" label="获得日期" width="110" />
        <el-table-column prop="validUntil" label="有效期" width="110" />
        <el-table-column prop="pointsAwarded" label="积分" width="70">
          <template #default="{ row }">
            <span class="gold-text">+{{ row.pointsAwarded }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 服役经历提交弹窗 -->
    <el-dialog v-model="seDialog.visible" title="提交服役经历" width="480px">
      <el-form :model="seDialog.form" label-width="80px" size="small" ref="seFormRef" :rules="seRules">
        <el-form-item label="军兵种" prop="branchCode">
          <el-select v-model="seDialog.form.branchCode" placeholder="选择军兵种" style="width: 100%">
            <el-option v-for="b in branchList" :key="b.code" :label="b.name" :value="b.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="骨干职务" prop="leaderPostCode">
          <el-select v-model="seDialog.form.leaderPostCode" placeholder="选择骨干职务" style="width: 100%">
            <el-option v-for="p in leaderPostList" :key="p.code" :label="p.name" :value="p.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="入伍时间" prop="startDate">
          <el-date-picker v-model="seDialog.form.startDate" type="month" value-format="YYYY-MM" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退役时间" prop="endDate">
          <el-date-picker v-model="seDialog.form.endDate" type="month" value-format="YYYY-MM" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="seDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitSE" :loading="seDialog.loading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 服役经历冲突弹窗 -->
    <el-dialog v-model="seConflict.visible" title="服役经历冲突" width="500px">
      <div style="margin-bottom:16px">
        <el-alert title="您已有一条服役经历记录，提交的内容与现有记录不一致" type="warning" :closable="false" show-icon />
      </div>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="字段" :span="1">旧记录</el-descriptions-item>
        <el-descriptions-item label="新提交" :span="1">新提交</el-descriptions-item>
        <el-descriptions-item label="军兵种">{{ seConflict.oldData?.branchName }}</el-descriptions-item>
        <el-descriptions-item label="军兵种">{{ seConflict.newData?.branchName }}</el-descriptions-item>
        <el-descriptions-item label="骨干职务">{{ seConflict.oldData?.leaderPostName }}</el-descriptions-item>
        <el-descriptions-item label="骨干职务">{{ seConflict.newData?.leaderPostName }}</el-descriptions-item>
        <el-descriptions-item label="入伍时间">{{ seConflict.oldData?.startDate }}</el-descriptions-item>
        <el-descriptions-item label="入伍时间">{{ seConflict.newData?.startDate }}</el-descriptions-item>
        <el-descriptions-item label="退役时间">{{ seConflict.oldData?.endDate }}</el-descriptions-item>
        <el-descriptions-item label="退役时间">{{ seConflict.newData?.endDate }}</el-descriptions-item>
      </el-descriptions>
      <div style="font-size:13px;color:var(--color-text-secondary);margin-top:12px">
        确认修改后，原记录将被覆盖并重新进入待审状态。
      </div>
      <template #footer>
        <el-button @click="seConflict.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateSE" :loading="seConflict.loading">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 受奖情况提交弹窗 -->
    <el-dialog v-model="honorDialog.visible" title="提交受奖情况" width="480px">
      <el-form :model="honorDialog.form" label-width="100px" size="small" ref="honorFormRef" :rules="honorRules">
        <el-form-item label="荣誉类别" prop="honorCategoryCode">
          <el-select v-model="honorDialog.form.honorCategoryCode" placeholder="选择荣誉类别" style="width: 100%">
            <el-option v-for="c in honorCategoryList" :key="c.code" :label="c.name" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="表彰奖励" prop="honorCode">
          <el-select v-model="honorDialog.form.honorCode" placeholder="选择表彰奖励" style="width: 100%">
            <el-option v-for="h in honorDict" :key="h.code" :label="h.name + ' (+' + h.defaultPoints + '分)'" :value="h.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="受奖时间" prop="awardDate">
          <el-date-picker v-model="honorDialog.form.awardDate" type="month" value-format="YYYY-MM" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="honorDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitHonorFn" :loading="honorDialog.loading">提交</el-button>
      </template>
    </el-dialog>

    <!-- 证书提交弹窗 -->
    <el-dialog v-model="skillDialog.visible" title="提交技能证书" width="480px">
      <el-form :model="skillDialog.form" label-width="100px" size="small" ref="skillFormRef" :rules="skillRules">
        <el-form-item label="证书类型" prop="certCode">
          <el-select v-model="skillDialog.form.certCode" placeholder="选择证书类型" style="width: 100%">
            <el-option v-for="c in certDict" :key="c.code" :label="c.name + ' (+' + c.defaultPoints + '分)'" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="证书编号" prop="certNo">
          <el-input v-model="skillDialog.form.certNo" placeholder="证书编号" />
        </el-form-item>
        <el-form-item label="获取日期" prop="obtainDate">
          <el-date-picker v-model="skillDialog.form.obtainDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期至">
          <el-date-picker v-model="skillDialog.form.validUntil" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            action="/api/app/student/submit/upload"
            :show-file-list="true"
            :on-success="(res) => skillDialog.form.proofUrl = res.data.url"
            :before-upload="beforeProofUpload"
            :limit="1"
            :on-exceed="() => ElMessage.warning('仅支持上传一份文件')"
            :on-remove="() => skillDialog.form.proofUrl = ''"
            accept=".png,.jpg,.jpeg,.pdf"
          >
            <el-button size="small" type="primary">选择文件</el-button>
            <template #tip><span class="el-upload__tip">支持 png、jpg、pdf 格式</span></template>
          </el-upload>
          <div v-if="skillDialog.form.proofUrl" style="margin-top:6px">
            <el-tag size="small" type="success">已上传</el-tag>
            <el-link :href="skillDialog.form.proofUrl" target="_blank" type="primary" style="margin-left:8px">查看</el-link>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skillDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitSkillFn" :loading="skillDialog.loading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import { getStudentInfo, updateStudentInfo } from '@/api/studentApp'
import { getMySubmissions, submitServiceExperience, submitHonor, submitSkill, updateServiceExperience } from '@/api/studentApp'
import { getDictBranch, getDictHonor, getDictCert, getDictHonorCategory, getDictLeaderPost } from '@/api/studentApp'

const router = useRouter()
const studentStore = useStudentStore()
const editMode = ref(false)
const loading = ref(false)

// 字典
const branchList = ref([])
const honorDict = ref([])
const certDict = ref([])
const honorCategoryList = ref([])
const leaderPostList = ref([])

// 民族选项
const ethnicityOptions = [
  '汉族', '满族', '蒙古族', '回族', '藏族', '壮族', '维吾尔族', '苗族', '彝族', '土家族',
  '布依族', '侗族', '瑶族', '白族', '朝鲜族', '哈尼族', '哈萨克族', '黎族', '傣族', '畲族',
  '傈僳族', '仡佬族', '东乡族', '高山族', '拉祜族', '水族', '佤族', '纳西族', '羌族', '土族'
]

// 政治面貌选项
const politicalStatusOptions = ['中共党员', '中共预备党员', '共青团员', '群众']

// 年月展示：后端存当月1日(YYYY-MM-DD)，前端只展示 YYYY-MM
function toYM(value) {
  return value ? String(value).substring(0, 7) : ''
}

// 列表
const seList = ref([])
const honorList = ref([])
const skillList = ref([])

// 表单 ref
const seFormRef = ref(null)
const honorFormRef = ref(null)
const skillFormRef = ref(null)

// 状态 tag
function statusTag(s) { return s === 0 ? 'warning' : s === 1 ? 'success' : 'danger' }
function statusLabel(s) { return s === 0 ? '待审核' : s === 1 ? '已通过' : '已驳回' }

// 文件上传校验
function beforeProofUpload(file) {
  const ext = file.name.split('.').pop().toLowerCase()
  if (!['png', 'jpg', 'jpeg', 'pdf'].includes(ext)) {
    ElMessage.error('仅支持 png、jpg、pdf 格式')
    return false
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

// ====== 服役经历提交 ======
const seDialog = reactive({
  visible: false,
  loading: false,
  form: { branchCode: '', leaderPostCode: '', startDate: '', endDate: '' }
})
const seRules = {
  branchCode: [{ required: true, message: '请选择军兵种', trigger: 'change' }],
  leaderPostCode: [{ required: true, message: '请选择骨干职务', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择入伍时间', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择退役时间', trigger: 'change' }]
}
// 冲突处理
const seConflict = reactive({
  visible: false,
  loading: false,
  oldData: null,
  newData: null,
  rawFormData: null
})

function getBranchName(code) {
  const b = branchList.value.find(x => x.code === code)
  return b ? b.name : code
}

function getLeaderPostName(code) {
  const p = leaderPostList.value.find(x => x.code === code)
  return p ? p.name : code
}

async function submitSE() {
  if (!seFormRef.value) return
  try { await seFormRef.value.validate() } catch { return }
  seDialog.loading = true
  try {
    const res = await submitServiceExperience({ ...seDialog.form, studentId: studentStore.studentId })
    if (res.data?.conflict) {
      seConflict.oldData = {
        branchName: res.data.existing?.branchName || getBranchName(res.data.existing?.branchCode),
        leaderPostName: res.data.existing?.leaderPostName || getLeaderPostName(res.data.existing?.leaderPostCode),
        startDate: toYM(res.data.existing?.startDate),
        endDate: toYM(res.data.existing?.endDate)
      }
      seConflict.newData = {
        branchName: getBranchName(seDialog.form.branchCode),
        leaderPostName: getLeaderPostName(seDialog.form.leaderPostCode),
        startDate: seDialog.form.startDate,
        endDate: seDialog.form.endDate
      }
      seConflict.rawFormData = { ...seDialog.form }
      seDialog.visible = false
      seConflict.visible = true
      return
    }
    ElMessage.success('提交成功，等待审核')
    seDialog.visible = false
    seDialog.form = { branchCode: '', leaderPostCode: '', startDate: '', endDate: '' }
    fetchSubmissions()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.message || '网络异常'))
  } finally {
    seDialog.loading = false
  }
}

async function confirmUpdateSE() {
  seConflict.loading = true
  try {
    await updateServiceExperience({ ...seConflict.rawFormData, studentId: studentStore.studentId })
    ElMessage.success('修改成功，重新进入待审状态')
    seConflict.visible = false
    seConflict.rawFormData = null
    fetchSubmissions()
  } catch (e) {
    ElMessage.error('修改失败：' + (e.message || '网络异常'))
  } finally {
    seConflict.loading = false
  }
}

// ====== 受奖情况提交 ======
const honorDialog = reactive({
  visible: false,
  loading: false,
  form: { honorCategoryCode: '', honorCode: '', awardDate: '' }
})
const honorRules = {
  honorCategoryCode: [{ required: true, message: '请选择荣誉类别', trigger: 'change' }],
  honorCode: [{ required: true, message: '请选择表彰奖励', trigger: 'change' }]
}
async function submitHonorFn() {
  if (!honorFormRef.value) return
  try { await honorFormRef.value.validate() } catch { return }
  honorDialog.loading = true
  try {
    await submitHonor({ ...honorDialog.form, studentId: studentStore.studentId })
    ElMessage.success('提交成功，等待审核')
    honorDialog.visible = false
    honorDialog.form = { honorCategoryCode: '', honorCode: '', awardDate: '' }
    fetchSubmissions()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.message || '网络异常'))
  } finally {
    honorDialog.loading = false
  }
}

// ====== 证书提交 ======
const skillDialog = reactive({
  visible: false,
  loading: false,
  form: { certCode: '', certNo: '', obtainDate: '', validUntil: '', proofUrl: '' }
})
const skillRules = {
  certCode: [{ required: true, message: '请选择证书类型', trigger: 'change' }]
}
async function submitSkillFn() {
  if (!skillFormRef.value) return
  try { await skillFormRef.value.validate() } catch { return }
  skillDialog.loading = true
  try {
    await submitSkill({ ...skillDialog.form, studentId: studentStore.studentId })
    ElMessage.success('提交成功，等待审核')
    skillDialog.visible = false
    skillDialog.form = { certCode: '', certNo: '', obtainDate: '', validUntil: '', proofUrl: '' }
    fetchSubmissions()
  } catch (e) {
    ElMessage.error('提交失败：' + (e.message || '网络异常'))
  } finally {
    skillDialog.loading = false
  }
}

// ====== 数据加载 ======
const profile = reactive({
  studentNo: studentStore.studentNo,
  name: studentStore.studentName,
  gender: '',
  ethnicity: '',
  birthDate: '',
  nativePlace: '',
  politicalStatus: '',
  college: '',
  major: '',
  grade: '',
  phone: studentStore.studentPhone,
  enrollDate: '',
  retireDate: ''
})

async function fetchProfile() {
  try {
    const res = await getStudentInfo(studentStore.studentId)
    const data = res.data
    if (data) {
      Object.assign(profile, {
        studentNo: data.studentNo || profile.studentNo,
        name: data.name || profile.name,
        gender: data.gender || '',
        ethnicity: data.ethnicity || '',
        birthDate: toYM(data.birthDate),
        nativePlace: data.nativePlace || '',
        politicalStatus: data.politicalStatus || '',
        college: data.college || '',
        major: data.major || '',
        grade: data.grade || '',
        phone: data.phone || profile.phone,
        enrollDate: toYM(data.enrollDate),
        retireDate: toYM(data.retireDate)
      })
    }
  } catch (e) {
    ElMessage.error('获取档案信息失败：' + (e.message || '网络异常'))
  }
}

async function fetchSubmissions() {
  loading.value = true
  try {
    const res = await getMySubmissions(studentStore.studentId)
    if (res.data) {
      seList.value = (res.data.serviceExperiences || []).map(s => ({
        ...s,
        startDate: toYM(s.startDate),
        endDate: toYM(s.endDate)
      }))
      honorList.value = (res.data.honors || []).map(h => ({
        ...h,
        awardDate: toYM(h.awardDate)
      }))
      skillList.value = res.data.skills || []
    }
  } catch (e) {
    ElMessage.error('获取数据失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

async function loadDicts() {
  try {
    const [b, h, c, hc, lp] = await Promise.all([
      getDictBranch(), getDictHonor(), getDictCert(), getDictHonorCategory(), getDictLeaderPost()
    ])
    branchList.value = b.data || []
    honorDict.value = h.data || []
    certDict.value = c.data || []
    honorCategoryList.value = hc.data || []
    leaderPostList.value = lp.data || []
  } catch (e) {
    ElMessage.error('加载字典失败')
  }
}

async function saveProfile() {
  try {
    await updateStudentInfo(studentStore.studentId, {
      name: profile.name,
      gender: profile.gender,
      ethnicity: profile.ethnicity,
      birthDate: profile.birthDate,
      nativePlace: profile.nativePlace,
      politicalStatus: profile.politicalStatus,
      college: profile.college,
      major: profile.major,
      grade: profile.grade,
      phone: profile.phone,
      enrollDate: profile.enrollDate,
      retireDate: profile.retireDate
    })
    ElMessage.success('档案已更新')
    editMode.value = false
  } catch (e) {
    ElMessage.error('保存档案失败：' + (e.message || '网络异常'))
  }
}

onMounted(() => {
  fetchProfile()
  fetchSubmissions()
  loadDicts()
})
</script>

<style scoped>
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.profile-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 20px;
}
.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-image: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.profile-basic h3 {
  font-size: 20px;
  color: var(--color-primary);
  margin-bottom: 6px;
}
.profile-basic p {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.section-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow-card);
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0;
}
.drawer-footer {
  text-align: right;
  padding-top: 12px;
}
.gold-text {
  color: #c4a35a;
  font-weight: 600;
}
</style>
