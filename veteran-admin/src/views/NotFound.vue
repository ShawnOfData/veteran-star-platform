<template>
  <div class="not-found">
    <div class="content">
      <div class="error-code">404</div>
      <div class="error-text">抱歉，您访问的页面不存在</div>
      <div class="error-hint">可能页面已下线、链接错误，或您输入的地址有误</div>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'

const router = useRouter()
const studentStore = useStudentStore()

function goHome() {
  if (studentStore.isLoggedIn) {
    router.push('/home')
  } else {
    router.push('/login')
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    goHome()
  }
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}
.content {
  text-align: center;
  padding: 40px;
}
.error-code {
  font-size: 120px;
  font-weight: 700;
  background: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 20px;
}
.error-text {
  font-size: 22px;
  color: #1e293b;
  font-weight: 500;
  margin-bottom: 10px;
}
.error-hint {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 30px;
}
.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
