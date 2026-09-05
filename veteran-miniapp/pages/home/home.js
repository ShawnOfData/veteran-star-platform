import { api } from '../../utils/api'

Page({
  data: {
    announcements: [],
    loaded: false,
    stats: {
      totalPoints: 0,
      serviceCount: 0,
      honorCount: 0,
      ranking: '-'
    },
    recentOpportunities: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    if (!this.data.loaded) {
      this.loadData()
    }
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const [announcements, oppRes] = await Promise.all([
        api.getAnnouncements().catch(() => []),
        api.getOpportunityList({ page: 1, size: 5 }).catch(() => ({}))
      ])
      const opportunities = oppRes?.records || []
      this.setData({
        announcements: (announcements || []).slice(0, 5),
        recentOpportunities: opportunities.map(o => ({
          ...o,
          statusText: o.status === 1 ? '进行中' : o.status === 4 ? '已结束' : '待审核'
        }))
      })
      this.setData({ loaded: true })
      this.loadStats()
    } catch {
      // handled
    } finally {
      wx.hideLoading()
    }
  },

  async loadStats() {
    try {
      const student = wx.getStorageSync('studentInfo')
      if (student?.id) {
        const points = await api.getMyPoints()
        const ranking = await api.getRanking({ page: 1, size: 1 }).catch(() => [])
        const rank = ranking.find(r => r.id === student.id)
        this.setData({
          'stats.totalPoints': points?.total || 0,
          'stats.serviceCount': points?.serviceCount || 0,
          'stats.honorCount': points?.honorCount || 0,
          'stats.ranking': rank?.rank || '-'
        })
      }
    } catch {
      // handled
    }
  },

  goToPoints() { wx.switchTab({ url: '/pages/profile/profile' }) },
  goToOpportunities() { wx.switchTab({ url: '/pages/opportunities/opportunities' }) },
  goToRanking() { wx.switchTab({ url: '/pages/ranking/ranking' }) },
  goToProfile() { wx.switchTab({ url: '/pages/profile/profile' }) },
  goToOpportunityDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/opportunities/opportunities?id=${id}` })
  }
})
