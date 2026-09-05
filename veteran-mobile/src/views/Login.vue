<!--
  Login.vue — 学生登录页
  全屏布局，学号+密码登录，移动端优化
-->
<template>
  <div class="login-page">
    <!-- 顶部装饰区 -->
    <div class="login-header">
      <div class="logo-circle">
        <van-icon name="shield-o" size="48" color="#fff" />
      </div>
      <h1 class="login-title">戎归·星辉</h1>
      <p class="login-subtitle">退役大学生士兵综合管理服务平台</p>
    </div>

    <!-- 表单区 -->
    <div class="login-form-wrapper">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="form.studentNo"
            name="studentNo"
            label="学号"
            label-width="50"
            placeholder="请输入学号"
            left-icon="manager-o"
            :rules="[{ required: true, message: '请输入学号' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            label-width="50"
            placeholder="请输入密码"
            left-icon="lock"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>

        <div class="login-btn-wrapper">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            loading-text="登录中..."
          >
            登 录
          </van-button>
        </div>
      </van-form>

      <!-- 底部注册入口 -->
      <div class="register-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页逻辑
 * 1. 表单验证：学号+密码必填
 * 2. 调用 studentLogin API，发送 loginMode: 'password'
 * 3. 登录成功后保存 token 到 store，跳转首页
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { studentLogin } from '@/api/studentApp'
import { showToast } from 'vant'

const router = useRouter()
const studentStore = useStudentStore()
const loading = ref(false)

const form = reactive({
  studentNo: localStorage.getItem('mobile_remember_no') || '',
  password: ''
})

/**
 * 处理登录
 * 发送 loginMode: 'password' 指定密码登录模式
 * 响应格式: { code:200, data:{ id, studentNo, name, phone, token } }
 */
async function handleLogin() {
  loading.value = true
  try {
    const res = await studentLogin({
      studentNo: form.studentNo,
      password: form.password,
      loginMode: 'password'
    })

    // 保存登录状态（res 是完整响应体，数据在 res.data 中）
    studentStore.login({
      id: res.data.id,
      studentNo: res.data.studentNo,
      name: res.data.name,
      phone: res.data.phone,
      token: res.data.token
    })

    // 记住学号
    localStorage.setItem('mobile_remember_no', form.studentNo)

    showToast({ message: '欢迎回来', type: 'success' })
    router.push('/home')
  } catch (e) {
    // 错误已由 request.js 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #2563EB 0%, #1d4ed8 40%, #f7f8fa 40%, #f7f8fa 100%);
  display: flex;
  flex-direction: column;
}

/* 顶部装饰 */
.login-header {
  text-align: center;
  padding: 60px 0 30px;
}
.logo-circle {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.2);
}
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
  margin-bottom: 4px;
}
.login-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

/* 表单区 */
.login-form-wrapper {
  flex: 1;
  padding: 20px 16px;
}
.login-btn-wrapper {
  margin: 24px 16px;
}
.register-link {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
  padding-bottom: 20px;
}
.register-link a {
  color: var(--color-primary);
  font-weight: 500;
}
</style>
