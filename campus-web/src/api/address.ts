import { request } from '@/utils/request'

export interface UserAddress {
  id?: number
  receiver: string
  phone: string
  province?: string
  city?: string
  district?: string
  detail: string
  isDefault?: number
}

export const addressApi = {
  list(): Promise<UserAddress[]> {
    return request({ url: '/web/address/list', method: 'get' })
  },
  add(data: UserAddress): Promise<number> {
    return request({ url: '/web/address', method: 'post', data })
  },
  update(data: UserAddress): Promise<number> {
    return request({ url: '/web/address', method: 'put', data })
  },
  remove(id: number): Promise<void> {
    return request({ url: `/web/address/${id}`, method: 'delete' })
  },
  setDefault(id: number): Promise<void> {
    return request({ url: `/web/address/default/${id}`, method: 'put' })
  }
}
