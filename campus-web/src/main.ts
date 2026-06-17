import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus, { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/theme.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'

// 供 axios 拦截器等非组件上下文使用
window.ElMessage = ElMessage

const app = createApp(App)

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 刷新后若已登录(token 由 persist 恢复)，重新拉取用户信息，避免头像/昵称丢失
const userStore = useUserStore()
if (userStore.token) {
  userStore.getInfo().catch(() => {
    // token 失效：静默清理本地登录态（不强制跳转，避免启动时打断浏览）
    userStore.token = null
    userStore.user = null
  })
}

app.mount('#app')
