import { defineStore } from 'pinia'
import { getMyCoursesApi } from '@/api'
import type { ClassDetail, CourseCard } from '@/types'

export const useCourseStore = defineStore('course', {
  state: () => ({
    courseList: [] as CourseCard[],
    loading: false,
    currentClassDetail: null as ClassDetail | null,
  }),
  actions: {
    async fetchMyCourses() {
      this.loading = true
      try {
        const data = await getMyCoursesApi()
        this.courseList = Array.isArray(data) ? data : data.records || []
      } finally {
        this.loading = false
      }
    },
    setCurrentClassDetail(detail: ClassDetail | null) {
      this.currentClassDetail = detail
    },
  },
})
