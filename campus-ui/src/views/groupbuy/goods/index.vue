<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="标题">
            <n-input v-model:value="searchForm.title" placeholder="商品标题" clearable style="width: 180px" />
          </n-form-item>
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
                新增商品
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
        :scroll-x="1100"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <n-modal v-model:show="editVisible" preset="card" :title="form.goods.id ? '编辑商品' : '新增商品'" style="width: 720px">
      <n-form :model="form.goods" label-placement="left" label-width="80">
        <n-form-item label="标题">
          <n-input v-model:value="form.goods.title" placeholder="请输入商品标题" />
        </n-form-item>
        <n-form-item label="分类">
          <n-input v-model:value="form.goods.category" placeholder="如 数码/服饰" />
        </n-form-item>
        <n-form-item label="封面图">
          <n-input v-model:value="form.goods.cover" placeholder="封面图URL" />
        </n-form-item>
        <n-form-item label="详情">
          <n-input v-model:value="form.goods.detail" type="textarea" placeholder="图文详情" :rows="3" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.goods.status" :options="statusOptions" style="width: 160px" />
        </n-form-item>
        <n-divider title-placement="left">SKU 规格</n-divider>
        <div v-for="(sku, idx) in form.skus" :key="idx" class="sku-row">
          <n-input v-model:value="sku.skuName" placeholder="规格名" style="width: 200px" />
          <n-input-number v-model:value="sku.originalPrice" placeholder="原价" :min="0" :precision="2" style="width: 130px" />
          <n-input-number v-model:value="sku.stock" placeholder="库存" :min="0" style="width: 110px" />
          <n-button quaternary type="error" @click="removeSku(idx)">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
          </n-button>
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
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NIcon, NTag, useMessage, useDialog, type DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, CreateOutline, TrashOutline, SwapHorizontalOutline } from '@vicons/ionicons5'
import { gbGoodsApi, type GbGoods, type GbSku } from '@/api/groupbuy'

const message = useMessage()
const dialog = useDialog()

const statusOptions = [
  { label: '下架', value: 0 },
  { label: '上架', value: 1 },
]

const searchForm = reactive({ title: '', status: null as number | null })
const tableData = ref<GbGoods[]>([])
const loading = ref(false)
const pagination = reactive({ page: 1, pageSize: 10, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const editVisible = ref(false)
const saving = ref(false)
const form = reactive<{ goods: GbGoods; skus: GbSku[] }>({ goods: {}, skus: [] })

const columns: DataTableColumns<GbGoods> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { title: '分类', key: 'category', width: 120, render: (row) => row.category || '-' },
  {
    title: '状态', key: 'status', width: 90,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' },
        { default: () => (row.status === 1 ? '上架' : '下架') })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 280, fixed: 'right',
    render(row) {
      return h('div', { style: { display: 'flex', gap: '4px' } }, [
        h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openEdit(row) },
          { default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => toggleStatus(row) },
          { default: () => [h(NIcon, null, { default: () => h(SwapHorizontalOutline) }), row.status === 1 ? ' 下架' : ' 上架'] }),
        h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) },
          { default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除'] }),
      ])
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await gbGoodsApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      status: searchForm.status ?? undefined,
    })
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; loadData() }
function handleReset() { searchForm.title = ''; searchForm.status = null; handleSearch() }
function handlePageChange(p: number) { pagination.page = p; loadData() }
function handlePageSizeChange(s: number) { pagination.pageSize = s; pagination.page = 1; loadData() }

function addSku() { form.skus.push({ skuName: '', originalPrice: 0, stock: 0, status: 1 }) }
function removeSku(idx: number) { form.skus.splice(idx, 1) }

function openCreate() {
  form.goods = { status: 1 }
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

onMounted(() => { loadData() })
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
.sku-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
</style>
