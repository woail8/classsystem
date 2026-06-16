<script setup lang="ts">
import { Bell, BookOpen, LogOut, UserRound } from 'lucide-vue-next'
import { ElNotification } from 'element-plus'
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAccessEntryUrl, getApiBaseUrl } from '@/config/runtime'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { wsClient } from '@/utils/ws'
import type { NotificationItem, WsMessage } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const accessEntry = computed(() => getAccessEntryUrl())
const apiBaseUrl = computed(() => getApiBaseUrl())
const navItems = [
  { label: '课程', path: '/courses' },
  { label: '通知', path: '/notifications' },
  { label: '个人中心', path: '/profile' },
]

function isActive(path: string): boolean {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function go(path: string): void {
  router.push(path)
}

function logout(): void {
  authStore.logout()
  router.push('/login')
}

function handleNotification(message: WsMessage<NotificationItem>): void {
  notificationStore.pushNotification(message.data)
  ElNotification({
    title: message.data.title || '新通知',
    message: message.data.content || message.message || '收到一条新消息',
    type: 'info',
  })
}

function handleSignInTask(message: WsMessage<Record<string, unknown>>): void {
  ElNotification({
    title: '新的签到任务',
    message: String(message.message || '教师发布了新的签到任务'),
    type: 'success',
  })
}

onMounted(() => {
  notificationStore.fetchNotifications().catch(() => {})
  wsClient.on('NEW_NOTIFICATION', handleNotification)
  wsClient.on('NEW_SIGNIN_TASK', handleSignInTask)
})

onBeforeUnmount(() => {
  wsClient.off('NEW_NOTIFICATION', handleNotification)
  wsClient.off('NEW_SIGNIN_TASK', handleSignInTask)
})
</script>

<template>
  <div class="app-shell">
    <header class="app-shell__header">
      <div class="app-shell__brand" @click="go('/courses')">
        <div class="app-shell__logo">
          <BookOpen :size="18" />
        </div>
        <div>
          <h1>校园在线学习平台</h1>
          <p>访问入口：{{ accessEntry || '未配置' }}</p>
          <p>API 地址：{{ apiBaseUrl || '未配置' }}</p>
        </div>
      </div>

      <nav class="app-shell__nav">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="app-shell__nav-btn"
          :class="{ 'is-active': isActive(item.path) }"
          @click="go(item.path)"
        >
          <Bell v-if="item.path === '/notifications'" :size="16" />
          <UserRound v-else-if="item.path === '/profile'" :size="16" />
          <BookOpen v-else :size="16" />
          <span>{{ item.label }}</span>
          <em v-if="item.path === '/notifications' && notificationStore.unreadCount > 0">
            {{ notificationStore.unreadCount }}
          </em>
        </button>
      </nav>

      <div class="app-shell__user">
        <div>
          <strong>{{ authStore.displayName }}</strong>
          <span>{{ authStore.user?.role === 'teacher' ? '教师' : '学生' }}</span>
        </div>
        <button class="app-shell__logout" @click="logout">
          <LogOut :size="16" />
          <span>退出</span>
        </button>
      </div>
    </header>

    <main class="app-shell__main">
      <router-view />
    </main>
  </div>
</template>
