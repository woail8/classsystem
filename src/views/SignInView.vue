<script setup lang="ts">
import { Location, Plus, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { createSignInTaskApi, getActiveSignInApi, getClassDetailApi, submitSignInApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ClassDetail, SignInTask } from '@/types'
import { formatDistance, haversineDistance } from '@/utils/distance'

const route = useRoute()
const classId = route.params.classId
const authStore = useAuthStore()

const loading = ref(false)
const locating = ref(false)
const submitting = ref(false)
const task = ref<SignInTask | null>(null)
const classDetail = ref<ClassDetail | null>(null)
const dialogVisible = ref(false)
const creating = ref(false)
const currentLocation = reactive({
  latitude: null as number | null,
  longitude: null as number | null,
})
const createForm = reactive({
  title: '',
  type: 'normal',
  deadline: '',
  locationLat: null as number | null,
  locationLng: null as number | null,
  radius: 100,
})

const isAdmin = computed(() => classDetail.value?.creatorId === authStore.user?.id)

const computedDistance = computed(() => {
  if (!task.value || task.value.type !== 'location') return '--'
  if (currentLocation.latitude == null || currentLocation.longitude == null) return '--'
  if (task.value.locationLat == null || task.value.locationLng == null) return '--'

  return formatDistance(
    haversineDistance(
      currentLocation.latitude,
      currentLocation.longitude,
      Number(task.value.locationLat),
      Number(task.value.locationLng)
    )
  )
})

async function loadTask(): Promise<void> {
  loading.value = true
  try {
    task.value = await getActiveSignInApi(String(classId))
  } finally {
    loading.value = false
  }
}

async function loadClassDetail(): Promise<void> {
  classDetail.value = await getClassDetailApi(String(classId))
}

function getLocation(): void {
  if (!navigator.geolocation) {
    ElMessage.error('当前浏览器不支持定位能力')
    return
  }

  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      currentLocation.latitude = position.coords.latitude
      currentLocation.longitude = position.coords.longitude
      locating.value = false
      ElMessage.success('定位成功')
    },
    (error) => {
      locating.value = false
      const messages: Record<number, string> = {
        1: '定位权限被拒绝，请在浏览器设置中允许位置访问',
        2: '定位失败，无法获取当前位置',
        3: '定位超时，请稍后重试',
      }
      ElMessage.error(messages[error.code] || '定位异常')
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0,
    }
  )
}

async function submit(): Promise<void> {
  if (!task.value) return

  if (task.value.type === 'location' && currentLocation.latitude == null) {
    ElMessage.warning('位置签到前请先获取当前位置')
    return
  }

  submitting.value = true
  try {
    await submitSignInApi({
      taskId: task.value.id,
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude,
    })
    ElMessage.success('签到成功')
    await loadTask()
  } finally {
    submitting.value = false
  }
}

function resetCreateForm(): void {
  createForm.title = ''
  createForm.type = 'normal'
  createForm.deadline = ''
  createForm.locationLat = null
  createForm.locationLng = null
  createForm.radius = 100
}

async function submitCreateTask(): Promise<void> {
  if (!createForm.title.trim()) {
    ElMessage.warning('请输入签到标题')
    return
  }
  if (!createForm.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }
  if (createForm.type === 'location' && (createForm.locationLat == null || createForm.locationLng == null)) {
    ElMessage.warning('位置签到需要填写经纬度')
    return
  }

  creating.value = true
  try {
    await createSignInTaskApi(String(classId), {
      title: createForm.title.trim(),
      type: createForm.type,
      deadline: createForm.deadline.replace('T', ' ') + ':00',
      locationLat: createForm.type === 'location' ? createForm.locationLat : null,
      locationLng: createForm.type === 'location' ? createForm.locationLng : null,
      radius: createForm.type === 'location' ? createForm.radius : null,
    })
    ElMessage.success('签到已发布')
    dialogVisible.value = false
    resetCreateForm()
    await loadTask()
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadClassDetail().catch(() => {})
  loadTask().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>签到中心</h2>
        <p>{{ isAdmin ? '管理员可在这里发布普通签到或位置签到，成员可查看当前有效签到。' : '教师发布有效签到后，这里会展示当前任务。位置签到的最终判定以服务端计算结果为准。' }}</p>
      </div>
      <div class="page-header__actions">
        <el-button v-if="isAdmin" :icon="Plus" type="primary" @click="dialogVisible = true">发布签到</el-button>
        <el-button :icon="RefreshRight" @click="loadTask">刷新任务</el-button>
      </div>
    </header>

    <el-skeleton :loading="loading" animated :rows="6">
      <template #default>
        <el-empty v-if="!task" description="当前暂无有效签到任务" />

        <el-card v-else class="page-card" shadow="never">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="签到标题">{{ task.title }}</el-descriptions-item>
            <el-descriptions-item label="签到类型">
              {{ task.type === 'location' ? '位置签到' : '普通签到' }}
            </el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ task.deadline || '以服务端配置为准' }}</el-descriptions-item>
            <el-descriptions-item v-if="task.type === 'location'" label="签到范围">
              {{ task.radius || '--' }} 米
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="task.type === 'location'" class="sign-area">
            <el-alert
              title="位置签到需要浏览器定位权限，请确保已开启定位。"
              type="info"
              :closable="false"
              show-icon
            />

            <div class="sign-area__action">
              <el-button :icon="Location" :loading="locating" @click="getLocation">
                获取当前位置
              </el-button>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="我的纬度">{{ currentLocation.latitude ?? '--' }}</el-descriptions-item>
              <el-descriptions-item label="我的经度">{{ currentLocation.longitude ?? '--' }}</el-descriptions-item>
              <el-descriptions-item label="辅助距离">{{ computedDistance }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="sign-area__submit">
            <el-button type="primary" :loading="submitting" @click="submit">立即签到</el-button>
          </div>
        </el-card>
      </template>
    </el-skeleton>

    <el-dialog v-model="dialogVisible" title="发布签到" width="560px" @closed="resetCreateForm">
      <el-form label-position="top">
        <el-form-item label="签到标题">
          <el-input v-model="createForm.title" maxlength="80" placeholder="例如：第一节课签到" />
        </el-form-item>
        <el-form-item label="签到类型">
          <el-radio-group v-model="createForm.type">
            <el-radio label="normal">普通签到</el-radio>
            <el-radio label="location">位置签到</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="createForm.deadline"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm"
            placeholder="请选择截止时间"
            class="w-full"
          />
        </el-form-item>
        <template v-if="createForm.type === 'location'">
          <el-form-item label="签到纬度">
            <el-input-number v-model="createForm.locationLat" :precision="6" :step="0.000001" class="w-full" />
          </el-form-item>
          <el-form-item label="签到经度">
            <el-input-number v-model="createForm.locationLng" :precision="6" :step="0.000001" class="w-full" />
          </el-form-item>
          <el-form-item label="允许半径（米）">
            <el-input-number v-model="createForm.radius" :min="10" :step="10" class="w-full" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreateTask">确认发布</el-button>
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

.sign-area {
  margin-top: 18px;
}

.sign-area__action {
  margin: 18px 0;
}

.sign-area__submit {
  margin-top: 20px;
}
</style>
