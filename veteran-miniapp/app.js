const { baseUrl, ENV } = require('./config/env')

App({
  globalData: {
    baseUrl,
    env: ENV,
    token: '',
    studentInfo: null
  },

  onLaunch() {
    // 环境提示：发布生产版前请确认 config/env.js 中 ENV = 'prod'
    if (ENV === 'prod') {
      console.log('[戎归星辉] 运行环境：production')
    } else {
      console.log('[戎归星辉] 运行环境：development')
    }

    const token = wx.getStorageSync('token')
    const studentInfo = wx.getStorageSync('studentInfo')
    if (token) {
      this.globalData.token = token
      this.globalData.studentInfo = studentInfo
    }
  }
})
