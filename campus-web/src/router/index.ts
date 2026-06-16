import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { public: true }
  },
  {
    // 旧登录路由：官网首页弹窗登录后已无独立登录页，统一回首页
    path: '/login',
    redirect: '/'
  },
  {
    path: '/app',
    component: () => import('@/layout/DefaultLayout.vue'),
    children: [
      {
        path: '/goods',
        name: 'GoodsList',
        component: () => import('@/views/GoodsList.vue')
      },
      {
        path: '/goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/GoodsDetail.vue')
      },
      {
        path: '/activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/ActivityDetail.vue')
      },
      {
        path: '/my-orders',
        name: 'MyOrders',
        component: () => import('@/views/MyOrders.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('campus-web-user')
  const hasToken = token && JSON.parse(token)?.token
  if (!to.meta.public && !hasToken) {
    return { path: '/', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
