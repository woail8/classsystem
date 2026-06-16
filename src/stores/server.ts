import { defineStore } from 'pinia'
import { clearServerHost, getServerHost, setServerHost } from '@/config/runtime'

export const useServerStore = defineStore('server', {
  state: () => ({
    serverHost: getServerHost(),
  }),
  getters: {
    hasServerHost: (state) => Boolean(state.serverHost),
  },
  actions: {
    updateServerHost(host: string) {
      this.serverHost = setServerHost(host)
    },
    resetServerHost() {
      clearServerHost()
      this.serverHost = ''
    },
  },
})
