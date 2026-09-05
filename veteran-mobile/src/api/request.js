/**
 * Axios 请求实例 + 拦截器
 * 统一 baseURL '/api'，自动携带 JWT Token，统一错误处理
 */
import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 错误去重：相同错误 2 秒内不重复弹出
let lastError = { message: '', time: 0 }

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('mobile_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码和 HTTP 错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务成功
    if (res.code === 200 || res.code === undefined) {
      return res
    }
    // 业务失败
    showError(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401:
          // Token 过期 / 被其他设备顶替 / 未登录
          localStorage.removeItem('mobile_token')
          localStorage.removeItem('mobile_student_id')
          localStorage.removeItem('mobile_student_no')
          localStorage.removeItem('mobile_student_name')
          localStorage.removeItem('mobile_student_phone')
          showError(response.data?.message || '登录已过期，请重新登录')
          router.push('/login')
          break
        case 403:
          showError('没有操作权限')
          break
        case 404:
          showError('请求的资源不存在')
          break
        case 429:
          showError('请求过于频繁，请稍后再试')
          break
        case 500:
          showError('服务器内部错误')
          break
        default:
          showError(response.data?.message || `请求失败(${response.status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      showError('请求超时，请检查网络')
    } else {
      showError('网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

/**
 * 错误提示（2秒去重）
 * @param {string} message - 错误消息
 */
function showError(message) {
  const now = Date.now()
  if (message === lastError.message && now - lastError.time < 2000) return
  lastError = { message, time: now }
  showToast(message)
}

export default request
