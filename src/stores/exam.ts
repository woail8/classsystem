import { defineStore } from 'pinia'
import type { ExamStartResponse } from '@/types'

export const useExamStore = defineStore('exam', {
  state: () => ({
    currentExam: null as ExamStartResponse['exam'] | null,
    remainSeconds: 0,
    answers: {} as Record<number, string | string[]>,
    screenSwitchCount: 0,
    started: false,
  }),
  actions: {
    setExamPayload(payload: ExamStartResponse) {
      this.currentExam = payload.exam
      this.remainSeconds = payload.remainSeconds
      this.answers = {}
      this.screenSwitchCount = 0
      this.started = true
    },
    setAnswer(questionId: number, value: string | string[]) {
      this.answers[questionId] = value
    },
    tick() {
      if (this.remainSeconds > 0) {
        this.remainSeconds -= 1
      }
    },
    syncRemainSeconds(seconds: number) {
      this.remainSeconds = Math.max(0, Math.floor(seconds || 0))
    },
    increaseScreenSwitch() {
      this.screenSwitchCount += 1
    },
    resetExam() {
      this.currentExam = null
      this.remainSeconds = 0
      this.answers = {}
      this.screenSwitchCount = 0
      this.started = false
    },
  },
})
