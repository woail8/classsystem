import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getServerHost } from '@/config/runtime'
import AssignmentListView from '@/views/AssignmentListView.vue'
import AssignmentSubmitView from '@/views/AssignmentSubmitView.vue'
import ClassDetailView from '@/views/ClassDetailView.vue'
import CourseListView from '@/views/CourseListView.vue'
import ExamListView from '@/views/ExamListView.vue'
import ExamPageView from '@/views/ExamPageView.vue'
import LoginView from '@/views/LoginView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import NotificationView from '@/views/NotificationView.vue'
import ProfileView from '@/views/ProfileView.vue'
import ResourceLibraryView from '@/views/ResourceLibraryView.vue'
import SignInView from '@/views/SignInView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/courses',
  },
  {
    path: '/login',
    component: LoginView,
    meta: { layout: 'blank' },
  },
  {
    path: '/courses',
    component: CourseListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/classes/:classId',
    component: ClassDetailView,
    meta: { requiresAuth: true },
  },
  {
    path: '/classes/:classId/signin',
    component: SignInView,
    meta: { requiresAuth: true },
  },
  {
    path: '/classes/:classId/assignments',
    component: AssignmentListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/assignments/:assignmentId/submit',
    component: AssignmentSubmitView,
    meta: { requiresAuth: true },
  },
  {
    path: '/classes/:classId/exams',
    component: ExamListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/exams/:examId',
    component: ExamPageView,
    meta: { requiresAuth: true, layout: 'blank' },
  },
  {
    path: '/classes/:classId/resources',
    component: ResourceLibraryView,
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    component: NotificationView,
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    component: ProfileView,
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    component: NotFoundView,
    meta: { layout: 'blank' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('campus_token')
  const serverHost = getServerHost()

  if (to.meta.requiresAuth) {
    if (!token || !serverHost) {
      next('/login')
      return
    }
  }

  if (to.path === '/login' && token && serverHost) {
    next('/courses')
    return
  }

  next()
})

export default router
