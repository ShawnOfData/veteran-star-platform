import { api } from '../../utils/api'

Page({
  data: {
    activeTab: 'login',
    loading: false,
    studentNo: '',
    phone: '',
    grades: ['2020级', '2021级', '2022级', '2023级', '2024级', '2025级'],
    gradeIndex: -1,
    registerData: {
      studentNo: '',
      name: '',
      phone: '',
      password: '',
      college: '',
      major: '',
      grade: ''
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [field]: e.detail.value })
  },

  onRegisterInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [`registerData.${field}`]: e.detail.value })
  },

  onGradeChange(e) {
    const idx = e.detail.value
    this.setData({
      gradeIndex: idx,
      'registerData.grade': this.data.grades[idx]
    })
  },

  async handleLogin() {
    const { studentNo, phone } = this.data
    if (!studentNo || !phone) {
      wx.showToast({ title: '请填写学号和手机号', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const res = await api.studentLogin({ studentNo, phone })
      wx.setStorageSync('token', res.token)
      wx.setStorageSync('studentInfo', { id: res.id, studentNo: res.studentNo, name: res.name })
      wx.switchTab({ url: '/pages/home/home' })
    } catch {
      // error handled in api.js
    } finally {
      this.setData({ loading: false })
    }
  },

  async handleRegister() {
    const rd = this.data.registerData
    if (!rd.studentNo || !rd.name || !rd.phone || !rd.password || !rd.college || !rd.major || !rd.grade) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    if (rd.password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' })
      return
    }
    if (!/^1\d{10}$/.test(rd.phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      await api.studentRegister(rd)
      wx.showToast({ title: '注册成功，请登录', icon: 'success' })
      this.setData({
        activeTab: 'login',
        studentNo: rd.studentNo,
        phone: rd.phone
      })
    } catch {
      // error handled in api.js
    } finally {
      this.setData({ loading: false })
    }
  },

  onLoad() {
    const token = wx.getStorageSync('token')
    if (token) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  }
})
