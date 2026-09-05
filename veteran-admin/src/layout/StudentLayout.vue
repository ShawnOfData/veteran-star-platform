<template>
  <div class="main-layout" :class="{ collapsed: appStore.sidebarCollapsed }">
    <aside class="sidebar">
      <div class="sidebar-bg"></div>
      <div class="sidebar-overlay"></div>
      <div class="sidebar-inner">
        <div class="sidebar-header">
          <div class="sidebar-logo">
            <img src="/logo.png" class="logo-image-sidebar" alt="logo" />
            <div v-show="!appStore.sidebarCollapsed" class="logo-text-group">
              <div class="logo-title">戎归·星辉<br/>退役大学生士兵管理系统</div>
              <span class="logo-sub">Veteran Management System</span>
            </div>
          </div>
        </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#475569"
        active-text-color="#2563EB"
        router
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人档案</span>
        </el-menu-item>
        <el-menu-item index="/points">
          <el-icon><Medal /></el-icon>
          <span>我的积分</span>
        </el-menu-item>
        <el-menu-item index="/opportunities">
          <el-icon><Opportunity /></el-icon>
          <span>机会广场</span>
        </el-menu-item>
        <el-menu-item index="/ranking">
          <el-icon><Trophy /></el-icon>
          <span>积分排行</span>
        </el-menu-item>
        <el-menu-item index="/my-applications">
          <el-icon><Document /></el-icon>
          <span>我的报名</span>
        </el-menu-item>
        <el-menu-item index="/my-favorites">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/portrait">
          <el-icon><DataAnalysis /></el-icon>
          <span>个人画像</span>
        </el-menu-item>
        <el-menu-item index="/resume-create">
          <el-icon><Document /></el-icon>
          <span>简历制作</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="collapse-btn" @click="appStore.toggleSidebar()">
          <el-icon><Fold v-if="!appStore.sidebarCollapsed" /><Expand v-else /></el-icon>
        </div>
      </div>
    </div>
    </aside>

    <div class="main-area">
      <header class="top-bar">
        <div class="top-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="top-right">
          <span class="welcome-text">{{ greeting }}，{{ studentStore.studentName }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ studentStore.studentNo }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人档案
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { useAppStore } from '@/stores/app'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const studentStore = useStudentStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      studentStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.sidebar-bg {
  position: absolute;
  inset: 0;
  background: url('/侧边栏背景图.png') center/cover no-repeat;
  z-index: 0;
}

.sidebar-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.92) 0%, rgba(255,255,255,0.85) 100%);
  z-index: 1;
}

.main-layout.collapsed .sidebar {
  width: 64px;
}

.sidebar-inner {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  position: relative;
  flex-shrink: 0;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.logo-image-sidebar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.logo-text-group {
  flex: 1;
  min-width: 0;
}
.logo-title {
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.4;
  letter-spacing: 0.5px;
}
.logo-sub {
  font-size: 10px;
  color: #909399;
  letter-spacing: 0.3px;
  display: block;
  margin-top: 2px;
}

.sidebar :deep(.el-menu) {
  position: relative;
  z-index: 2;
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.sidebar :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.sidebar :deep(.el-menu-item:hover) {
  background-color: rgba(37, 99, 235, 0.1) !important;
  color: #2563EB !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.12) !important;
  color: #2563EB !important;
  font-weight: 600;
  border-left: none !important;
}

.sidebar-footer {
  position: relative;
  z-index: 2;
  padding: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  color: #475569;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.collapse-btn:hover {
  color: #2563EB;
  background: rgba(37, 99, 235, 0.08);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
  z-index: 10;
}

.top-left :deep(.el-breadcrumb__inner) {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text);
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: var(--color-bg);
}

.content-area {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg);
}

/* 限制主内容区最大宽度，避免宽屏下内容过宽 */
.content-area :deep(.page-container) {
  max-width: 1400px;
  margin: 0 auto;
}
</style>