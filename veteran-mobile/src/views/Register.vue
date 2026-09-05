<!--
  Register.vue — 学生注册页
  三步向导：身份验证 → 填写信息 → 确认提交
-->
<template>
  <div class="register-page">
    <van-nav-bar title="学生注册" left-arrow @click-left="router.push('/login')" />

    <!-- 步骤指示器 -->
    <van-steps :active="step" active-color="#2563EB">
      <van-step>身份验证</van-step>
      <van-step>填写信息</van-step>
      <van-step>确认提交</van-step>
    </van-steps>

    <!-- 步骤1: 身份验证 -->
    <div v-show="step === 0" class="step-content">
      <van-form @submit="nextStep0">
        <van-cell-group inset>
          <van-field
            v-model="form.studentNo"
            label="学号"
            label-width="60"
            placeholder="请输入学号"
            left-icon="manager-o"
            :rules="[{ required: true, message: '请输入学号' },
                     { pattern: /^[A-Za-z0-9]{6,20}$/, message: '学号为6-20位字母或数字' }]"
          />
          <van-field
            v-model="form.name"
            label="姓名"
            label-width="60"
            placeholder="请输入真实姓名"
            left-icon="contact"
            :rules="[{ required: true, message: '请输入姓名' }]"
          />
          <van-field
            v-model="form.phone"
            label="手机号"
            label-width="60"
            type="digit"
            maxlength="11"
            placeholder="请输入手机号"
            left-icon="phone-o"
            :rules="[{ required: true, message: '请输入手机号' },
                     { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
          />
          <van-field
            v-model="form.password"
            label="密码"
            label-width="60"
            type="password"
            placeholder="至少6位密码"
            left-icon="lock"
            :rules="[{ required: true, message: '请输入密码' },
                     { validator: (v) => v.length >= 6, message: '密码至少6位' }]"
          />
        </van-cell-group>
        <div class="step-btn">
          <van-button round block type="primary" native-type="submit">下一步</van-button>
        </div>
      </van-form>
    </div>

    <!-- 步骤2: 填写信息 -->
    <div v-show="step === 1" class="step-content">
      <van-form @submit="nextStep1">
        <van-cell-group inset>
          <van-field
            v-model="form.college"
            label="学院"
            label-width="60"
            placeholder="如：计算机学院"
            left-icon="cluster-o"
          />
          <van-field
            v-model="form.major"
            label="专业"
            label-width="60"
            placeholder="如：软件工程"
            left-icon="bookmark-o"
          />
          <van-field
            v-model="form.grade"
            label="年级"
            label-width="60"
            placeholder="如：2024级"
            left-icon="medal-o"
          />
          <van-field
            v-model="form.retireDate"
            label="退役时间"
            label-width="60"
            placeholder="选择退役时间（年月）"
            left-icon="calendar-o"
            readonly
            @click="showDatePicker = true"
          />
        </van-cell-group>

        <van-popup v-model:show="showDatePicker" position="bottom" round>
          <van-date-picker
            v-model="datePick"
            type="year-month"
            title="选择退役时间"
            @confirm="onDateConfirm"
            @cancel="showDatePicker = false"
          />
        </van-popup>

        <div class="step-btn">
          <van-button round block plain style="margin-bottom: 12px" @click="step = 0">上一步</van-button>
          <van-button round block type="primary" native-type="submit">下一步</van-button>
        </div>
      </van-form>
    </div>

    <!-- 步骤3: 确认提交 -->
    <div v-show="step === 2" class="step-content">
      <van-cell-group inset title="请确认以下信息">
        <van-cell title="学号" :value="form.studentNo" />
        <van-cell title="姓名" :value="form.name" />
        <van-cell title="手机号" :value="maskPhone(form.phone)" />
        <van-cell title="学院" :value="form.college || '-'" />
        <van-cell title="专业" :value="form.major || '-'" />
        <van-cell title="年级" :value="form.grade || '-'" />
        <van-cell title="退役时间" :value="form.retireDate || '-'" />
      </van-cell-group>

      <div class="step-btn">
        <van-button round block plain style="margin-bottom: 12px" @click="step = 1">上一步</van-button>
        <van-button round block type="primary" :loading="submitting" @click="handleSubmit">
          确认注册
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 注册页逻辑
 * 三步向导：身份验证 → 填写信息 → 确认提交
 * 注册接口不返回 token，成功后跳转登录页
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { studentRegister } from '@/api/studentApp'
import { showToast } from 'vant'

const router = useRouter()
const step = ref(0)
const submitting = ref(false)
const showDatePicker = ref(false)
const datePick = ref([])

const form = reactive({
  studentNo: '',
  name: '',
  phone: '',
  password: '',
  college: '',
  major: '',
  grade: '',
  retireDate: ''
})

/** 步骤1 → 步骤2 */
function nextStep0() {
  step.value = 1
}

/** 步骤2 → 步骤3 */
function nextStep1() {
  step.value = 2
}

/** 日期选择确认 */
function onDateConfirm({ selectedValues }) {
  form.retireDate = selectedValues.join('-')
  showDatePicker.value = false
}

/** 手机号脱敏显示 */
function maskPhone(phone) {
  if (!phone || phone.length !== 11) return phone
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

/**
 * 提交注册
 * 不发送 smsCode（后端已改为可选）
 * 注册成功后跳转登录页（注册接口不返回 token）
 */
async function handleSubmit() {
  submitting.value = true
  try {
    await studentRegister({
      studentNo: form.studentNo,
      name: form.name,
      phone: form.phone,
      password: form.password,
      college: form.college,
      major: form.major,
      grade: form.grade,
      retireDate: form.retireDate
    })

    showToast({ message: '注册成功，请登录', type: 'success' })
    // 注册不返回 token，跳转登录页
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    // 错误已由 request.js 拦截器统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.step-content {
  padding: 20px 0;
}

.step-btn {
  margin: 24px 16px;
}
</style>
