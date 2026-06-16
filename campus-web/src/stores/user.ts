import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginParams, type UserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string | null>(null)
    const user = ref<UserInfo | null>(null)
    const roles = ref<string[]>([])

    const isLogin = computed(() => !!token.value)
    const nickname = computed(() => user.value?.nickname || user.value?.username || '')
    const avatar = computed(() => user.value?.avatar || '')

    async function login(params: LoginParams) {
      const res = await authApi.login(params)
      token.value = res.token
      await getInfo()
      return res
    }

    async function getInfo() {
      const res = await authApi.getInfo()
      user.value = res.user
      roles.value = res.roles
      return res
    }

    async function logout() {
      const hadToken = !!token.value
      token.value = null
      user.value = null
      roles.value = []

      if (hadToken) {
        try {
          await authApi.logout()
        } catch {
          // 忽略
        }
      }
      router.push('/login')
    }

    return { token, user, roles, isLogin, nickname, avatar, login, getInfo, logout }
  },
  {
    persist: {
      key: 'campus-web-user',
      paths: ['token']
    }
  }
)
