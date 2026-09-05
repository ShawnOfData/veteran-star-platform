import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useStudentStore = defineStore('student', () => {
  const studentId = ref(localStorage.getItem('studentId') || '')
  const studentNo = ref(localStorage.getItem('studentNo') || '')
  const studentName = ref(localStorage.getItem('studentName') || '')
  const studentPhone = ref(localStorage.getItem('studentPhone') || '')
  const token = ref(localStorage.getItem('token') || '')

  // 有 token 才算真正登录（旧逻辑仅看 studentId，会导致无 token 的“假登录”态）
  const isLoggedIn = computed(() => !!token.value && !!studentId.value)

  /**
   * 登录成功后保存状态
   * @param {Object} info - { id, studentNo, name, phone, token }
   */
  function login(info) {
    studentId.value = info.id
    studentNo.value = info.studentNo
    studentName.value = info.name
    studentPhone.value = info.phone
    token.value = info.token || ''
    localStorage.setItem('studentId', info.id)
    localStorage.setItem('studentNo', info.studentNo)
    localStorage.setItem('studentName', info.name)
    localStorage.setItem('studentPhone', info.phone)
    if (info.token) {
      localStorage.setItem('token', info.token)
    }
  }

  function register(info) {
    studentId.value = info.id
    studentNo.value = info.studentNo
    studentName.value = info.name
    studentPhone.value = info.phone
    localStorage.setItem('studentId', info.id)
    localStorage.setItem('studentNo', info.studentNo)
    localStorage.setItem('studentName', info.name)
    localStorage.setItem('studentPhone', info.phone)
  }

  function logout() {
    studentId.value = ''
    studentNo.value = ''
    studentName.value = ''
    studentPhone.value = ''
    token.value = ''
    localStorage.removeItem('studentId')
    localStorage.removeItem('studentNo')
    localStorage.removeItem('studentName')
    localStorage.removeItem('studentPhone')
    localStorage.removeItem('token')
  }

  return { studentId, studentNo, studentName, studentPhone, token, isLoggedIn, login, register, logout }
})
