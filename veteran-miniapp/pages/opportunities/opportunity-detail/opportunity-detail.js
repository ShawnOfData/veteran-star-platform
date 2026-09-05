import { api } from '../../../utils/api'
import { formatDate } from '../../../utils/util'

Page({
  data: {
    id: null,
    detail: null,
    statusText: '',
    loading: true,
    applying: false,
    applied: false
  },

  onLoad(options) {
    if (!options.id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ id: options.id })
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const res = await api.getOpportunityDetail(this.data.id)
      const detail = res || {}
      this.setData({
        detail: { ...detail, endTime: formatDate(detail.endTime) },
        statusText: detail.status === 1 ? '进行中'
          : detail.status === 4 ? '已结束' : '待审核',
        loading: false
      })
      this.checkApplied()
    } catch {
      this.setData({ loading: false })
    }
  },

  // 简单判断是否已报名（通过我的报名列表匹配）
  async checkApplied() {
    try {
      const res = await api.getMyApplications() || {}
      const list = res?.records || []
      const applied = list.some(a => String(a.opportunityId) === String(this.data.id) && a.applyStatus !== 2)
      this.setData({ applied })
    } catch {
      // 忽略
    }
  },

  async handleApply() {
    const student = wx.getStorageSync('studentInfo') || {}
    if (!student.id) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再报名',
        showCancel: false,
        success: () => wx.navigateTo({ url: '/pages/login/login' })
      })
      return
    }
    if (this.data.detail.status !== 1) {
      wx.showToast({ title: '该机会当前不可报名', icon: 'none' })
      return
    }
    if (this.data.applied) {
      wx.showToast({ title: '您已报名，请勿重复', icon: 'none' })
      return
    }

    this.setData({ applying: true })
    try {
      await api.applyOpportunity({
        opportunityId: Number(this.data.id),
        studentId: student.id,
        remark: ''
      })
      wx.showToast({ title: '报名成功', icon: 'success' })
      this.setData({ applied: true })
      // 刷新报名人数
      this.loadDetail()
    } catch {
      // handled in api.js
    } finally {
      this.setData({ applying: false })
    }
  }
})
