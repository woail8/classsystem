<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getServerHost } from '@/config/runtime'
import { useAuthStore } from '@/stores/auth'
import { useServerStore } from '@/stores/server'

const router = useRouter()
const authStore = useAuthStore()
const serverStore = useServerStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const currentServerHost = computed(() => getServerHost())

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin(): Promise<void> {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true
    serverStore.updateServerHost(currentServerHost.value)
    await authStore.login({
      username: form.username,
      password: form.password,
    })
    ElMessage.success('登录成功')
    router.push('/courses')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="login-hero__content">
        <p class="login-hero__tag">LAN Learning Client</p>
        <h1>校园在线学习平台</h1>
        <p class="login-hero__desc">
          用户机只需访问服务机提供的局域网链接，即可直接使用课程、签到、作业、考试和通知等核心教学功能。
        </p>
      </div>
    </section>

    <section class="login-panel page-card">
      <div class="login-panel__header">
        <h2>登录客户端</h2>
        <p>用户机通过访问服务机局域网地址即可进入系统，无需手动填写服务器地址。</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button class="login-panel__submit" type="primary" :loading="loading" @click="handleLogin">
          进入平台
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background:
    radial-gradient(circle at top left, rgba(29, 78, 216, 0.14), transparent 36%),
    radial-gradient(circle at bottom right, rgba(16, 185, 129, 0.14), transparent 30%),
    linear-gradient(135deg, #eef4ff, #f7f9fc 42%, #ecfffb);
}

.login-hero,
.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-hero__content {
  max-width: 520px;
}

.login-hero__tag {
  display: inline-block;
  margin: 0 0 18px;
  padding: 6px 12px;
  border-radius: 999px;
  color: var(--color-primary);
  background: rgba(29, 78, 216, 0.08);
  font-size: 13px;
}

.login-hero h1 {
  margin: 0;
  font-size: 52px;
  line-height: 1.1;
}

.login-hero__desc {
  margin-top: 20px;
  color: var(--color-text-secondary);
  font-size: 18px;
}

.login-panel {
  margin: 32px;
  border-radius: 26px;
}

.login-panel__header {
  margin-bottom: 20px;
}

.login-panel__header h2 {
  margin: 0 0 8px;
}

.login-panel__header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.login-panel__submit {
  width: 100%;
  height: 46px;
  margin-top: 8px;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-hero {
    padding-bottom: 0;
  }

  .login-hero h1 {
    font-size: 38px;
  }

  .login-panel {
    margin: 0 20px 20px;
  }
}
</style>
