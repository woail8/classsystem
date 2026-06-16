<script setup lang="ts">
import { onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()

onMounted(() => {
  notificationStore.fetchNotifications().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>通知中心</h2>
        <p>这里会展示服务端推送和历史通知，实时通知会自动插入到列表顶部。</p>
      </div>
      <el-button @click="notificationStore.markAllRead()">全部已读</el-button>
    </header>

    <el-skeleton :loading="notificationStore.loading" animated :rows="6">
      <template #default>
        <el-empty v-if="notificationStore.list.length === 0" description="暂无通知" />

        <el-timeline v-else>
          <el-timeline-item
            v-for="(item, index) in notificationStore.list"
            :key="item.id || index"
            :timestamp="item.createTime || '刚刚'"
          >
            <el-card class="page-card" shadow="never">
              <div class="notice-card">
                <div>
                  <h3>{{ item.title || '新通知' }}</h3>
                  <p>{{ item.content || '暂无详细内容' }}</p>
                </div>
                <el-tag v-if="!item.read" type="danger">未读</el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.notice-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.notice-card h3 {
  margin: 0 0 8px;
}

.notice-card p {
  margin: 0;
  color: var(--color-text-secondary);
}
</style>
