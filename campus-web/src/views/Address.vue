<template>
  <div class="address-page">
    <div class="section-title list-head">
      <span>收货地址</span>
      <el-button type="danger" @click="openAdd">新增地址</el-button>
    </div>

    <div v-loading="loading" class="addr-list">
      <el-empty v-if="!loading && list.length === 0" description="还没有收货地址，添加一个吧" />
      <div v-for="a in list" :key="a.id" class="addr-card jd-card">
        <div class="addr-main">
          <div class="addr-line1">
            <span class="addr-receiver">{{ a.receiver }}</span>
            <span class="addr-phone">{{ a.phone }}</span>
            <el-tag v-if="a.isDefault === 1" type="danger" size="small" effect="dark">默认</el-tag>
          </div>
          <div class="addr-detail">
            {{ [a.province, a.city, a.district].filter(Boolean).join(' ') }} {{ a.detail }}
          </div>
        </div>
        <div class="addr-ops">
          <el-button v-if="a.isDefault !== 1" text size="small" @click="setDefault(a.id!)">设为默认</el-button>
          <el-button text size="small" @click="openEdit(a)">编辑</el-button>
          <el-button text type="danger" size="small" @click="remove(a.id!)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑地址' : '新增地址'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="form.receiver" placeholder="请输入收货人" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-cascader
            v-model="region"
            :options="areaOptions"
            placeholder="请选择省/市/区"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="2"
            placeholder="街道、门牌号等"
            maxlength="255"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch
            :model-value="form.isDefault === 1"
            @update:model-value="(v: boolean) => (form.isDefault = v ? 1 : 0)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="danger" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pcaTextArr } from 'element-china-area-data'
import { addressApi, type UserAddress } from '@/api/address'

const areaOptions = pcaTextArr
const list = ref<UserAddress[]>([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const region = ref<string[]>([])

const form = reactive<UserAddress>({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0
})

async function load() {
  loading.value = true
  try {
    list.value = (await addressApi.list()) || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = undefined
  form.receiver = ''
  form.phone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detail = ''
  form.isDefault = 0
  region.value = []
}

function openAdd() {
  resetForm()
  dialog.value = true
}

function openEdit(a: UserAddress) {
  resetForm()
  Object.assign(form, a)
  region.value = [a.province, a.city, a.district].filter(Boolean) as string[]
  dialog.value = true
}

async function save() {
  if (!form.receiver || !form.phone || !form.detail) {
    ElMessage.warning('请填写收货人、手机号和详细地址')
    return
  }
  // 级联值回填到省市区
  form.province = region.value[0] || ''
  form.city = region.value[1] || ''
  form.district = region.value[2] || ''
  saving.value = true
  try {
    if (form.id) {
      await addressApi.update(form)
    } else {
      await addressApi.add(form)
    }
    ElMessage.success('保存成功')
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function setDefault(id: number) {
  await addressApi.setDefault(id)
  await load()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确定删除这个地址吗？', '提示', { type: 'warning' }).catch(() => null)
  await addressApi.remove(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.addr-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  min-height: 120px;
}
.addr-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-4);
}
.addr-line1 {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: 6px;
}
.addr-receiver {
  font-weight: 700;
  font-size: var(--fz-md);
}
.addr-phone {
  color: var(--c-text-2);
}
.addr-detail {
  color: var(--c-text-2);
  font-size: var(--fz-sm);
}
</style>
