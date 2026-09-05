import request from './request'

export function getPointsDetail(studentId, params) {
  return request.get(`/admin/social/points/${studentId}`, { params })
}

export function getServiceRecords(params) {
  return request.get('/admin/social/record/page', { params })
}

export function createServiceRecord(data) {
  return request.post('/admin/social/record', data)
}

export function reviewServiceRecord(data) {
  return request.put('/admin/social/record/review', data)
}

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}