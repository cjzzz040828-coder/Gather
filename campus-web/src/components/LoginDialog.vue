<template>
  <el-dialog
    :model-value="modelValue"
    width="400px"
    :show-close="true"
    align-center
    class="login-dialog"
    modal-class="login-overlay"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="loadCaptcha"
  >
    <div class="dlg-head">
      <span class="wordmark">GATHER</span>
      <span class="dlg-cn">集萃</span>
    </div>
    <h3 class="dlg-title">欢迎登录</h3>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      size="large"
      @keyup.enter="onSubmit"
    >
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="请输入密码"
          :prefix-icon="Lock"
        />
      </el-form-item>
      <el-form-item v-if="captcha.img" prop="code">
        <div class="captcha-row">
          <el-input v-model="form.code" placeholder="验证码" :prefix-icon="Key" />
          <img :src="captcha.img" class="captcha-img" title="点击刷新" @click="loadCaptcha" />
        </div>
      </el-form-item>
      <el-button
        size="large"
        :loading="loading"
        class="submit-btn"
        @click="onSubmit"
      >
        登 录
      </el-button>
    </el-form>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [v: boolean]
  success: []
}>()

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '', uuid: '', code: '' })
const captcha = reactive({ img: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function loadCaptcha() {
  try {
    const res = await authApi.getCaptcha()
    captcha.img = res.img
    form.uuid = res.uuid
    form.code = ''
  } catch {
    captcha.img = ''
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
      uuid: form.uuid || undefined,
      code: form.code || undefined
    })
    ElMessage.success('登录成功')
    emit('update:modelValue', false)
    emit('success')
  } catch {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 弹窗外壳皮肤（毛玻璃/定位/输入框对比度）已移至全局 theme.css，
   因为 el-dialog teleport 到 body，scoped :deep 匹配不到。
   以下为弹窗 body 内部元素，scoped 可正常生效。 */
.dlg-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
.dlg-head .wordmark {
  display: flex;
  align-items: center;
  align-self: stretch;
  font-family: 'Arial Narrow', 'Helvetica Neue', Arial, sans-serif;
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 8px;
  color: #fff;
  text-transform: uppercase;
  padding-left: 4px;
  user-select: none;
}
.dlg-head .dlg-cn {
  display: flex;
  align-items: center;
  align-self: stretch;
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 3px;
  color: #fff;
  padding-left: 12px;
  border-left: 1px solid rgba(255, 255, 255, 0.45);
  user-select: none;
}
.dlg-title {
  font-size: var(--fz-xl);
  font-weight: 700;
  color: #fff;
  margin-bottom: var(--sp-5);
}

.captcha-row {
  display: flex;
  gap: var(--sp-3);
  width: 100%;
}
.captcha-img {
  height: 40px;
  width: 120px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: var(--radius);
}
.submit-btn {
  width: 100%;
  margin-top: var(--sp-2);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: #fff;
  font-weight: 700;
  letter-spacing: 4px;
  transition: all 0.2s;
}
.submit-btn:hover,
.submit-btn:focus {
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.7);
  color: #fff;
}
</style>
