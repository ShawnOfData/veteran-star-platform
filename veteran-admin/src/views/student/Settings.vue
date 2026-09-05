<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 账号安全 -->
      <el-tab-pane label="账号安全" name="security">
        <div class="section-card">
          <h4 class="section-title">当前绑定手机号</h4>
          <div class="phone-display">
            <el-icon :size="18" color="#409eff"><PhoneFilled /></el-icon>
            <span class="phone-number">{{ maskedPhone }}</span>
            <el-tag type="success" size="small" effect="plain">已绑定</el-tag>
          </div>
        </div>
        <div class="section-card">
          <h4 class="section-title">修改手机号</h4>
          <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" label-width="100px" class="config-form">
            <el-form-item label="当前手机号" prop="oldPhone">
              <el-input v-model="phoneForm.oldPhone" maxlength="11" placeholder="请输入当前手机号" style="max-width:300px" />
            </el-form-item>
            <el-form-item label="新手机号" prop="newPhone">
              <el-input v-model="phoneForm.newPhone" maxlength="11" placeholder="请输入新手机号" style="max-width:300px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="phoneLoading" @click="handleUpdatePhone">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="section-card">
          <h4 class="section-title">修改密码</h4>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px" class="config-form">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" style="max-width:300px" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请设置新密码（至少6位）" style="max-width:300px" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" style="max-width:300px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleUpdatePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 个人偏好 -->
      <el-tab-pane label="个人偏好" name="preference">
        <div class="section-card">
          <h4 class="section-title">通知与隐私</h4>
          <el-form :model="prefForm" label-width="160px" class="config-form">
            <el-form-item label="新机会通知">
              <el-switch v-model="prefForm.notifyEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="简历默认模板">
              <el-radio-group v-model="prefForm.resumeTemplate">
                <el-radio value="military">军事风</el-radio>
                <el-radio value="simple">简约风</el-radio>
                <el-radio value="government">政务风</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="排行榜显示真实姓名">
              <el-switch v-model="prefForm.showRealName" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="prefLoading" @click="handleUpdatePref">保存偏好</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 关于 -->
      <el-tab-pane label="关于" name="about">
        <div class="section-card">
          <h4 class="section-title">系统信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="系统名称">{{ sysInfo.siteName || '戎归·星辉 退役大学生士兵管理平台' }}</el-descriptions-item>
            <el-descriptions-item label="系统版本">{{ sysInfo.version || 'v3.0' }}</el-descriptions-item>
            <el-descriptions-item label="适用对象">退役复学大学生</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="section-card" v-if="sysInfo.contactPhone || sysInfo.contactEmail || sysInfo.contactAddress">
          <h4 class="section-title">联系我们</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item v-if="sysInfo.contactPhone" label="联系电话">{{ sysInfo.contactPhone }}</el-descriptions-item>
            <el-descriptions-item v-if="sysInfo.contactEmail" label="联系邮箱">{{ sysInfo.contactEmail }}</el-descriptions-item>
            <el-descriptions-item v-if="sysInfo.contactAddress" label="服务站地址">{{ sysInfo.contactAddress }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="section-card">
          <h4 class="section-title">法律条款</h4>
          <div class="legal-links">
            <el-link type="primary" :underline="false" style="margin-right:24px" @click="showLegal('service')">用户服务协议</el-link>
            <el-link type="primary" :underline="false" @click="showLegal('privacy')">隐私政策</el-link>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 法律条款弹窗 -->
    <el-dialog v-model="legalVisible" :title="legalTitle" width="560px" destroy-on-close>
      <div class="legal-content">{{ legalContent }}</div>
      <template #footer>
        <el-button type="primary" @click="legalVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import { PhoneFilled } from '@element-plus/icons-vue'
import { getSettings, updateSettings, updatePhone, updatePassword, getSystemInfo, getStudentInfo } from '@/api/studentApp'

const studentStore = useStudentStore()
const activeTab = ref('security')

// ==================== 账号安全 ====================
const phoneLoading = ref(false)
const pwdLoading = ref(false)
const rawPhone = ref('')
const maskedPhone = computed(() => {
  const p = rawPhone.value
  if (p && p.length === 11) {
    return p.substring(0, 3) + '****' + p.substring(7)
  }
  return '未绑定'
})

const phoneForm = reactive({ oldPhone: '', newPhone: '' })
const phoneFormRef = ref(null)
const phoneRules = {
  oldPhone: [
    { required: true, message: '请输入当前手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ]
}

async function handleUpdatePhone() {
  const valid = await phoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  phoneLoading.value = true
  try {
    await updatePhone(studentStore.studentId, {
      oldPhone: phoneForm.oldPhone,
      newPhone: phoneForm.newPhone
    })
    ElMessage.success('手机号已更新')
    phoneForm.oldPhone = ''
    phoneForm.newPhone = ''
    fetchStudentPhone()
  } catch (e) {
    // 拦截器已处理通用错误
  } finally {
    phoneLoading.value = false
  }
}

// 修改密码
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

async function handleUpdatePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await updatePassword(studentStore.studentId, {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码已更新')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}

async function fetchStudentPhone() {
  try {
    const res = await getStudentInfo(studentStore.studentId)
    if (res.data && res.data.phone) {
      rawPhone.value = res.data.phone
    }
  } catch (e) {
    // 静默失败
  }
}

// ==================== 个人偏好 ====================
const prefLoading = ref(false)
const prefForm = reactive({
  notifyEnabled: 1,
  resumeTemplate: 'military',
  showRealName: 1
})

async function fetchSettings() {
  try {
    const res = await getSettings(studentStore.studentId)
    if (res.data) {
      Object.assign(prefForm, {
        notifyEnabled: res.data.notifyEnabled ?? 1,
        resumeTemplate: res.data.resumeTemplate || 'military',
        showRealName: res.data.showRealName ?? 1
      })
    }
  } catch (e) {
    // 默认设置即可
  }
}

async function handleUpdatePref() {
  prefLoading.value = true
  try {
    await updateSettings(studentStore.studentId, prefForm)
    ElMessage.success('偏好已保存')
  } catch (e) {
    // 拦截器已处理
  } finally {
    prefLoading.value = false
  }
}

// ==================== 关于 ====================
const sysInfo = reactive({
  siteName: '',
  version: '',
  contactPhone: '',
  contactEmail: '',
  contactAddress: ''
})

async function fetchSysInfo() {
  try {
    const res = await getSystemInfo()
    if (res.data) {
      Object.assign(sysInfo, res.data)
    }
  } catch (e) {
    // 使用默认值
  }
}

// ==================== 法律条款 ====================
const legalVisible = ref(false)
const legalTitle = ref('')
const legalContent = ref('')

function showLegal(type) {
  legalTitle.value = type === 'service' ? '用户服务协议' : '隐私政策'
  legalContent.value = type === 'service' ? serviceAgreement : privacyPolicy
  legalVisible.value = true
}

const serviceAgreement = `欢迎使用「戎归·星辉」退役大学生士兵管理平台（以下简称"本平台"）。

一、服务条款的接受
用户在使用本平台服务前，应当仔细阅读本协议。用户使用本平台服务即视为用户已阅读并同意本协议的全部内容。

二、服务内容
本平台为退役复学大学生提供信息档案管理、积分统计、就业机会推荐、简历制作等服务。

三、用户义务
用户应保证提供的个人信息真实、准确、完整，并及时更新。用户不得利用本平台从事违法违规活动。

四、免责声明
本平台将尽力保障服务的稳定性和安全性，但不对因不可抗力或用户自身原因导致的服务中断承担责任。

五、协议修改
本平台有权随时修改本协议，修改后的协议一经发布即生效。`

const privacyPolicy = `「戎归·星辉」退役大学生士兵管理平台（以下简称"我们"）深知个人信息对您的重要性，我们将按照法律法规的规定，保护您的个人信息安全。

一、信息收集
我们收集的信息包括：您主动提供的姓名、学号、学院、专业、手机号、服役经历、荣誉证书等档案信息。

二、信息使用
我们使用您的信息用于：档案管理、积分统计、就业机会匹配推荐、简历生成等服务。

三、信息存储与保护
您的个人信息采用 AES 加密存储，手机号等敏感信息进行脱敏处理。我们采取合理的安全措施保护您的信息。

四、信息共享
未经您的同意，我们不会向第三方提供您的个人信息，法律法规另有规定的除外。

五、您的权利
您可以随时查看、修改您的个人信息，或通过系统设置调整隐私偏好。

六、联系我们
如有任何疑问，请通过系统设置中的联系方式与我们取得联系。`
</script>

<style scoped>
.settings-tabs {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
}
.section-card {
  margin-bottom: 16px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0 0 16px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}
.config-form {
  max-width: 500px;
}
.phone-display {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  max-width: 350px;
}
.phone-number {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
}
.legal-links {
  font-size: 14px;
}
.legal-content {
  white-space: pre-line;
  line-height: 1.8;
  font-size: 14px;
  color: #606266;
  max-height: 400px;
  overflow-y: auto;
}
</style>
