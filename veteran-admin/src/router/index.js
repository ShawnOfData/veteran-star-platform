import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useStudentStore } from '@/stores/student'

const routes = [
  // ========== 学生端 ==========
  {
    path: '/login',
    name: 'StudentLogin',
    component: () => import('@/views/student/Login.vue'),
    meta: { title: '学生登录 - 戎归·星辉' }
  },
  {
    path: '/register',
    name: 'StudentRegister',
    component: () => import('@/views/student/Register.vue'),
    meta: { title: '学生注册 - 戎归·星辉' }
  },
  {
    path: '/',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人档案', icon: 'User' }
      },
      {
        path: 'points',
        name: 'StudentPoints',
        component: () => import('@/views/student/Points.vue'),
        meta: { title: '我的积分', icon: 'Medal' }
      },
      {
        path: 'opportunities',
        name: 'StudentOpportunities',
        component: () => import('@/views/student/Opportunities.vue'),
        meta: { title: '机会广场', icon: 'Opportunity' }
      },
      {
        path: 'ranking',
        name: 'StudentRanking',
        component: () => import('@/views/student/Ranking.vue'),
        meta: { title: '积分排行', icon: 'Trophy' }
      },
      {
        path: 'my-applications',
        name: 'StudentMyApplications',
        component: () => import('@/views/student/MyApplications.vue'),
        meta: { title: '我的报名', icon: 'Document' }
      },
      {
        path: 'my-favorites',
        name: 'StudentMyFavorites',
        component: () => import('@/views/student/MyFavorites.vue'),
        meta: { title: '我的收藏', icon: 'Star' }
      },
      {
        path: 'settings',
        name: 'StudentSettings',
        component: () => import('@/views/student/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      },
      {
        path: 'portrait',
        name: 'StudentPortrait',
        component: () => import('@/views/student/Portrait.vue'),
        meta: { title: '个人画像', icon: 'DataAnalysis' }
      },
      {
        path: 'resume-create',
        name: 'StudentResumeCreate',
        component: () => import('@/views/student/ResumeCreate.vue'),
        meta: { title: '简历制作', icon: 'Document' }
      }
    ]
  },
  // ========== 管理端 ==========
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录 - 戎归·星辉' }
  },
  {
    path: '/admin',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'students',
        name: 'Students',
        component: () => import('@/views/Students.vue'),
        meta: { title: '学生管理', icon: 'User' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/Points.vue'),
        meta: { title: '积分管理', icon: 'Medal' }
      },
      {
        path: 'opportunities',
        name: 'Opportunities',
        component: () => import('@/views/Opportunities.vue'),
        meta: { title: '机会管理', icon: 'Opportunity' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting', requireSuper: true }
      },
      {
        path: 'review',
        name: 'Review',
        component: () => import('@/views/Review.vue'),
        meta: { title: '审核管理', icon: 'Finished' }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/Applications.vue'),
        meta: { title: '报名管理', icon: 'Document' }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/Announcements.vue'),
        meta: { title: '公告管理', icon: 'Warning' }
      }
    ]
  },
  // ========== 404 兜底 ==========
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到 - 戎归·星辉' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '戎归·星辉'

  const userStore = useUserStore()
  const studentStore = useStudentStore()

  // 管理端优先检查
  const isAdminPath = to.path.startsWith('/admin/') || to.path === '/admin'

  if (isAdminPath) {
    // 管理端登录页
    if (to.path === '/admin/login') {
      if (userStore.token) {
        next('/admin/dashboard')
      } else {
        next()
      }
      return
    }

    // 管理端页面（需登录）
    if (!userStore.token) {
      next('/admin/login')
      return
    }

    if (to.meta.requireSuper && userStore.role !== 'super') {
      next('/admin/dashboard')
      return
    }

    next()
    return
  }

  // 学生端登录/注册页
  if (to.path === '/login' || to.path === '/register') {
    if (studentStore.isLoggedIn) {
      next('/home')
    } else {
      next()
    }
    return
  }

  // 学生端页面（需登录）
  if (!studentStore.isLoggedIn) {
    next('/login')
    return
  }

  next()
})

export default router
