<template>
  <div class="multi-image-upload">
    <n-upload
      :action="uploadUrl"
      :headers="headers"
      list-type="image-card"
      :file-list="fileList"
      @finish="handleFinish"
      @remove="handleRemove"
      accept="image/*"
      multiple
    >
      上传图片
    </n-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { type UploadFileInfo } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const userStore = useUserStore()
const uploadUrl = '/api/sys/file/upload/image'
const headers = computed(() => ({ Authorization: userStore.token || '' }))

const fileList = ref<UploadFileInfo[]>([])

// modelValue（逗号分隔 URL）→ 回显
watch(
  () => props.modelValue,
  (val) => {
    const urls = (val || '').split(',').map((s) => s.trim()).filter(Boolean)
    // 仅在与当前展示不一致时重建，避免上传过程中被覆盖
    const cur = fileList.value.map((f) => f.url).filter(Boolean).join(',')
    if (urls.join(',') !== cur) {
      fileList.value = urls.map((u, i) => ({
        id: 'img-' + i,
        name: 'image-' + i,
        status: 'finished' as const,
        url: u
      }))
    }
  },
  { immediate: true }
)

function syncModel() {
  const urls = fileList.value
    .filter((f) => f.status === 'finished' && f.url)
    .map((f) => f.url as string)
  emit('update:modelValue', urls.join(','))
}

function handleFinish({ file, event }: { file: UploadFileInfo; event?: ProgressEvent }) {
  try {
    const response = JSON.parse((event?.target as XMLHttpRequest).response)
    if ((response.code === 0 || response.code === 200) && response.data) {
      const data = response.data as { url?: string; filePath?: string }
      const url = data.url || data.filePath
      if (url) {
        file.url = url
        file.status = 'finished'
        syncModel()
      }
    }
  } catch {
    window.$message?.error('图片上传失败')
  }
  return file
}

function handleRemove({ file }: { file: UploadFileInfo }) {
  fileList.value = fileList.value.filter((f) => f.id !== file.id)
  syncModel()
  return true
}
</script>

<style scoped>
.multi-image-upload {
  width: 100%;
}
:deep(.n-upload-file-list .n-upload-file.n-upload-file--image-card-type),
:deep(.n-upload-trigger.n-upload-trigger--image-card) {
  width: 100px;
  height: 100px;
}
</style>
