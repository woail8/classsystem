<script setup lang="ts">
import { CirclePlus, Link, Plus, RefreshRight, Tickets } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  createClassApi,
  createCourseApi,
  dismissClassApi,
  dismissCourseApi,
  joinClassApi,
  joinCourseApi,
  leaveCourseApi,
} from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useCourseStore } from '@/stores/course'

const router = useRouter()
const authStore = useAuthStore()
const courseStore = useCourseStore()

const actionLoading = ref(false)
const courseDialogVisible = ref(false)
const classDialogVisible = ref(false)
const joinCourseDialogVisible = ref(false)
const joinClassDialogVisible = ref(false)

const createCourseForm = reactive({
  name: '',
})

const createClassForm = reactive({
  courseId: undefined as number | undefined,
  className: '',
})

const joinCourseForm = reactive({
  inviteCode: '',
})

const joinClassForm = reactive({
  inviteCode: '',
})

const uniqueCourseOptions = computed(() => {
  const map = new Map<number, { courseId: number; courseName: string }>()
  for (const item of courseStore.courseList) {
    if (!map.has(item.courseId)) {
      map.set(item.courseId, { courseId: item.courseId, courseName: item.courseName })
    }
  }
  return Array.from(map.values())
})

const adminCourseOptions = computed(() =>
  uniqueCourseOptions.value.filter((option) =>
    courseStore.courseList.some(
      (item) => item.courseId === option.courseId && item.creatorId === authStore.user?.id
    )
  )
)

function openClass(classId: number): void {
  router.push(`/classes/${classId}`)
}

function isAdmin(item: { creatorId?: number }): boolean {
  return authStore.user?.id === item.creatorId
}

async function copyText(text: string, successText: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(successText)
  } catch (_error) {
    ElMessage.warning(`复制失败，请手动复制：${text}`)
  }
}

function resetCreateCourseForm(): void {
  createCourseForm.name = ''
}

function resetCreateClassForm(): void {
  createClassForm.courseId = adminCourseOptions.value[0]?.courseId
  createClassForm.className = ''
}

function openCreateClassDialog(): void {
  if (adminCourseOptions.value.length === 0) {
    ElMessage.warning('只有课程管理员才能创建班级，请先创建课程或进入自己创建的课程')
    return
  }

  resetCreateClassForm()
  classDialogVisible.value = true
}

function resetJoinCourseForm(): void {
  joinCourseForm.inviteCode = ''
}

function resetJoinClassForm(): void {
  joinClassForm.inviteCode = ''
}

async function refreshCourses(): Promise<void> {
  await courseStore.fetchMyCourses()
}

async function submitCreateCourse(): Promise<void> {
  if (!createCourseForm.name.trim()) {
    ElMessage.warning('请输入课程名称')
    return
  }

  actionLoading.value = true
  try {
    const detail = await createCourseApi({ name: createCourseForm.name.trim() })
    ElMessage.success('课程创建成功，已自动创建默认班级')
    courseDialogVisible.value = false
    resetCreateCourseForm()
    await refreshCourses()
    openClass(detail.classId)
  } finally {
    actionLoading.value = false
  }
}

async function submitCreateClass(): Promise<void> {
  if (!createClassForm.courseId) {
    ElMessage.warning('请选择所属课程')
    return
  }
  if (!createClassForm.className.trim()) {
    ElMessage.warning('请输入班级名称')
    return
  }

  actionLoading.value = true
  try {
    const detail = await createClassApi({
      courseId: createClassForm.courseId,
      className: createClassForm.className.trim(),
    })
    ElMessage.success('班级创建成功')
    classDialogVisible.value = false
    resetCreateClassForm()
    await refreshCourses()
    openClass(detail.classId)
  } finally {
    actionLoading.value = false
  }
}

async function submitJoinCourse(): Promise<void> {
  if (!joinCourseForm.inviteCode.trim()) {
    ElMessage.warning('请输入课程邀请码')
    return
  }

  actionLoading.value = true
  try {
    const detail = await joinCourseApi({ inviteCode: joinCourseForm.inviteCode.trim() })
    ElMessage.success('加入课程成功')
    joinCourseDialogVisible.value = false
    resetJoinCourseForm()
    await refreshCourses()
    openClass(detail.classId)
  } finally {
    actionLoading.value = false
  }
}

async function submitJoinClass(): Promise<void> {
  if (!joinClassForm.inviteCode.trim()) {
    ElMessage.warning('请输入班级邀请码')
    return
  }

  actionLoading.value = true
  try {
    const detail = await joinClassApi({ inviteCode: joinClassForm.inviteCode.trim() })
    ElMessage.success('加入班级成功')
    joinClassDialogVisible.value = false
    resetJoinClassForm()
    await refreshCourses()
    openClass(detail.classId)
  } finally {
    actionLoading.value = false
  }
}

async function handleDismissCourse(item: { courseId: number; courseName: string }): Promise<void> {
  await ElMessageBox.confirm(`确认解散课程“${item.courseName}”吗？该课程下所有班级和相关数据都会被移除。`, '解散课程', {
    type: 'warning',
    confirmButtonText: '确认解散',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await dismissCourseApi(item.courseId)
    ElMessage.success('课程已解散')
    await refreshCourses()
  } finally {
    actionLoading.value = false
  }
}

async function handleDismissClass(item: { classId: number; className: string }): Promise<void> {
  await ElMessageBox.confirm(`确认解散班级“${item.className}”吗？班级内成员和教学数据会被移除。`, '解散班级', {
    type: 'warning',
    confirmButtonText: '确认解散',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await dismissClassApi(item.classId)
    ElMessage.success('班级已解散')
    await refreshCourses()
  } finally {
    actionLoading.value = false
  }
}

async function handleLeaveCourse(item: { courseId: number; courseName: string }): Promise<void> {
  await ElMessageBox.confirm(`确认退出课程“${item.courseName}”吗？你会退出该课程下已加入的所有班级。`, '退出课程', {
    type: 'warning',
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
  })

  actionLoading.value = true
  try {
    await leaveCourseApi(item.courseId)
    ElMessage.success('已退出课程')
    await refreshCourses()
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  resetCreateClassForm()
  refreshCourses().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>我的课程与班级</h2>
        <p>这里可以创建课程与班级，也可以通过课程邀请码或班级邀请码快速加入。</p>
      </div>
      <div class="page-header__actions">
        <el-button :icon="Plus" type="primary" @click="courseDialogVisible = true">创建课程</el-button>
        <el-button :icon="CirclePlus" @click="openCreateClassDialog">创建班级</el-button>
        <el-button :icon="Link" @click="joinCourseDialogVisible = true">加入课程</el-button>
        <el-button :icon="Tickets" @click="joinClassDialogVisible = true">加入班级</el-button>
        <el-button :icon="RefreshRight" @click="refreshCourses()">刷新课程</el-button>
      </div>
    </header>

    <el-skeleton :loading="courseStore.loading" animated :rows="8">
      <template #default>
        <el-empty v-if="courseStore.courseList.length === 0" description="暂无课程，可先创建课程或通过邀请码加入" />

        <div v-else class="course-grid">
          <article
            v-for="item in courseStore.courseList"
            :key="item.classId"
            class="course-card page-card"
          >
            <div class="course-card__top">
              <span class="course-card__role">{{ isAdmin(item) ? '管理员' : item.roleName || '成员' }}</span>
              <span class="course-card__semester">{{ item.semester || '当前学期' }}</span>
            </div>

            <h3>{{ item.courseName }}</h3>
            <p>班级：{{ item.className }}</p>
            <p>管理员：{{ item.creatorName || item.teacherName || '未设置' }}</p>
            <p>班级邀请码：{{ item.inviteCode || '暂无' }}</p>
            <p>课程邀请码：{{ item.courseInviteCode || '暂无' }}</p>

            <footer class="course-card__footer">
              <el-button type="primary" text @click="openClass(item.classId)">进入班级</el-button>
              <el-button
                v-if="item.inviteCode"
                text
                @click="copyText(item.inviteCode, '班级邀请码已复制')"
              >
                复制班级码
              </el-button>
              <el-button
                v-if="item.courseInviteCode"
                text
                @click="copyText(item.courseInviteCode, '课程邀请码已复制')"
              >
                复制课程码
              </el-button>
              <el-button
                v-if="isAdmin(item)"
                text
                type="danger"
                :disabled="actionLoading"
                @click="handleDismissClass(item)"
              >
                解散班级
              </el-button>
              <el-button
                v-if="isAdmin(item)"
                text
                type="danger"
                :disabled="actionLoading"
                @click="handleDismissCourse(item)"
              >
                解散课程
              </el-button>
              <el-button
                v-if="!isAdmin(item)"
                text
                type="danger"
                :disabled="actionLoading"
                @click="handleLeaveCourse(item)"
              >
                退出课程
              </el-button>
            </footer>
          </article>
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="courseDialogVisible" title="创建课程" width="420px" @closed="resetCreateCourseForm">
      <el-form label-position="top">
        <el-form-item label="课程名称">
          <el-input v-model="createCourseForm.name" maxlength="50" placeholder="例如：软件工程实践" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitCreateCourse">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="classDialogVisible" title="创建班级" width="460px" @opened="resetCreateClassForm">
      <el-form label-position="top">
        <el-form-item label="所属课程">
          <el-select v-model="createClassForm.courseId" class="w-full" placeholder="请选择课程">
            <el-option
              v-for="item in adminCourseOptions"
              :key="item.courseId"
              :label="item.courseName"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称">
          <el-input v-model="createClassForm.className" maxlength="50" placeholder="例如：软工 2302 班" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitCreateClass">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="joinCourseDialogVisible" title="通过课程邀请码加入" width="420px" @closed="resetJoinCourseForm">
      <el-form label-position="top">
        <el-form-item label="课程邀请码">
          <el-input v-model="joinCourseForm.inviteCode" maxlength="30" placeholder="请输入课程邀请码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinCourseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitJoinCourse">确认加入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="joinClassDialogVisible" title="通过班级邀请码加入" width="420px" @closed="resetJoinClassForm">
      <el-form label-position="top">
        <el-form-item label="班级邀请码">
          <el-input v-model="joinClassForm.inviteCode" maxlength="30" placeholder="请输入班级邀请码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="joinClassDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitJoinClass">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}

.course-card {
  padding: 22px;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-strong);
}

.course-card__top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.course-card__role,
.course-card__semester {
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
}

.course-card__role {
  color: var(--color-primary);
  background: rgba(29, 78, 216, 0.08);
}

.course-card__semester {
  color: var(--color-text-secondary);
  background: #f3f4f6;
}

.course-card h3 {
  margin: 0 0 14px;
  font-size: 22px;
}

.course-card p {
  margin: 8px 0;
  color: var(--color-text-secondary);
}

.course-card__footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.w-full {
  width: 100%;
}
</style>
