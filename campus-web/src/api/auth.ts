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
  },

  getProfile(): Promise<UserInfo> {
    return request({ url: '/web/auth/profile', method: 'get' })
  },

  updateProfile(data: Partial<UserInfo>): Promise<void> {
    return request({ url: '/web/auth/profile', method: 'put', data })
  },

  // 修改密码（旧/新密码均 RSA 加密）
  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    const payload = await encryptPasswordFields(
      { oldPassword, newPassword },
      ['oldPassword', 'newPassword']
    )
    return request({ url: '/web/auth/password', method: 'post', data: payload })
  },

  // 上传头像，返回图片 URL
  uploadAvatar(file: File): Promise<string> {
    const form = new FormData()
    form.append('file', file)
    return request({
      url: '/web/auth/avatar',
      method: 'post',
      data: form,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
