<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getClassDetailApi, getResourcesApi, uploadResourceApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ClassDetail, ResourceItem } from '@/types'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const resourceList = ref<ResourceItem[]>([])
const classDetail = ref<ClassDetail | null>(null)
const uploadDialogVisible = ref(false)
const actionLoading = ref(false)
const uploadForm = reactive({
  title: '',
  file: null as File | null,
})

const isAdmin = computed(() => classDetail.value?.creatorId === authStore.user?.id)

async function loadResources(): Promise<void> {
  loading.value = true
  try {
    resourceList.value = await getResourcesApi(String(route.params.classId))
  } finally {
    loading.value = false
  }
}

async function loadClassDetail(): Promise<void> {
  classDetail.value = await getClassDetailApi(String(route.params.classId))
}

function resetUploadForm(): void {
  uploadForm.title = ''
  uploadForm.file = null
}

function handleFileChange(uploadFile: { raw?: File }): void {
  uploadForm.file = uploadFile.raw || null
}

async function submitUpload(): Promise<void> {
  if (!uploadForm.title.trim()) {
    ElMessage.warning('请输入资料标题')
    return
  }
  if (!uploadForm.file) {
    ElMessage.warning('请选择上传文件')
    return
  }

  const formData = new FormData()
  formData.append('classId', String(route.params.classId))
  formData.append('title', uploadForm.title.trim())
  formData.append('file', uploadForm.file)

  actionLoading.value = true
  try {
    await uploadResourceApi(formData)
    ElMessage.success('资料上传成功')
    uploadDialogVisible.value = false
    resetUploadForm()
    await loadResources()
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadClassDetail().catch(() => {})
  loadResources().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>资料库</h2>
        <p>{{ isAdmin ? '可浏览已有资料，也可直接上传新的课程资料。' : '浏览教师上传的课程资料，点击链接即可下载。' }}</p>
      </div>
      <div class="page-header__actions">
        <el-button v-if="isAdmin" :icon="Plus" type="primary" @click="uploadDialogVisible = true">上传资料</el-button>
        <el-button :icon="RefreshRight" @click="loadResources">刷新资料</el-button>
      </div>
    </header>

    <el-skeleton :loading="loading" animated :rows="6">
      <template #default>
        <el-empty v-if="resourceList.length === 0" description="当前暂无资料" />

        <el-table v-else :data="resourceList" class="page-card" border>
          <el-table-column prop="title" label="资料标题" min-width="180" />
          <el-table-column prop="fileName" label="文件名" min-width="200" />
          <el-table-column prop="uploadTime" label="上传时间" min-width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-link :href="row.downloadUrl" type="primary" target="_blank">下载</el-link>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-skeleton>

    <el-dialog v-model="uploadDialogVisible" title="上传课程资料" width="520px" @closed="resetUploadForm">
      <el-form label-position="top">
        <el-form-item label="资料标题">
          <el-input v-model="uploadForm.title" maxlength="80" placeholder="例如：第 3 章课件" />
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            :auto-upload="false"
            :show-file-list="true"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="resetUploadForm"
          >
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitUpload">确认上传</el-button>
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
</style>
