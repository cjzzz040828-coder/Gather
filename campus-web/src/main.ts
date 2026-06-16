import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus, { ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/theme.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

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
app.mount('#app')
