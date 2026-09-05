import { api } from '../../utils/api'

Page({
  data: {
    student: {},
    studentInitial: '?',
    points: { total: 0 }
  },

  onShow() {
    const student = wx.getStorageSync('studentInfo') || {}
    const studentInitial = (student.name && student.name.length > 0) ? student.name[0] : '?'
    this.setData({ student, studentInitial })
    this.loadPoints()
  },

  async loadPoints() {
    try {
      const points = await api.getMyPoints()
      this.setData({ points: points || { total: 0 } })
    } catch {
      // handled
    }
  },

  goToPoints() {
    wx.navigateTo({ url: '/pages/points/points' })
  },

  goToMyApplications() {
    wx.navigateTo({ url: '/pages/my-applications/my-applications' })
  },

  goToRanking() {
    wx.switchTab({ url: '/pages/ranking/ranking' })
  },

  editProfile() {
    wx.showToast({ title: '编辑功能开发中', icon: 'none' })
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorageSync()
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})
