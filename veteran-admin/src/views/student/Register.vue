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
          <span class="badge-label">★ 星辉计划</span>
          <span class="badge-title">退役大学生士兵 · 人才服务</span>
        </div>
      </div>

      <h1 class="hero-title">
        <span class="gold">戎归</span><span class="sep">·</span><span class="light">星辉</span>
        <span class="dim">STARLIGHT HONOR</span>
      </h1>

      <p class="hero-subtitle">
        <span class="typewriter" ref="typewriterRef">退役不退志，校园续荣光</span>
      </p>

      <div class="hero-divider"></div>

      <p class="hero-tagline">
        信息档案 · 积分管理 · 就业服务<br>
        为退役大学生士兵打造专属成长平台
      </p>
    </div>

    <!-- ===== Right: Register Panel ===== -->
    <div class="register-panel">
      <div class="panel-accent"></div>

      <div class="panel-header">
        <h2>加入戎归</h2>
        <p>退役大学生士兵信息登记</p>
      </div>

      <el-steps :active="step" align-center class="reg-steps" finish-status="success">
        <el-step title="身份验证" />
        <el-step title="填写信息" />
        <el-step title="完成注册" />
      </el-steps>

      <div class="panel-form-wrap">
        <el-form
          v-show="step === 0"
          ref="step0Ref"
          :model="form"
          :rules="step0Rules"
          class="login-form"
        >
          <el-form-item prop="studentNo">
            <el-input
              v-model="form.studentNo"
              placeholder="请输入学号"
              :prefix-icon="PostcardIcon"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入姓名"
              :prefix-icon="UserIcon"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              :prefix-icon="IphoneIcon"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              placeholder="请设置登录密码（至少6位）"
              :prefix-icon="LockIcon"
              size="large"
              type="password"
              show-password
            />
          </el-form-item>
        </el-form>

        <el-form
          v-show="step === 1"
          ref="step1Ref"
          :model="form"
          :rules="step1Rules"
          class="login-form"
        >
          <el-form-item prop="college">
            <el-select
              v-model="form.college"
              placeholder="请选择学院"
              size="large"
              style="width: 100%"
              @change="onCollegeChange"
            >
              <el-option
                v-for="c in colleges"
                :key="c.name"
                :label="c.name"
                :value="c.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="major">
            <el-select
              v-model="form.major"
              placeholder="请选择专业"
              size="large"
              style="width: 100%"
              :disabled="!form.college"
            >
              <el-option
                v-for="m in currentMajors"
                :key="m.name"
                :label="m.suspended ? `${m.name}（停招）` : m.name"
                :value="m.name"
                :disabled="m.suspended"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="grade">
            <el-select
              v-model="form.grade"
              placeholder="请选择年级"
              size="large"
              style="width: 100%"
            >
              <el-option
                v-for="y in gradeOptions"
                :key="y"
                :label="y"
                :value="y"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="retireDate">
            <el-date-picker
              v-model="form.retireDate"
              type="month"
              placeholder="请选择退役时间（年月）"
              value-format="YYYY-MM"
              style="width: 100%"
              size="large"
            />
          </el-form-item>
        </el-form>

        <div v-show="step === 2" class="complete-section">
          <div class="complete-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <h3>信息确认</h3>
          <p class="complete-hint">请确认以下信息无误</p>
          <div class="info-preview">
            <div class="info-row"><span>学号</span><span>{{ form.studentNo }}</span></div>
            <div class="info-row"><span>姓名</span><span>{{ form.name }}</span></div>
            <div class="info-row"><span>手机号</span><span>{{ form.phone }}</span></div>
            <div class="info-row"><span>学院</span><span>{{ form.college }}</span></div>
            <div class="info-row"><span>专业</span><span>{{ form.major }}</span></div>
            <div class="info-row"><span>年级</span><span>{{ form.grade }}</span></div>
            <div class="info-row"><span>退役时间</span><span>{{ form.retireDate }}</span></div>
          </div>
        </div>
      </div>

      <!-- ===== Shared Actions (fixed below scrollable area) ===== -->
      <div class="panel-actions">
        <template v-if="step === 0">
          <button type="button" class="login-btn" @click="nextStep">
            <span class="btn-text">下一步</span>
          </button>
        </template>
        <template v-if="step === 1">
          <div class="btn-group">
            <button type="button" class="login-btn secondary" @click="step = 0">
              <span class="btn-text">上一步</span>
            </button>
            <button type="button" class="login-btn" @click="nextStep">
              <span class="btn-text">下一步</span>
            </button>
          </div>
        </template>
        <template v-if="step === 2">
          <div class="btn-group">
            <button type="button" class="login-btn secondary" @click="step = 1">
              <span class="btn-text">上一步</span>
            </button>
            <button type="button" class="login-btn" :disabled="submitting" @click="handleRegister">
              <span class="btn-text" v-if="!submitting">确认注册</span>
              <span class="btn-text" v-else>
                <span class="spinner">
                  <span class="spinner-dot"></span>
                  <span class="spinner-dot"></span>
                  <span class="spinner-dot"></span>
                </span>
              </span>
            </button>
          </div>
        </template>
      </div>

      <div class="panel-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="register-link">立即登录</router-link>
      </div>

      <div class="panel-bottom">
        <span><span class="dot"></span> v3.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, h, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentStore } from '@/stores/student'
import { studentRegister } from '@/api/studentApp'
import { ElMessage } from 'element-plus'

let typeTimer = null
let animFrameId = null

const router = useRouter()
const studentStore = useStudentStore()

// ==================== 学院/专业 数据 ====================
const colleges = [
  { name: '地球科学学院', majors: ['地质学', '资源勘查工程', { name: '海洋资源与环境', suspended: true }, '古生物学', '地球物理学', '勘查技术与工程'] },
  { name: '水资源与环境学院', majors: ['环境工程', '水文与水资源工程', '地下水科学与工程'] },
  { name: '城市地质与工程学院', majors: ['地质工程', '土木工程', '工程管理', '工程造价', { name: '城市地下空间工程', suspended: true }] },
  { name: '宝石与材料学院', majors: ['宝石及材料工艺学', '矿物加工工程', '产品设计（珠宝首饰设计方向）', '材料科学与工程'] },
  { name: '土地科学与空间规划学院', majors: ['土地资源管理', '地理信息科学', '测绘工程', '城乡规划', '遥感科学与技术', { name: '土地整治工程', suspended: true }] },
  { name: '经济学院', majors: ['经济学', { name: '国际经济与贸易', suspended: true }, '金融学', '经济统计学'] },
  { name: '管理学院', majors: ['工商管理', '市场营销', '会计学', { name: '财务管理', suspended: true }, '审计学', '信息管理与信息系统', '电子商务', '物流管理', '旅游管理', { name: '采购管理', suspended: true }] },
  { name: '法政学院', majors: ['法学', '行政管理', '劳动与社会保障'] },
  { name: '艺术学院', majors: ['视觉传达设计', '环境设计', '广告学', '产品设计（家居产品方向）', '书法学', '播音与主持艺术', '广播电视编导', '影视摄影与制作'] },
  { name: '信息工程学院', majors: ['计算机科学与技术', '软件工程', '数据科学与大数据技术', '信息安全', '电子信息工程', '通信工程'] },
  { name: '语言文化学院', majors: ['英语', '法语', '捷克语', '汉语国际教育'] },
  { name: '数理教学部', majors: ['数学与应用数学', '信息与计算科学', '应用统计学'] }
]

// 标准化 majors：将字符串转成 { name, suspended } 结构
function normalizeMajor(m) {
  return typeof m === 'string' ? { name: m, suspended: false } : m
}

// 当前学院对应的专业列表
const currentMajors = computed(() => {
  const college = colleges.find(c => c.name === form.college)
  return college ? college.majors.map(normalizeMajor) : []
})

// 切换学院时清空专业选择
function onCollegeChange() {
  form.major = ''
}

// 年级选项：当前年份前后各 5 年
const currentYear = new Date().getFullYear()
const gradeOptions = computed(() => {
  const years = []
  for (let y = currentYear + 2; y >= currentYear - 5; y--) {
    years.push(String(y))
  }
  return years
})

const canvasRef = ref(null)
const typewriterRef = ref(null)
const step0Ref = ref(null)
const step1Ref = ref(null)
const step = ref(0)
const submitting = ref(false)

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

const step0Rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const step1Rules = {
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  major: [{ required: true, message: '请选择专业', trigger: 'change' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  retireDate: [{ required: true, message: '请选择退役时间', trigger: 'change' }]
}

// SVG icon components
const PostcardIcon = h('svg', {
  viewBox: '0 0 24 24', width: '16', height: '16',
  fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5',
  'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const UserIcon = h('svg', {
  viewBox: '0 0 24 24', width: '16', height: '16',
  fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5',
  'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: '12', cy: '7', r: '4' })
])

const IphoneIcon = h('svg', {
  viewBox: '0 0 24 24', width: '16', height: '16',
  fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5',
  'stroke-linecap': 'round', 'stroke-linejoin': 'round'
}, [
  h('rect', { x: '5', y: '2', width: '14', height: '20', rx: '2', ry: '2' }),
  h('line', { x1: '12', y1: '18', x2: '12.01', y2: '18' })
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

// ===== Typewriter =====
onMounted(() => {
  initStars()
  const el = typewriterRef.value
  if (!el) return

  const phrases = [
    '退役不退志，校园续荣光',
    '戎耀青春，再启新程',
    '自强不息，厚德载物'
  ]
  let phraseIdx = 0
  let charIdx = 0
  let isDeleting = false

  function type() {
    if (!el) return
    const current = phrases[phraseIdx]
    if (!isDeleting) {
      el.textContent = current.substring(0, charIdx + 1)
      charIdx++
      if (charIdx === current.length) {
        setTimeout(() => { isDeleting = true; type() }, 2000)
        return
      }
    } else {
      el.textContent = current.substring(0, charIdx - 1)
      charIdx--
      if (charIdx === 0) {
        isDeleting = false
        phraseIdx = (phraseIdx + 1) % phrases.length
      }
    }
    const speed = isDeleting ? 40 : 80
    typeTimer = setTimeout(type, speed)
  }
  type()
})

onUnmounted(() => {
  if (typeTimer) clearTimeout(typeTimer)
  if (animFrameId) cancelAnimationFrame(animFrameId)
})

async function nextStep() {
  if (step.value === 0) {
    const valid = await step0Ref.value.validate().catch(() => false)
    if (!valid) return
    step.value = 1
  } else if (step.value === 1) {
    const valid = await step1Ref.value.validate().catch(() => false)
    if (!valid) return
    step.value = 2
  }
}

async function handleRegister() {
  submitting.value = true
  try {
    const res = await studentRegister({
      studentNo: form.studentNo,
      name: form.name,
      phone: form.phone,
      password: form.password,
      college: form.college,
      major: form.major,
      grade: form.grade,
      retireDate: form.retireDate
    })

    studentStore.login({
      id: res.data.id,
      studentNo: res.data.studentNo,
      name: res.data.name,
      phone: res.data.phone
    })

    ElMessage.success('注册成功，欢迎加入戎归！')
    setTimeout(() => {
      router.push('/home')
    }, 800)
  } catch (e) {
    ElMessage.error(e.message || '注册失败，请重试')
  } finally {
    submitting.value = false
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
.hero-title .gold { color: #c4a35a; }
.hero-title .sep { font-weight: 300; color: rgba(255,255,255,0.3); }
.hero-title .light { font-weight: 400; color: rgba(255,255,255,0.8); }
.hero-title .dim {
  font-weight: 300;
  color: rgba(255,255,255,0.3);
  letter-spacing: 12px;
  font-size: 0.5em;
  display: block;
  margin-top: 6px;
}

/* Subtitle */
.hero-subtitle {
  font-size: 15px;
  font-weight: 300;
  color: rgba(255,255,255,0.4);
  letter-spacing: 4px;
  margin-top: 14px;
}
.hero-subtitle .typewriter {
  display: inline;
  border-right: 2px solid rgba(196, 163, 90, 0.6);
  animation: blink 0.8s step-end infinite;
  padding-right: 2px;
}
@keyframes blink {
  0%, 100% { border-color: rgba(196, 163, 90, 0.6); }
  50%      { border-color: transparent; }
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

/* ===== Right: Register Panel ===== */
.register-panel {
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

.panel-accent {
  position: absolute;
  top: 0;
  left: 52px;
  right: 52px;
  height: 2px;
  background: linear-gradient(90deg, rgba(196, 163, 90, 0.6), rgba(196, 163, 90, 0.1));
}

.panel-header {
  margin-bottom: 24px;
}
.panel-header h2 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px;
  margin-bottom: 4px;
  color: #fff;
}
.panel-header p {
  font-size: 13px;
  color: rgba(255,255,255,0.3);
  letter-spacing: 1px;
  font-weight: 300;
}

.panel-form-wrap {
  max-height: calc(100vh - 380px);
  overflow-y: auto;
  padding: 4px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(196,163,90,0.15) transparent;
}
.panel-form-wrap::-webkit-scrollbar { width: 4px; }
.panel-form-wrap::-webkit-scrollbar-thumb { background: rgba(196,163,90,0.2); border-radius: 2px; }

/* ===== Panel Actions (fixed buttons) ===== */
.panel-actions {
  padding: 16px 0 0;
}
.panel-actions .btn-group {
  display: flex;
  gap: 12px;
  width: 100%;
}
.panel-actions .login-btn {
  width: 100%;
}

/* ===== Complete Section ===== */
.complete-section {
  text-align: center;
  padding: 8px 0;
}

.complete-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: var(--color-military);
  border-radius: 50%;
  margin-bottom: 12px;
  border: 1px solid rgba(196, 163, 90, 0.2);
}

.complete-section h3 {
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 2px;
  margin-bottom: 4px;
}

.complete-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.info-preview {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255,255,255,0.06);
  padding: 14px 20px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  letter-spacing: 0.5px;
}

.info-row:last-child { border-bottom: none; }
.info-row span:last-child { color: rgba(255, 255, 255, 0.8); }

/* ===== Steps ===== */
.reg-steps {
  margin-bottom: 28px;
}
.reg-steps :deep(.el-step__title) {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 0.5px;
}
.reg-steps :deep(.el-step__title.is-process) {
  color: #c4a35a;
}
.reg-steps :deep(.el-step__title.is-success) {
  color: var(--color-military);
}
.reg-steps :deep(.el-step__head) {
  border-color: rgba(255,255,255,0.15);
}
.reg-steps :deep(.el-step__head.is-process) {
  border-color: #c4a35a;
  color: #c4a35a;
}
.reg-steps :deep(.el-step__head.is-success) {
  border-color: var(--color-military);
  color: var(--color-military);
}
.reg-steps :deep(.el-step__line) {
  background: rgba(255,255,255,0.1);
}
.reg-steps :deep(.el-step__line.is-success) {
  background: var(--color-military);
}
.reg-steps :deep(.el-step__icon) {
  background: transparent;
  border-color: inherit;
}
.reg-steps :deep(.el-step__icon-inner) {
  color: inherit;
  font-weight: 600;
}

/* ===== Form ===== */
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper),
.login-form :deep(.el-select .el-input__wrapper),
.login-form :deep(.el-date-editor .el-input__wrapper) {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 0;
  box-shadow: none !important;
  padding: 0 16px;
  height: 48px;
  transition: border-color 0.4s ease, background 0.4s ease;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-select .el-input__wrapper:hover),
.login-form :deep(.el-date-editor .el-input__wrapper:hover) {
  border-color: rgba(196, 163, 90, 0.25);
}

.login-form :deep(.el-input__wrapper.is-focus),
.login-form :deep(.el-select .el-input__wrapper.is-focus),
.login-form :deep(.el-date-editor .el-input__wrapper.is-focus) {
  border-color: #c4a35a;
  background: rgba(196, 163, 90, 0.03);
}

.login-form :deep(.el-input__inner),
.login-form :deep(.el-select .el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 300;
  letter-spacing: 0.5px;
  height: 48px;
}

.login-form :deep(.el-input__inner::placeholder),
.login-form :deep(.el-select .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.15);
  font-weight: 300;
}

.login-form :deep(.el-input__prefix),
.login-form :deep(.el-select .el-input__prefix) {
  color: rgba(196, 163, 90, 0.3);
  margin-right: 8px;
}

.login-form :deep(.el-form-item__error) {
  color: #e74c3c;
  font-size: 12px;
  padding-top: 4px;
}

.login-form :deep(.el-select) { width: 100%; }

/* Date picker icon color */
.login-form :deep(.el-date-editor .el-input__prefix) {
  color: rgba(196, 163, 90, 0.3);
}

/* ===== Login Button ===== */
.login-btn {
  position: relative;
  flex: 1;
  height: 48px;
  border: none;
  background-image: linear-gradient(135deg, #409eff 0%, #2563EB 100%);
  color: #fff;
  font-size: 15px;
  font-family: inherit;
  font-weight: 500;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.3);
}

.login-btn:hover {
  background-image: linear-gradient(135deg, #2563EB 0%, #1d4ed8 100%);
  box-shadow: 0 6px 22px rgba(37, 99, 235, 0.4);
  transform: translateY(-2px);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.login-btn.secondary {
  background: rgba(255,255,255,0.06);
  border-color: rgba(255,255,255,0.12);
  color: rgba(255,255,255,0.5);
  letter-spacing: 2px;
}
.login-btn.secondary:hover {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.2);
  box-shadow: none;
}

.login-btn .btn-text { position: relative; z-index: 1; }

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

/* ===== Panel Footer ===== */
.panel-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 12px;
  color: rgba(255,255,255,0.2);
  letter-spacing: 1px;
}
.register-link {
  color: rgba(196, 163, 90, 0.5);
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.3s;
}
.register-link:hover { color: #c4a35a; }

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
  .register-panel {
    width: 100%;
    min-width: 0;
    border-left: none;
  }
  .hero-area { display: none; }
}
</style>
