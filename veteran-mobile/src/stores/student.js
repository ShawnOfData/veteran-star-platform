/**
 * 学生状态管理
 * 使用 'mobile_' 前缀避免与桌面端 Token 冲突
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useStudentStore = defineStore('student', () => {
  const studentId = ref(localStorage.getItem('mobile_student_id') || '')
  const studentNo = ref(localStorage.getItem('mobile_student_no') || '')
  const studentName = ref(localStorage.getItem('mobile_student_name') || '')
  const studentPhone = ref(localStorage.getItem('mobile_student_phone') || '')
  const token = ref(localStorage.getItem('mobile_token') || '')

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
    token.value = info.token
    localStorage.setItem('mobile_student_id', info.id)
    localStorage.setItem('mobile_student_no', info.studentNo)
    localStorage.setItem('mobile_student_name', info.name)
    localStorage.setItem('mobile_student_phone', info.phone)
    localStorage.setItem('mobile_token', info.token)
  }

  function logout() {
    studentId.value = ''
    studentNo.value = ''
    studentName.value = ''
    studentPhone.value = ''
    token.value = ''
    localStorage.removeItem('mobile_student_id')
    localStorage.removeItem('mobile_student_no')
    localStorage.removeItem('mobile_student_name')
    localStorage.removeItem('mobile_student_phone')
    localStorage.removeItem('mobile_token')
  }

  return { studentId, studentNo, studentName, studentPhone, token, isLoggedIn, login, logout }
})
