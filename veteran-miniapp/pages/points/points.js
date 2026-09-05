import { api } from '../../utils/api'
import { formatDateTime } from '../../utils/util'

Page({
  data: {
    totalPoints: 0,
    serviceCount: 0,
    honorCount: 0,
    typeFilters: ['全部', '荣誉', '服务', '技能', '学业'],
    typeIndex: 0,
    details: [],
    loading: true,
    page: 1,
    hasMore: true
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true, page: 1, hasMore: true })
    try {
      const points = await api.getMyPoints()
      this.setData({
        totalPoints: points?.total || 0,
        serviceCount: points?.serviceCount || 0,
        honorCount: points?.honorCount || 0
      })
      await this.loadDetails(true)
    } catch {
      // handled
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadDetails(reset = false) {
    const typeMap = ['', 'honor', 'service', 'skill', 'academic']
    const type = typeMap[this.data.typeIndex]
    const params = { page: this.data.page, size: 20 }
    if (type) params.reasonType = type

    const list = await api.getPointsDetail(params)
    const formatted = (list || []).map(d => ({
      ...d,
      createTime: formatDateTime(d.createTime)
    }))

    this.setData({
      details: reset ? formatted : [...this.data.details, ...formatted],
      hasMore: (list || []).length >= 20
    })
  },

  onTypeChange(e) {
    this.setData({ typeIndex: e.detail.value }, () => this.loadData())
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ page: this.data.page + 1 }, () => this.loadDetails())
  }
})
