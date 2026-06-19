<template>
  <div class="rich-editor" style="border: 1px solid #e0e0e6; border-radius: 4px">
    <Toolbar
      :editor="editorRef"
      :default-config="toolbarConfig"
      mode="default"
      style="border-bottom: 1px solid #e0e0e6"
    />
    <Editor
      v-model="valueHtml"
      :default-config="editorConfig"
      mode="default"
      style="height: 360px; overflow-y: hidden"
      @on-created="handleCreated"
      @on-change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, watch, onBeforeUnmount } from 'vue'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor } from '@wangeditor/editor'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ value?: string }>()
const emit = defineEmits<{ (e: 'update:value', v: string): void }>()

const userStore = useUserStore()

// editor 实例用 shallowRef（官方要求，避免深响应式破坏实例）
const editorRef = shallowRef<IDomEditor>()
const valueHtml = ref(props.value || '')

// 外部 value 变化时回显（避免与内部输入互相覆盖）
watch(
  () => props.value,
  (v) => {
    if (v !== valueHtml.value) valueHtml.value = v || ''
  }
)

const toolbarConfig = {
  excludeKeys: ['group-video', 'fullScreen']
}

const editorConfig = {
  placeholder: '请输入商品图文详情…',
  MENU_CONF: {
    uploadImage: {
      // 自定义上传，复用后台文件上传接口（与 ImageUpload 一致：Authorization 原样 token，取 data.url）
      async customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
        const fd = new FormData()
        fd.append('file', file)
        try {
          const res = await fetch('/api/sys/file/upload/image', {
            method: 'POST',
            headers: { Authorization: userStore.token || '' },
            body: fd
          }).then((r) => r.json())
          if ((res.code === 0 || res.code === 200) && res.data?.url) {
            insertFn(res.data.url, '', res.data.url)
          } else {
            window.$message?.error('图片上传失败')
          }
        } catch {
          window.$message?.error('图片上传失败')
        }
      }
    }
  }
}

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
}

function handleChange(editor: IDomEditor) {
  emit('update:value', editor.getHtml())
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>
