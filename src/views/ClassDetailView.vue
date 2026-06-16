<script setup lang="ts">
import { ClipboardCheck, Copy, FileText, FolderOpen, Megaphone, NotebookPen } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  dismissClassApi,
  dismissCourseApi,
  getClassDetailApi,
  leaveCourseApi,
  publishNotificationApi,
} from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useCourseStore } from '@/stores/course'
import type { ClassDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const courseStore = useCourseStore()
const classId = computed(() => route.params.classId)
const loading = ref(false)
const actionLoading = ref(false)
const detail = ref<ClassDetail | null>(null)
const publishDialogVisible = ref(false)
const publishForm = reactive({
  title: '',
  content: '',
})

const isAdmin = computed(() => detail.value?.creatorId === authStore.user?.id)

const actionCards = computed(() => [
  {
    title: '签到',
    description: '查看当前签到任务并快速完成签到',
    icon: ClipboardCheck,
    to: () => `/classes/${classId.value}/signin`,
  },
  {
    title: '作业',
    description: '查看班级作业列表并进入提交页面',
    icon: NotebookPen,
    to: () => `/classes/${classId.value}/assignments`,
  },
  {
    title: '考试',
    description: isAdmin.value ? '进入考试列表并使用管理员入口创建考试' : '进入当前可参加的考试列表',
    icon: FileText,
    to: () => `/classes/${classId.value}/exams`,
  },
  {
    title: '资料库',
    description: isAdmin.value ? '浏览资料并使用上传入口发布新资料' : '浏览教师上传的课程资料和附件',
    icon: FolderOpen,
    to: () => `/classes/${classId.value}/resources`,
  },
])

function resetPublishForm(): void {
  publishForm.title = ''
  publishForm.content = ''
}

async function refreshCourses(): Promise<void> {
  await courseStore.fetchMyCourses()
}

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    detail.value = await getClassDetailApi(String(classId.value))
    courseStore.setCurrentClassDetail(detail.value)
  } finally {
    loading.value = false
  }
}

async function copyInviteCode(value?: string, text = '邀请码已复制'): Promise<void> {
  if (!value) {
    ElMessage.warning('当前暂无邀请码')
    return
  }
  try {
    await navigator.clipboard.writeText(value)
    ElMessage.success(text)
  } catch (_error) {
    ElMessage.warning(`复制失败，请手动复制：${value}`)
  }
}

async function submitPublishNotification(): Promise<void> {
  if (!detail.value) return
  if (!publishForm.title.trim()) {
    ElMessage.warning('请输入通知标题')
    return
  }
  if (!publishForm.content.trim()) {
    ElMessage.warning('请输入通知内容')
    return
  }

  actionLoading.value = true
  try {
    await publishNotificationApi({
      classId: detail.value.classId,
      title: publishForm.title.trim(),
      content: publishForm.content.trim(),
    })
    ElMessage.success('通知已发布')
    publishDialogVisible.value = false
    resetPublishForm()
  } finally {
    actionLoading.value = false
  }
}

async function handleDismissClass(): Promise<void> {
  if (!detail.value) return
  await ElMessageBox.confirm(`确认解散班级“${detail.value.className}”吗？班级成员与教学数据会被移除。`, '解散班级', {
    type: 'warning',
    confirmButtonText: '确认解散',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await dismissClassApi(detail.value.classId)
    await refreshCourses()
    ElMessage.success('班级已解散')
    router.push('/courses')
  } finally {
    actionLoading.value = false
  }
}

async function handleDismissCourse(): Promise<void> {
  if (!detail.value) return
  await ElMessageBox.confirm(`确认解散课程“${detail.value.courseName}”吗？课程下全部班级都会一起移除。`, '解散课程', {
    type: 'warning',
    confirmButtonText: '确认解散',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await dismissCourseApi(detail.value.courseId)
    await refreshCourses()
    ElMessage.success('课程已解散')
    router.push('/courses')
  } finally {
    actionLoading.value = false
  }
}

async function handleLeaveCourse(): Promise<void> {
  if (!detail.value) return
  await ElMessageBox.confirm(`确认退出课程“${detail.value.courseName}”吗？你会退出该课程下已加入的所有班级。`, '退出课程', {
    type: 'warning',
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await leaveCourseApi(detail.value.courseId)
    await refreshCourses()
    ElMessage.success('已退出课程')
    router.push('/courses')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadDetail().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>班级详情</h2>
        <p>查看当前班级信息、邀请码以及功能入口。</p>
      </div>
      <el-button @click="router.push('/courses')">返回课程列表</el-button>
    </header>

    <el-skeleton :loading="loading" animated :rows="6">
      <template #default>
        <section v-if="detail" class="summary-card page-card">
          <div class="summary-card__main">
            <div>
              <h3>{{ detail.courseName }}</h3>
              <p>班级：{{ detail.className }}</p>
              <p>管理员：{{ detail.creatorName || detail.teacherName || '未设置' }}</p>
              <p>学期：{{ detail.semester || '当前学期' }}</p>
            </div>

            <div class="summary-card__codes">
              <div class="summary-card__code-item">
                <span>课程邀请码</span>
                <strong>{{ detail.courseInviteCode || '暂无' }}</strong>
                <el-button text @click="copyInviteCode(detail.courseInviteCode, '课程邀请码已复制')">
                  <Copy :size="16" />
                  复制
                </el-button>
              </div>
              <div class="summary-card__code-item">
                <span>班级邀请码</span>
                <strong>{{ detail.inviteCode || '暂无' }}</strong>
                <el-button text @click="copyInviteCode(detail.inviteCode, '班级邀请码已复制')">
                  <Copy :size="16" />
                  复制
                </el-button>
              </div>
            </div>
          </div>
        </section>

        <el-empty v-else description="未获取到班级详情" />
      </template>
    </el-skeleton>

    <section v-if="detail" class="manage-card page-card">
      <div class="manage-card__header">
        <div>
          <h3>{{ isAdmin ? '管理员操作' : '成员操作' }}</h3>
          <p>
            {{ isAdmin ? '可以直接发布通知、进入考试与资料管理，并解散班级或课程。' : '可以从这里快速退出当前课程。' }}
          </p>
        </div>
      </div>

      <div class="manage-card__actions">
        <template v-if="isAdmin">
          <el-button type="primary" @click="publishDialogVisible = true">
            <Megaphone :size="16" />
            发布通知
          </el-button>
          <el-button @click="router.push(`/classes/${classId}/signin`)">发布签到</el-button>
          <el-button @click="router.push(`/classes/${classId}/assignments`)">发布作业</el-button>
          <el-button @click="router.push(`/classes/${classId}/resources`)">上传资料</el-button>
          <el-button @click="router.push(`/classes/${classId}/exams`)">考试管理</el-button>
          <el-button type="danger" plain :loading="actionLoading" @click="handleDismissClass">解散班级</el-button>
          <el-button type="danger" :loading="actionLoading" @click="handleDismissCourse">解散课程</el-button>
        </template>
        <el-button v-else type="danger" plain :loading="actionLoading" @click="handleLeaveCourse">退出课程</el-button>
      </div>
    </section>

    <section class="detail-grid">
      <article
        v-for="card in actionCards"
        :key="card.title"
        class="detail-card page-card"
        @click="router.push(card.to())"
      >
        <component :is="card.icon" :size="28" />
        <h3>{{ card.title }}</h3>
        <p>{{ card.description }}</p>
      </article>
    </section>

    <el-dialog v-model="publishDialogVisible" title="发布班级通知" width="520px" @closed="resetPublishForm">
      <el-form label-position="top">
        <el-form-item label="通知标题">
          <el-input v-model="publishForm.title" maxlength="80" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入通知内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitPublishNotification">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.summary-card {
  margin-bottom: 20px;
  padding: 24px;
}

.summary-card__main {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.summary-card h3 {
  margin: 0 0 16px;
  font-size: 24px;
}

.summary-card p {
  margin: 8px 0;
  color: var(--color-text-secondary);
}

.summary-card__codes {
  display: grid;
  gap: 14px;
  min-width: 260px;
}

.summary-card__code-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.summary-card__code-item span {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.summary-card__code-item strong {
  font-size: 18px;
  letter-spacing: 1px;
}

.manage-card {
  margin-bottom: 20px;
  padding: 24px;
}

.manage-card__header h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.manage-card__header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.manage-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.detail-card {
  padding: 24px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.detail-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-strong);
}

.detail-card h3 {
  margin: 18px 0 12px;
  font-size: 22px;
}

.detail-card p {
  margin: 0;
  color: var(--color-text-secondary);
}
</style>
