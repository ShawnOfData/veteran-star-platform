import request from './request'

export function getStudentPage(params) {
  return request.get('/admin/student/page', { params })
}

export function getStudentById(id) {
  return request.get(`/admin/student/${id}`)
}

export function createStudent(data) {
  return request.post('/admin/student', data)
}

export function updateStudent(id, data) {
  return request.put(`/admin/student/${id}`, data)
}

export function deleteStudent(id) {
  return request.delete(`/admin/student/${id}`)
}

export function getServiceExperiences(studentId) {
  return request.get(`/admin/service-experience/list/${studentId}`)
}

export function createServiceExperience(data) {
  return request.post('/admin/service-experience', data)
}

export function deleteServiceExperience(id) {
  return request.delete(`/admin/service-experience/${id}`)
}

export function getHonors(serviceExperienceId) {
  return request.get(`/admin/service-experience/honor/list/${serviceExperienceId}`)
}

export function addHonor(data) {
  return request.post('/admin/service-experience/honor', data)
}

export function removeHonor(id) {
  return request.delete(`/admin/service-experience/honor/${id}`)
}

export function getSkills(studentId) {
  return request.get(`/admin/social/skill/list/${studentId}`)
}

export function addSkill(data) {
  return request.post('/admin/social/skill', data)
}

export function removeSkill(id) {
  return request.delete(`/admin/social/skill/${id}`)
}