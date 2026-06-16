<template>
  <div class="page-container">
    <n-card>
      <n-tabs v-model:value="activeTab" type="line" @update:value="onTabChange">
        <n-tab-pane name="team" tab="组队监控">
          <n-data-table
            :columns="teamColumns"
            :data="teamData"
            :loading="loading"
            :pagination="teamPagination"
            :row-key="(row) => row.id"
            :scroll-x="1000"
            @update:page="(p) => onTeamPage(p)"
            @update:page-size="(s) => onTeamPageSize(s)"
          />
        </n-tab-pane>
        <n-tab-pane name="order" tab="拼单记录">
          <n-data-table
            :columns="orderColumns"
            :data="orderData"
            :loading="loading"
            :pagination="orderPagination"
            :row-key="(row) => row.id"
            :scroll-x="1200"
            @update:page="(p) => onOrderPage(p)"
            @update:page-size="(s) => onOrderPageSize(s)"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NTag, type DataTableColumns } from 'naive-ui'
import { gbOrderApi, type GbOrder, type GbTeam } from '@/api/groupbuy'

const activeTab = ref<'team' | 'order'>('team')
const loading = ref(false)

const TEAM_STATUS: Record<number, { label: string; type: any }> = {
  0: { label: '拼团中', type: 'info' },
  1: { label: '成功', type: 'success' },
  2: { label: '失败', type: 'error' },
}
const ORDER_STATUS: Record<number, { label: string; type: any }> = {
  0: { label: '锁定', type: 'warning' },
  1: { label: '已支付', type: 'info' },
  2: { label: '成团', type: 'success' },
  3: { label: '退款', type: 'error' },
}

// ===== 组队 =====
const teamData = ref<GbTeam[]>([])
const teamPagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const teamColumns: DataTableColumns<GbTeam> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '活动ID', key: 'activityId', width: 90 },
  { title: '团长', key: 'leaderId', width: 90 },
  { title: '目标', key: 'targetCount', width: 80 },
  { title: '已锁单', key: 'lockCount', width: 90 },
  { title: '已支付', key: 'completeCount', width: 90 },
  {
    title: '状态', key: 'status', width: 90,
    render(row) {
      const m = TEAM_STATUS[row.status ?? 0] || { label: row.status, type: 'default' }
      return h(NTag, { type: m.type, size: 'small' }, { default: () => m.label })
    }
  },
  { title: '截止时间', key: 'validEndTime', width: 170 },
  { title: '创建时间', key: 'createTime', width: 170 },
]

// ===== 拼单 =====
const orderData = ref<GbOrder[]>([])
const orderPagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const orderColumns: DataTableColumns<GbOrder> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '组队ID', key: 'teamId', width: 90 },
  { title: '活动ID', key: 'activityId', width: 90 },
  { title: '用户', key: 'userId', width: 90 },
  { title: '交易号', key: 'outTradeNo', width: 180, ellipsis: { tooltip: true } },
  { title: '原价', key: 'originalAmount', width: 90, render: (row) => '¥' + (row.originalAmount ?? 0) },
  { title: '优惠', key: 'deductionAmount', width: 90, render: (row) => '¥' + (row.deductionAmount ?? 0) },
  { title: '实付', key: 'payAmount', width: 90, render: (row) => '¥' + (row.payAmount ?? 0) },
  {
    title: '状态', key: 'status', width: 90,
    render(row) {
      const m = ORDER_STATUS[row.status ?? 0] || { label: row.status, type: 'default' }
      return h(NTag, { type: m.type, size: 'small' }, { default: () => m.label })
    }
  },
  { title: '支付时间', key: 'payTime', width: 170, render: (row) => row.payTime || '-' },
]

async function loadTeam() {
  loading.value = true
  try {
    const res = await gbOrderApi.teamPage({ page: teamPagination.page, pageSize: teamPagination.pageSize })
    teamData.value = res.list
    teamPagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

async function loadOrder() {
  loading.value = true
  try {
    const res = await gbOrderApi.orderPage({ page: orderPagination.page, pageSize: orderPagination.pageSize })
    orderData.value = res.list
    orderPagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

function onTabChange(name: 'team' | 'order') {
  if (name === 'team') loadTeam()
  else loadOrder()
}
function onTeamPage(p: number) { teamPagination.page = p; loadTeam() }
function onTeamPageSize(s: number) { teamPagination.pageSize = s; teamPagination.page = 1; loadTeam() }
function onOrderPage(p: number) { orderPagination.page = p; loadOrder() }
function onOrderPageSize(s: number) { orderPagination.pageSize = s; orderPagination.page = 1; loadOrder() }

onMounted(() => { loadTeam() })
</script>

<style scoped>
</style>
