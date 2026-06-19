import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '@/router'
import { useUserStore } from '@/stores/user'

/**
 * 全局登录弹框：未登录用户在任意页面触发交易动作时就地弹登录，
 * 登录成功后执行原动作或跳转原目标，无需离开当前页。
 */
export const useAuthDialogStore = defineStore('authDialog', () => {
  const visible = ref(false)
  let successCb: (() => void) | null = null
  let redirectTo: string | null = null

  function open(opts?: { onSuccess?: () => void; redirect?: string }) {
    successCb = opts?.onSuccess || null
    redirectTo = opts?.redirect || null
    visible.value = true
  }

  function close() {
    visible.value = false
    successCb = null
    redirectTo = null
  }

  function handleSuccess() {
    visible.value = false
    const cb = successCb
    const redirect = redirectTo
    successCb = null
    redirectTo = null
    if (redirect) {
      router.push(redirect)
    } else if (cb) {
      cb()
    } else {
      // 无指定目标时，默认进入拼团商城
      router.push('/goods')
    }
  }

  /**
   * 需登录守卫：已登录直接执行 action；未登录弹登录，成功后执行 action。
   */
  function requireLogin(action: () => void) {
    const userStore = useUserStore()
    if (userStore.isLogin) {
      action()
    } else {
      open({ onSuccess: action })
    }
  }

  return { visible, open, close, handleSuccess, requireLogin }
})
