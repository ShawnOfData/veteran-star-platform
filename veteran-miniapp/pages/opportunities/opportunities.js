import { api } from '../../utils/api'
import { formatDate } from '../../utils/util'

Page({
  data: {
    typeFilter: '',
    list: [],
    loading: true,
    page: 1,
    hasMore: true
  },

  onLoad(options) {
    if (options.id) {
      this.goToDetailDirect(options.id)
      return
    }
    this.loadData()
  },

  onShow() {
    if (!this.data.list.length && !this.data.loading) {
      this.loadData()
    }
  },

  async loadData(reset = true) {
    if (reset) {
      this.setData({ page: 1, hasMore: true })
    }
    this.setData({ loading: true })

    try {
      const params = { page: this.data.page, size: 20 }
      if (this.data.typeFilter) params.type = this.data.typeFilter

      const res = await api.getOpportunityList(params)
      const list = res?.records || []
      const formatted = list.map(o => ({
        ...o,
        endTime: formatDate(o.endTime),
        statusText: o.status === 1 ? '进行中' : o.status === 4 ? '已结束' : '待审核'
      }))

      this.setData({
        list: reset ? formatted : [...this.data.list, ...formatted],
        hasMore: list.length >= 20
      })
    } catch {
      // handled
    } finally {
      this.setData({ loading: false })
    }
  },

  onFilterType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ typeFilter: type }, () => this.loadData(true))
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/opportunities/opportunity-detail/opportunity-detail?id=${id}` })
  },

  goToDetailDirect(id) {
    wx.navigateTo({ url: `/pages/opportunities/opportunity-detail/opportunity-detail?id=${id}` })
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ page: this.data.page + 1 }, () => this.loadData(false))
  }
})
