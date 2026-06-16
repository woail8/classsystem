<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reportCheatApi, startExamApi, submitExamApi } from '@/api'
import { useExamStore } from '@/stores/exam'
import type { WsMessage } from '@/types'
import { formatSeconds } from '@/utils/time'
import { wsClient } from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()
const examId = String(route.params.examId)
const loading = ref(false)
const submitting = ref(false)
let timer: number | null = null

const remainText = computed(() => formatSeconds(examStore.remainSeconds))
const questions = computed(() => examStore.currentExam?.questions || [])
const answeredCount = computed(() =>
  questions.value.filter((question) => {
    const value = examStore.answers[question.id]
    return Array.isArray(value) ? value.length > 0 : Boolean(value)
  }).length
)

const questionTypeLabel: Record<string, string> = {
  single: '单选题',
  multiple: '多选题',
  judge: '判断题',
  short: '简答题',
}

async function loadExam(): Promise<void> {
  loading.value = true
  try {
    const data = await startExamApi(examId)
    examStore.setExamPayload(data)
    startTimer()
    bindWs()
    await nextTick()
    enterFullscreen()
  } finally {
    loading.value = false
  }
}

function updateAnswer(questionId: number, value: string | string[]): void {
  examStore.setAnswer(questionId, value)
}

function normalizeAnswers() {
  return questions.value.map((question) => ({
    questionId: question.id,
    answer: Array.isArray(examStore.answers[question.id])
      ? (examStore.answers[question.id] as string[]).join(',')
      : String(examStore.answers[question.id] || ''),
  }))
}

function startTimer(): void {
  stopTimer()
  timer = window.setInterval(async () => {
    examStore.tick()
    if (examStore.remainSeconds <= 0) {
      stopTimer()
      ElMessage.warning('考试时间已到，系统将自动提交')
      await submitPaper(true)
    }
  }, 1000)
}

function stopTimer(): void {
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

async function submitPaper(autoSubmit = false): Promise<void> {
  if (submitting.value) return

  if (!autoSubmit) {
    try {
      await ElMessageBox.confirm('确认提交试卷吗？提交后将无法继续作答。', '提交确认', {
        type: 'warning',
      })
    } catch {
      return
    }
  }

  submitting.value = true
  try {
    await submitExamApi(examId, { answers: normalizeAnswers() })
    ElMessage.success(autoSubmit ? '试卷已自动提交' : '试卷提交成功')
    cleanup()
    router.push('/courses')
  } finally {
    submitting.value = false
  }
}

async function reportScreenSwitch(): Promise<void> {
  examStore.increaseScreenSwitch()
  await reportCheatApi({
    examId,
    action: 'screen_switch',
  })
}

function handleVisibilityChange(): void {
  if (document.hidden) {
    reportScreenSwitch().catch(() => {})
    ElMessage.warning('检测到页面切换，系统已自动上报')
  }
}

async function enterFullscreen(): Promise<void> {
  if (document.fullscreenElement) return
  try {
    await document.documentElement.requestFullscreen?.()
  } catch (error) {
    console.warn('全屏请求失败：', error)
  }
}

function handleTimeSync(message: WsMessage<{ examId: string; remainSeconds: number }>): void {
  if (String(message.data.examId) === examId) {
    examStore.syncRemainSeconds(message.data.remainSeconds)
  }
}

async function handleForceSubmit(message: WsMessage<{ examId: string }>): Promise<void> {
  if (String(message.data.examId) === examId) {
    ElMessage.warning('服务端已要求强制交卷')
    await submitPaper(true)
  }
}

function bindWs(): void {
  wsClient.on('EXAM_TIME_SYNC', handleTimeSync)
  wsClient.on('EXAM_FORCE_SUBMIT', handleForceSubmit)
}

function unbindWs(): void {
  wsClient.off('EXAM_TIME_SYNC', handleTimeSync)
  wsClient.off('EXAM_FORCE_SUBMIT', handleForceSubmit)
}

function scrollToQuestion(index: number): void {
  const cards = document.querySelectorAll('.exam-question')
  cards[index]?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function beforeUnload(event: BeforeUnloadEvent): void {
  event.preventDefault()
  event.returnValue = '考试进行中，离开页面可能影响作答'
}

function cleanup(): void {
  stopTimer()
  unbindWs()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('beforeunload', beforeUnload)
  examStore.resetExam()
}

onMounted(() => {
  loadExam().catch(() => {})
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('beforeunload', beforeUnload)
})

onBeforeUnmount(() => {
  cleanup()
})
</script>

<template>
  <div class="exam-page">
    <header class="exam-page__header">
      <div>
        <h2>{{ examStore.currentExam?.title || '考试加载中' }}</h2>
        <p>请保持页面可见并尽量全屏作答，切屏行为会同步到服务端。</p>
      </div>
      <div class="exam-page__tools">
        <el-tag type="danger" size="large">剩余时间：{{ remainText }}</el-tag>
        <el-tag type="warning" size="large">切屏次数：{{ examStore.screenSwitchCount }}</el-tag>
        <el-button @click="enterFullscreen">进入全屏</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPaper()">提交试卷</el-button>
      </div>
    </header>

    <el-row :gutter="20" v-loading="loading">
      <el-col :lg="18" :xs="24">
        <el-card
          v-for="(question, index) in questions"
          :key="question.id"
          class="exam-question page-card"
          shadow="never"
        >
          <template #header>
            <div class="exam-question__head">
              <span>第 {{ index + 1 }} 题</span>
              <el-tag>{{ questionTypeLabel[question.type] || question.type }}</el-tag>
              <span>{{ question.score }} 分</span>
            </div>
          </template>

          <p class="exam-question__content">{{ question.content }}</p>

          <el-radio-group
            v-if="question.type === 'single' || question.type === 'judge'"
            :model-value="String(examStore.answers[question.id] || '')"
            @change="(value) => updateAnswer(question.id, String(value))"
          >
            <el-radio v-for="option in question.options || []" :key="option.key" :label="option.key">
              {{ option.key }}. {{ option.label }}
            </el-radio>
          </el-radio-group>

          <el-checkbox-group
            v-else-if="question.type === 'multiple'"
            :model-value="(examStore.answers[question.id] as string[]) || []"
            @change="(value) => updateAnswer(question.id, value as string[])"
          >
            <el-checkbox v-for="option in question.options || []" :key="option.key" :label="option.key">
              {{ option.key }}. {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>

          <el-input
            v-else
            :model-value="String(examStore.answers[question.id] || '')"
            type="textarea"
            :rows="4"
            placeholder="请输入你的答案"
            @input="(value) => updateAnswer(question.id, String(value))"
          />
        </el-card>
      </el-col>

      <el-col :lg="6" :xs="24">
        <el-card class="exam-answer-card page-card" shadow="never">
          <template #header>
            <div class="exam-answer-card__header">
              <span>答题卡</span>
              <span>{{ answeredCount }}/{{ questions.length }}</span>
            </div>
          </template>

          <div class="exam-answer-card__grid">
            <el-button
              v-for="(question, index) in questions"
              :key="question.id"
              :type="examStore.answers[question.id] ? 'primary' : 'default'"
              circle
              @click="scrollToQuestion(index)"
            >
              {{ index + 1 }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.exam-page {
  min-height: 100vh;
  padding: 20px;
  background: var(--color-bg);
}

.exam-page__header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.exam-page__header h2 {
  margin: 0 0 8px;
}

.exam-page__header p {
  margin: 0;
  color: var(--color-text-secondary);
}

.exam-page__tools {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.exam-question {
  margin-bottom: 16px;
}

.exam-question__head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.exam-question__content {
  margin: 0 0 16px;
  line-height: 1.8;
}

.exam-answer-card {
  position: sticky;
  top: 20px;
}

.exam-answer-card__header {
  display: flex;
  justify-content: space-between;
}

.exam-answer-card__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

@media (max-width: 960px) {
  .exam-page__header {
    flex-direction: column;
  }

  .exam-page__tools {
    justify-content: flex-start;
  }
}
</style>
