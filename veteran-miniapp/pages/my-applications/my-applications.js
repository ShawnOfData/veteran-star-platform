import { api } from '../../utils/api'
import { formatDateTime } from '../../utils/util'

Page({
  data: {
    list: [],
    loading: true
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    if (!this.data.list.length) this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const res = await api.getMyApplications() || {}
      const list = res?.records || []
      this.setData({
        list: list.map(a => ({
          ...a,
          applyTime: formatDateTime(a.applyTime),
          applyStatusText: a.applyStatus === 0 ? '待审核' : a.applyStatus === 1 ? '已通过' : '已取消'
        })),
        loading: false
      })
    } catch {
      this.setData({ loading: false })
    }
  }
})
