import request from './request'

// 获取模板列表
export function getTemplates() {
  return request.get('/app/resume/templates')
}

// 获取编辑初始数据（从学生画像回填）
export function getEditInitData(studentId) {
  return request.get(`/app/resume/edit-data/${studentId}`)
}

// 获取简历生成历史
export function getResumeRecords(studentId, limit = 10) {
  return request.get(`/app/resume/records/${studentId}`, { params: { limit } })
}

// 按记录 ID 下载
export function getRecordDownloadUrl(recordId) {
  return `/app/resume/record/${recordId}/download`
}

// 获取编辑后的 HTML 预览（POST 提交编辑数据）
export function getEditedPreview(studentId, data) {
  return request.post('/app/resume/preview', data, { params: { studentId } })
}

// 生成编辑后的 PDF
export function generateEditedPdf(studentId, data) {
  return request.post('/app/resume/generate', data, { params: { studentId } })
}

// 上传头像
export function uploadAvatar(studentId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/app/student/${studentId}/avatar`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
