<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createExamApi, getActiveExamsApi, getClassDetailApi, getExamPresetsApi, getExamRecordsApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ClassDetail, ExamItem, ExamPreset, ExamRecordItem } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const examList = ref<ExamItem[]>([])
const classDetail = ref<ClassDetail | null>(null)
const presets = ref<ExamPreset[]>([])
const createDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const actionLoading = ref(false)
const recordLoading = ref(false)
const currentRecords = ref<ExamRecordItem[]>([])
const currentExamTitle = ref('')
const form = reactive({
  title: '',
  presetKey: '',
  duration: 30,
  startTime: '',
  endTime: '',
})

const isAdmin = computed(() => classDetail.value?.creatorId === authStore.user?.id)

async function loadExams(): Promise<void> {
  loading.value = true
  try {
    examList.value = await getActiveExamsApi(String(route.params.classId))
  } finally {
    loading.value = false
  }
}

function openExam(item: ExamItem): void {
  router.push(`/exams/${item.id}`)
}

async function loadClassDetail(): Promise<void> {
  classDetail.value = await getClassDetailApi(String(route.params.classId))
}

async function loadPresets(): Promise<void> {
  presets.value = await getExamPresetsApi()
  if (!form.presetKey) {
    form.presetKey = presets.value[0]?.presetKey || ''
  }
}

function resetForm(): void {
  form.title = ''
  form.presetKey = presets.value[0]?.presetKey || ''
  form.duration = 30
  form.startTime = ''
  form.endTime = ''
}

function openCreateExam(): void {
  resetForm()
  createDialogVisible.value = true
}

async function submitCreateExam(): Promise<void> {
  if (!form.title.trim()) {
    ElMessage.warning('请输入考试标题')
    return
  }
  if (!form.presetKey) {
    ElMessage.warning('请选择预置题卷')
    return
  }
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }

  actionLoading.value = true
  try {
    await createExamApi(String(route.params.classId), {
      title: form.title.trim(),
      presetKey: form.presetKey,
      duration: form.duration,
      startTime: form.startTime.replace('T', ' ') + ':00',
      endTime: form.endTime.replace('T', ' ') + ':00',
    })
    ElMessage.success('考试已发布')
    createDialogVisible.value = false
    resetForm()
    await loadExams()
  } finally {
    actionLoading.value = false
  }
}

async function openRecords(item: ExamItem): Promise<void> {
  currentExamTitle.value = item.title
  recordDialogVisible.value = true
  recordLoading.value = true
  try {
    currentRecords.value = await getExamRecordsApi(item.id)
  } finally {
    recordLoading.value = false
  }
}

onMounted(() => {
  loadClassDetail().catch(() => {})
  loadPresets().catch(() => {})
  loadExams().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>考试列表</h2>
        <p>{{ isAdmin ? '管理员可从预置题卷发布考试，并查看成员作答详情与成绩。' : '选择当前可参加的考试，开始后将进入全屏答题模式。' }}</p>
      </div>
      <div class="page-header__actions">
        <el-button v-if="isAdmin" :icon="Plus" type="primary" @click="openCreateExam">创建考试</el-button>
        <el-button :icon="RefreshRight" @click="loadExams">刷新列表</el-button>
      </div>
    </header>

    <el-skeleton :loading="loading" animated :rows="6">
      <template #default>
        <el-empty v-if="examList.length === 0" description="当前暂无可参加考试" />

        <div v-else class="exam-grid">
          <article v-for="item in examList" :key="item.id" class="page-card exam-card">
            <h3>{{ item.title }}</h3>
            <div class="exam-card__meta">
              <span>科目：{{ item.subject || '--' }}</span>
              <span>开始：{{ item.startTime || '未设置' }}</span>
              <span>结束：{{ item.endTime || '未设置' }}</span>
              <span>时长：{{ item.duration || '--' }} 分钟</span>
              <span>状态：{{ item.status || '--' }}</span>
            </div>
            <el-button v-if="!isAdmin" type="primary" @click="openExam(item)">开始考试</el-button>
            <el-button v-else type="primary" plain @click="openRecords(item)">查看作答详情</el-button>
          </article>
        </div>
      </template>
    </el-skeleton>

    <el-dialog v-model="createDialogVisible" title="发布考试" width="560px" @closed="resetForm">
      <el-form label-position="top">
        <el-form-item label="考试标题">
          <el-input v-model="form.title" maxlength="80" placeholder="例如：小学数学周测一" />
        </el-form-item>
        <el-form-item label="选择题卷">
          <el-select v-model="form.presetKey" class="w-full" placeholder="请选择题卷">
            <el-option
              v-for="item in presets"
              :key="item.presetKey"
              :label="`${item.presetName} / ${item.subject} / ${item.questionCount}题 / ${item.totalScore}分`"
              :value="item.presetKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长（分钟）">
          <el-input-number v-model="form.duration" :min="5" :max="180" class="w-full" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm" class="w-full" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitCreateExam">确认发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="`${currentExamTitle} - 作答详情`" width="760px">
      <el-skeleton :loading="recordLoading" animated :rows="6">
        <template #default>
          <el-empty v-if="currentRecords.length === 0" description="当前暂无成员作答记录" />
          <div v-else class="record-list">
            <article v-for="record in currentRecords" :key="record.recordId" class="page-card record-card">
              <h3>{{ record.studentName }}（{{ record.username }}）</h3>
              <p>成绩：{{ record.score ?? 0 }} 分</p>
              <p>切屏次数：{{ record.screenSwitchCount }}</p>
              <p>提交时间：{{ record.submittedAt || '未提交' }}</p>
              <el-table :data="record.answers" size="small">
                <el-table-column prop="content" label="题目" min-width="220" />
                <el-table-column prop="studentAnswer" label="成员答案" min-width="100" />
                <el-table-column prop="correctAnswer" label="正确答案" min-width="100" />
                <el-table-column prop="score" label="得分" width="80" />
              </el-table>
            </article>
          </div>
        </template>
      </el-skeleton>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.exam-grid {
  display: grid;
  gap: 16px;
}

.exam-card {
  padding: 22px;
}

.exam-card h3 {
  margin: 0 0 14px;
}

.exam-card__meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  color: var(--color-text-secondary);
}

.record-list {
  display: grid;
  gap: 16px;
}

.record-card {
  padding: 18px;
}

.record-card h3,
.record-card p {
  margin: 0 0 10px;
}
</style>
