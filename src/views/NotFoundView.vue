<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="page-card not-found__card">
      <p class="not-found__code">404</p>
      <h1>页面不存在</h1>
      <p>当前访问的地址不存在，请返回课程列表继续使用系统。</p>
      <el-button type="primary" @click="router.push('/courses')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.not-found__card {
  max-width: 420px;
  padding: 32px;
  text-align: center;
}

.not-found__code {
  margin: 0;
  color: var(--color-primary);
  font-size: 56px;
  font-weight: 700;
}

.not-found__card h1 {
  margin: 12px 0;
}

.not-found__card p:last-of-type {
  margin: 0 0 20px;
  color: var(--color-text-secondary);
}
</style>
