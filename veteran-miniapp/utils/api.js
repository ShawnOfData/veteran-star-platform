const app = getApp()

// 401 防重入标志：避免并发请求同时触发登出导致重复弹窗/跳转
let isLoggingOut = false

function request(url, data = {}, method = 'GET') {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: app.globalData.baseUrl + url,
      data,
      method,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      success(res) {
        // 401：登录态失效（账号在其他设备登录 / token 过期）→ 自动登出
        if (res.statusCode === 401) {
          if (!isLoggingOut) {
            isLoggingOut = true
            wx.removeStorageSync('token')
            wx.removeStorageSync('studentInfo')
            wx.showToast({ title: (res.data && res.data.message) || '登录已过期，请重新登录', icon: 'none' })
            setTimeout(() => {
              isLoggingOut = false
              wx.reLaunch({ url: '/pages/login/login' })
            }, 1500)
          }
          reject(res.data)
          return
        }
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const api = {
  // 学生
  studentLogin: (data) => request('/app/student/login', data, 'POST'),
  studentRegister: (data) => request('/app/student/register', data, 'POST'),
  // 详情：GET /app/student/{id}
  getStudentInfo: (id) => request('/app/student/' + id),
  // 更新：PUT /app/student/{id}，body 为 Student
  updateStudent: (id, data) => request('/app/student/' + id, data, 'PUT'),
  // 积分总览
  getMyPoints: () => {
    const student = wx.getStorageSync('studentInfo')
    return request('/app/student/points?studentId=' + (student?.id || ''))
  },
  // 积分明细
  getPointsDetail: (data) => {
    const student = wx.getStorageSync('studentInfo')
    return request('/app/student/points/detail', { ...data, studentId: student?.id }, 'POST')
  },

  // 公告
  getAnnouncements: () => request('/app/announcement/active'),

  // 机会
  getOpportunityList: (data) => request('/app/opportunity/list', data),
  // 详情：GET /app/opportunity/{id}
  getOpportunityDetail: (id) => request('/app/opportunity/' + id),
  // 报名：POST /app/opportunity/apply，body = { opportunityId, studentId, remark }
  applyOpportunity: (data) => request('/app/opportunity/apply', data, 'POST'),
  // 取消报名：PUT /app/opportunity/cancel/{applicationId}?studentId=xxx
  cancelApply: (applicationId, studentId) =>
    request('/app/opportunity/cancel/' + applicationId + '?studentId=' + studentId, {}, 'PUT'),
  // 我的报名列表
  getMyApplications: () => {
    const student = wx.getStorageSync('studentInfo')
    return request('/app/opportunity/my-applications?studentId=' + (student?.id || ''))
  },

  // 排行
  getRanking: (data) => request('/app/ranking/top', data),

  // 字典
  getDictBranch: () => request('/app/dict/branch'),
  getDictHonor: () => request('/app/dict/honor'),
  getDictCert: () => request('/app/dict/cert')
}
