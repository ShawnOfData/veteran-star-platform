<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>简历制作</h2>
      <div class="header-actions">
        <el-button @click="handleAutoFill" :loading="autoFilling">
          <el-icon><MagicStick /></el-icon> 一键填写
        </el-button>
        <el-button @click="openHistory">
          <el-icon><Clock /></el-icon> 历史简历
        </el-button>
        <el-button type="primary" @click="handleGenerate" :loading="generating">
          生成简历
        </el-button>
      </div>
    </div>

    <!-- 数据回填提示 -->
    <div v-if="portraitMeta" class="portrait-banner">
      <el-icon><InfoFilled /></el-icon>
      <span>已从你的学生画像自动回填基本信息</span>
      <span class="meta-items">
        <el-tag size="small" type="info">服役经历 {{ portraitMeta.hasMilitary ? '已填' : '未填' }}</el-tag>
        <el-tag size="small" type="info">荣誉 {{ portraitMeta.honorCount }}</el-tag>
        <el-tag size="small" type="info">证书 {{ portraitMeta.certCount }}</el-tag>
        <el-tag size="small" type="info">社会服务 {{ portraitMeta.serviceCount }}</el-tag>
      </span>
    </div>

    <!-- 步骤 1：选择模板 -->
    <div class="template-section">
      <h4 class="step-title">选择简历模板</h4>
      <div class="template-cards">
        <div
          v-for="t in templates"
          :key="t.code"
          class="template-card"
          :class="{ active: selectedTemplate === t.code }"
          @click="selectTemplate(t.code)"
        >
          <div class="template-preview" :class="'preview-' + t.code">
            <div class="preview-header">
              <span :class="'header-sim-' + t.code"></span>
            </div>
            <div class="preview-content">
              <div class="preview-line long"></div>
              <div class="preview-line short"></div>
              <div class="preview-line medium"></div>
              <div class="preview-line short"></div>
            </div>
          </div>
          <div class="template-name">{{ t.name }}</div>
          <div class="template-desc">{{ templateDescription(t.code) }}</div>
          <el-icon v-if="selectedTemplate === t.code" class="check-icon"><CircleCheckFilled /></el-icon>
        </div>
      </div>
    </div>

    <!-- 步骤 2 + 3：编辑 + 预览（左右分栏） -->
    <div class="editor-area">
      <!-- 左侧编辑表单 -->
      <div class="edit-panel">
        <h4 class="step-title">编辑简历信息</h4>

        <!-- 头像 -->
        <div class="form-group">
          <label>头像</label>
          <div class="avatar-upload">
            <el-upload
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="avatar-preview">
                <img v-if="editData.avatarUrl" :src="editData.avatarUrl" class="avatar-img" />
                <el-icon v-else class="avatar-placeholder"><Plus /></el-icon>
              </div>
            </el-upload>
            <span class="upload-tip">点击上传（JPG/PNG, &lt;2MB）</span>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="form-group">
          <label>姓名 <span class="required">*</span></label>
          <el-input v-model="editData.name" placeholder="请输入姓名" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>手机号</label>
            <el-input v-model="editData.phone" placeholder="11 位手机号" maxlength="11" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <el-input v-model="editData.email" placeholder="例：name@example.com" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>学院</label>
            <el-input v-model="editData.college" placeholder="请输入学院" />
          </div>
          <div class="form-group">
            <label>专业</label>
            <el-input v-model="editData.major" placeholder="请输入专业" />
          </div>
        </div>

        <!-- 求职意向 -->
        <h5 class="sub-title">求职意向</h5>
        <div class="form-row">
          <div class="form-group">
            <label>期望岗位</label>
            <el-input v-model="editData.expectedJob" placeholder="如：部队文职" />
          </div>
          <div class="form-group">
            <label>期望城市</label>
            <el-input v-model="editData.expectedCity" placeholder="如：北京" />
          </div>
        </div>
        <div class="form-group">
          <label>期望薪资</label>
          <el-input v-model="editData.expectedSalary" placeholder="如：8000-12000" />
        </div>

        <!-- 个人简介 -->
        <div class="form-group">
          <label>个人简介</label>
          <el-input
            v-model="editData.personalSummary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="简介将展示在简历顶部，突出个人优势..."
          />
        </div>

        <!-- 技能标签 -->
        <div class="form-group">
          <label>技能标签 <span class="hint">（最多 10 个，已 {{ editData.skills.length }}）</span></label>
          <div class="skill-tags">
            <el-tag
              v-for="(skill, idx) in editData.skills"
              :key="idx"
              closable
              @close="removeSkill(idx)"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <el-input
              v-if="skillInputVisible"
              ref="skillInputRef"
              v-model="skillInputValue"
              size="small"
              class="skill-input"
              @keyup.enter="addSkill"
              @blur="addSkill"
              placeholder="输入技能后回车"
            />
            <el-button v-else size="small" @click="showSkillInput" :disabled="editData.skills.length >= 10">
              + 添加
            </el-button>
          </div>
        </div>

        <!-- 模块开关 -->
        <h5 class="sub-title">简历模块</h5>
        <div class="switch-group">
          <div class="switch-item">
            <span>荣誉奖项</span>
            <el-switch v-model="editData.showHonors" />
          </div>
          <div class="switch-item">
            <span>技能证书</span>
            <el-switch v-model="editData.showCerts" />
          </div>
          <div class="switch-item">
            <span>社会服务</span>
            <el-switch v-model="editData.showServices" />
          </div>
          <div class="switch-item">
            <span>学业表现</span>
            <el-switch v-model="editData.showAcademic" />
          </div>
          <div class="switch-item">
            <span>就业意向</span>
            <el-switch v-model="editData.showIntention" />
          </div>
          <div class="switch-item">
            <span>实习经历</span>
            <el-switch v-model="editData.showInternships" />
          </div>
          <div class="switch-item">
            <span>项目经历</span>
            <el-switch v-model="editData.showProjects" />
          </div>
        </div>

        <!-- 实习经历 -->
        <h5 class="sub-title">实习经历</h5>
        <div v-for="(item, idx) in editData.internships" :key="'intern-' + idx" class="dyn-item">
          <div class="dyn-head">
            <span class="dyn-idx">实习 {{ idx + 1 }}</span>
            <el-button text type="danger" size="small" @click="removeInternship(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="form-group">
            <label>实习单位</label>
            <el-input v-model="item.company" placeholder="如：XX科技有限公司" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>职位</label>
              <el-input v-model="item.position" placeholder="如：前端开发实习生" />
            </div>
            <div class="form-group">
              <label>开始时间</label>
              <el-input v-model="item.startDate" placeholder="如：2024-06" />
            </div>
          </div>
          <div class="form-group">
            <label>结束时间</label>
            <el-input v-model="item.endDate" placeholder="如：2024-09 或 至今" />
          </div>
          <div class="form-group">
            <label>工作内容</label>
            <el-input v-model="item.description" type="textarea" :rows="2" placeholder="负责的工作与成果..." />
          </div>
        </div>
        <el-button size="small" plain class="add-btn" @click="addInternship">
          <el-icon><Plus /></el-icon> 添加实习经历
        </el-button>

        <!-- 项目经历 -->
        <h5 class="sub-title">项目经历</h5>
        <div v-for="(item, idx) in editData.projects" :key="'proj-' + idx" class="dyn-item">
          <div class="dyn-head">
            <span class="dyn-idx">项目 {{ idx + 1 }}</span>
            <el-button text type="danger" size="small" @click="removeProject(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="form-group">
            <label>项目名称</label>
            <el-input v-model="item.name" placeholder="如：校园就业服务平台" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>担任角色</label>
              <el-input v-model="item.role" placeholder="如：后端负责人" />
            </div>
            <div class="form-group">
              <label>开始时间</label>
              <el-input v-model="item.startDate" placeholder="如：2024-03" />
            </div>
          </div>
          <div class="form-group">
            <label>结束时间</label>
            <el-input v-model="item.endDate" placeholder="如：2024-06 或 至今" />
          </div>
          <div class="form-group">
            <label>项目描述</label>
            <el-input v-model="item.description" type="textarea" :rows="2" placeholder="项目职责与成果..." />
          </div>
        </div>
        <el-button size="small" plain class="add-btn" @click="addProject">
          <el-icon><Plus /></el-icon> 添加项目经历
        </el-button>

        <!-- 自定义模块 -->
        <h5 class="sub-title">自定义模块 <span class="hint">（可添加多个，灵活扩展）</span></h5>
        <div v-for="(item, idx) in editData.customSections" :key="'custom-' + idx" class="dyn-item">
          <div class="dyn-head">
            <span class="dyn-idx">模块 {{ idx + 1 }}</span>
            <el-button text type="danger" size="small" @click="removeCustomSection(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div class="form-group">
            <label>模块标题</label>
            <el-input v-model="item.title" placeholder="如：校园经历 / 特长爱好 / 自我评价" />
          </div>
          <div class="form-group">
            <label>模块内容</label>
            <el-input v-model="item.content" type="textarea" :rows="3" placeholder="支持多行内容..." />
          </div>
        </div>
        <el-button size="small" plain class="add-btn" @click="addCustomSection">
          <el-icon><Plus /></el-icon> 添加自定义模块
        </el-button>
      </div>

      <!-- 右侧预览 -->
      <div class="preview-panel">
        <h4 class="step-title">
          实时预览
          <span v-if="previewing" class="previewing-tag">刷新中...</span>
        </h4>
        <div class="preview-frame-wrapper">
          <iframe
            :srcdoc="previewHtml"
            class="preview-frame"
            frameborder="0"
          ></iframe>
        </div>
      </div>
    </div>

    <!-- 历史简历抽屉 -->
    <el-drawer v-model="historyDrawer" title="历史简历" size="420px" direction="rtl">
      <div v-loading="historyLoading">
        <div v-if="historyList.length === 0 && !historyLoading" class="empty-tip">
          <el-icon><DocumentRemove /></el-icon>
          <p>暂无生成记录</p>
        </div>
        <div v-else class="history-list">
          <div v-for="r in historyList" :key="r.id" class="history-item">
            <div class="history-info">
              <div class="history-template">
                <el-tag size="small" :type="templateTagType(r.templateCode)">
                  {{ templateLabel(r.templateCode) }}
                </el-tag>
              </div>
              <div class="history-time">{{ formatTime(r.createTime) }}</div>
              <div class="history-size">{{ formatSize(r.fileSize) }}</div>
            </div>
            <el-button size="small" type="primary" @click="downloadRecord(r)">
              下载
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { useStudentStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import {
  getTemplates,
  getEditInitData,
  getResumeRecords,
  getRecordDownloadUrl,
  getEditedPreview,
  generateEditedPdf,
  uploadAvatar
} from '@/api/resume'
import request from '@/api/request'

const studentStore = useStudentStore()
const loading = ref(false)
const generating = ref(false)
const previewing = ref(false)
const autoFilling = ref(false)

// 模板
const templates = ref([])
const selectedTemplate = ref('military')

// 画像完整度
const portraitMeta = ref(null)

// 编辑数据
const editData = reactive({
  name: '',
  phone: '',
  email: '',
  avatarUrl: '',
  college: '',
  major: '',
  expectedJob: '',
  expectedCity: '',
  expectedSalary: '',
  personalSummary: '',
  skills: [],
  showHonors: true,
  showCerts: true,
  showServices: true,
  showAcademic: true,
  showIntention: true,
  // 实习 / 项目 / 自定义模块（用户自填，可灵活增删）
  internships: [],
  projects: [],
  customSections: [],
  showInternships: true,
  showProjects: true,
  templateCode: 'military'
})

// 技能标签输入
const skillInputVisible = ref(false)
const skillInputValue = ref('')
const skillInputRef = ref(null)
const MAX_SKILLS = 10

// 预览 HTML
const previewHtml = ref('')

// 历史抽屉
const historyDrawer = ref(false)
const historyLoading = ref(false)
const historyList = ref([])

// 防抖定时器
let previewTimer = null

function selectTemplate(code) {
  selectedTemplate.value = code
  editData.templateCode = code
  requestPreview()
}

async function fetchTemplates() {
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } catch (e) {
    ElMessage.error('获取模板列表失败')
  }
}

// 拉取画像回填数据
async function fetchEditInitData() {
  loading.value = true
  try {
    const res = await getEditInitData(studentStore.studentId)
    const d = res.data || {}
    if (d.name != null) editData.name = d.name
    if (d.phone != null) editData.phone = d.phone
    if (d.email != null) editData.email = d.email
    if (d.college != null) editData.college = d.college
    if (d.major != null) editData.major = d.major
    if (d.expectedJob != null) editData.expectedJob = d.expectedJob
    if (d.expectedCity != null) editData.expectedCity = d.expectedCity
    if (d.expectedSalary != null) editData.expectedSalary = d.expectedSalary
    if (d.personalSummary != null) editData.personalSummary = d.personalSummary
    if (d.avatarUrl != null) editData.avatarUrl = d.avatarUrl
    if (Array.isArray(d.skills)) editData.skills = d.skills.slice(0, MAX_SKILLS)
    if (d.showHonors != null) editData.showHonors = d.showHonors
    if (d.showCerts != null) editData.showCerts = d.showCerts
    if (d.showServices != null) editData.showServices = d.showServices
    if (d.showAcademic != null) editData.showAcademic = d.showAcademic
    if (d.showIntention != null) editData.showIntention = d.showIntention
    if (Array.isArray(d.internships)) editData.internships = d.internships
    if (Array.isArray(d.projects)) editData.projects = d.projects
    if (Array.isArray(d.customSections)) editData.customSections = d.customSections
    if (d.showInternships != null) editData.showInternships = d.showInternships
    if (d.showProjects != null) editData.showProjects = d.showProjects
    portraitMeta.value = d.portraitMeta || null
  } catch (e) {
    // 静默失败，让用户手动填写
  } finally {
    loading.value = false
  }
}

// 一键自动填写：重置表单 + 从画像重新回填
async function handleAutoFill() {
  // 重置所有字段
  editData.name = ''
  editData.phone = ''
  editData.email = ''
  editData.avatarUrl = ''
  editData.college = ''
  editData.major = ''
  editData.expectedJob = ''
  editData.expectedCity = ''
  editData.expectedSalary = ''
  editData.personalSummary = ''
  editData.skills = []
  editData.showHonors = true
  editData.showCerts = true
  editData.showServices = true
  editData.showAcademic = true
  editData.showIntention = true
  editData.internships = []
  editData.projects = []
  editData.customSections = []
  editData.showInternships = true
  editData.showProjects = true

  autoFilling.value = true
  try {
    const res = await getEditInitData(studentStore.studentId)
    const d = res.data || {}
    if (d.name != null) editData.name = d.name
    if (d.phone != null) editData.phone = d.phone
    if (d.email != null) editData.email = d.email
    if (d.college != null) editData.college = d.college
    if (d.major != null) editData.major = d.major
    if (d.expectedCity != null) editData.expectedCity = d.expectedCity
    if (d.expectedSalary != null) editData.expectedSalary = d.expectedSalary
    if (Array.isArray(d.skills)) editData.skills = d.skills.slice(0, MAX_SKILLS)
    if (d.showHonors != null) editData.showHonors = d.showHonors
    if (d.showCerts != null) editData.showCerts = d.showCerts
    if (d.showServices != null) editData.showServices = d.showServices
    if (d.showAcademic != null) editData.showAcademic = d.showAcademic
    if (d.showIntention != null) editData.showIntention = d.showIntention
    if (Array.isArray(d.internships)) editData.internships = d.internships
    if (Array.isArray(d.projects)) editData.projects = d.projects
    if (Array.isArray(d.customSections)) editData.customSections = d.customSections
    if (d.showInternships != null) editData.showInternships = d.showInternships
    if (d.showProjects != null) editData.showProjects = d.showProjects
    portraitMeta.value = d.portraitMeta || null

    ElMessage.success('已从学生画像自动填写完成')
  } catch (e) {
    ElMessage.error('自动填写失败：' + (e.message || '网络异常'))
  } finally {
    autoFilling.value = false
  }
}

async function requestPreview() {
  if (previewTimer) clearTimeout(previewTimer)
  previewTimer = setTimeout(async () => {
    previewing.value = true
    try {
      const data = { ...editData }
      const res = await getEditedPreview(studentStore.studentId, data)
      const html = res.data || ''
      previewHtml.value = html.replace(
        /<img([^>]*?)src="([^"]*?)"/g,
        (match, before, src) => {
          if (src.startsWith('http') || src.startsWith('/')) return match
          return `<img${before}src="${window.location.origin}${src}"`
        }
      )
    } catch (e) {
      // 预览失败静默
    } finally {
      previewing.value = false
    }
  }, 500)
}

// 表单校验
function validate() {
  if (!editData.name || !editData.name.trim()) {
    ElMessage.warning('请输入姓名')
    return false
  }
  if (editData.phone && !/^1[3-9]\d{9}$/.test(editData.phone)) {
    ElMessage.warning('手机号格式不正确')
    return false
  }
  if (editData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editData.email)) {
    ElMessage.warning('邮箱格式不正确')
    return false
  }
  return true
}

// 监听表单变化自动预览
watch(
  () => ({ ...editData }),
  () => {
    requestPreview()
  },
  { deep: true }
)

// 头像上传
function beforeAvatarUpload(file) {
  const isValid = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
  if (!isValid) ElMessage.error('仅支持 JPG/PNG/WebP 格式')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) ElMessage.error('文件不能超过 2MB')
  return isValid && isLt2M
}

async function handleAvatarUpload(options) {
  try {
    const res = await uploadAvatar(studentStore.studentId, options.file)
    editData.avatarUrl = res.data.url
    ElMessage.success('头像上传成功')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

// 技能标签
function showSkillInput() {
  if (editData.skills.length >= MAX_SKILLS) {
    ElMessage.warning(`最多添加 ${MAX_SKILLS} 个技能标签`)
    return
  }
  skillInputVisible.value = true
  nextTick(() => {
    skillInputRef.value?.focus()
  })
}

function addSkill() {
  const val = skillInputValue.value.trim()
  if (val) {
    if (editData.skills.length >= MAX_SKILLS) {
      ElMessage.warning(`最多添加 ${MAX_SKILLS} 个技能标签`)
    } else if (editData.skills.includes(val)) {
      ElMessage.warning('该技能已存在')
    } else {
      editData.skills.push(val)
    }
  }
  skillInputVisible.value = false
  skillInputValue.value = ''
}

function removeSkill(idx) {
  editData.skills.splice(idx, 1)
}

// 实习经历
function addInternship() {
  editData.internships.push({ company: '', position: '', startDate: '', endDate: '', description: '' })
}
function removeInternship(idx) {
  editData.internships.splice(idx, 1)
}

// 项目经历
function addProject() {
  editData.projects.push({ name: '', role: '', startDate: '', endDate: '', description: '' })
}
function removeProject(idx) {
  editData.projects.splice(idx, 1)
}

// 自定义模块
function addCustomSection() {
  editData.customSections.push({ title: '', content: '' })
}
function removeCustomSection(idx) {
  editData.customSections.splice(idx, 1)
}

// 生成简历
async function handleGenerate() {
  if (!validate()) return
  generating.value = true
  try {
    const data = { ...editData }
    const res = await generateEditedPdf(studentStore.studentId, data)
    const url = res.data.url
    if (url) {
      // 用 axios 拉 blob，避免 window.open 在 404/500 时展示浏览器原生错误页
      try {
        const blobRes = await request.get(url, { responseType: 'blob', timeout: 60000 })
        const blobUrl = URL.createObjectURL(blobRes.data)
        const a = document.createElement('a')
        a.href = blobUrl
        a.download = `简历_${editData.name || studentStore.studentId}_${Date.now()}.pdf`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(blobUrl)
        ElMessage.success('简历生成成功，已开始下载')
      } catch (dlErr) {
        // 下载失败但 PDF 已生成，提示用户从历史记录重试
        ElMessage.warning('简历已生成，但下载失败，请从"历史简历"中重试下载')
      }
    } else {
      ElMessage.error('生成成功但未获取下载链接')
    }
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理，这里不再重复弹窗
    // 仅在控制台留痕，便于开发排查
    console.error('[Resume Generate Error]', e)
  } finally {
    generating.value = false
  }
}

// 历史简历
async function openHistory() {
  historyDrawer.value = true
  historyLoading.value = true
  try {
    const res = await getResumeRecords(studentStore.studentId, 10)
    historyList.value = res.data || []
  } catch (e) {
    // 错误提示已由拦截器统一处理
    console.error('[Load History Error]', e)
  } finally {
    historyLoading.value = false
  }
}

async function downloadRecord(r) {
  try {
    const blobRes = await request.get(getRecordDownloadUrl(r.id), { responseType: 'blob', timeout: 60000 })
    const blobUrl = URL.createObjectURL(blobRes.data)
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = `简历_${r.templateCode || 'resume'}_${r.id}.pdf`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(blobUrl)
    ElMessage.success('已开始下载')
  } catch (e) {
    // 错误提示已由拦截器统一处理
    console.error('[Download Record Error]', e)
  }
}

function templateLabel(code) {
  const map = { military: '军旅风', simple: '简约风', government: '政务风' }
  return map[code] || code
}

function templateDescription(code) {
  const map = {
    military: '深蓝配金 · 适合军人气质与政府单位',
    simple: '极简清爽 · 适合通用求职场景',
    government: '红金配色 · 适合文职与事业单位'
  }
  return map[code] || ''
}

function templateTagType(code) {
  if (code === 'military') return 'warning'
  if (code === 'government') return 'danger'
  return 'info'
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

onMounted(async () => {
  await fetchTemplates()
  await fetchEditInitData()
  // 初始加载默认预览
  requestPreview()
})
</script>

<style scoped>
/* ========== 步骤标题 ========== */
.step-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 0 0 14px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.previewing-tag {
  font-size: 12px;
  font-weight: 400;
  color: var(--color-gold);
}

/* ========== 页头操作 ========== */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.page-header h2 {
  flex: 1;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-primary);
}
.header-actions {
  display: flex;
  gap: 8px;
}

/* ========== 画像回填提示 ========== */
.portrait-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
  padding: 10px 16px;
  border-radius: var(--radius);
  margin-bottom: 16px;
  font-size: 13px;
}
.portrait-banner .el-icon {
  font-size: 16px;
}
.meta-items {
  display: flex;
  gap: 6px;
  margin-left: auto;
}

/* ========== 表单 ========== */
.required {
  color: #ef4444;
  margin-left: 2px;
}
.hint {
  font-size: 12px;
  color: var(--color-text-secondary);
  font-weight: 400;
}

/* ========== 模板选择 ========== */
.template-section {
  background: #fff;
  border-radius: var(--radius);
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
}
.template-cards {
  display: flex;
  gap: 16px;
}
.template-card {
  flex: 1;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
}
.template-card:hover {
  border-color: var(--color-gold);
  box-shadow: 0 2px 12px rgba(196, 163, 90, 0.15);
}
.template-card.active {
  border-color: var(--color-gold);
  box-shadow: 0 2px 16px rgba(196, 163, 90, 0.25);
}
.template-preview {
  height: 120px;
  border-radius: 4px;
  margin-bottom: 10px;
  overflow: hidden;
  padding: 12px 16px;
}
.preview-military {
  background: #1a1a2e;
}
.preview-military .header-sim-military {
  display: block;
  height: 24px;
  width: 60%;
  background: #c4a35a;
  margin-bottom: 16px;
}
.preview-simple {
  background: #fff;
  border: 1px solid #eee;
}
.preview-simple .header-sim-simple {
  display: block;
  height: 18px;
  width: 50%;
  background: #999;
  margin: 0 auto 16px;
}
.preview-government {
  background: #d44;
}
.preview-government .header-sim-government {
  display: block;
  height: 24px;
  width: 70%;
  background: rgba(255,255,255,0.5);
  margin-bottom: 16px;
}
.preview-content {
  padding: 0 8px;
}
.preview-line {
  height: 8px;
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
  margin-bottom: 8px;
}
.preview-simple .preview-line { background: #ddd; }
.preview-government .preview-line { background: rgba(255,255,255,0.3); }
.preview-line.long { width: 85%; }
.preview-line.medium { width: 60%; }
.preview-line.short { width: 40%; }

.template-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}
.template-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  line-height: 1.4;
}
.check-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 22px;
  color: var(--color-gold);
}

/* ========== 编辑区域 ========== */
.editor-area {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 360px);
}

.edit-panel {
  width: 380px;
  flex-shrink: 0;
  background: #fff;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
  overflow-y: auto;
  max-height: calc(100vh - 260px);
}

.preview-panel {
  flex: 1;
  background: #fff;
  border-radius: var(--radius);
  padding: 20px 24px;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
}

/* ========== 表单 ========== */
.form-group {
  margin-bottom: 14px;
}
.form-group label {
  display: block;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}
.form-row {
  display: flex;
  gap: 12px;
}
.form-row .form-group {
  flex: 1;
}
.sub-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
  margin: 16px 0 10px 0;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

/* ========== 头像 ========== */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-preview {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px dashed var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  background: var(--color-bg);
}
.avatar-preview:hover {
  border-color: var(--color-gold);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  font-size: 24px;
  color: var(--color-text-secondary);
}
.upload-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ========== 技能标签 ========== */
.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.skill-tag {
  margin: 0;
}
.skill-input {
  width: 120px;
}

/* ========== 模块开关 ========== */
.switch-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--color-text);
}

/* ========== 动态条目（实习/项目/自定义模块） ========== */
.dyn-item {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  background: var(--color-bg);
}
.dyn-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.dyn-idx {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-gold);
}
.add-btn {
  width: 100%;
  border-style: dashed;
  margin-bottom: 4px;
}

/* ========== 预览区 ========== */
.preview-frame-wrapper {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
}
.preview-frame {
  width: 100%;
  height: 100%;
  min-height: 600px;
  background: #fff;
}

/* ========== 历史简历抽屉 ========== */
.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: var(--color-text-secondary);
}
.empty-tip .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #cbd5e1;
}
.empty-tip p {
  margin: 0;
  font-size: 14px;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 4px;
}
.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: all 0.2s;
}
.history-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}
.history-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.history-template {
  margin-bottom: 2px;
}
.history-time {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.history-size {
  font-size: 11px;
  color: #94a3b8;
}
</style>
