import request from './request'

export function getOpportunityPage(params) {
  return request.get('/admin/opportunity/page', { params })
}

export function getOpportunityById(id) {
  return request.get(`/admin/opportunity/${id}`)
}

export function createOpportunity(data) {
  return request.post('/admin/opportunity', data)
}

export function updateOpportunity(id, data) {
  return request.put(`/admin/opportunity/${id}`, data)
}

export function deleteOpportunity(id) {
  return request.delete(`/admin/opportunity/${id}`)
}

export function publishOpportunity(id) {
  return request.put(`/admin/opportunity/${id}/publish`)
}

export function closeOpportunity(id) {
  return request.put(`/admin/opportunity/${id}/close`)
}

// 报名管理
export function getApplicationPage(params) {
  return request.get('/admin/opportunity/applications/page', { params })
}

export function reviewApplication(id, status) {
  return request.put(`/admin/opportunity/applications/${id}/review`, null, { params: { status } })
}