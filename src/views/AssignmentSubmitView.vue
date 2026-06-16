<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getAssignmentDetailApi, submitAssignmentApi } from '@/api'
import type { AssignmentItem } from '@/types'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<AssignmentItem | null>(null)
const fileList = ref<{ name: string }[]>([])
const selectedFile = ref<File | null>(null)
const remark = ref('')

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    detail.value = await getAssignmentDetailApi(String(route.params.assignmentId))
  } finally {
    loading.value = false
  }
}

function handleChange(file: { raw?: File; name: string }): void {
  selectedFile.value = file.raw || null
  fileList.value = [{ name: file.name }]
}

function handleRemove(): void {
  selectedFile.value = null
  fileList.value = []
}

async function submitAssignment(): Promise<void> {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择提交文件')
    return
  }

  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('remark', remark.value)

  submitting.value = true
  try {
    await submitAssignmentApi(String(route.params.assignmentId), formData)
    ElMessage.success('作业提交成功')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDetail().catch(() => {})
})
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <h2>作业提交</h2>
        <p>查看作业详情、下载教师附件并提交自己的作业文件。</p>
      </div>
      <el-button @click="loadDetail">刷新详情</el-button>
    </header>

    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <el-card v-if="detail" class="page-card" shadow="never">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
            <el-descriptions-item label="说明">{{ detail.content || '暂无说明' }}</el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ detail.deadline || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="教师附件">
              <el-link v-if="detail.attachmentUrl" :href="detail.attachmentUrl" target="_blank" type="primary">
                下载附件
              </el-link>
              <span v-else>暂无附件</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="upload-panel">
            <el-upload
              :auto-upload="false"
              :limit="1"
              :file-list="fileList"
              :on-change="handleChange"
              :on-remove="handleRemove"
            >
              <el-button type="primary">选择作业文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅提交一个主文件，最终成绩以后端批改结果为准。</div>
              </template>
            </el-upload>
          </div>

          <el-input
            v-model="remark"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="可填写提交备注"
          />

          <div class="upload-panel__submit">
            <el-button type="primary" :loading="submitting" @click="submitAssignment">提交作业</el-button>
          </div>
        </el-card>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.upload-panel {
  margin: 20px 0;
}

.upload-panel__submit {
  margin-top: 18px;
}
</style>
