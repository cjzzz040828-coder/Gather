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
        component: () => import('@/views/GoodsList.vue'),
        meta: { public: true }
      },
      {
        path: '/goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/GoodsDetail.vue'),
        meta: { public: true }
      },
      {
        path: '/activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/ActivityDetail.vue'),
        meta: { public: true }
      },
      {
        path: '/my-orders',
        name: 'MyOrders',
        component: () => import('@/views/MyOrders.vue')
      },
      {
        path: '/order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetail.vue')
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: '/address',
        name: 'Address',
        component: () => import('@/views/Address.vue')
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
    // 需登录页未登录：不跳走，停在当前页并就地弹登录，成功后再去目标
    import('@/stores/authDialog').then(({ useAuthDialogStore }) => {
      useAuthDialogStore().open({ redirect: to.fullPath })
    })
    return false
  }
  return true
})

export default router
