import request from './request'

// 管理端
export function getAnnouncementPage(params) {
  return request.get('/admin/announcement/page', { params })
}

export function getAnnouncementById(id) {
  return request.get(`/admin/announcement/${id}`)
}

export function createAnnouncement(data) {
  return request.post('/admin/announcement', data)
}

export function updateAnnouncement(id, data) {
  return request.put(`/admin/announcement/${id}`, data)
}

export function deleteAnnouncement(id) {
  return request.delete(`/admin/announcement/${id}`)
}

export function publishAnnouncement(id) {
  return request.put(`/admin/announcement/${id}/publish`)
}

export function closeAnnouncement(id) {
  return request.put(`/admin/announcement/${id}/close`)
}

// 学生端
export function getActiveAnnouncements() {
  return request.get('/app/announcement/active')
}
