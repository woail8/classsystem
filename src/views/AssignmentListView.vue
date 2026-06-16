<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssignmentsApi, getClassDetailApi, publishAssignmentApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { AssignmentItem, ClassDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const assignmentList = ref<AssignmentItem[]>([])
const classDetail = ref<ClassDetail | null>(null)
const dialogVisible = ref(false)
const actionLoading = ref(false)
const form = reactive({
  title: '',
  content: '',
  deadline: '',
  file: null as File | null,
})

const isAdmin = computed(() => classDetail.value?.creatorId === authStore.user?.id)

async function loadAssignments(): Promise<void> {
  loading.value = true
  try {
    assignmentList.value = await getAssignmentsApi(String(route.params.classId))
  } finally {
    loading.value = false
  }
}

function openSubmit(item: AssignmentItem): void {
  router.push(`/assignments/${item.id}/submit`)
}

async function loadClassDetail(): Promise<void> {
  classDetail.value = await getClassDetailApi(String(route.params.classId))
}

function resetForm(): void {
  form.title = ''
  form.content = ''
  form.deadline = ''
  form.file = null
}

function handleFileChange(uploadFile: { raw?: File }): void {
  form.file = uploadFile.raw || null
}

async function submitPublish(): Promise<void> {
  if (!form.title.trim()) {
    ElMessage.warning('请输入作业标题')
    return
  }
  if (!form.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }

  const formData = new FormData()
  formData.append('classId', String(route.params.classId))
  formData.append('title', form.title.trim())
  formData.append('content', form.content.trim())
  formData.append('deadline', form.deadline.replace('T', ' ') + ':00')
  if (form.file) {
    formData.append('file', form.file)
  }

  actionLoading.value = true
  try {
    await publishAssignmentApi(formData)
    ElMessage.success('作业已发布')
    dialogVisible.value = false
    resetForm()
    await loadAssignments()
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadClassDetail().catch(() => {})
  loadAssignments().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>作业列表</h2>
        <p>{{ isAdmin ? '管理员可在这里发布作业，成员可查看作业与提交状态。' : '查看当前班级的作业任务、截止时间和提交状态。' }}</p>
      </div>
      <div class="page-header__actions">
        <el-button v-if="isAdmin" :icon="Plus" type="primary" @click="dialogVisible = true">发布作业</el-button>
        <el-button :icon="RefreshRight" @click="loadAssignments">刷新列表</el-button>
      </div>
    </header>

    <el-skeleton :loading="loading" animated :rows="6">
      <template #default>
        <el-empty v-if="assignmentList.length === 0" description="当前班级暂无作业" />

        <div v-else class="assignment-grid">
          <article v-for="item in assignmentList" :key="item.id" class="page-card assignment-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.content || '暂无作业说明' }}</p>
            <div class="assignment-card__meta">
              <span>截止：{{ item.deadline || '未设置' }}</span>
              <span>状态：{{ item.submitStatus || '未提交' }}</span>
            </div>
            <el-button type="primary" @click="openSubmit(item)">进入提交</el-button>
          </article>
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="dialogVisible" title="发布作业" width="560px" @closed="resetForm">
      <el-form label-position="top">
        <el-form-item label="作业标题">
          <el-input v-model="form.title" maxlength="80" placeholder="例如：第 2 章练习题" />
        </el-form-item>
        <el-form-item label="作业说明">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="请选择截止时间"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload :auto-upload="false" :limit="1" :on-change="handleFileChange" :show-file-list="true">
            <el-button>选择附件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.assignment-grid {
  display: grid;
  gap: 16px;
}

.assignment-card {
  padding: 22px;
}

.assignment-card h3 {
  margin: 0 0 10px;
}

.assignment-card p {
  margin: 0 0 16px;
  color: var(--color-text-secondary);
}

.assignment-card__meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  color: var(--color-text-secondary);
  font-size: 14px;
}
</style>
