<template>
  <div v-loading="loading" class="order-detail">
    <div class="crumb-bar">
      <a @click="router.push('/my-orders')">我的拼单</a>
      <span class="sep">›</span>
      <span class="cur">订单详情</span>
    </div>

    <template v-if="data && data.order">
      <!-- 状态条 -->
      <div class="od-status jd-card">
        <div class="od-status-main">
          <span class="status-badge" :class="`s${data.order.status}`">{{ statusText(data.order.status) }}</span>
          <span class="od-no">订单号 {{ data.order.outTradeNo }}</span>
        </div>
        <el-button
          v-if="data.order.status === 0 || data.order.status === 1"
          type="danger"
          @click="cancelDialog = true"
        >
          {{ data.order.status === 1 ? '申请退款' : '取消订单' }}
        </el-button>
      </div>

      <!-- 商品 -->
      <div class="od-goods jd-card" v-if="data.goods">
        <el-image :src="data.goods.cover" fit="cover" class="od-cover" />
        <div class="od-goods-info">
          <div class="od-goods-title">{{ data.goods.title }}</div>
          <div class="od-activity" v-if="data.activity">{{ data.activity.activityName }}</div>
          <div class="od-qty">数量 ×{{ data.order.quantity ?? 1 }}</div>
          <div class="od-cat" v-if="data.goods.category">{{ data.goods.category }}</div>
        </div>
      </div>

      <!-- 金额明细 -->
      <div class="od-block jd-card">
        <h4 class="od-h4">金额明细</h4>
        <div class="od-row"><span>数量</span><b>×{{ data.order.quantity ?? 1 }}</b></div>
        <div class="od-row"><span>原价</span><b>¥{{ data.order.originalAmount }}</b></div>
        <div class="od-row"><span>优惠</span><b class="cut">-¥{{ data.order.deductionAmount }}</b></div>
        <div class="od-row total"><span>实付</span><b class="pay">¥{{ data.order.payAmount }}</b></div>
      </div>

      <!-- 收货 -->
      <div class="od-block jd-card" v-if="data.order.receiver">
        <h4 class="od-h4">收货信息</h4>
        <div class="od-addr">
          <b>{{ data.order.receiver }}</b> {{ data.order.phone }}
        </div>
        <div class="od-addr-detail">{{ data.order.address }}</div>
      </div>

      <!-- 拼团进度 -->
      <div class="od-block jd-card" v-if="data.team">
        <h4 class="od-h4">拼团进度</h4>
        <div class="od-row">
          <span>成团进度</span>
          <b>{{ data.team.completeCount }} / {{ data.team.targetCount }} 人</b>
        </div>
        <div class="od-row">
          <span>状态</span>
          <el-tag :type="teamTag(data.team.status).type" effect="dark" size="small">{{ teamTag(data.team.status).text }}</el-tag>
        </div>
      </div>

      <div v-if="data.order.refundReason" class="od-block jd-card">
        <h4 class="od-h4">退款原因</h4>
        <div class="od-addr-detail">{{ data.order.refundReason }}</div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="订单不存在" />

    <!-- 取消/退款 -->
    <el-dialog v-model="cancelDialog" title="取消订单" width="420px">
      <p class="cancel-tip">
        {{ data?.order?.status === 1 ? '该订单已支付，取消将发起退款（演示环境为模拟退款）。' : '确认取消该未支付订单？' }}
      </p>
      <el-input v-model="cancelReason" type="textarea" :rows="3" maxlength="200" placeholder="请填写取消/退款原因（选填）" />
      <template #footer>
        <el-button @click="cancelDialog = false">再想想</el-button>
        <el-button type="danger" :loading="canceling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { groupbuyApi, type OrderDetailVO } from '@/api/groupbuy'

const route = useRoute()
const router = useRouter()
const data = ref<OrderDetailVO | null>(null)
const loading = ref(false)
const cancelDialog = ref(false)
const cancelReason = ref('')
const canceling = ref(false)

const orderId = () => Number(route.params.id)

function statusText(s: number) {
  return { 0: '待支付', 1: '已支付', 2: '已成团', 3: '已退款' }[s] || '未知'
}
function teamTag(s: number) {
  return ({ 0: { text: '拼团中', type: 'warning' }, 1: { text: '成功', type: 'success' }, 2: { text: '失败', type: 'danger' } }[s] || { text: '未知', type: 'info' }) as { text: string; type: any }
}

async function load() {
  loading.value = true
  try {
    data.value = await groupbuyApi.orderDetail(orderId())
  } finally {
    loading.value = false
  }
}

async function confirmCancel() {
  canceling.value = true
  try {
    await groupbuyApi.cancelOrder(orderId(), cancelReason.value)
    ElMessage.success(data.value?.order?.status === 1 ? '退款已提交' : '订单已取消')
    cancelDialog.value = false
    await load()
  } finally {
    canceling.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.od-status,
.od-goods,
.od-block {
  padding: 18px 20px;
  margin-bottom: 16px;
}
.od-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.od-status-main {
  display: flex;
  align-items: center;
  gap: 14px;
}
.od-no {
  color: var(--c-text-2);
  font-size: 13px;
}
.status-badge {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 999px;
  border: 1px solid var(--c-border);
}
.status-badge.s1 { color: #6b6b70; }
.status-badge.s2 { color: #3a9d5d; border-color: #cfe6d6; }
.status-badge.s3 { color: var(--c-text-3); }
.od-goods {
  display: flex;
  gap: 16px;
}
.od-cover {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  flex-shrink: 0;
}
.od-goods-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}
.od-activity {
  color: var(--c-text-2);
  font-size: 13px;
  margin-bottom: 6px;
}
.od-qty {
  color: var(--c-text-2);
  font-size: 13px;
  margin-bottom: 6px;
}
.od-cat {
  display: inline-block;
  font-size: 12px;
  color: var(--c-text-3);
  border: 1px solid var(--c-border);
  border-radius: 999px;
  padding: 2px 10px;
}
.od-h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 14px;
  letter-spacing: 1px;
}
.od-row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  color: var(--c-text-2);
  font-size: 14px;
}
.od-row.total {
  border-top: 1px dashed var(--c-border);
  margin-top: 6px;
  padding-top: 14px;
}
.od-row .cut { color: var(--c-accent); }
.od-row .pay { color: var(--c-text); font-size: 18px; }
.od-addr b { font-size: 14px; }
.od-addr-detail {
  color: var(--c-text-2);
  font-size: 13px;
  margin-top: 6px;
  line-height: 1.6;
}
.cancel-tip {
  color: var(--c-text-2);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
}
</style>
