import { request } from '@/utils/request'

// ===== 类型定义 =====
export interface GbGoods {
  id?: number
  title?: string
  subtitle?: string
  cover?: string
  images?: string
  detail?: string
  marketPrice?: number
  sales?: number
  sort?: number
  publishTime?: string
  category?: string
  status?: number
  createTime?: string
  minPrice?: number
}

export interface GbSku {
  id?: number
  goodsId?: number
  skuName?: string
  skuImage?: string
  originalPrice?: number
  stock?: number
  status?: number
}

export interface GbActivity {
  id?: number
  goodsId?: number
  skuId?: number
  activityName?: string
  targetCount?: number
  validStart?: string
  validEnd?: string
  timeLimitMinutes?: number
  status?: number
  createTime?: string
}

export interface GbDiscount {
  id?: number
  activityId?: number
  discountType?: number
  discountValue?: number
  crowdTag?: string
  priority?: number
}

export interface GbTeam {
  id?: number
  activityId?: number
  leaderId?: number
  targetCount?: number
  lockCount?: number
  completeCount?: number
  status?: number
  validEndTime?: string
  createTime?: string
}

export interface GbOrder {
  id?: number
  teamId?: number
  activityId?: number
  skuId?: number
  userId?: number
  outTradeNo?: string
  originalAmount?: number
  deductionAmount?: number
  payAmount?: number
  status?: number
  payTime?: string
  createTime?: string
}

// ===== 商品 API =====
export const gbGoodsApi = {
  page(params: {
    page: number
    pageSize: number
    title?: string
    status?: number
    minPrice?: number
    maxPrice?: number
    lowStock?: boolean
    stockThreshold?: number
  }) {
    return request<{ list: GbGoods[]; total: number }>({ url: '/biz/gbGoods/page', method: 'get', params })
  },
  detail(id: number) {
    return request<{ goods: GbGoods; skus: GbSku[] }>({ url: `/biz/gbGoods/${id}`, method: 'get' })
  },
  add(data: { goods: GbGoods; skus: GbSku[] }) {
    return request<number>({ url: '/biz/gbGoods', method: 'post', data })
  },
  edit(data: { goods: GbGoods; skus: GbSku[] }) {
    return request<number>({ url: '/biz/gbGoods', method: 'put', data })
  },
  changeStatus(id: number, status: number) {
    return request({ url: '/biz/gbGoods/status', method: 'put', data: { id, status } })
  },
  changeStatusBatch(ids: number[], status: number) {
    return request({ url: '/biz/gbGoods/status/batch', method: 'put', data: { ids, status } })
  },
  delete(id: number) {
    return request({ url: `/biz/gbGoods/${id}`, method: 'delete' })
  }
}

// ===== 活动 API =====
export const gbActivityApi = {
  page(params: { page: number; pageSize: number; goodsId?: number; status?: number }) {
    return request<{ list: GbActivity[]; total: number }>({ url: '/biz/gbActivity/page', method: 'get', params })
  },
  detail(id: number) {
    return request<{ activity: GbActivity; discounts: GbDiscount[] }>({ url: `/biz/gbActivity/${id}`, method: 'get' })
  },
  add(data: { activity: GbActivity; discounts: GbDiscount[] }) {
    return request<number>({ url: '/biz/gbActivity', method: 'post', data })
  },
  edit(data: { activity: GbActivity; discounts: GbDiscount[] }) {
    return request<number>({ url: '/biz/gbActivity', method: 'put', data })
  },
  changeStatus(id: number, status: number) {
    return request({ url: '/biz/gbActivity/status', method: 'put', data: { id, status } })
  },
  delete(id: number) {
    return request({ url: `/biz/gbActivity/${id}`, method: 'delete' })
  }
}

// ===== 拼单/组队监控 API =====
export const gbOrderApi = {
  orderPage(params: { page: number; pageSize: number; teamId?: number; userId?: number; status?: number }) {
    return request<{ list: GbOrder[]; total: number }>({ url: '/biz/gbOrder/page', method: 'get', params })
  },
  teamPage(params: { page: number; pageSize: number; activityId?: number; status?: number }) {
    return request<{ list: GbTeam[]; total: number }>({ url: '/biz/gbOrder/team/page', method: 'get', params })
  }
}
