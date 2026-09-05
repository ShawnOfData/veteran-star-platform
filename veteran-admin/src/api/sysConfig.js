import request from './request'

export function getSysConfig() {
  return request.get('/admin/sys-config')
}

export function updateSysConfig(data) {
  return request.put('/admin/sys-config', data)
}
