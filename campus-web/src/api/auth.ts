import { request } from '@/utils/request'
import { encryptPasswordFields } from '@/utils/crypto'

export interface LoginParams {
  username: string
  password: string
  uuid?: string
  code?: string
  loginType?: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  email?: string
  phone?: string
  userType?: string
  status?: number
}

export interface CaptchaResult {
  uuid: string
  img: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string
}

export interface UserInfoResult {
  user: UserInfo
  roles: string[]
  permissions: string[]
}

export const authApi = {
  // 图片验证码（与后台共用 /auth/captcha，已在 SaToken 放行）
  getCaptcha(): Promise<CaptchaResult> {
    return request({ url: '/auth/captcha', method: 'get' })
  },

  // PC 端登录（自动 RSA 加密 password）
  async login(data: LoginParams): Promise<LoginResult> {
    const payload = await encryptPasswordFields({ loginType: 'PASSWORD', ...data }, ['password'])
    return request({ url: '/web/auth/login', method: 'post', data: payload })
  },

  logout(): Promise<void> {
    return request({ url: '/web/auth/logout', method: 'post' })
  },

  getInfo(): Promise<UserInfoResult> {
    return request({ url: '/web/auth/info', method: 'get' })
  }
}
