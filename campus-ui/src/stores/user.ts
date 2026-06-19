import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginParams, type UserInfo, type MenuInfo } from '@/api/auth'
import router, { resetRouter, addDynamicRoutes } from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null)
  const user = ref<UserInfo | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<MenuInfo[]>([])
  
  // 计算属性
  const isLogin = computed(() => !!token.value)
  const nickname = computed(() => user.value?.nickname || user.value?.username || '')
  const avatar = computed(() => user.value?.avatar || '')
  
  // 登录
  async function login(params: LoginParams) {
    const res = await authApi.login(params)
    token.value = res.token
    user.value = res.user
    // 登录成功后立即获取用户信息（包含菜单、权限等）
    await getInfo()
    // 注册动态路由：守卫只在 user 为空时注册，而 getInfo 已填充 user，
    // 故登录态下需主动注册，否则业务菜单（如拼团）的动态路由缺失会 404
    addDynamicRoutes(menus.value)
    return res
  }
  
  // 获取用户信息
  async function getInfo() {
    const res = await authApi.getInfo()
    user.value = res.user
    roles.value = res.roles
    permissions.value = res.permissions
    menus.value = res.menus
    return res
  }
  
  // 退出登录
  async function logout() {
    // 先清除本地状态，再发送请求（避免 logout 请求带过期 token 触发 401 循环）
    const hadToken = !!token.value
    token.value = null
    user.value = null
    roles.value = []
    permissions.value = []
    menus.value = []
    
    // 重置路由
    resetRouter()
    
    // 只有之前有 token 时才发送 logout 请求
    if (hadToken) {
      try {
        await authApi.logout()
      } catch (error) {
        // 忽略错误
      }
    }
    
    router.push('/login')
  }
  
  // 检查权限
  function hasPermission(permission: string): boolean {
    if (!roles.value || !permissions.value) return false
    if (roles.value.includes('admin')) return true
    return permissions.value.includes(permission)
  }
  
  // 检查角色
  function hasRole(role: string): boolean {
    if (!roles.value) return false
    return roles.value.includes(role)
  }
  
  return {
    token,
    user,
    roles,
    permissions,
    menus,
    isLogin,
    nickname,
    avatar,
    login,
    getInfo,
    logout,
    hasPermission,
    hasRole
  }
}, {
  persist: {
    key: 'campus-user',
    paths: ['token']
  }
})
