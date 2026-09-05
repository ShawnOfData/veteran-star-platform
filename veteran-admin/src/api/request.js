import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 是否为开发环境（开发环境打印详细错误）
const isDev = import.meta.env.DEV

// 错误去重：相同错误 2 秒内不重复弹出
let lastErrorMessage = ''
let lastErrorTime = 0
function showError(message, type = 'error') {
  const now = Date.now()
  if (message === lastErrorMessage && now - lastErrorTime < 2000) {
    return
  }
  lastErrorMessage = message
  lastErrorTime = now
  if (type === 'warning') {
    ElMessage.warning(message)
  } else {
    ElMessage.error(message)
  }
}

// 提取后端返回的业务消息（兼容 Result 包装 和 裸字符串）
function extractBusinessMessage(data) {
  if (!data) return ''
  if (typeof data === 'string') return data
  if (data.message) return data.message
  if (data.error) return data.error
  return ''
}

// 公开接口：无需携带 token（携带本地旧 token 可能被后端误判为已被顶替 → 401）
const PUBLIC_PATHS = [
  '/admin/login',
  '/app/student/login',
  '/app/student/register',
  '/app/sms/'
]

request.interceptors.request.use(config => {
  const isPublic = PUBLIC_PATHS.some(p => config.url && config.url.includes(p))
  if (!isPublic) {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    // 非 JSON 响应（如文件流）直接返回
    if (res instanceof Blob || typeof res !== 'object') {
      return response
    }
    if (res.code !== 200) {
      const msg = res.message || '请求失败'
      // 业务层 4xx → warning, 5xx → error
      if (res.code >= 500) {
        ElMessage.error(msg)
      } else {
        ElMessage.warning(msg)
      }
      return Promise.reject(new Error(msg))
    }
    return res
  },
  error => {
    // axios 取消请求，不打扰用户
    if (axios.isCancel(error) || error.name === 'CanceledError') {
      return Promise.reject(error)
    }

    if (isDev) {
      console.error('[Request Error]', error)
    }

    // ===== 1. 无响应：网络断开 / 超时 / CORS =====
    if (!error.response) {
      if (error.code === 'ECONNABORTED' || /timeout/i.test(error.message || '')) {
        ElMessage.error('请求超时，请检查网络后重试')
      } else if (error.message === 'Network Error') {
        ElMessage.error('网络连接异常，请检查网络后重试')
      } else {
        ElMessage.error('网络异常，请稍后重试')
      }
      return Promise.reject(error)
    }

    // ===== 2. 有响应：按 HTTP 状态码细分 =====
    const status = error.response.status
    const data = error.response.data
    const bizMsg = extractBusinessMessage(data)

    switch (status) {
      case 400:
        showError(bizMsg || '请求参数有误', 'warning')
        break
      case 401: {
        // token 失效或未登录
        localStorage.clear()
        const current = router.currentRoute.value
        if (current.name !== 'Login' && current.name !== 'StudentLogin') {
          router.push('/login')
        }
        showError(bizMsg || '登录已过期，请重新登录', 'error')
        break
      }
      case 403:
        showError(bizMsg || '权限不足，无法执行此操作', 'error')
        break
      case 404:
        showError(bizMsg || '请求的资源不存在', 'warning')
        break
      case 405:
        showError('请求方法不允许', 'warning')
        break
      case 408:
        showError('请求超时，请稍后重试', 'error')
        break
      case 409:
        showError(bizMsg || '操作冲突，请刷新后重试', 'warning')
        break
      case 413:
        showError('上传文件过大，请压缩后重试', 'error')
        break
      case 415:
        showError(bizMsg || '请求格式不支持', 'warning')
        break
      case 422:
        showError(bizMsg || '参数校验失败', 'warning')
        break
      case 429:
        showError(bizMsg || '操作过于频繁，请稍后再试', 'warning')
        break
      case 500:
        showError(bizMsg || '服务器开小差了，请稍后重试', 'error')
        break
      case 502:
      case 503:
      case 504:
        showError('服务暂时不可用，请稍后重试', 'error')
        break
      default:
        if (status >= 500) {
          showError(bizMsg || '服务器异常，请稍后重试', 'error')
        } else {
          showError(bizMsg || '请求失败，请稍后重试', 'warning')
        }
    }

    return Promise.reject(error)
  }
)

export default request