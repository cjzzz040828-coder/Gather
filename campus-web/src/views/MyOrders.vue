<template>
  <div v-loading="loading" class="my-orders">
    <div class="section-title"><span>我的拼单</span></div>

    <el-empty v-if="!loading && orders.length === 0" description="还没有拼单记录，去首页拼一单吧" />

    <div v-else class="order-table jd-card">
      <!-- 表头 -->
      <div class="order-head">
        <span class="col-info">订单信息</span>
        <span class="col-amount">金额</span>
        <span class="col-status">状态</span>
        <span class="col-op">操作</span>
      </div>

      <!-- 行 -->
      <div v-for="o in orders" :key="o.id" class="order-row">
        <div class="col-info">
          <div class="oc-no">订单号 {{ o.outTradeNo }}</div>
          <div class="oc-qty">数量 ×{{ o.quantity ?? 1 }}</div>
          <div class="oc-time">
            {{ o.payTime ? o.payTime.replace('T', ' ').slice(0, 16) : '未支付' }}
          </div>
          <div v-if="o.receiver" class="oc-addr">
            收货：{{ o.receiver }} {{ o.phone }} · {{ o.address }}
          </div>
        </div>
        <div class="col-amount">
          <span class="price"><span class="symbol">¥</span>{{ o.payAmount }}</span>
          <span class="oc-cut">已优惠 ¥{{ o.deductionAmount }}</span>
        </div>
        <div class="col-status">
          <span class="status-badge" :class="`s${o.status}`">{{ statusText(o.status) }}</span>
        </div>
        <div class="col-op">
          <el-button text size="small" @click="goDetail(o.id)">详情</el-button>
          <el-button text size="small" @click="viewTeam(o.teamId)">进度</el-button>
          <el-button
            v-if="o.status === 0 || o.status === 1"
            type="danger"
            text
            size="small"
            @click="openCancel(o)"
          >
            {{ o.status === 1 ? '退款' : '取消' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 取消/退款 原因弹窗 -->
    <el-dialog v-model="cancelDialog" title="取消订单" width="420px">
      <p class="cancel-tip">
        {{ cancelTarget?.status === 1 ? '该订单已支付，取消将发起退款（演示环境为模拟退款）。' : '确认取消该未支付订单？' }}
      </p>
      <el-input
        v-model="cancelReason"
        type="textarea"
        :rows="3"
        maxlength="200"
        placeholder="请填写取消/退款原因（选填）"
      />
      <template #footer>
        <el-button @click="cancelDialog = false">再想想</el-button>
        <el-button type="danger" :loading="canceling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="teamDialog" title="拼团进度" width="400px">
      <template v-if="team">
        <div class="team-prog">
          <div class="tp-ring">
            <el-progress
              type="circle"
              :percentage="Math.round((team.completeCount / team.targetCount) * 100)"
              :width="120"
              color="#1c1c1e"
            />
          </div>
          <p class="tp-line">
            <b class="hl">{{ team.completeCount }}</b> / {{ team.targetCount }} 人已支付
          </p>
          <el-tag :type="teamStatusTag(team.status).type" effect="dark" size="large">
            {{ teamStatusTag(team.status).text }}
          </el-tag>
          <p v-if="team.status === 0" class="tp-deadline">
            剩余 <b class="cd">{{ formatCountdown(team.validEndTime) }}</b>
          </p>
          <p v-else class="tp-deadline">
            截止：{{
              team.validEndTime ? team.validEndTime.replace('T', ' ').slice(0, 16) : '-'
            }}
          </p>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { groupbuyApi, type GbOrder, type GbTeam } from '@/api/groupbuy'

const router = useRouter()
const orders = ref<GbOrder[]>([])
const loading = ref(false)
const teamDialog = ref(false)
const team = ref<GbTeam | null>(null)

// 取消/退款
const cancelDialog = ref(false)
const cancelTarget = ref<GbOrder | null>(null)
const cancelReason = ref('')
const canceling = ref(false)

function goDetail(id: number) {
  router.push(`/order/${id}`)
}

function openCancel(o: GbOrder) {
  cancelTarget.value = o
  cancelReason.value = ''
  cancelDialog.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  canceling.value = true
  try {
    await groupbuyApi.cancelOrder(cancelTarget.value.id, cancelReason.value)
    ElMessage.success(cancelTarget.value.status === 1 ? '退款已提交' : '订单已取消')
    cancelDialog.value = false
    await load()
  } finally {
    canceling.value = false
  }
}

// 倒计时
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | null = null

function formatCountdown(endTime?: string): string {
  if (!endTime) return ''
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

function statusText(s: number) {
  return { 0: '待支付', 1: '已支付', 2: '已成团', 3: '已退款' }[s] || '未知'
}
function teamStatusTag(s: number) {
  return (
    { 0: { text: '拼团中', type: 'warning' }, 1: { text: '成功', type: 'success' }, 2: { text: '失败', type: 'danger' } }[
      s
    ] || { text: '未知', type: 'info' }
  ) as { text: string; type: any }
}

async function load() {
  loading.value = true
  try {
    orders.value = (await groupbuyApi.myOrders()) || []
  } finally {
    loading.value = false
  }
}

async function viewTeam(teamId: number) {
  team.value = await groupbuyApi.teamProgress(teamId)
  teamDialog.value = true
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
.order-table {
  overflow: hidden;
}
/* 表头 */
.order-head,
.order-row {
  display: grid;
  grid-template-columns: 1fr 160px 120px 120px;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-4) var(--sp-5);
}
.order-head {
  background: #fafafa;
  border-bottom: 1px solid var(--c-border);
  color: var(--c-text-3);
  font-size: var(--fz-sm);
}
.order-row {
  border-bottom: 1px solid var(--c-border);
  transition: background 0.15s;
}
.order-row:last-child {
  border-bottom: none;
}
.order-row:hover {
  background: #fffafa;
}

.oc-no {
  font-weight: 600;
  margin-bottom: 4px;
}
.oc-qty {
  color: var(--c-text-2);
  font-size: var(--fz-xs);
  margin-bottom: 4px;
}
.oc-time {
  color: var(--c-text-3);
  font-size: var(--fz-sm);
}
.oc-addr {
  color: var(--c-text-2);
  font-size: var(--fz-xs);
  margin-top: 4px;
}
.col-amount {
  display: flex;
  flex-direction: column;
}
.col-amount .price {
  font-size: var(--fz-lg);
}
.oc-cut {
  color: var(--c-accent);
  font-size: var(--fz-xs);
}

/* 方形状态标签 */
.status-badge {
  display: inline-block;
  font-size: var(--fz-xs);
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid;
}
.status-badge.s0 {
  color: #909399;
  border-color: #d8dade;
  background: #f4f4f5;
}
.status-badge.s1 {
  color: #e6a23c;
  border-color: #f5dab1;
  background: #fdf6ec;
}
.status-badge.s2 {
  color: #67c23a;
  border-color: #c2e7b0;
  background: #f0f9eb;
}
.status-badge.s3 {
  color: var(--c-primary);
  border-color: #f6bcb9;
  background: var(--c-primary-light);
}

/* 进度弹窗 */
.team-prog {
  text-align: center;
  padding: var(--sp-3) 0;
}
.tp-ring {
  margin-bottom: var(--sp-4);
}
.tp-line {
  font-size: var(--fz-md);
  margin-bottom: var(--sp-3);
}
.hl {
  color: var(--c-primary);
  font-size: var(--fz-lg);
}
.tp-deadline {
  color: var(--c-text-3);
  font-size: var(--fz-sm);
  margin-top: var(--sp-3);
}
.tp-deadline .cd {
  color: var(--c-primary);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.cancel-tip {
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
}
</style>
