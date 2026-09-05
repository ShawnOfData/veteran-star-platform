import request from './request'

export function getDictBranch() {
  return request.get('/admin/dict/branch')
}

export function getDictHonor() {
  return request.get('/admin/dict/honor')
}

export function getDictCert() {
  return request.get('/admin/dict/cert')
}

export function getDictHonorCategory() {
  return request.get('/admin/dict/honor-category')
}

export function getDictLeaderPost() {
  return request.get('/admin/dict/leader-post')
}

export function getDictPostType() {
  return request.get('/admin/dict/post-type')
}

export function getDictJobType() {
  return request.get('/admin/dict/job-type')
}

export function refreshDictCache() {
  return request.post('/admin/dict/refresh')
}

export function addDictItem(dictType, data) {
  return request.post(`/admin/dict/${dictType}`, data)
}

export function updateDictItem(dictType, id, data) {
  return request.put(`/admin/dict/${dictType}/${id}`, data)
}

export function deleteDictItem(dictType, id) {
  return request.delete(`/admin/dict/${dictType}/${id}`)
}
