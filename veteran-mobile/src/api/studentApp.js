/**
 * 学生端 API 接口定义
 * 迁移自 veteran-admin/src/api/studentApp.js，适配移动端
 * 所有接口通过 request 实例自动添加 /api 前缀
 */
import request from './request'

// ========== 认证 ==========

/**
 * 学生登录
 * @param {Object} data - { studentNo, password }
 * @returns {Promise} 登录结果
 */
export function studentLogin(data) {
  return request.post('/app/student/login', data)
}

/**
 * 学生注册
 * @param {Object} data - 注册信息
 * @returns {Promise} 注册结果
 */
export function studentRegister(data) {
  return request.post('/app/student/register', data)
}

// ========== 学生信息 ==========

/**
 * 获取学生信息
 * @param {number} studentId - 学生ID
 */
export function getStudentInfo(studentId) {
  return request.get(`/app/student/${studentId}`)
}

/**
 * 更新学生信息
 * @param {number} studentId - 学生ID
 * @param {Object} data - 更新数据
 */
export function updateStudentInfo(studentId, data) {
  return request.put(`/app/student/${studentId}`, data)
}

// ========== 服役经历 / 荣誉 / 证书 ==========

export function getServiceExperiences(studentId) {
  return request.get(`/app/student/service-experience/list/${studentId}`)
}

export function getHonors(serviceExperienceId) {
  return request.get(`/app/student/honor/list/${serviceExperienceId}`)
}

export function getSkills(studentId) {
  return request.get(`/app/student/skill/list/${studentId}`)
}

// ========== 积分 / 服务记录 ==========

export function getPointsDetail(studentId, params) {
  return request.get(`/app/social/points/${studentId}`, { params })
}

export function getServiceRecords(studentId) {
  return request.get(`/app/social/record/list/${studentId}`)
}

// ========== 就业机会 ==========

export function getOpportunityList(params) {
  return request.get('/app/opportunity/list', { params })
}

export function getOpportunityById(id) {
  return request.get(`/app/opportunity/${id}`)
}

export function applyOpportunity(data) {
  return request.post('/app/opportunity/apply', data)
}

export function cancelApplication(applicationId, studentId) {
  return request.put(`/app/opportunity/cancel/${applicationId}`, null, { params: { studentId } })
}

export function incrementViewCount(id) {
  return request.put(`/app/opportunity/view/${id}`)
}

export function getMyApplications(studentId, params) {
  return request.get('/app/opportunity/my-applications', { params: { studentId, ...params } })
}

export function favoriteOpportunity(studentId, opportunityId) {
  return request.post('/app/opportunity/favorite', { studentId, opportunityId })
}

export function unfavoriteOpportunity(studentId, opportunityId) {
  return request.delete(`/app/opportunity/favorite/${studentId}/${opportunityId}`)
}

export function getMyFavorites(studentId, params) {
  return request.get('/app/opportunity/my-favorites', { params: { studentId, ...params } })
}

// ========== 字典 ==========

export function getDictBranch() {
  return request.get('/app/dict/branch')
}

export function getDictHonor() {
  return request.get('/app/dict/honor')
}

export function getDictCert() {
  return request.get('/app/dict/cert')
}

export function getDictHonorCategory() {
  return request.get('/app/dict/honor-category')
}

export function getDictLeaderPost() {
  return request.get('/app/dict/leader-post')
}

export function getDictPostType() {
  return request.get('/app/dict/post-type')
}

export function getDictJobType() {
  return request.get('/app/dict/job-type')
}

// ========== 排行 / 首页 ==========

export function getRankingList(params) {
  return request.get('/app/ranking/list', { params })
}

export function getStudentHome(studentId) {
  return request.get(`/app/home/${studentId}`)
}

// ========== 学生提交审核 ==========

export function submitServiceExperience(data) {
  return request.post('/app/student/submit/service-experience', data)
}

export function updateServiceExperience(data) {
  return request.put('/app/student/submit/service-experience', data)
}

export function submitHonor(data) {
  return request.post('/app/student/submit/honor', data)
}

export function submitSkill(data) {
  return request.post('/app/student/submit/skill', data)
}

// 上传证明材料（证书等，支持 png/jpg/pdf）
export function uploadProof(file) {
  return request.post('/app/student/submit/upload', file, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getMySubmissions(studentId) {
  return request.get(`/app/student/submit/list/${studentId}`)
}

// ========== 设置 / 画像 / 简历 ==========

export function getSettings(studentId) {
  return request.get(`/app/student/${studentId}/settings`)
}

export function updateSettings(studentId, data) {
  return request.put(`/app/student/${studentId}/settings`, data)
}

export function updatePhone(studentId, data) {
  return request.put(`/app/student/${studentId}/phone`, data)
}

export function getSystemInfo() {
  return request.get('/app/settings/info')
}

export function updatePassword(studentId, data) {
  return request.put(`/app/student/${studentId}/password`, data)
}

export function getPortrait(studentId) {
  return request.get(`/app/student/${studentId}/portrait`)
}

export function getAIAnalysis(studentId) {
  return request.get(`/app/student/${studentId}/ai-analysis`)
}

// ========== 简历 ==========

export function getResumeTemplates() {
  return request.get('/app/resume/templates')
}

export function getResumePreview(studentId, template) {
  return request.get(`/app/resume/${studentId}/preview`, { params: { template } })
}

export function getResumeDownloadUrl(studentId, template) {
  return `/api/app/resume/${studentId}/download?template=${template}`
}

export function getResumeEditData(studentId) {
  return request.get(`/app/resume/edit-data/${studentId}`)
}

export function generateResume(data) {
  return request.post('/app/resume/generate', data)
}

export function getResumeRecords(studentId) {
  return request.get(`/app/resume/records/${studentId}`)
}
