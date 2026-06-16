export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: 'teacher' | 'student'
}

export interface LoginResponseData {
  token: string
  user: UserInfo
}

export interface CourseCard {
  classId: number
  className: string
  courseId: number
  courseName: string
  teacherName?: string
  semester?: string
  roleName?: string
  inviteCode?: string
  courseInviteCode?: string
  creatorId?: number
  creatorName?: string
  teacherId?: number
}

export interface ClassDetail extends CourseCard {
  inviteCode: string
  courseInviteCode?: string
}

export interface SignInTask {
  id: number
  title: string
  type: 'normal' | 'location'
  deadline?: string
  radius?: number
  locationLat?: number
  locationLng?: number
}

export interface AssignmentItem {
  id: number
  title: string
  content?: string
  deadline?: string
  attachmentUrl?: string
  submitStatus?: string
}

export interface QuestionOption {
  key: string
  label: string
}

export interface ExamQuestion {
  id: number
  type: 'single' | 'multiple' | 'judge' | 'short'
  content: string
  score: number
  options?: QuestionOption[]
}

export interface ExamItem {
  id: number
  title: string
  subject?: string
  startTime?: string
  endTime?: string
  duration?: number
  status?: string
}

export interface ExamPreset {
  presetKey: string
  presetName: string
  subject: string
  questionCount: number
  totalScore: number
}

export interface ExamRecordDetail {
  questionId: number
  content: string
  type: 'single' | 'multiple' | 'judge' | 'short'
  correctAnswer: string
  studentAnswer: string
  correct: boolean
  score: number
}

export interface ExamRecordItem {
  recordId: number
  studentId: number
  studentName: string
  username: string
  score: number
  status: string
  screenSwitchCount: number
  submittedAt?: string
  answers: ExamRecordDetail[]
}

export interface ExamStartResponse {
  exam: {
    id: number
    title: string
    questions: ExamQuestion[]
  }
  remainSeconds: number
}

export interface ResourceItem {
  id: number
  title: string
  fileName?: string
  uploadTime?: string
  downloadUrl?: string
}

export interface NotificationItem {
  id?: number
  title?: string
  content?: string
  createTime?: string
  read?: boolean
}

export interface WsMessage<T = unknown> {
  type: string
  data: T
  message?: string
}
