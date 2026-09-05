<!--
  ResumeCreate.vue — 简历生成
  模板选择 + 预览 + 生成下载
-->
<template>
  <div class="resume-page page-with-tabbar">
    <van-nav-bar title="简历生成" />

    <!-- 模板选择 -->
    <div class="template-section">
      <h3 class="section-title">选择模板</h3>
      <div class="template-list">
        <div
          v-for="tpl in templates"
          :key="tpl.key"
          class="template-card"
          :class="{ active: selectedTemplate === tpl.key }"
          @click="selectTemplate(tpl.key)"
        >
          <div class="tpl-icon">
            <van-icon :name="tpl.icon" size="28" :color="selectedTemplate === tpl.key ? '#fff' : '#2563EB'" />
          </div>
          <p class="tpl-name">{{ tpl.name }}</p>
          <p class="tpl-desc">{{ tpl.desc }}</p>
        </div>
      </div>
    </div>

    <!-- 简历预览 -->
    <div class="preview-section">
      <div class="preview-header">
        <h3 class="section-title">简历预览</h3>
        <van-button plain hairline size="mini" @click="refreshPreview" :loading="previewLoading">
          刷新
        </van-button>
      </div>
      <LoadingState v-if="previewLoading" height="400px" />
      <div v-else class="preview-container">
        <iframe
          v-if="previewUrl"
          :src="previewUrl"
          class="preview-iframe"
          frameborder="0"
        />
        <EmptyState v-else description="选择模板后点击刷新预览" />
      </div>
    </div>

    <!-- 生成记录 -->
    <div class="records-section" v-if="records.length">
      <h3 class="section-title">历史记录</h3>
      <van-cell-group inset>
        <van-cell
          v-for="record in records.slice(0, 5)"
          :key="record.id"
          :title="record.templateName || '简历'"
          :label="formatTime(record.createTime) + ' · ' + (record.fileSize || '-')"
          is-link
          @click="downloadRecord(record)"
        >
          <template #right-icon>
            <van-icon name="down" color="#2563EB" size="18" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <!-- 底部操作栏 -->
    <van-action-bar>
      <van-action-bar-button type="warning" text="生成PDF" @click="handleGenerate" :loading="generating" />
      <van-action-bar-button type="primary" text="下载" @click="handleDownload" :disabled="!downloadUrl" />
    </van-action-bar>
  </div>
</template>

<script setup>
/**
 * 简历生成页
 * - 选择模板（军旅风/简约风/政务风）
 * - iframe 预览
 * - 生成 PDF + 下载
 * - 历史记录
 */
import { ref, onMounted, watch } from 'vue'
import { useStudentStore } from '@/stores/student'
import { getResumeTemplates, getResumePreview, getResumeDownloadUrl, generateResume, getResumeRecords } from '@/api/studentApp'
import { showToast } from 'vant'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const store = useStudentStore()

// 模板列表
const templates = ref([
  { key: 'military', name: '军旅风', desc: '深蓝配金·军人气质', icon: 'medal-o' },
  { key: 'simple', name: '简约风', desc: '简洁清新·通用场景', icon: 'description' },
  { key: 'government', name: '政务风', desc: '庄重严肃·政府单位', icon: 'gem-o' }
])
const selectedTemplate = ref('military')

// 预览
const previewUrl = ref('')
const previewLoading = ref(false)

// 生成/下载
const generating = ref(false)
const downloadUrl = ref('')

// 历史记录
const records = ref([])

onMounted(async () => {
  await fetchTemplates()
  await refreshPreview()
  fetchRecords()
})

/** 获取模板列表（后端） */
async function fetchTemplates() {
  try {
    const res = await getResumeTemplates()
    if (res.data && Array.isArray(res.data) && res.data.length) {
      templates.value = res.data.map(t => ({
        key: t.key || t.id,
        name: t.name,
        desc: t.description || '',
        icon: 'description'
      }))
    }
  } catch (e) { /* 使用默认模板列表 */ }
}

/** 选择模板 */
async function selectTemplate(key) {
  selectedTemplate.value = key
  downloadUrl.value = ''
  await refreshPreview()
}

/** 刷新预览 */
async function refreshPreview() {
  previewLoading.value = true
  try {
    const res = await getResumePreview(store.studentId, selectedTemplate.value)
    if (res.data) {
      // 如果返回 HTML 字符串，用 blob URL
      if (typeof res.data === 'string') {
        const blob = new Blob([res.data], { type: 'text/html' })
        previewUrl.value = URL.createObjectURL(blob)
      } else if (res.data.url) {
        previewUrl.value = res.data.url
      } else if (res.data.previewUrl) {
        previewUrl.value = res.data.previewUrl
      }
    }
  } catch (e) { /* 拦截器处理 */ }
  finally { previewLoading.value = false }
}

/** 生成 PDF */
async function handleGenerate() {
  generating.value = true
  try {
    const res = await generateResume({
      studentId: store.studentId,
      template: selectedTemplate.value
    })
    if (res.data) {
      downloadUrl.value = res.data.downloadUrl || getResumeDownloadUrl(store.studentId, selectedTemplate.value)
      showToast({ message: '生成成功', type: 'success' })
      fetchRecords()
    }
  } catch (e) { /* 拦截器处理 */ }
  finally { generating.value = false }
}

/** 下载简历 */
function handleDownload() {
  if (downloadUrl.value) {
    window.open(downloadUrl.value, '_blank')
  } else {
    showToast('请先生成PDF')
  }
}

/** 下载历史记录 */
function downloadRecord(record) {
  if (record.downloadUrl) {
    window.open(record.downloadUrl, '_blank')
  }
}

/** 获取历史记录 */
async function fetchRecords() {
  try {
    const res = await getResumeRecords(store.studentId)
    records.value = res.data || []
  } catch (e) { /* 拦截器处理 */ }
}

function formatTime(t) { return t ? (t.length > 10 ? t.slice(0, 10) : t) : '-' }
</script>

<style scoped>
.resume-page { background: var(--color-bg-page); min-height: 100vh; padding-bottom: 60px; }

/* 模板选择 */
.template-section { padding: 12px 16px; }
.section-title { font-size: 15px; font-weight: 600; margin-bottom: 8px; }
.template-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}
.template-card {
  min-width: 100px;
  background: #fff;
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}
.template-card.active {
  border-color: var(--color-primary);
  background: linear-gradient(135deg, #409eff, #2563EB);
}
.tpl-icon {
  width: 48px; height: 48px;
  margin: 0 auto 6px;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.1);
  display: flex; align-items: center; justify-content: center;
}
.template-card.active .tpl-icon { background: rgba(255, 255, 255, 0.2); }
.tpl-name { font-size: 13px; font-weight: 600; color: var(--color-text-primary); }
.template-card.active .tpl-name { color: #fff; }
.tpl-desc { font-size: 11px; color: var(--color-text-secondary); margin-top: 2px; }
.template-card.active .tpl-desc { color: rgba(255, 255, 255, 0.8); }

/* 预览 */
.preview-section { margin-top: 8px; padding: 0 16px; }
.preview-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.preview-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.preview-iframe { width: 100%; height: 400px; border: none; }

/* 历史记录 */
.records-section { margin-top: 12px; padding: 0 16px; }
</style>
