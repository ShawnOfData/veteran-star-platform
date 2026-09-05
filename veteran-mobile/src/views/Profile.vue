<!--
  Profile.vue — 个人档案
  基本信息 + 服役经历 + 所获荣誉 + 技能证书
-->
<template>
  <div class="profile-page page-with-tabbar">
    <van-nav-bar title="个人档案">
      <template #right>
        <van-icon name="edit" size="18" @click="editMode = !editMode" />
      </template>
    </van-nav-bar>

    <!-- 基本信息 -->
    <van-cell-group inset title="基本信息" v-if="!editMode">
      <van-cell title="学号" :value="info.studentNo" />
      <van-cell title="姓名" :value="info.name" />
      <van-cell title="性别" :value="info.gender || '-'" />
      <van-cell title="民族" :value="info.ethnicity || '-'" />
      <van-cell title="出生年月" :value="toYM(info.birthDate)" />
      <van-cell title="籍贯" :value="info.nativePlace || '-'" />
      <van-cell title="政治面貌" :value="info.politicalStatus || '-'" />
      <van-cell title="学院" :value="info.college || '-'" />
      <van-cell title="专业" :value="info.major || '-'" />
      <van-cell title="年级" :value="info.grade || '-'" />
      <van-cell title="入学时间" :value="toYM(info.enrollDate)" />
      <van-cell title="退役时间" :value="toYM(info.retireDate)" />
      <van-cell title="手机号" :value="maskPhone(info.phone)" />
    </van-cell-group>

    <van-cell-group inset title="编辑信息" v-else>
      <van-field v-model="editForm.name" label="姓名" placeholder="请输入姓名" />
      <van-field label="性别" is-link readonly :model-value="editForm.gender" placeholder="请选择性别" @click="showGenderPicker = true" />
      <van-field label="民族" is-link readonly :model-value="editForm.ethnicity" placeholder="请选择民族" @click="showEthnicityPicker = true" />
      <van-field label="出生年月" is-link readonly :model-value="editForm.birthDate" placeholder="选择出生年月" @click="showBirthPicker = true" />
      <van-field v-model="editForm.nativePlace" label="籍贯" placeholder="如：河北省石家庄市" />
      <van-field label="政治面貌" is-link readonly :model-value="editForm.politicalStatus" placeholder="请选择政治面貌" @click="showPoliticalPicker = true" />
      <van-field v-model="editForm.college" label="学院" placeholder="请输入学院" />
      <van-field v-model="editForm.major" label="专业" placeholder="请输入专业" />
      <van-field v-model="editForm.grade" label="年级" placeholder="请输入年级" />
      <van-field label="入学时间" is-link readonly :model-value="editForm.enrollDate" placeholder="选择入学时间" @click="showEnrollPicker = true" />
      <van-field label="退役时间" is-link readonly :model-value="editForm.retireDate" placeholder="选择退役时间" @click="showRetirePicker = true" />
      <div style="padding: 12px 16px">
        <van-button round block type="primary" @click="saveInfo">保存</van-button>
      </div>
    </van-cell-group>

    <!-- 编辑信息选择器 -->
    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker :columns="genderColumns" @confirm="(e) => { editForm.gender = e.selectedOptions[0].text; showGenderPicker = false }" @cancel="showGenderPicker = false" />
    </van-popup>
    <van-popup v-model:show="showEthnicityPicker" position="bottom" round>
      <van-picker :columns="ethnicityColumns" @confirm="(e) => { editForm.ethnicity = e.selectedOptions[0].text; showEthnicityPicker = false }" @cancel="showEthnicityPicker = false" />
    </van-popup>
    <van-popup v-model:show="showPoliticalPicker" position="bottom" round>
      <van-picker :columns="politicalColumns" @confirm="(e) => { editForm.politicalStatus = e.selectedOptions[0].text; showPoliticalPicker = false }" @cancel="showPoliticalPicker = false" />
    </van-popup>
    <van-popup v-model:show="showBirthPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择出生年月" @confirm="(e) => { editForm.birthDate = e.selectedValues.join('-'); showBirthPicker = false }" @cancel="showBirthPicker = false" />
    </van-popup>
    <van-popup v-model:show="showEnrollPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择入学时间" @confirm="(e) => { editForm.enrollDate = e.selectedValues.join('-'); showEnrollPicker = false }" @cancel="showEnrollPicker = false" />
    </van-popup>
    <van-popup v-model:show="showRetirePicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择退役时间" @confirm="(e) => { editForm.retireDate = e.selectedValues.join('-'); showRetirePicker = false }" @cancel="showRetirePicker = false" />
    </van-popup>

    <!-- 服役经历 -->
    <van-cell-group inset title="服役经历">
      <EmptyState v-if="!seList.length" description="暂无服役经历" />
      <van-cell
        v-for="se in seList"
        :key="se.id"
        :title="se.branchName"
        :label="(se.leaderPostName || '--') + ' | ' + toYM(se.startDate) + '~' + toYM(se.endDate)"
        is-link
      >
        <template #value>
          <van-tag :type="statusType(se.status)" size="mini">{{ statusLabel(se.status) }}</van-tag>
        </template>
      </van-cell>
      <div style="padding: 8px 16px">
        <van-button plain hairline block size="small" icon="plus" @click="openSePopup">提交服役经历</van-button>
      </div>
    </van-cell-group>

    <!-- 受奖情况 -->
    <van-cell-group inset title="受奖情况">
      <EmptyState v-if="!honorList.length" description="暂无受奖情况" />
      <van-cell
        v-for="h in honorList"
        :key="h.id"
        :title="h.honorName"
        :label="(h.honorCategoryName || '') + ' | 受奖时间: ' + toYM(h.awardDate)"
      >
        <template #value>
          <span class="gold-text">+{{ h.pointsAwarded }}</span>
          <van-tag :type="statusType(h.status)" size="mini" style="margin-left:4px">{{ statusLabel(h.status) }}</van-tag>
        </template>
      </van-cell>
      <div style="padding: 8px 16px">
        <van-button plain hairline block size="small" icon="plus" @click="openHonorPopup">提交受奖情况</van-button>
      </div>
    </van-cell-group>

    <!-- 技能证书 -->
    <van-cell-group inset title="技能证书">
      <EmptyState v-if="!skillList.length" description="暂无证书" />
      <van-cell
        v-for="s in skillList"
        :key="s.id"
        :title="s.certName"
        :label="[s.certNo ? '编号: ' + s.certNo : '', s.obtainDate ? '获得于 ' + s.obtainDate : ''].filter(Boolean).join('  ')"
      >
        <template #value>
          <span class="gold-text">+{{ s.pointsAwarded }}</span>
          <van-tag :type="statusType(s.status)" size="mini" style="margin-left:4px">{{ statusLabel(s.status) }}</van-tag>
        </template>
        <template #right-icon>
          <a v-if="s.proofUrl" :href="s.proofUrl" target="_blank" @click.stop style="display:flex;align-items:center">
            <van-icon name="eye-o" size="18" color="#2563EB" />
          </a>
        </template>
      </van-cell>
      <div style="padding: 8px 16px">
        <van-button plain hairline block size="small" icon="plus" @click="openSkillPopup">提交证书</van-button>
      </div>
    </van-cell-group>

    <!-- 服役经历弹窗 -->
    <van-popup v-model:show="sePopup" position="bottom" round :style="{ maxHeight: '80%' }">
      <van-nav-bar title="提交服役经历" />
      <van-form @submit="submitSe">
        <van-cell-group inset>
          <van-field label="军兵种" is-link readonly :model-value="seForm.branchName" placeholder="选择军兵种" @click="showBranchPicker = true" :rules="[{ required: true, message: '请选择军兵种' }]" />
          <van-field label="骨干职务" is-link readonly :model-value="seForm.leaderPostName" placeholder="选择骨干职务" @click="showPostPicker = true" :rules="[{ required: true, message: '请选择骨干职务' }]" />
          <van-field v-model="seForm.startDate" label="入伍时间" placeholder="选择入伍时间" readonly @click="showStartPicker = true" :rules="[{ required: true, message: '请选择入伍时间' }]" />
          <van-field v-model="seForm.endDate" label="退役时间" placeholder="选择退役时间" readonly @click="showEndPicker = true" />
        </van-cell-group>
        <div style="padding: 12px 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">提交</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 军兵种选择 -->
    <van-popup v-model:show="showBranchPicker" position="bottom" round>
      <van-picker :columns="branchColumns" @confirm="onBranchConfirm" @cancel="showBranchPicker = false" />
    </van-popup>

    <!-- 骨干职务选择 -->
    <van-popup v-model:show="showPostPicker" position="bottom" round>
      <van-picker :columns="postColumns" @confirm="onPostConfirm" @cancel="showPostPicker = false" />
    </van-popup>

    <!-- 年月选择 -->
    <van-popup v-model:show="showStartPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择入伍时间" @confirm="(e) => { seForm.startDate = e.selectedValues.join('-'); showStartPicker = false }" @cancel="showStartPicker = false" />
    </van-popup>
    <van-popup v-model:show="showEndPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择退役时间" @confirm="(e) => { seForm.endDate = e.selectedValues.join('-'); showEndPicker = false }" @cancel="showEndPicker = false" />
    </van-popup>

    <!-- 受奖情况弹窗 -->
    <van-popup v-model:show="honorPopup" position="bottom" round :style="{ maxHeight: '80%' }">
      <van-nav-bar title="提交受奖情况" />
      <van-form @submit="submitHonor">
        <van-cell-group inset>
          <van-field label="荣誉类别" is-link readonly :model-value="honorForm.honorCategoryName" placeholder="选择荣誉类别" @click="showCategoryPicker = true" :rules="[{ required: true, message: '请选择荣誉类别' }]" />
          <van-field label="表彰奖励" is-link readonly :model-value="honorForm.honorName" placeholder="选择表彰奖励" @click="showHonorPicker = true" :rules="[{ required: true, message: '请选择表彰奖励' }]" />
          <van-field v-model="honorForm.awardDate" label="受奖时间" placeholder="选择受奖时间" readonly @click="showAwardPicker = true" :rules="[{ required: true, message: '请选择受奖时间' }]" />
        </van-cell-group>
        <div style="padding: 12px 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">提交</van-button>
        </div>
      </van-form>
    </van-popup>
    <van-popup v-model:show="showCategoryPicker" position="bottom" round>
      <van-picker :columns="categoryColumns" @confirm="onCategoryConfirm" @cancel="showCategoryPicker = false" />
    </van-popup>
    <van-popup v-model:show="showHonorPicker" position="bottom" round>
      <van-picker :columns="honorColumns" @confirm="onHonorConfirm" @cancel="showHonorPicker = false" />
    </van-popup>
    <van-popup v-model:show="showAwardPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择受奖时间" @confirm="(e) => { honorForm.awardDate = e.selectedValues.join('-'); showAwardPicker = false }" @cancel="showAwardPicker = false" />
    </van-popup>

    <!-- 证书弹窗 -->
    <van-popup v-model:show="skillPopup" position="bottom" round :style="{ maxHeight: '80%' }">
      <van-nav-bar title="提交技能证书" />
      <van-form @submit="submitSkill">
        <van-cell-group inset>
          <van-field label="证书类型" is-link readonly :model-value="skillForm.certName" placeholder="选择证书类型" @click="showCertPicker = true" :rules="[{ required: true, message: '请选择证书类型' }]" />
          <van-field v-model="skillForm.certNo" label="证书编号" placeholder="请输入证书编号" :rules="[{ required: true, message: '请输入证书编号' }]" />
          <van-field v-model="skillForm.obtainDate" label="获得日期" placeholder="选择获得年月" readonly @click="showObtainPicker = true" :rules="[{ required: true, message: '请选择获得年月' }]" />
          <van-field label="证书证明">
            <template #input>
              <van-uploader :max-count="1" accept=".png,.jpg,.jpeg,.pdf" :after-read="onSkillProofRead" :disabled="proofUploading" />
              <div v-if="skillForm.proofUrl" style="margin-top:6px">
                <a :href="skillForm.proofUrl" target="_blank" style="color:#2563EB;font-size:13px;text-decoration:underline">已上传证明，点击查看</a>
              </div>
            </template>
          </van-field>
        </van-cell-group>
        <div style="padding: 12px 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">提交</van-button>
        </div>
      </van-form>
    </van-popup>
    <van-popup v-model:show="showCertPicker" position="bottom" round>
      <van-picker :columns="certColumns" @confirm="onCertConfirm" @cancel="showCertPicker = false" />
    </van-popup>
    <van-popup v-model:show="showObtainPicker" position="bottom" round>
      <van-date-picker :columns-type="['year', 'month']" title="选择获得年月" @confirm="(e) => { skillForm.obtainDate = e.selectedValues.join('-'); showObtainPicker = false }" @cancel="showObtainPicker = false" />
    </van-popup>
  </div>
</template>

<script setup>
/**
 * 个人档案页
 * 展示基本信息、服役经历、荣誉、证书
 * 支持编辑基本信息和提交各类申请
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getStudentInfo, updateStudentInfo, getMySubmissions, submitServiceExperience as apiSubmitSE, submitHonor as apiSubmitHonor, submitSkill as apiSubmitSkill, uploadProof, getDictBranch, getDictHonor, getDictCert, getDictHonorCategory, getDictLeaderPost } from '@/api/studentApp'
import { showToast } from 'vant'
import EmptyState from '@/components/EmptyState.vue'

const store = useStudentStore()
const info = ref({})
const editMode = ref(false)
const editForm = reactive({})
const seList = ref([])
const honorList = ref([])
const skillList = ref([])
const submitting = ref(false)

// 字典数据
const branchDict = ref([])
const honorDict = ref([])
const certDict = ref([])
const honorCategoryDict = ref([])
const leaderPostDict = ref([])

// 弹窗控制
const sePopup = ref(false)
const honorPopup = ref(false)
const skillPopup = ref(false)
const showBranchPicker = ref(false)
const showPostPicker = ref(false)
const showHonorPicker = ref(false)
const showCategoryPicker = ref(false)
const showCertPicker = ref(false)
const showStartPicker = ref(false)
const showEndPicker = ref(false)
const showAwardPicker = ref(false)
const showObtainPicker = ref(false)
const showGenderPicker = ref(false)
const showEthnicityPicker = ref(false)
const showPoliticalPicker = ref(false)
const showBirthPicker = ref(false)
const showEnrollPicker = ref(false)
const showRetirePicker = ref(false)

// 表单
const seForm = reactive({ branchCode: '', branchName: '', leaderPostCode: '', leaderPostName: '', startDate: '', endDate: '' })
const honorForm = reactive({ honorCategoryCode: '', honorCategoryName: '', honorCode: '', honorName: '', awardDate: '' })
const skillForm = reactive({ certCode: '', certName: '', certNo: '', obtainDate: '', proofUrl: '' })
const proofUploading = ref(false)

// Picker columns
const branchColumns = computed(() => branchDict.value.map(b => ({ text: b.name, value: b.code })))
const honorColumns = computed(() => honorDict.value.map(h => ({ text: h.name + '(+' + h.defaultPoints + '分)', value: h.code })))
const certColumns = computed(() => certDict.value.map(c => ({ text: c.name + '(+' + c.defaultPoints + '分)', value: c.code })))
const postColumns = computed(() => leaderPostDict.value.map(p => ({ text: p.name, value: p.code })))
const categoryColumns = computed(() => honorCategoryDict.value.map(c => ({ text: c.name, value: c.code })))
const genderColumns = ['男', '女'].map(g => ({ text: g, value: g }))
const ethnicityColumns = [
  '汉族', '满族', '蒙古族', '回族', '藏族', '壮族', '维吾尔族', '苗族', '彝族', '土家族',
  '布依族', '侗族', '瑶族', '白族', '朝鲜族', '哈尼族', '哈萨克族', '黎族', '傣族', '畲族',
  '傈僳族', '仡佬族', '东乡族', '高山族', '拉祜族', '水族', '佤族', '纳西族', '羌族', '土族'
].map(n => ({ text: n, value: n }))
const politicalColumns = ['中共党员', '中共预备党员', '共青团员', '群众'].map(p => ({ text: p, value: p }))

// 年月展示：后端存当月1日(YYYY-MM-DD)，前端只展示 YYYY-MM
function toYM(value) {
  return value ? String(value).substring(0, 7) : '-'
}

onMounted(async () => {
  await Promise.all([fetchInfo(), fetchExperiences(), fetchDicts()])
})

async function fetchInfo() {
  const res = await getStudentInfo(store.studentId)
  const data = res.data || {}
  info.value = data
  Object.assign(editForm, data, {
    birthDate: toYM(data.birthDate),
    enrollDate: toYM(data.enrollDate),
    retireDate: toYM(data.retireDate)
  })
}

async function fetchExperiences() {
  const res = await getMySubmissions(store.studentId)
  const data = res.data || {}
  seList.value = data.serviceExperiences || []
  honorList.value = data.honors || []
  skillList.value = data.skills || []
}

async function fetchDicts() {
  const [b, h, c, hc, lp] = await Promise.all([getDictBranch(), getDictHonor(), getDictCert(), getDictHonorCategory(), getDictLeaderPost()])
  branchDict.value = b.data || []
  honorDict.value = h.data || []
  certDict.value = c.data || []
  honorCategoryDict.value = hc.data || []
  leaderPostDict.value = lp.data || []
}

async function saveInfo() {
  try {
    await updateStudentInfo(store.studentId, editForm)
    info.value = { ...editForm }
    editMode.value = false
    showToast({ message: '保存成功', type: 'success' })
  } catch (e) { /* 拦截器处理 */ }
}

function openSePopup() { Object.assign(seForm, { branchCode: '', branchName: '', leaderPostCode: '', leaderPostName: '', startDate: '', endDate: '' }); sePopup.value = true }
function openHonorPopup() { Object.assign(honorForm, { honorCategoryCode: '', honorCategoryName: '', honorCode: '', honorName: '', awardDate: '' }); honorPopup.value = true }
function openSkillPopup() { Object.assign(skillForm, { certCode: '', certName: '', certNo: '', obtainDate: '', proofUrl: '' }); skillPopup.value = true }

function onBranchConfirm({ selectedOptions }) { seForm.branchCode = selectedOptions[0].value; seForm.branchName = selectedOptions[0].text; showBranchPicker.value = false }
function onPostConfirm({ selectedOptions }) { seForm.leaderPostCode = selectedOptions[0].value; seForm.leaderPostName = selectedOptions[0].text; showPostPicker.value = false }
function onCategoryConfirm({ selectedOptions }) { honorForm.honorCategoryCode = selectedOptions[0].value; honorForm.honorCategoryName = selectedOptions[0].text; showCategoryPicker.value = false }
function onHonorConfirm({ selectedOptions }) { honorForm.honorCode = selectedOptions[0].value; honorForm.honorName = selectedOptions[0].text; showHonorPicker.value = false }
function onCertConfirm({ selectedOptions }) { skillForm.certCode = selectedOptions[0].value; skillForm.certName = selectedOptions[0].text; showCertPicker.value = false }

async function submitSe() {
  submitting.value = true
  try {
    await apiSubmitSE({ studentId: store.studentId, ...seForm })
    showToast({ message: '提交成功', type: 'success' })
    sePopup.value = false
    fetchExperiences()
  } catch (e) {} finally { submitting.value = false }
}
async function submitHonor() {
  submitting.value = true
  try {
    await apiSubmitHonor({ studentId: store.studentId, ...honorForm })
    showToast({ message: '提交成功', type: 'success' })
    honorPopup.value = false
    fetchExperiences()
  } catch (e) {} finally { submitting.value = false }
}
// 证书证明上传（支持图片与 PDF）
async function onSkillProofRead(file) {
  if (!file || !file.file) return
  proofUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file.file)
    const res = await uploadProof(fd)
    skillForm.proofUrl = res.data.url
    showToast({ message: '上传成功', type: 'success' })
  } catch (e) {
    showToast('上传失败，请重试')
  } finally {
    proofUploading.value = false
  }
}

async function submitSkill() {
  submitting.value = true
  try {
    await apiSubmitSkill({ studentId: store.studentId, ...skillForm })
    showToast({ message: '提交成功', type: 'success' })
    skillPopup.value = false
  } catch (e) {} finally { submitting.value = false }
}

function maskPhone(phone) { return phone ? phone.slice(0, 3) + '****' + phone.slice(7) : '-' }
function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'default' }
function statusLabel(s) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知' }
</script>

<style scoped>
.profile-page { background: var(--color-bg-page); padding-bottom: 60px; }
.gold-text { color: var(--color-gold); font-weight: 600; font-size: 13px; }
</style>
