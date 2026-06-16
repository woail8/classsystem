import { defineStore } from 'pinia'
import { getNotificationsApi } from '@/api'
import type { NotificationItem } from '@/types'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    list: [] as NotificationItem[],
    unreadCount: 0,
    loading: false,
  }),
  actions: {
    async fetchNotifications() {
      this.loading = true
      try {
        const data = await getNotificationsApi()
        this.list = data
        this.unreadCount = data.filter((item) => !item.read).length
      } finally {
        this.loading = false
      }
    },
    pushNotification(item: NotificationItem) {
      this.list.unshift(item)
      this.unreadCount += 1
    },
    markAllRead() {
      this.list = this.list.map((item) => ({ ...item, read: true }))
      this.unreadCount = 0
    },
  },
})
