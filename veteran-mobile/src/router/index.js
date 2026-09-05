/**
 * 路由配置
 * - /login, /register: 无需认证
 * - 其他路由: 需要 Token，走 StudentLayout（含 Tabbar）
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useStudentStore } from '@/stores/student'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
      { path: 'settings', name: 'Settings', component: () => import('@/views/Settings.vue') },
      { path: 'points', name: 'Points', component: () => import('@/views/Points.vue') },
      { path: 'ranking', name: 'Ranking', component: () => import('@/views/Ranking.vue') },
      { path: 'portrait', name: 'Portrait', component: () => import('@/views/Portrait.vue') },
      { path: 'opportunities', name: 'Opportunities', component: () => import('@/views/Opportunities.vue') },
      { path: 'opportunities/:id', name: 'OpportunityDetail', component: () => import('@/views/OpportunityDetail.vue') },
      { path: 'applications', name: 'MyApplications', component: () => import('@/views/MyApplications.vue') },
      { path: 'favorites', name: 'MyFavorites', component: () => import('@/views/MyFavorites.vue') },
      { path: 'resume', name: 'ResumeCreate', component: () => import('@/views/ResumeCreate.vue') }
    ]
  },
  // 404 兜底
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 全局前置守卫：未登录跳转 /login
router.beforeEach((to, from, next) => {
  const store = useStudentStore()
  if (to.meta.requiresAuth !== false && !store.isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && store.isLoggedIn) {
    // 已登录访问登录页，跳转首页
    next('/home')
  } else {
    next()
  }
})

export default router
