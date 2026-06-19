<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="标题">
            <n-input v-model:value="searchForm.title" placeholder="商品标题" clearable style="width: 160px" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="searchForm.status" placeholder="全部" clearable style="width: 120px" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="价格">
            <n-input-number v-model:value="searchForm.minPrice" placeholder="最低" :min="0" style="width: 110px" />
            <span style="margin: 0 6px">~</span>
            <n-input-number v-model:value="searchForm.maxPrice" placeholder="最高" :min="0" style="width: 110px" />
          </n-form-item>
          <n-form-item label="库存预警">
            <n-switch v-model:value="searchForm.lowStock" />
            <n-input-number
              v-if="searchForm.lowStock"
              v-model:value="searchForm.stockThreshold"
              :min="0"
              style="width: 100px; margin-left: 8px"
            />
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
            </n-space>
          </n-form-item>
        </n-form>
      </div>

      <div class="toolbar">
        <n-space>
          <n-button type="primary" @click="openCreate">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增商品
          </n-button>
          <n-button :disabled="!checkedRowKeys.length" @click="batchStatus(1)">批量上架</n-button>
          <n-button :disabled="!checkedRowKeys.length" @click="batchStatus(0)">批量下架</n-button>
          <n-button @click="exportCsv">
            <template #icon><n-icon><DownloadOutline /></n-icon></template>
            导出 CSV
          </n-button>
        </n-space>
        <span v-if="checkedRowKeys.length" class="sel-tip">已选 {{ checkedRowKeys.length }} 项</span>
      </div>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row) => row.id"
        :scroll-x="1500"
        v-model:checked-row-keys="checkedRowKeys"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <n-modal v-model:show="editVisible" preset="card" :title="form.goods.id ? '编辑商品' : '新增商品'" style="width: 820px">
      <n-form :model="form.goods" label-placement="left" label-width="90">
        <n-form-item label="标题">
          <n-input v-model:value="form.goods.title" placeholder="请输入商品标题" />
        </n-form-item>
        <n-form-item label="副标题">
          <n-input v-model:value="form.goods.subtitle" placeholder="一句话卖点（选填）" />
        </n-form-item>
        <n-form-item label="分类">
          <n-input v-model:value="form.goods.category" placeholder="如 数码/服饰" />
        </n-form-item>
        <n-form-item label="封面图">
          <ImageUpload v-model="form.goods.cover" />
        </n-form-item>
        <n-form-item label="轮播图">
          <MultiImageUpload v-model="form.goods.images" />
        </n-form-item>
        <n-form-item label="划线价">
          <n-input-number v-model:value="form.goods.marketPrice" placeholder="市场价/原价" :min="0" :precision="2" style="width: 160px" />
        </n-form-item>
        <n-form-item label="排序权重">
          <n-input-number v-model:value="form.goods.sort" :min="0" style="width: 160px" />
          <span style="margin-left: 8px; color: #999">值越大越靠前</span>
        </n-form-item>
        <n-form-item label="上架时间">
          <n-date-picker v-model:value="publishTs" type="datetime" clearable style="width: 260px" />
        </n-form-item>
        <n-form-item v-if="form.goods.id" label="销量">
          <n-input-number :value="form.goods.sales" disabled style="width: 160px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.goods.status" :options="statusOptions" style="width: 160px" />
        </n-form-item>
        <n-form-item label="图文详情">
          <RichEditor v-model:value="form.goods.detail" />
        </n-form-item>
        <n-divider title-placement="left">SKU 规格</n-divider>
        <div v-for="(sku, idx) in form.skus" :key="idx" class="sku-row">
          <ImageUpload v-model="sku.skuImage" />
          <div class="sku-fields">
            <n-input v-model:value="sku.skuName" placeholder="规格名" style="width: 200px" />
            <n-input-number v-model:value="sku.originalPrice" placeholder="原价" :min="0" :precision="2" style="width: 130px" />
            <n-input-number v-model:value="sku.stock" placeholder="库存" :min="0" style="width: 110px" />
            <n-switch v-model:value="sku.status" :checked-value="1" :unchecked-value="0">
              <template #checked>启用</template>
              <template #unchecked>停用</template>
            </n-switch>
            <n-button quaternary type="error" @click="removeSku(idx)">
              <template #icon><n-icon><TrashOutline /></n-icon></template>
            </n-button>
          </div>
        </div>
        <n-button dashed block @click="addSku" style="margin-top: 8px">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          添加 SKU
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
import { useRouter } from 'vue-router'
import { NButton, NSpace, NIcon, NTag, NImage, useMessage, useDialog, type DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, CreateOutline, TrashOutline, SwapHorizontalOutline, DownloadOutline, EyeOutline, FlashOutline } from '@vicons/ionicons5'
import { gbGoodsApi, type GbGoods, type GbSku } from '@/api/groupbuy'
import ImageUpload from '@/components/ImageUpload.vue'
import MultiImageUpload from '@/components/MultiImageUpload.vue'
import RichEditor from '@/components/RichEditor.vue'

const message = useMessage()
const dialog = useDialog()
const router = useRouter()

// C 端站点地址（预览用），dev 留空走相对路径，生产可配 VITE_WEB_BASE
const WEB_BASE = (import.meta.env.VITE_WEB_BASE as string) || ''

const statusOptions = [
  { label: '下架', value: 0 },
  { label: '上架', value: 1 },
]

const searchForm = reactive({
  title: '',
  status: null as number | null,
  minPrice: null as number | null,
  maxPrice: null as number | null,
  lowStock: false,
  stockThreshold: 10,
})
const tableData = ref<GbGoods[]>([])
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })
const checkedRowKeys = ref<number[]>([])

const editVisible = ref(false)
const saving = ref(false)
const form = reactive<{ goods: GbGoods; skus: GbSku[] }>({ goods: {}, skus: [] })

// 上架时间：毫秒时间戳 <-> ISO 字符串
const publishTs = computed<number | null>({
  get() {
    return form.goods.publishTime ? new Date(form.goods.publishTime).getTime() : null
  },
  set(val) {
    form.goods.publishTime = val ? toLocalIso(val) : undefined
  }
})

function toLocalIso(ts: number): string {
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const columns: DataTableColumns<GbGoods> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  {
    title: '封面', key: 'cover', width: 70,
    render: (row) => row.cover ? h(NImage, { src: row.cover, width: 40, height: 40, objectFit: 'cover' }) : '-'
  },
  { title: '标题', key: 'title', width: 160, ellipsis: { tooltip: true } },
  { title: '副标题', key: 'subtitle', width: 140, ellipsis: { tooltip: true }, render: (row) => row.subtitle || '-' },
  { title: '分类', key: 'category', width: 90, render: (row) => row.category || '-' },
  { title: '最低价', key: 'minPrice', width: 90, render: (row) => row.minPrice != null ? '¥' + row.minPrice : '-' },
  { title: '划线价', key: 'marketPrice', width: 90, render: (row) => row.marketPrice != null ? '¥' + row.marketPrice : '-' },
  { title: '销量', key: 'sales', width: 70, render: (row) => row.sales ?? 0 },
  { title: '排序', key: 'sort', width: 70, render: (row) => row.sort ?? 0 },
  {
    title: '状态', key: 'status', width: 80,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' },
        { default: () => (row.status === 1 ? '上架' : '下架') })
    }
  },
  { title: '上架时间', key: 'publishTime', width: 160, render: (row) => row.publishTime || '-' },
  {
    title: '操作', key: 'actions', width: 320, fixed: 'right',
    render(row) {
      return h('div', { style: { display: 'flex', gap: '2px', flexWrap: 'wrap' } }, [
        h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => toggleStatus(row) },
          { default: () => [h(NIcon, null, { default: () => h(SwapHorizontalOutline) }), row.status === 1 ? ' 下架' : ' 上架'] }),
        h(NButton, { size: 'small', quaternary: true, onClick: () => preview(row) },
          { default: () => [h(NIcon, null, { default: () => h(EyeOutline) }), ' 预览'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => createActivity(row) },
          { default: () => [h(NIcon, null, { default: () => h(FlashOutline) }), ' 建活动'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) },
          { default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除'] }),
      ])
    }
  }
]

function buildParams(pageSize?: number) {
  return {
    page: 1,
    pageSize: pageSize ?? pagination.pageSize,
    title: searchForm.title || undefined,
    status: searchForm.status ?? undefined,
    minPrice: searchForm.minPrice ?? undefined,
    maxPrice: searchForm.maxPrice ?? undefined,
    lowStock: searchForm.lowStock || undefined,
    stockThreshold: searchForm.lowStock ? searchForm.stockThreshold : undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await gbGoodsApi.page({ ...buildParams(), page: pagination.page })
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; loadData() }
function handleReset() {
  searchForm.title = ''
  searchForm.status = null
  searchForm.minPrice = null
  searchForm.maxPrice = null
  searchForm.lowStock = false
  searchForm.stockThreshold = 10
  handleSearch()
}
function handlePageChange(p: number) { pagination.page = p; loadData() }
function handlePageSizeChange(s: number) { pagination.pageSize = s; pagination.page = 1; loadData() }

function addSku() { form.skus.push({ skuName: '', originalPrice: 0, stock: 0, status: 1 }) }
function removeSku(idx: number) { form.skus.splice(idx, 1) }

function openCreate() {
  form.goods = { status: 1, sort: 0 }
  form.skus = [{ skuName: '', originalPrice: 0, stock: 0, status: 1 }]
  editVisible.value = true
}

async function openEdit(row: GbGoods) {
  const res = await gbGoodsApi.detail(row.id!)
  form.goods = { ...res.goods }
  form.skus = res.skus.length ? res.skus.map(s => ({ ...s })) : [{ skuName: '', originalPrice: 0, stock: 0, status: 1 }]
  editVisible.value = true
}

async function handleSave() {
  if (!form.goods.title) { message.warning('请填写商品标题'); return }
  if (!form.skus.length || form.skus.some(s => !s.skuName)) { message.warning('请完善 SKU 名称'); return }
  saving.value = true
  try {
    const payload = { goods: form.goods, skus: form.skus }
    if (form.goods.id) await gbGoodsApi.edit(payload)
    else await gbGoodsApi.add(payload)
    message.success('保存成功')
    editVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function toggleStatus(row: GbGoods) {
  const next = row.status === 1 ? 0 : 1
  gbGoodsApi.changeStatus(row.id!, next).then(() => {
    message.success(next === 1 ? '已上架' : '已下架')
    loadData()
  })
}

function batchStatus(status: number) {
  if (!checkedRowKeys.value.length) { message.warning('请先选择商品'); return }
  gbGoodsApi.changeStatusBatch(checkedRowKeys.value, status).then(() => {
    message.success(status === 1 ? '已批量上架' : '已批量下架')
    checkedRowKeys.value = []
    loadData()
  })
}

function preview(row: GbGoods) {
  window.open(`${WEB_BASE}/goods/${row.id}`, '_blank')
}

function createActivity(row: GbGoods) {
  router.push({ path: '/groupbuy/activity', query: { goodsId: row.id, action: 'create' } })
}

function handleDelete(row: GbGoods) {
  dialog.warning({
    title: '提示', content: '确定删除该商品及其 SKU 吗？', positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      await gbGoodsApi.delete(row.id!)
      message.success('删除成功')
      loadData()
    }
  })
}

async function exportCsv() {
  const res = await gbGoodsApi.page({ ...buildParams(10000) })
  const rows = res.list || []
  if (!rows.length) { message.warning('暂无数据可导出'); return }
  const headers = ['ID', '标题', '副标题', '分类', '最低价', '划线价', '销量', '排序', '状态', '上架时间']
  const esc = (v: any) => {
    const s = v == null ? '' : String(v)
    return /[",\n]/.test(s) ? '"' + s.replace(/"/g, '""') + '"' : s
  }
  const lines = rows.map(r => [
    r.id, r.title, r.subtitle, r.category, r.minPrice, r.marketPrice,
    r.sales ?? 0, r.sort ?? 0, r.status === 1 ? '上架' : '下架', r.publishTime
  ].map(esc).join(','))
  const csv = '﻿' + headers.join(',') + '\n' + lines.join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `商品列表_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

onMounted(() => { loadData() })
</script>

<style scoped>
.search-form { margin-bottom: 12px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.sel-tip { color: #18a058; font-size: 13px; }
.sku-row { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px dashed #eee; }
.sku-fields { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>
