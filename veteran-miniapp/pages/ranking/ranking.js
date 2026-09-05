import { api } from '../../utils/api'

Page({
  data: {
    top3: [],
    ranking: [],
    myRank: null,
    loading: true
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    if (!this.data.ranking.length) this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const list = await api.getRanking({ page: 1, size: 50 }) || []
      const withInit = list.map((r, i) => ({
        ...r,
        rank: i + 1,
        initial: (r.name && r.name.length > 0) ? r.name[0] : '?'
      }))
      this.setData({
        top3: withInit.slice(0, 3),
        ranking: withInit.slice(3),
        loading: false
      })

      const student = wx.getStorageSync('studentInfo')
      if (student && student.id) {
        const me = withInit.find(r => r.id === student.id)
        if (me) this.setData({ myRank: me })
      }
    } catch {
      this.setData({ loading: false })
    }
  }
})
