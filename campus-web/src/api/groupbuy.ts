import { request } from '@/utils/request'

// ===== 实体类型（对齐后端 entity）=====

export interface GbGoods {
  id: number
  title: string
  cover: string
  images: string
  detail: string
  category: string
  status: number
  minPrice?: number
}

export interface GoodsPageResult {
  list: GbGoods[]
  total: number
}

export interface GbSku {
  id: number
  goodsId: number
  skuName: string
  originalPrice: number
  stock: number
  status: number
}

export interface GbActivity {
  id: number
  goodsId: number
  skuId: number
  activityName: string
  targetCount: number
  validStart: string
  validEnd: string
  timeLimitMinutes: number
  status: number // 0未开始 1进行中 2已结束
}

export interface GbDiscount {
  id: number
  activityId: number
  discountType: number // 1直减 2折扣 3N元购
  discountValue: number
  crowdTag: string
  priority: number
}

export interface GbTeam {
  id: number
  activityId: number
  leaderId: number
  targetCount: number
  lockCount: number
  completeCount: number
  status: number // 0拼团中 1成功 2失败
  validEndTime: string
}

export interface GbOrder {
  id: number
  teamId: number
  activityId: number
  skuId: number
  userId: number
  outTradeNo: string
  originalAmount: number
  deductionAmount: number
  payAmount: number
  status: number // 0锁定 1已支付 2成团 3退款
  payTime: string
  receiver?: string
  phone?: string
  address?: string
}

export interface GoodsDetailVO {
  goods: GbGoods
  skus: GbSku[]
}

export interface ActivityDetailVO {
  activity: GbActivity
  discounts: GbDiscount[]
  joinableTeams: GbTeam[]
}

export interface TrialResult {
  activityId: number
  skuId: number
  originalPrice: number
  deductionAmount: number
  payPrice: number
  targetCount: number
}

export interface LockOrderResult {
  orderId: number
  teamId: number
  outTradeNo: string
  payParams: Record<string, string>
}

// ===== 接口 =====

export const groupbuyApi = {
  // 公开
  goodsList(title?: string): Promise<GbGoods[]> {
    return request({ url: '/web/groupbuy/goods', method: 'get', params: { title } })
  },
  goodsPage(params: {
    page: number
    pageSize: number
    title?: string
    category?: string
    sort?: string
  }): Promise<GoodsPageResult> {
    return request({ url: '/web/groupbuy/goods/page', method: 'get', params })
  },
  goodsCategories(): Promise<string[]> {
    return request({ url: '/web/groupbuy/goods/categories', method: 'get' })
  },
  goodsDetail(id: number): Promise<GoodsDetailVO> {
    return request({ url: `/web/groupbuy/goods/${id}`, method: 'get' })
  },
  goodsActivities(goodsId: number): Promise<GbActivity[]> {
    return request({ url: `/web/groupbuy/goods/${goodsId}/activities`, method: 'get' })
  },
  activityDetail(id: number): Promise<ActivityDetailVO> {
    return request({ url: `/web/groupbuy/activity/${id}`, method: 'get' })
  },

  // 需登录
  trial(activityId: number): Promise<TrialResult> {
    return request({ url: '/web/groupbuy/trial', method: 'get', params: { activityId } })
  },
  lock(data: { activityId: number; teamId?: number; addressId: number }): Promise<LockOrderResult> {
    return request({ url: '/web/groupbuy/lock', method: 'post', data })
  },
  payCallback(outTradeNo: string): Promise<void> {
    return request({ url: '/web/groupbuy/pay-callback', method: 'post', data: { outTradeNo } })
  },
  myOrders(): Promise<GbOrder[]> {
    return request({ url: '/web/groupbuy/my-orders', method: 'get' })
  },
  orderDetail(id: number): Promise<OrderDetailVO> {
    return request({ url: `/web/groupbuy/order/${id}`, method: 'get' })
  },
  cancelOrder(id: number, reason: string): Promise<void> {
    return request({ url: `/web/groupbuy/order/${id}/cancel`, method: 'post', data: { reason } })
  },
  teamProgress(id: number): Promise<GbTeam> {
    return request({ url: `/web/groupbuy/team/${id}`, method: 'get' })
  }
}

export interface OrderDetailVO {
  order: GbOrder
  goods: GbGoods | null
  activity: GbActivity | null
  team: GbTeam | null
}
