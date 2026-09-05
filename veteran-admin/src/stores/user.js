import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || 'admin')

  const isLoggedIn = computed(() => !!token.value)

  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    username.value = res.data.username
    role.value = res.data.role || 'admin'
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role || 'admin')
  }

  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.clear()
  }

  return { token, username, role, isLoggedIn, login, logout }
})