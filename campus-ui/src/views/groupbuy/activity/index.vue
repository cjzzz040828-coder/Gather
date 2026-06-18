<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="状态">
            <n-select v-model:value="searchForm.status" placeholder="全部" clearable style="width: 140px" :options="statusOptions" />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">
                <template #icon><n-icon><SearchOutline /></n-icon></template>
                搜索
              </n-button>
              <n-button @click="handleReset">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置
              </n-button>
              <n-button type="primary" @click="openCreate">
                <template #icon><n-icon><AddOutline /></n-icon></template>
                新增活动
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </div>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row) => row.id"
        :scroll-x="1200"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <n-modal v-model:show="editVisible" preset="card" :title="form.activity.id ? '编辑活动' : '新增活动'" style="width: 760px">
      <n-form :model="form.activity" label-placement="left" label-width="90">
        <n-form-item label="活动名称">
          <n-input v-model:value="form.activity.activityName" placeholder="请输入活动名称" />
        </n-form-item>
        <n-form-item label="商品">
          <n-select v-model:value="form.activity.goodsId" :options="goodsOptions" placeholder="选择商品" style="width: 240px" />
        </n-form-item>
        <n-form-item label="成团人数">
          <n-input-number v-model:value="form.activity.targetCount" :min="2" style="width: 160px" />
        </n-form-item>
        <n-form-item label="活动时间">
          <n-date-picker v-model:value="validRange" type="datetimerange" clearable style="width: 420px" />
        </n-form-item>
        <n-form-item label="单团时限">
          <n-input-number v-model:value="form.activity.timeLimitMinutes" :min="1" style="width: 160px" />
          <span style="margin-left: 8px; color: #999">分钟</span>
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.activity.status" :options="statusOptions" style="width: 160px" />
        </n-form-item>
        <n-divider title-placement="left">折扣规则</n-divider>
        <div v-for="(d, idx) in form.discounts" :key="idx" class="discount-row">
          <n-select v-model:value="d.discountType" :options="discountTypeOptions" placeholder="类型" style="width: 130px" />
          <n-input-number v-model:value="d.discountValue" placeholder="折扣值" :min="0" :precision="2" style="width: 130px" />
          <n-input v-model:value="d.crowdTag" placeholder="人群标签(可空)" style="width: 150px" />
          <n-input-number v-model:value="d.priority" placeholder="优先级" :min="0" style="width: 110px" />
          <n-button quaternary type="error" @click="removeDiscount(idx)">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
          </n-button>
        </div>
        <n-button dashed block @click="addDiscount" style="margin-top: 8px">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          添加折扣
        </n-button>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="editVisible = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, computed, onMounted } from 'vue'
import { NButton, NSpace, NIcon, NTag, useMessage, useDialog, type DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { gbActivityApi, gbGoodsApi, type GbActivity, type GbDiscount } from '@/api/groupbuy'

const message = useMessage()
const dialog = useDialog()

const statusOptions = [
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 },
]
const STATUS_META: Record<number, { label: string; type: any }> = {
  0: { label: '未开始', type: 'warning' },
  1: { label: '进行中', type: 'success' },
  2: { label: '已结束', type: 'default' },
}
const discountTypeOptions = [
  { label: '直减', value: 1 },
  { label: '折扣', value: 2 },
  { label: 'N元购', value: 3 },
]
function discountTypeLabel(t?: number) { return discountTypeOptions.find(o => o.value === t)?.label ?? '-' }

const searchForm = reactive({ status: null as number | null })
const tableData = ref<GbActivity[]>([])
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const editVisible = ref(false)
const saving = ref(false)
const form = reactive<{ activity: GbActivity; discounts: GbDiscount[] }>({ activity: {}, discounts: [] })

const goodsOptions = ref<{ label: string; value: number }[]>([])

// 时间区间双向桥接（毫秒时间戳 <-> ISO 字符串）
const validRange = computed<[number, number] | null>({
  get() {
    if (form.activity.validStart && form.activity.validEnd) {
      return [new Date(form.activity.validStart).getTime(), new Date(form.activity.validEnd).getTime()]
    }
    return null
  },
  set(val) {
    if (val) {
      form.activity.validStart = toLocalIso(val[0])
      form.activity.validEnd = toLocalIso(val[1])
    } else {
      form.activity.validStart = undefined
      form.activity.validEnd = undefined
    }
  }
})

function toLocalIso(ts: number): string {
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const columns: DataTableColumns<GbActivity> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '活动名称', key: 'activityName', ellipsis: { tooltip: true } },
  { title: '商品ID', key: 'goodsId', width: 90 },
  { title: '成团人数', key: 'targetCount', width: 90 },
  { title: '单团时限', key: 'timeLimitMinutes', width: 100, render: (row) => (row.timeLimitMinutes ?? 0) + ' 分' },
  { title: '开始', key: 'validStart', width: 170 },
  { title: '结束', key: 'validEnd', width: 170 },
  {
    title: '状态', key: 'status', width: 90,
    render(row) {
      const m = STATUS_META[row.status ?? 0] || { label: row.status, type: 'default' }
      return h(NTag, { type: m.type, size: 'small' }, { default: () => m.label })
    }
  },
  {
    title: '操作', key: 'actions', width: 200, fixed: 'right',
    render(row) {
      return h('div', { style: { display: 'flex', gap: '4px' } }, [
        h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) },
          { default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除'] }),
      ])
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await gbActivityApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: searchForm.status ?? undefined,
    })
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

async function loadGoodsOptions() {
  const res = await gbGoodsApi.page({ page: 1, pageSize: 100 })
  goodsOptions.value = res.list.map(g => ({ label: `#${g.id} ${g.title}`, value: g.id! }))
}

function handleSearch() { pagination.page = 1; loadData() }
function handleReset() { searchForm.status = null; handleSearch() }
function handlePageChange(p: number) { pagination.page = p; loadData() }
function handlePageSizeChange(s: number) { pagination.pageSize = s; pagination.page = 1; loadData() }

function addDiscount() { form.discounts.push({ discountType: 1, discountValue: 0, priority: 0 }) }
function removeDiscount(idx: number) { form.discounts.splice(idx, 1) }

async function openCreate() {
  form.activity = { targetCount: 2, timeLimitMinutes: 1440, status: 0 }
  form.discounts = []
  editVisible.value = true
}

async function openEdit(row: GbActivity) {
  const res = await gbActivityApi.detail(row.id!)
  form.activity = { ...res.activity }
  form.discounts = res.discounts.map(d => ({ ...d }))
  editVisible.value = true
}

async function handleSave() {
  if (!form.activity.activityName) { message.warning('请填写活动名称'); return }
  if (!form.activity.goodsId) { message.warning('请选择商品'); return }
  if (!form.activity.validStart || !form.activity.validEnd) { message.warning('请选择活动时间区间'); return }
  saving.value = true
  try {
    const payload = { activity: form.activity, discounts: form.discounts }
    if (form.activity.id) await gbActivityApi.edit(payload)
    else await gbActivityApi.add(payload)
    message.success('保存成功')
    editVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function handleDelete(row: GbActivity) {
  dialog.warning({
    title: '提示', content: '确定删除该活动及其折扣吗？', positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await gbActivityApi.delete(row.id!)
      message.success('删除成功')
      loadData()
    }
  })
}

onMounted(() => { loadData(); loadGoodsOptions() })
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
.discount-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
</style>
