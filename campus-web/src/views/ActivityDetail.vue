<template>
  <div v-loading="loading" class="activity-detail">
    <!-- 面包屑 -->
    <div class="crumb-bar">
      <a @click="router.push('/goods')">首页</a>
      <span class="sep">›</span>
      <a @click="router.back()">商品</a>
      <span class="sep">›</span>
      <span class="cur">拼团活动</span>
    </div>

    <template v-if="data">
      <!-- 活动头部：京东红促销条 -->
      <div class="act-hero">
        <div class="act-hero-left">
          <h2 class="act-name">{{ data.activity.activityName }}</h2>
          <div class="act-meta">
            <el-tag :type="statusTag(data.activity.status).type" effect="dark">{{
              statusTag(data.activity.status).text
            }}</el-tag>
            <span class="meta-item">{{ data.activity.targetCount }} 人成团</span>
            <span class="meta-item">单团 {{ data.activity.timeLimitMinutes }} 分钟</span>
          </div>
          <div class="act-time">
            活动时间：{{ fmt(data.activity.validStart) }} 至 {{ fmt(data.activity.validEnd) }}
          </div>
        </div>
        <div class="act-hero-right">
          <div class="trial-amount" v-if="trial">
            <span class="ta-label">我的拼团价</span>
            <span class="price big"><span class="symbol">¥</span>{{ trial.payPrice }}</span>
            <span class="ta-cut">已省 ¥{{ trial.deductionAmount }}</span>
          </div>
          <div class="trial-amount placeholder" v-else>
            <span class="ta-label">点击试算查看你的专属价</span>
          </div>
          <div class="hero-actions">
            <el-button @click="doTrial">价格试算</el-button>
            <el-button type="danger" class="start-btn" :loading="locking" @click="startTeam">发起拼团</el-button>
          </div>
        </div>
      </div>

      <div class="layout-2col">
        <!-- 左：可参与的团 -->
        <div class="col-main">
          <!-- 被邀请提示 -->
          <el-alert
            v-if="invitedTeamId"
            class="invite-banner"
            type="error"
            :closable="false"
            show-icon
          >
            <template #title>
              好友邀请你加入拼团 #{{ invitedTeamId }}，点下方「去参团」一起拼
            </template>
          </el-alert>

          <div class="section-title"><span>正在拼（{{ data.joinableTeams.length }}）</span></div>
          <div class="teams-card">
            <el-empty
              v-if="data.joinableTeams.length === 0"
              description="还没有人开团，快来当第一个团长吧"
            />
            <div v-else class="team-list">
              <div
                v-for="t in data.joinableTeams"
                :key="t.id"
                class="team-item"
                :class="{ invited: t.id === invitedTeamId }"
              >
                <el-avatar :size="44" class="team-avatar">团</el-avatar>
                <div class="team-info">
                  <div class="team-line">
                    <span class="team-id">
                      拼团 #{{ t.id }}
                      <el-tag v-if="t.id === invitedTeamId" type="danger" size="small" effect="dark">好友的团</el-tag>
                    </span>
                    <span class="team-count">还差 {{ t.targetCount - t.lockCount }} 人成团</span>
                  </div>
                  <div class="team-countdown">
                    <span class="cd-label">剩余</span>
                    <span class="cd-time" :class="{ ended: formatCountdown(t.validEndTime) === '已结束' }">
                      {{ formatCountdown(t.validEndTime) }}
                    </span>
                  </div>
                  <el-progress
                    :percentage="Math.round((t.lockCount / t.targetCount) * 100)"
                    :stroke-width="8"
                    color="#e1251b"
                  />
                </div>
                <div class="team-ops">
                  <el-button type="danger" :loading="locking" @click="joinTeam(t.id)">
                    去参团
                  </el-button>
                  <el-button text size="small" @click="copyShareLink(t.id)">邀请好友</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右：折扣 + 玩法 -->
        <div class="col-side">
          <div class="section-title"><span>优惠</span></div>
          <div class="side-card">
            <div v-for="d in data.discounts" :key="d.id" class="discount-row">
              <span class="dtype">{{ discountTypeText(d.discountType) }}</span>
              <span class="dval">{{ discountValueText(d) }}</span>
              <span class="dcrowd">{{ d.crowdTag || '全部人群' }}</span>
            </div>
            <el-empty v-if="data.discounts.length === 0" description="暂无折扣" :image-size="60" />
          </div>

          <div class="side-card howto">
            <h4>拼团玩法</h4>
            <p>1. 发起或加入一个拼团</p>
            <p>2. 完成支付锁定名额</p>
            <p>3. 凑齐 {{ data.activity.targetCount }} 人即成团</p>
            <p>4. 不成团？自动原路退款</p>
          </div>
        </div>
      </div>
    </template>

    <!-- 收货地址选择对话框 -->
    <el-dialog v-model="addressDialog" title="选择收货地址" width="460px">
      <div class="addr-select">
        <label
          v-for="a in addresses"
          :key="a.id"
          class="addr-opt"
          :class="{ active: selectedAddressId === a.id }"
        >
          <input
            type="radio"
            name="addr"
            :value="a.id"
            :checked="selectedAddressId === a.id"
            @change="selectedAddressId = a.id!"
          />
          <div class="addr-opt-body">
            <div class="addr-opt-l1">
              <b>{{ a.receiver }}</b>
              <span>{{ a.phone }}</span>
              <el-tag v-if="a.isDefault === 1" type="danger" size="small" effect="dark">默认</el-tag>
            </div>
            <div class="addr-opt-l2">
              {{ [a.province, a.city, a.district].filter(Boolean).join(' ') }} {{ a.detail }}
            </div>
          </div>
        </label>
      </div>
      <template #footer>
        <el-button @click="$router.push('/address')">管理地址</el-button>
        <el-button type="danger" :loading="locking" @click="confirmLock">确认，去支付</el-button>
      </template>
    </el-dialog>

    <!-- 支付对话框 -->
    <el-dialog v-model="payDialog" title="模拟支付" width="400px" :close-on-click-modal="false">
      <div class="pay-body">
        <div class="pay-row"><span>订单号</span><b>{{ lockResult?.outTradeNo }}</b></div>
        <div class="pay-row"><span>组队 ID</span><b>#{{ lockResult?.teamId }}</b></div>
        <div class="pay-amount">
          <span>应付金额</span>
          <span class="price big"><span class="symbol">¥</span>{{ trial?.payPrice ?? '-' }}</span>
        </div>
        <el-alert type="warning" :closable="false" title="演示环境：点击确认即模拟支付成功" />
      </div>
      <template #footer>
        <el-button @click="payDialog = false">取消</el-button>
        <el-button type="danger" :loading="paying" @click="doPay">确认支付</el-button>
      </template>
    </el-dialog>
    <!-- 分享邀请对话框 -->
    <el-dialog v-model="shareDialog" title="邀请好友一起拼" width="420px">
      <div v-if="shareTeamId" class="share-body">
        <p class="share-tip">把链接发给好友，TA 打开即可加入你的拼团 #{{ shareTeamId }}，一起凑齐更快成团：</p>
        <div class="share-link-row">
          <el-input :model-value="shareLink(shareTeamId)" readonly />
          <el-button type="danger" @click="copyShareLink(shareTeamId)">复制</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi, type UserAddress } from '@/api/address'
import {
  groupbuyApi,
  type ActivityDetailVO,
  type TrialResult,
  type LockOrderResult,
  type GbDiscount
} from '@/api/groupbuy'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const data = ref<ActivityDetailVO | null>(null)
const trial = ref<TrialResult | null>(null)
const lockResult = ref<LockOrderResult | null>(null)
const loading = ref(false)
const locking = ref(false)
const paying = ref(false)
const payDialog = ref(false)

// 分享邀请：被邀请人通过 ?teamId= 直达的目标团；开团/支付后自己可分享的团
const invitedTeamId = ref<number | null>(
  route.query.teamId ? Number(route.query.teamId) : null
)
const shareTeamId = ref<number | null>(null)
const shareDialog = ref(false)

// 收货地址选择（下单前）
const addresses = ref<UserAddress[]>([])
const addressDialog = ref(false)
const selectedAddressId = ref<number | null>(null)
const pendingTeamId = ref<number | null>(null)

async function loadAddresses() {
  addresses.value = (await addressApi.list()) || []
}

const activityId = () => Number(route.params.id)

// 倒计时：每秒 tick 触发重算，团/订单按各自 validEndTime 计算剩余
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | null = null

function formatCountdown(endTime?: string): string {
  if (!endTime) return ''
  // 后端时间格式 "2026-07-16 22:21:46" 或带 T，统一替换确保跨浏览器可解析
  const end = new Date(endTime.replace('T', ' ').replace(/-/g, '/')).getTime()
  const diff = end - now.value
  if (isNaN(end)) return ''
  if (diff <= 0) return '已结束'
  const h = Math.floor(diff / 3_600_000)
  const m = Math.floor((diff % 3_600_000) / 60_000)
  const s = Math.floor((diff % 60_000) / 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(h)}:${pad(m)}:${pad(s)}`
}

function shareLink(teamId: number) {
  return `${window.location.origin}/activity/${activityId()}?teamId=${teamId}`
}

async function copyShareLink(teamId: number) {
  const link = shareLink(teamId)
  try {
    // 安全上下文(https/localhost)优先用 clipboard API
    if (window.isSecureContext && navigator.clipboard) {
      await navigator.clipboard.writeText(link)
    } else {
      // http+IP 站点降级：execCommand
      const ta = document.createElement('textarea')
      ta.value = link
      ta.style.position = 'fixed'
      ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
    }
    ElMessage.success('邀请链接已复制，发给好友一起拼')
  } catch {
    ElMessage.warning('复制失败，请手动复制链接')
  }
}

function openShare(teamId: number) {
  shareTeamId.value = teamId
  shareDialog.value = true
}

function fmt(s: string) {
  return s ? s.replace('T', ' ').slice(0, 16) : '-'
}

function statusTag(s: number) {
  return (
    {
      0: { text: '未开始', type: 'info' },
      1: { text: '进行中', type: 'success' },
      2: { text: '已结束', type: 'danger' }
    }[s] || { text: '未知', type: 'info' }
  ) as { text: string; type: any }
}

function discountTypeText(t: number) {
  return { 1: '直减', 2: '折扣', 3: 'N元购' }[t] || '其它'
}
function discountValueText(d: GbDiscount) {
  if (d.discountType === 1) return `减 ¥${d.discountValue}`
  if (d.discountType === 2) return `${Number(d.discountValue) > 1 ? d.discountValue + ' 折' : d.discountValue}`
  if (d.discountType === 3) return `¥${d.discountValue} 购`
  return String(d.discountValue)
}

async function load() {
  loading.value = true
  try {
    data.value = await groupbuyApi.activityDetail(activityId())
  } finally {
    loading.value = false
  }
}

function requireLogin(): boolean {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function doTrial() {
  if (!requireLogin()) return
  trial.value = await groupbuyApi.trial(activityId())
}

async function lock(teamId?: number) {
  if (!requireLogin()) return
  // 先选收货地址再锁单
  pendingTeamId.value = teamId ?? null
  await loadAddresses()
  if (addresses.value.length === 0) {
    ElMessageBox.confirm('您还没有收货地址，去添加一个吧', '提示', {
      confirmButtonText: '去添加',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => router.push('/address'))
      .catch(() => null)
    return
  }
  // 默认选中默认地址，否则第一个
  const def = addresses.value.find((a) => a.isDefault === 1)
  selectedAddressId.value = def ? def.id! : addresses.value[0].id!
  addressDialog.value = true
}

// 选定地址后真正锁单
async function confirmLock() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  locking.value = true
  try {
    if (!trial.value) trial.value = await groupbuyApi.trial(activityId())
    lockResult.value = await groupbuyApi.lock({
      activityId: activityId(),
      teamId: pendingTeamId.value ?? undefined,
      addressId: selectedAddressId.value
    })
    addressDialog.value = false
    payDialog.value = true
  } finally {
    locking.value = false
  }
}

const startTeam = () => lock(undefined)
const joinTeam = (teamId: number) => lock(teamId)

async function doPay() {
  if (!lockResult.value) return
  paying.value = true
  try {
    await groupbuyApi.payCallback(lockResult.value.outTradeNo)
    ElMessage.success('支付成功')
    payDialog.value = false
    const paidTeamId = lockResult.value.teamId
    await load()
    const team = await groupbuyApi.teamProgress(paidTeamId)
    if (team.status === 1) {
      ElMessage.success('恭喜，拼团已成功！')
    } else {
      ElMessage.info(`当前进度 ${team.completeCount}/${team.targetCount} 人，邀请好友一起拼更快成团`)
      openShare(paidTeamId)
    }
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  load()
  timer = setInterval(() => (now.value = Date.now()), 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 活动头部：京东红促销条 */
.act-hero {
  background: linear-gradient(120deg, #e1251b, #ff6a00);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  padding: var(--sp-6);
  display: flex;
  justify-content: space-between;
  gap: var(--sp-6);
  color: #fff;
}
.act-name {
  font-size: var(--fz-xl);
  font-weight: 700;
  margin-bottom: var(--sp-3);
}
.act-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-3);
}
.meta-item {
  color: rgba(255, 255, 255, 0.92);
  font-size: var(--fz-sm);
}
.act-time {
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--fz-sm);
}
.act-hero-right {
  flex-shrink: 0;
  text-align: right;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--sp-3);
  min-width: 240px;
}
.trial-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.trial-amount.placeholder {
  color: rgba(255, 255, 255, 0.85);
  font-size: var(--fz-sm);
  padding: var(--sp-3) 0;
}
.ta-label {
  font-size: var(--fz-sm);
  color: rgba(255, 255, 255, 0.85);
}
.act-hero .price.big {
  font-size: 36px;
  color: #fff;
}
.ta-cut {
  background: rgba(0, 0, 0, 0.22);
  color: #fff;
  font-size: var(--fz-xs);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  margin-top: 4px;
}
.hero-actions {
  display: flex;
  gap: var(--sp-2);
  justify-content: flex-end;
}
/* 发起拼团：与商品详情页「去拼团」同色（红底白字实底）；因 hero 背景红橙渐变与按钮同红，
   加白色描边 + 阴影让按钮从背景浮出，保证与白底上的「去参团」同等醒目度 */
.start-btn {
  --el-button-bg-color: var(--c-primary);
  --el-button-border-color: var(--c-primary);
  --el-button-hover-bg-color: var(--c-primary-dark);
  --el-button-hover-border-color: var(--c-primary-dark);
  --el-button-active-bg-color: var(--c-primary-dark);
  --el-button-active-border-color: var(--c-primary-dark);
  box-shadow: 0 0 0 1.5px rgba(255, 255, 255, 0.85), 0 2px 8px rgba(0, 0, 0, 0.18);
  font-weight: 600;
}

/* 两栏 */
.layout-2col {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--sp-5);
}
.teams-card,
.side-card {
  background: var(--c-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  padding: var(--sp-4);
}
.team-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}
.team-item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  transition: box-shadow 0.2s;
}
.team-item:hover {
  box-shadow: var(--shadow-sm);
}
.team-avatar {
  background: linear-gradient(135deg, var(--c-primary), var(--c-accent));
  flex-shrink: 0;
}
.team-info {
  flex: 1;
}
.team-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.team-id {
  font-weight: 600;
}
.team-count {
  color: var(--c-primary);
  font-size: var(--fz-sm);
  font-weight: 600;
}

/* 折扣 */
.discount-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-2) 0;
  border-bottom: 1px dashed var(--c-border);
}
.discount-row:last-child {
  border-bottom: none;
}
.dtype {
  background: var(--c-primary-light);
  color: var(--c-primary);
  font-size: var(--fz-xs);
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.dval {
  flex: 1;
  font-weight: 600;
}
.dcrowd {
  color: var(--c-text-3);
  font-size: var(--fz-xs);
}
.howto {
  margin-top: var(--sp-4);
}
.howto h4 {
  margin-bottom: var(--sp-3);
}
.howto p {
  color: var(--c-text-2);
  font-size: var(--fz-sm);
  line-height: 2;
}

/* 支付弹窗 */
.pay-row {
  display: flex;
  justify-content: space-between;
  padding: var(--sp-2) 0;
  color: var(--c-text-2);
}
.pay-amount {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) 0;
  margin-bottom: var(--sp-3);
  border-top: 1px dashed var(--c-border);
}

/* 分享邀请 */
.invite-banner {
  margin-bottom: var(--sp-3);
}
.team-item.invited {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 2px var(--c-primary-light);
  background: #fffafa;
}
.team-ops {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 4px;
}
.share-body {
  padding: var(--sp-2) 0;
}
.share-tip {
  color: var(--c-text-2);
  font-size: var(--fz-sm);
  line-height: 1.6;
  margin-bottom: var(--sp-3);
}
.share-link-row {
  display: flex;
  gap: var(--sp-2);
}

/* 团倒计时 */
.team-countdown {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-top: 6px;
}
.cd-label {
  font-size: var(--fz-xs);
  color: var(--c-text-3);
}
.cd-time {
  font-size: var(--fz-sm);
  font-weight: 700;
  color: var(--c-primary);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.5px;
}
.cd-time.ended {
  color: var(--c-text-3);
  font-weight: 400;
}

/* 地址选择 */
.addr-select {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  max-height: 320px;
  overflow-y: auto;
}
.addr-opt {
  display: flex;
  align-items: flex-start;
  gap: var(--sp-2);
  padding: var(--sp-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.15s;
}
.addr-opt.active {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 2px var(--c-primary-light);
}
.addr-opt-body {
  flex: 1;
}
.addr-opt-l1 {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: 4px;
}
.addr-opt-l2 {
  color: var(--c-text-2);
  font-size: var(--fz-sm);
}
</style>
