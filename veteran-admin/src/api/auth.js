import request from './request'

export function login(data) {
  return request.post('/admin/login', data)
}

export function getAdminList() {
  return request.get('/admin/admin-account/list')
}

export function createAdmin(data) {
  return request.post('/admin/admin-account', data)
}

export function updateAdmin(id, data) {
  return request.put(`/admin/admin-account/${id}`, data)
}

export function deleteAdmin(id) {
  return request.delete(`/admin/admin-account/${id}`)
}