import { defineStore } from 'pinia'
import { loginApi } from '@/api'
import type { LoginResponseData, UserInfo } from '@/types'
import { wsClient } from '@/utils/ws'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('campus_token') || '',
    user: JSON.parse(localStorage.getItem('campus_user') || 'null') as UserInfo | null,
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    displayName: (state) => state.user?.realName || state.user?.username || '未登录',
  },
  actions: {
    async login(form: { username: string; password: string }) {
      const data: LoginResponseData = await loginApi(form)
      this.token = data.token
      this.user = data.user

      localStorage.setItem('campus_token', data.token)
      localStorage.setItem('campus_user', JSON.stringify(data.user))
      wsClient.connect(data.token)
    },
    restoreSession() {
      if (this.token) {
        wsClient.connect(this.token)
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_user')
      wsClient.close()
    },
  },
})
