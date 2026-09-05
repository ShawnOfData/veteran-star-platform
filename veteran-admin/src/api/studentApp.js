import request from './request'

// 学生注册
export function studentRegister(data) {
  return request.post('/app/student/register', data)
}

// 学生登录
export function studentLogin(data) {
  return request.post('/app/student/login', data)
}

export function getStudentInfo(studentId) {
  return request.get(`/app/student/${studentId}`)
}

export function updateStudentInfo(studentId, data) {
  return request.put(`/app/student/${studentId}`, data)
}

export function getServiceExperiences(studentId) {
  return request.get(`/app/student/service-experience/list/${studentId}`)
}

export function getHonors(serviceExperienceId) {
  return request.get(`/app/student/honor/list/${serviceExperienceId}`)
}

export function getSkills(studentId) {
  return request.get(`/app/student/skill/list/${studentId}`)
}

export function getPointsDetail(studentId, params) {
  return request.get(`/app/social/points/${studentId}`, { params })
}

export function getServiceRecords(studentId) {
  return request.get(`/app/social/record/list/${studentId}`)
}

export function createServiceRecord(data) {
  return request.post('/admin/social/record', data)
}

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

export function getRankingList(params) {
  return request.get('/app/ranking/list', { params })
}

export function getStudentHome(studentId) {
  return request.get(`/app/home/${studentId}`)
}

// ========== 学生提交审核 ==========

// 提交服役经历
export function submitServiceExperience(data) {
  return request.post('/app/student/submit/service-experience', data)
}

// 修改服役经历
export function updateServiceExperience(data) {
  return request.put('/app/student/submit/service-experience', data)
}

// 提交荣誉
export function submitHonor(data) {
  return request.post('/app/student/submit/honor', data)
}

// 提交技能证书
export function submitSkill(data) {
  return request.post('/app/student/submit/skill', data)
}

// 查询我的提交记录
export function getMySubmissions(studentId) {
  return request.get(`/app/student/submit/list/${studentId}`)
}

// ========== 设置与画像 ==========
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

// 短信验证码
export function sendSmsCode(phone) {
  return request.post('/app/sms/send', { phone })
}

// 修改密码
export function updatePassword(studentId, data) {
  return request.put(`/app/student/${studentId}/password`, data)
}

export function getPortrait(studentId) {
  return request.get(`/app/student/${studentId}/portrait`)
}

export function getAIAnalysis(studentId) {
  return request.get(`/app/student/${studentId}/ai-analysis`)
}

// ========== 简历生成 ==========
export function getResumeTemplates() {
  return request.get('/app/resume/templates')
}

export function getResumePreview(studentId, template) {
  return request.get(`/app/resume/${studentId}/preview`, { params: { template } })
}

export function getResumeDownloadUrl(studentId, template) {
  return `/app/resume/${studentId}/download?template=${template}`
}