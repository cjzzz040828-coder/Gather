import JSEncrypt from 'jsencrypt'
import { fetchCryptoConfig, clearCryptoConfigCache } from './request'

interface CryptoConfig {
  enabled: boolean
  publicKey: string
}

let cryptoConfig: CryptoConfig | null = null

export async function getCryptoConfig(): Promise<CryptoConfig> {
  if (cryptoConfig) {
    return cryptoConfig
  }
  cryptoConfig = await fetchCryptoConfig()
  return cryptoConfig
}

export function clearCryptoConfig() {
  cryptoConfig = null
  clearCryptoConfigCache()
}

export function rsaEncrypt(data: string, publicKey: string): string {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(publicKey)
  const encrypted = encrypt.encrypt(data)
  if (!encrypted) {
    throw new Error('RSA加密失败')
  }
  return encrypted
}

export async function encryptPasswordFields<T extends Record<string, any>>(
  data: T,
  fields: string[] = ['password']
): Promise<T> {
  const config = await getCryptoConfig()

  if (!config.enabled || !config.publicKey) {
    return data
  }

  const result: Record<string, any> = { ...data }
  for (const field of fields) {
    if (result[field] && typeof result[field] === 'string') {
      result[field] = rsaEncrypt(result[field], config.publicKey)
    }
  }
  return result as T
}
