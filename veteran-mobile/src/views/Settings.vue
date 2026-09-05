<!--
  Settings.vue — 学生设置
  三个标签页：账号安全 / 个人偏好 / 关于
-->
<template>
  <div class="settings-page page-with-tabbar">
    <van-nav-bar title="设置" />

    <van-tabs v-model:active="activeTab" sticky>
      <!-- 账号安全 -->
      <van-tab title="账号安全">
        <van-cell-group inset title="修改密码">
          <van-field
            v-model="pwdForm.oldPassword"
            type="password"
            label="当前密码"
            placeholder="请输入当前密码"
            left-icon="lock"
          />
          <van-field
            v-model="pwdForm.newPassword"
            type="password"
            label="新密码"
            placeholder="至少6位"
            left-icon="lock"
          />
          <van-field
            v-model="pwdForm.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入"
            left-icon="lock"
          />
        </van-cell-group>
        <div class="btn-wrapper">
          <van-button round block type="primary" @click="handleChangePassword" :loading="pwdLoading">
            确认修改
          </van-button>
        </div>

        <van-cell-group inset title="退出登录">
          <van-cell title="退出登录" is-link @click="handleLogout">
            <template #right-icon><van-icon name="cross" color="#ee0a24" /></template>
          </van-cell>
        </van-cell-group>
      </van-tab>

      <!-- 个人偏好 -->
      <van-tab title="个人偏好">
        <van-cell-group inset title="显示设置">
          <van-cell title="深色模式">
            <template #right-icon>
              <van-switch v-model="prefForm.darkMode" size="22px" @change="savePrefs" />
            </template>
          </van-cell>
          <van-cell title="消息通知">
            <template #right-icon>
              <van-switch v-model="prefForm.notification" size="22px" @change="savePrefs" />
            </template>
          </van-cell>
          <van-cell title="自动播放">
            <template #right-icon>
              <van-switch v-model="prefForm.autoplay" size="22px" @change="savePrefs" />
            </template>
          </van-cell>
        </van-cell-group>

        <van-cell-group inset title="语言设置">
          <van-cell title="界面语言" :value="prefForm.language || '简体中文'" is-link @click="showLangPicker = true" />
        </van-cell-group>
      </van-tab>

      <!-- 关于 -->
      <van-tab title="关于">
        <div class="about-card">
          <div class="about-logo">
            <van-icon name="shield-o" size="48" color="#2563EB" />
          </div>
          <h2 class="about-name">{{ sysInfo.siteName || '戎归·星辉' }}</h2>
          <p class="about-version">v{{ sysInfo.version || '3.0.0' }}</p>
          <p class="about-desc">{{ sysInfo.description || '退役大学生士兵综合管理服务平台' }}</p>
        </div>

        <van-cell-group inset>
          <van-cell title="联系电话" :value="sysInfo.contact || '-'" />
          <van-cell title="官方邮箱" :value="sysInfo.email || '-'" />
          <van-cell title="系统版本" :value="'v' + (sysInfo.version || '3.0.0')" />
        </van-cell-group>

        <p class="copyright">© 2026 戎归·星辉 All Rights Reserved</p>
      </van-tab>
    </van-tabs>

    <!-- 语言选择 -->
    <van-popup v-model:show="showLangPicker" position="bottom" round>
      <van-picker
        :columns="langColumns"
        @confirm="(e) => { prefForm.language = e.selectedOptions[0].text; showLangPicker = false; savePrefs() }"
        @cancel="showLangPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
/**
 * 学生设置页
 * - 账号安全：修改密码 + 退出登录
 * - 个人偏好：深色模式/通知/语言
 * - 关于：系统信息
 */
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { getSettings, updateSettings, updatePassword, getSystemInfo } from '@/api/studentApp'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const store = useStudentStore()
const activeTab = ref(0)
const pwdLoading = ref(false)
const showLangPicker = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const prefForm = reactive({ darkMode: false, notification: true, autoplay: false, language: '简体中文' })
const sysInfo = ref({})
const langColumns = [{ text: '简体中文', value: 'zh-CN' }, { text: 'English', value: 'en-US' }]

onMounted(async () => {
  try {
    const [settingsRes, sysRes] = await Promise.all([getSettings(store.studentId), getSystemInfo()])
    if (settingsRes.data) {
      Object.assign(prefForm, settingsRes.data)
    }
    sysInfo.value = sysRes.data || {}
  } catch (e) { /* 拦截器处理 */ }
})

/** 修改密码 */
async function handleChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    return showToast('请填写完整')
  }
  if (pwdForm.newPassword.length < 6) {
    return showToast('新密码至少6位')
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    return showToast('两次密码不一致')
  }
  pwdLoading.value = true
  try {
    await updatePassword(store.studentId, {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    showToast({ message: '密码修改成功', type: 'success' })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) { /* 拦截器处理 */ }
  finally { pwdLoading.value = false }
}

/** 保存偏好设置 */
async function savePrefs() {
  try {
    await updateSettings(store.studentId, prefForm)
    showToast({ message: '已保存', type: 'success' })
  } catch (e) { /* 拦截器处理 */ }
}

/** 退出登录 */
function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确定要退出登录吗？' })
    .then(() => {
      store.logout()
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.settings-page { background: var(--color-bg-page); min-height: 100vh; padding-bottom: 60px; }

.btn-wrapper { padding: 16px; }

/* 关于 */
.about-card { text-align: center; padding: 30px 16px 20px; }
.about-logo {
  width: 72px; height: 72px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.1);
  display: flex; align-items: center; justify-content: center;
}
.about-name { font-size: 20px; font-weight: 700; color: var(--color-text-primary); margin-bottom: 4px; }
.about-version { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; }
.about-desc { font-size: 13px; color: var(--color-text-secondary); }
.copyright { text-align: center; font-size: 12px; color: var(--color-text-secondary); padding: 16px; }
</style>
