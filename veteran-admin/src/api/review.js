import request from './request'

// 查询待审项
export function getPendingReviews(params) {
  return request.get('/admin/review/pending', { params })
}

// 审核服役经历
export function reviewServiceExperience(data) {
  return request.put('/admin/review/service-experience', data)
}

// 审核荣誉
export function reviewHonor(data) {
  return request.put('/admin/review/honor', data)
}

// 审核技能证书
export function reviewSkill(data) {
  return request.put('/admin/review/skill', data)
}
