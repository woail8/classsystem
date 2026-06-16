import request from '@/utils/request'
import type {
  ApiResponse,
  AssignmentItem,
  ClassDetail,
  CourseCard,
  ExamItem,
  ExamPreset,
  ExamRecordItem,
  ExamStartResponse,
  LoginResponseData,
  NotificationItem,
  ResourceItem,
  SignInTask,
} from '@/types'

async function unwrap<T>(config: Record<string, unknown>): Promise<T> {
  const response = await request(config)
  const payload = response.data as ApiResponse<T>

  if (payload.code !== 200) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export function loginApi(data: { username: string; password: string }) {
  return unwrap<LoginResponseData>({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

export function getMyCoursesApi() {
  return unwrap<CourseCard[] | { records: CourseCard[] }>({
    url: '/courses/my',
    method: 'get',
  })
}

export function createCourseApi(data: { name: string }) {
  return unwrap<ClassDetail>({
    url: '/courses',
    method: 'post',
    data,
  })
}

export function joinCourseApi(data: { inviteCode: string }) {
  return unwrap<ClassDetail>({
    url: '/courses/join',
    method: 'post',
    data,
  })
}

export function createClassApi(data: { courseId: number; className: string }) {
  return unwrap<ClassDetail>({
    url: '/classes',
    method: 'post',
    data,
  })
}

export function joinClassApi(data: { inviteCode: string }) {
  return unwrap<ClassDetail>({
    url: '/classes/join',
    method: 'post',
    data,
  })
}

export function getClassDetailApi(classId: string | number) {
  return unwrap<ClassDetail>({
    url: `/classes/${classId}`,
    method: 'get',
  })
}

export function dismissCourseApi(courseId: string | number) {
  return unwrap<null>({
    url: `/courses/${courseId}`,
    method: 'delete',
  })
}

export function leaveCourseApi(courseId: string | number) {
  return unwrap<null>({
    url: `/courses/${courseId}/leave`,
    method: 'post',
  })
}

export function dismissClassApi(classId: string | number) {
  return unwrap<null>({
    url: `/classes/${classId}`,
    method: 'delete',
  })
}

export function publishNotificationApi(data: { classId: number; title: string; content: string }) {
  return unwrap<NotificationItem>({
    url: '/notifications',
    method: 'post',
    data,
  })
}

export function uploadResourceApi(formData: FormData) {
  return unwrap<ResourceItem>({
    url: '/resources',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getActiveSignInApi(classId: string | number) {
  return unwrap<SignInTask>({
    url: `/classes/${classId}/active-signin`,
    method: 'get',
  })
}

export function createSignInTaskApi(classId: string | number, data: Record<string, unknown>) {
  return unwrap<SignInTask>({
    url: `/classes/${classId}/signin`,
    method: 'post',
    data,
  })
}

export function submitSignInApi(data: {
  taskId: number
  latitude?: number | null
  longitude?: number | null
}) {
  return unwrap<unknown>({
    url: '/signin/submit',
    method: 'post',
    data,
  })
}

export function getAssignmentsApi(classId: string | number) {
  return unwrap<AssignmentItem[]>({
    url: `/assignments/${classId}`,
    method: 'get',
  })
}

export function publishAssignmentApi(formData: FormData) {
  return unwrap<AssignmentItem>({
    url: '/assignments',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getAssignmentDetailApi(id: string | number) {
  return unwrap<AssignmentItem>({
    url: `/assignments/${id}/detail`,
    method: 'get',
  })
}

export function submitAssignmentApi(id: string | number, formData: FormData) {
  return unwrap<unknown>({
    url: `/assignments/${id}/submit`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getActiveExamsApi(classId: string | number) {
  return unwrap<ExamItem[]>({
    url: `/exams/${classId}/active`,
    method: 'get',
  })
}

export function getExamPresetsApi() {
  return unwrap<ExamPreset[]>({
    url: '/exams/presets',
    method: 'get',
  })
}

export function createExamApi(classId: string | number, data: Record<string, unknown>) {
  return unwrap<ExamItem>({
    url: `/exams/classes/${classId}`,
    method: 'post',
    data,
  })
}

export function getExamRecordsApi(examId: string | number) {
  return unwrap<ExamRecordItem[]>({
    url: `/exams/${examId}/records`,
    method: 'get',
  })
}

export function startExamApi(examId: string | number) {
  return unwrap<ExamStartResponse>({
    url: `/exams/${examId}/start`,
    method: 'post',
  })
}

export function submitExamApi(
  examId: string | number,
  data: { answers: Array<{ questionId: number; answer: string }> }
) {
  return unwrap<unknown>({
    url: `/exams/${examId}/submit`,
    method: 'post',
    data,
  })
}

export function reportCheatApi(data: { examId: string | number; action: string }) {
  return unwrap<unknown>({
    url: '/exams/cheat',
    method: 'post',
    data,
  })
}

export function getResourcesApi(classId: string | number) {
  return unwrap<ResourceItem[]>({
    url: `/resources/${classId}`,
    method: 'get',
  })
}

export function getNotificationsApi() {
  return unwrap<NotificationItem[]>({
    url: '/notifications',
    method: 'get',
  })
}
