<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { getAccessEntryUrl, getApiBaseUrl } from '@/config/runtime'

const authStore = useAuthStore()
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>个人中心</h2>
        <p>展示当前登录用户信息与客户端连接状态。</p>
      </div>
    </header>

    <el-descriptions class="page-card profile-card" :column="1" border>
      <el-descriptions-item label="姓名">{{ authStore.user?.realName || '--' }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ authStore.user?.username || '--' }}</el-descriptions-item>
      <el-descriptions-item label="角色">
        {{ authStore.user?.role === 'teacher' ? '教师' : '学生' }}
      </el-descriptions-item>
      <el-descriptions-item label="当前访问入口">{{ getAccessEntryUrl() || '--' }}</el-descriptions-item>
      <el-descriptions-item label="API 地址">{{ getApiBaseUrl() || '--' }}</el-descriptions-item>
      <el-descriptions-item label="登录状态">{{ authStore.isLogin ? '已登录' : '未登录' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<style scoped>
.profile-card {
  padding: 12px;
}
</style>
