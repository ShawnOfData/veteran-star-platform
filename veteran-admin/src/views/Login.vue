<template>
  <div class="login-page">
    <!-- Background -->
    <div class="login-bg">
      <img src="/background.png" alt="background" />
    </div>

    <!-- Scan overlay -->
    <div class="scan-overlay"></div>
    <div class="scan-line"></div>

    <!-- Particles -->
    <canvas ref="canvasRef" class="particle-canvas"></canvas>

    <!-- ===== Left: Hero ===== -->
    <div class="hero-area">
      <div class="hero-corner hero-corner--tl"></div>
      <div class="hero-corner hero-corner--tr"></div>
      <div class="hero-corner hero-corner--bl"></div>
      <div class="hero-corner hero-corner--br"></div>

      <div class="hero-badge">
        <div class="emblem">
          <svg viewBox="0 0 24 24" fill="none" stroke="#c4a35a" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="badge-text">
          <span class="badge-label">★ 戎归·星辉</span>
          <span class="badge-title">信息档案与积分管理系统</span>
        </div>
      </div>

      <h1 class="hero-title">
        <span class="gold">管理</span><span class="sep">·</span><span class="light">门户</span>
        <span class="dim">ADMIN PORTAL</span>
      </h1>

      <div class="hero-divider"></div>

      <p class="hero-tagline">
        学生管理 · 积分核发 · 机会发布<br>
        档案审核 · 数据分析 · 公告管理
      </p>
    </div>

    <!-- ===== Right: Login Panel ===== -->
    <div class="login-panel" :class="{ shake: shaking }">
      <div class="panel-accent"></div>

      <div class="panel-header">
        <h2>管理员登录</h2>
        <p>请使用管理员账号登录系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="UserIcon"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="LockIcon"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
        </el-form-item>

        <el-form-item class="form-btn-item">
          <button type="submit" class="login-btn" :disabled="loading">
            <span class="btn-text" v-if="!loading">登 录</span>
            <span class="btn-text" v-else>
              <span class="spinner">
                <span class="spinner-dot"></span>
                <span class="spinner-dot"></span>
                <span class="spinner-dot"></span>
              </span>
            </span>
          </button>
        </el-form-item>
      </el-form>

      <div class="panel-bottom">
        <span><span class="dot"></span> v3.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const canvasRef = ref(null)
const formRef = ref(null)
const loading = ref(false)
const shaking = ref(false)
let animFrameId = null

const form = reactive({
  username: localStorage.getItem('rememberedUser') || '',
  password: localStorage.getItem('rememberedPass') || ''
})

const rememberMe = ref(!!localStorage.getItem('rememberedUser'))

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// SVG icon components
const UserIcon = h('svg', {
  viewBox: '0 0 24 24', width: '16', height: '16',
  fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5',
  'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const LockIcon = h('svg', {
  viewBox: '0 0 24 24', width: '16', height: '16',
  fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5',
  'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('rect', { x: '3', y: '11', width: '18', height: '11', rx: '2', ry: '2' }),
  h('path', { d: 'M7 11V7a5 5 0 0 1 10 0v4' })
])

// ===== Star Particles =====
function initStars() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  let w, h

  function resize() {
    w = canvas.width = window.innerWidth
    h = canvas.height = window.innerHeight
  }
  window.addEventListener('resize', resize)
  resize()

  const COUNT = 120
  const stars = []
  for (let i = 0; i < COUNT; i++) {
    stars.push({
      x: Math.random() * w,
      y: Math.random() * h,
      r: Math.random() * 1.2 + 0.3,
      speed: Math.random() * 0.15 + 0.03,
      opacity: Math.random() * 0.5 + 0.1
    })
  }

  function draw() {
    ctx.clearRect(0, 0, w, h)
    stars.forEach(s => {
      s.y -= s.speed
      if (s.y < -5) {
        s.y = h + 5
        s.x = Math.random() * w
      }
      const twinkle = 0.6 + 0.4 * Math.sin(Date.now() * 0.002 + s.x)
      ctx.beginPath()
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255,255,255,${s.opacity * twinkle})`
      ctx.fill()
    })
    animFrameId = requestAnimationFrame(draw)
  }
  draw()
}

onMounted(() => {
  initStars()
})

onUnmounted(() => {
  if (animFrameId) cancelAnimationFrame(animFrameId)
})

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password })

    if (rememberMe.value) {
      localStorage.setItem('rememberedUser', form.username)
      localStorage.setItem('rememberedPass', form.password)
    } else {
      localStorage.removeItem('rememberedUser')
      localStorage.removeItem('rememberedPass')
    }

    ElMessage.success('欢迎回来')
    router.push('/admin/dashboard')
  } catch (e) {
    shaking.value = true
    setTimeout(() => { shaking.value = false }, 500)
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== Reset & Page ===== */
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: #080c14;
}

/* ===== Background ===== */
.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.login-bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.45) saturate(0.7);
  transform: scale(1.05);
}
.login-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(8,12,20,0.92) 0%, rgba(8,12,20,0.55) 40%, transparent 65%);
  pointer-events: none;
}

/* ===== Scan overlay & line ===== */
.scan-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(196, 163, 90, 0.015) 2px,
    rgba(196, 163, 90, 0.015) 4px
  );
}

.scan-line {
  position: absolute;
  left: 0;
  width: 100%;
  height: 1px;
  z-index: 1;
  background: linear-gradient(90deg, transparent 0%, rgba(196, 163, 90, 0.15) 50%, transparent 100%);
  animation: scanMove 8s linear infinite;
  pointer-events: none;
}
@keyframes scanMove {
  0%   { top: 0; opacity: 0; }
  10%  { opacity: 1; }
  90%  { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* ===== Particles ===== */
.particle-canvas {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

/* ===== Left: Hero ===== */
.hero-area {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px 80px 100px;
  min-width: 0;
}

/* Corner brackets */
.hero-corner {
  position: absolute;
  width: 60px;
  height: 60px;
  border-color: rgba(196, 163, 90, 0.2);
  border-style: solid;
  border-width: 0;
  pointer-events: none;
}
.hero-corner--tl { top: 40px; left: 40px; border-top-width: 1px; border-left-width: 1px; }
.hero-corner--tr { top: 40px; right: 40px; border-top-width: 1px; border-right-width: 1px; }
.hero-corner--bl { bottom: 40px; left: 40px; border-bottom-width: 1px; border-left-width: 1px; }
.hero-corner--br { bottom: 40px; right: 40px; border-bottom-width: 1px; border-right-width: 1px; }

/* Badge */
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 32px;
}
.hero-badge .emblem {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid rgba(196, 163, 90, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
}
.hero-badge .emblem::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  border: 1px solid rgba(196, 163, 90, 0.1);
}
.hero-badge .emblem svg {
  width: 28px;
  height: 28px;
}
.hero-badge .badge-text {
  display: flex;
  flex-direction: column;
}
.hero-badge .badge-text .badge-label {
  font-size: 11px;
  color: rgba(196, 163, 90, 0.5);
  letter-spacing: 3px;
  text-transform: uppercase;
}
.hero-badge .badge-text .badge-title {
  font-size: 13px;
  color: rgba(196, 163, 90, 0.8);
  letter-spacing: 2px;
  margin-top: 2px;
}

/* Title */
.hero-title {
  font-size: clamp(36px, 5vw, 56px);
  font-weight: 900;
  line-height: 1.15;
  letter-spacing: 6px;
  margin-bottom: 4px;
}
.hero-title .gold {
  color: #c4a35a;
}
.hero-title .sep {
  font-weight: 300;
  color: rgba(255,255,255,0.3);
}
.hero-title .light {
  font-weight: 400;
  color: rgba(255,255,255,0.8);
}
.hero-title .dim {
  font-weight: 300;
  color: rgba(255,255,255,0.3);
  letter-spacing: 12px;
  font-size: 0.5em;
  display: block;
  margin-top: 6px;
}

/* Divider */
.hero-divider {
  width: 80px;
  height: 1px;
  background: linear-gradient(90deg, rgba(196, 163, 90, 0.5), transparent);
  margin: 28px 0 20px;
}

/* Tagline */
.hero-tagline {
  font-size: 13px;
  font-weight: 300;
  color: rgba(255,255,255,0.25);
  letter-spacing: 2px;
  line-height: 1.8;
}

/* ===== Right: Login Panel ===== */
.login-panel {
  position: relative;
  z-index: 2;
  width: 460px;
  min-width: 460px;
  height: 100vh;
  background: rgba(10, 15, 24, 0.95);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 52px;
  border-left: 1px solid rgba(196, 163, 90, 0.08);
  box-shadow: -20px 0 60px rgba(0,0,0,0.5);
}

.login-panel.shake {
  animation: shake 0.4s ease-in-out;
}
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-8px); }
  40% { transform: translateX(8px); }
  60% { transform: translateX(-6px); }
  80% { transform: translateX(6px); }
}

.panel-accent {
  position: absolute;
  top: 0;
  left: 52px;
  right: 52px;
  height: 2px;
  background: linear-gradient(90deg, rgba(196, 163, 90, 0.6), rgba(196, 163, 90, 0.1));
}

.panel-header {
  margin-bottom: 44px;
}
.panel-header h2 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px;
  margin-bottom: 6px;
  color: #fff;
}
.panel-header p {
  font-size: 13px;
  color: rgba(255,255,255,0.3);
  letter-spacing: 1px;
  font-weight: 300;
}

/* ===== Form ===== */
.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 0;
  box-shadow: none !important;
  padding: 0 16px;
  height: 48px;
  transition: border-color 0.4s ease, background 0.4s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(196, 163, 90, 0.25);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #c4a35a;
  background: rgba(196, 163, 90, 0.03);
}

.login-form :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 300;
  letter-spacing: 0.5px;
  height: 48px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.15);
  font-weight: 300;
}

.login-form :deep(.el-input__prefix) {
  color: rgba(196, 163, 90, 0.3);
  margin-right: 8px;
}

.login-form :deep(.el-form-item__error) {
  color: #e74c3c;
  font-size: 12px;
  padding-top: 4px;
}

.login-form :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  letter-spacing: 0.5px;
}

.login-form :deep(.el-checkbox__inner) {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.2);
}

.login-form :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #c4a35a;
  border-color: #c4a35a;
}

.login-form :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: rgba(196, 163, 90, 0.7);
}

/* ===== Login Button ===== */
.form-btn-item {
  margin-bottom: 0 !important;
}

.login-btn {
  position: relative;
  width: 100%;
  height: 48px;
  border: 1px solid #2d5a27;
  background: #2d5a27;
  color: #fff;
  font-size: 15px;
  font-family: inherit;
  font-weight: 500;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.4s ease;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover {
  background: #3a6e33;
  border-color: #3a6e33;
  box-shadow: 0 0 30px rgba(45, 90, 39, 0.3);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -75%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.06), transparent);
  transform: skewX(-25deg);
  transition: left 0.6s ease;
}

.login-btn:hover::before {
  left: 125%;
}

/* Loading spinner */
.spinner {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.spinner-dot {
  width: 5px;
  height: 5px;
  background: rgba(255,255,255,0.6);
  border-radius: 50%;
  animation: dotBounce 1.2s ease-in-out infinite;
}
.spinner-dot:nth-child(2) { animation-delay: 0.2s; }
.spinner-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* ===== Bottom bar ===== */
.panel-bottom {
  position: absolute;
  bottom: 32px;
  left: 52px;
  right: 52px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: rgba(255,255,255,0.12);
  letter-spacing: 1px;
}
.panel-bottom .dot {
  display: inline-block;
  width: 3px;
  height: 3px;
  background: rgba(196, 163, 90, 0.2);
  border-radius: 50%;
  margin: 0 8px;
  vertical-align: middle;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .login-panel {
    width: 100%;
    min-width: 0;
    border-left: none;
  }
  .hero-area { display: none; }
}
</style>
