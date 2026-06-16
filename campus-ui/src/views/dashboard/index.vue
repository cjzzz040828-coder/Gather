<template>
  <div class="page-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <!-- 左侧欢迎信息 -->
      <div class="welcome-info">
        <div class="welcome-header">
          <n-avatar round :size="56" :src="userStore.avatar || undefined">
            {{ userStore.nickname?.charAt(0) || 'U' }}
          </n-avatar>
          <div class="welcome-text">
            <h1 class="welcome-title">
              {{ getGreeting() }}，{{ userStore.nickname }} 👋
            </h1>
            <p class="welcome-desc">
              这是您的管理控制台，您可以在这里管理系统的各项功能
            </p>
          </div>
        </div>
        <div class="welcome-time">
          <div class="time-display">{{ currentTime }}</div>
          <div class="date-display">{{ currentDate }}</div>
        </div>
      </div>
      <!-- 右侧轮播Banner -->
      <div class="welcome-banner">
        <n-carousel autoplay :interval="5000" dot-type="line" show-arrow="hover" class="banner-carousel">
          <div v-for="(banner, index) in banners" :key="index" class="banner-item"
               :style="{ background: banner.bgColor }">
            <div class="banner-content">
              <div class="banner-text">
                <h3 class="banner-title">{{ banner.title }}</h3>
                <p class="banner-subtitle">{{ banner.subtitle }}</p>
              </div>
              <div class="banner-icon">
                <n-icon :size="64" :color="banner.iconColor">
                  <component :is="banner.icon"/>
                </n-icon>
              </div>
            </div>
          </div>
        </n-carousel>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <n-card v-for="stat in stats" :key="stat.title" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <n-icon size="24" :color="stat.color">
              <component :is="stat.icon"/>
            </n-icon>
          </div>
          <div class="stat-info">
            <n-skeleton v-if="loading" :width="60" :height="28"/>
            <div v-else class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 中间区域：快捷入口 -->
    <div class="middle-section">
      <n-card title="快捷入口" class="shortcuts-card">
        <div class="shortcuts-grid">
          <div
              v-for="shortcut in shortcuts"
              :key="shortcut.path"
              class="shortcut-item"
              @click="router.push(shortcut.path)"
          >
            <div class="shortcut-icon" :style="{ background: shortcut.bgColor }">
              <n-icon size="24" :color="shortcut.color">
                <component :is="shortcut.icon"/>
              </n-icon>
            </div>
            <div class="shortcut-name">{{ shortcut.name }}</div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 底部区域：系统信息 -->
    <div class="bottom-section">
      <n-card title="系统信息" class="system-card">
        <n-descriptions :column="1" label-placement="left">
          <n-descriptions-item label="系统名称">Campus Admin</n-descriptions-item>
          <n-descriptions-item label="系统版本">v1.0.7</n-descriptions-item>
          <n-descriptions-item label="前端框架">Vue 3.4 + Naive UI</n-descriptions-item>
          <n-descriptions-item label="后端框架">Spring Boot 3.2</n-descriptions-item>
          <n-descriptions-item label="数据库">MySQL 8.0</n-descriptions-item>
          <n-descriptions-item label="缓存">Redis 7.0</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted, markRaw} from 'vue'
import {useRouter} from 'vue-router'
import {
  PersonOutline,
  PeopleOutline,
  MenuOutline,
  ShieldCheckmarkOutline,
  SettingsOutline,
  TimerOutline,
  ServerOutline,
  RocketOutline,
  CodeSlashOutline,
  CloudOutline
} from '@vicons/ionicons5'
import {useUserStore} from '@/stores/user'
import {dashboardApi} from '@/api/system'

const router = useRouter()
const userStore = useUserStore()

const currentTime = ref('')
const currentDate = ref('')
const loading = ref(true)

// 获取问候语
function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

// 轮播Banner数据
const banners = [
  {
    title: 'Campus Admin',
    subtitle: '现代化后台管理系统',
    bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    icon: markRaw(RocketOutline),
    iconColor: 'rgba(255,255,255,0.3)'
  },
  {
    title: '技术栈',
    subtitle: 'Spring Boot 3 + Vue 3',
    bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    icon: markRaw(CodeSlashOutline),
    iconColor: 'rgba(255,255,255,0.3)'
  },
  {
    title: '云端部署',
    subtitle: '支持 Docker 一键部署',
    bgColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    icon: markRaw(CloudOutline),
    iconColor: 'rgba(255,255,255,0.3)'
  }
]

// 统计数据
const stats = ref([
  {
    title: '用户总数',
    value: 0,
    icon: markRaw(PersonOutline),
    color: '#111827',
    bgColor: '#F3F4F6'
  },
  {
    title: '角色数量',
    value: 0,
    icon: markRaw(PeopleOutline),
    color: '#059669',
    bgColor: '#D1FAE5'
  },
  {
    title: '菜单数量',
    value: 0,
    icon: markRaw(MenuOutline),
    color: '#2563EB',
    bgColor: '#DBEAFE'
  },
  {
    title: '权限数量',
    value: 0,
    icon: markRaw(ShieldCheckmarkOutline),
    color: '#D97706',
    bgColor: '#FEF3C7'
  }
])

// 快捷入口
const shortcuts = [
  {
    name: '用户管理',
    path: '/system/user',
    icon: markRaw(PersonOutline),
    color: '#111827',
    bgColor: '#F3F4F6'
  },
  {
    name: '角色管理',
    path: '/system/role',
    icon: markRaw(PeopleOutline),
    color: '#059669',
    bgColor: '#D1FAE5'
  },
  {
    name: '菜单管理',
    path: '/system/menu',
    icon: markRaw(MenuOutline),
    color: '#2563EB',
    bgColor: '#DBEAFE'
  },
  {
    name: '系统配置',
    path: '/system/config',
    icon: markRaw(SettingsOutline),
    color: '#7C3AED',
    bgColor: '#EDE9FE'
  },
  {
    name: '定时任务',
    path: '/monitor/job',
    icon: markRaw(TimerOutline),
    color: '#DC2626',
    bgColor: '#FEE2E2'
  },
  {
    name: '服务监控',
    path: '/monitor/server',
    icon: markRaw(ServerOutline),
    color: '#0891B2',
    bgColor: '#CFFAFE'
  }
]

// 加载统计数据
async function loadStats() {
  try {
    loading.value = true
    const data = await dashboardApi.getStats()
    stats.value[0].value = data.userCount
    stats.value[1].value = data.roleCount
    stats.value[2].value = data.menuCount
    stats.value[3].value = data.permissionCount
  } catch (error) {
    console.error('加载统计数据失败', error)
  } finally {
    loading.value = false
  }
}

// 更新时间
function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {hour12: false})
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

let timer: number
onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
  loadStats()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style lang="scss" scoped>
// 欢迎区域
.welcome-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.welcome-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 6px 0;
}

.welcome-desc {
  font-size: 14px;
  color: #6B7280;
  margin: 0;
}

.welcome-time {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 16px;
}

.time-display {
  font-size: 36px;
  font-weight: 700;
  color: #111827;
  font-variant-numeric: tabular-nums;
}

.date-display {
  font-size: 14px;
  color: #6B7280;
}

// 轮播Banner
.welcome-banner {
  width: 380px;
  flex-shrink: 0;
}

.banner-carousel {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;

  :deep(.n-carousel__slides) {
    height: 100%;
  }

  :deep(.n-carousel__slide) {
    height: 100%;
  }

  :deep(.n-carousel__dots) {
    bottom: 12px;
  }

  :deep(.n-carousel__dot) {
    background: rgba(255, 255, 255, 0.5);

    &.n-carousel__dot--active {
      background: #fff;
    }
  }

  :deep(.n-carousel__arrow) {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.banner-item {
  height: 100%;
  min-height: 140px;
  padding: 24px;
  display: flex;
  align-items: center;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px 0;
}

.banner-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.banner-icon {
  opacity: 0.6;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  :deep(.n-card__content) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

.stat-title {
  font-size: 14px;
  color: #6B7280;
  margin-top: 4px;
}

.middle-section {
  margin-bottom: 20px;
}

.shortcuts-card {
  height: 100%;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #F3F4F6;
  }
}

.shortcut-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.shortcut-name {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.bottom-section {
  margin-bottom: 20px;
}

.system-card {
  height: 100%;

  :deep(.n-descriptions) {
    --n-th-padding: 10px 12px;
    --n-td-padding: 10px 12px;
  }
}

@media (max-width: 1200px) {
  .welcome-section {
    flex-direction: column;
  }

  .welcome-banner {
    width: 100%;
  }

  .banner-item {
    min-height: 120px;
  }

  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }

}

@media (max-width: 768px) {
  .welcome-header {
    flex-direction: column;
    text-align: center;
  }

  .welcome-time {
    flex-direction: column;
    align-items: center;
    gap: 4px;
  }

  .stat-cards {
    grid-template-columns: 1fr;
  }

  .shortcuts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

</style>
