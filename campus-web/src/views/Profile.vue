<template>
  <div class="profile">
    <div class="section-title"><span>个人中心</span></div>

    <div class="profile-grid">
      <!-- 资料卡 -->
      <div class="pf-card jd-card">
        <h3 class="pf-card-title">基本资料</h3>
        <div class="avatar-row">
          <el-avatar :size="80" :src="form.avatar" class="pf-avatar">
            {{ (form.nickname || '用').charAt(0) }}
          </el-avatar>
          <div class="avatar-ops">
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="onAvatarChange"
            />
            <el-button :loading="uploading" @click="fileInput?.click()">更换头像</el-button>
            <p class="avatar-tip">支持 jpg/png，建议 200×200</p>
          </div>
        </div>

        <el-form label-width="72px" class="pf-form">
          <el-form-item label="用户名">
            <el-input :model-value="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="30" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-button type="danger" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form>
      </div>

      <!-- 右侧：拼团统计 + 改密码 -->
      <div class="pf-side">
        <div class="pf-card jd-card stat-card">
          <h3 class="pf-card-title">我的拼团</h3>
          <div class="stat-row">
            <div class="stat-item">
              <span class="stat-num">{{ stats.total }}</span>
              <span class="stat-label">拼单总数</span>
            </div>
            <div class="stat-item">
              <span class="stat-num hl">{{ stats.success }}</span>
              <span class="stat-label">已成团</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ stats.paid }}</span>
              <span class="stat-label">已支付</span>
            </div>
          </div>
          <el-button text type="danger" @click="$router.push('/my-orders')">查看我的拼单 ›</el-button>
        </div>

        <div class="pf-card jd-card">
          <h3 class="pf-card-title">修改密码</h3>
          <el-form label-width="72px" class="pf-form">
            <el-form-item label="原密码">
              <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="6-20 位" />
            </el-form-item>
            <el-form-item label="确认">
              <el-input v-model="pwd.confirm" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-button type="danger" :loading="changingPwd" @click="changePassword">确认修改</el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { groupbuyApi } from '@/api/groupbuy'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const fileInput = ref<HTMLInputElement>()

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const stats = reactive({ total: 0, success: 0, paid: 0 })

const saving = ref(false)
const uploading = ref(false)
const changingPwd = ref(false)

async function loadProfile() {
  const u = await authApi.getProfile()
  form.username = u.username || ''
  form.nickname = u.nickname || ''
  form.email = u.email || ''
  form.phone = u.phone || ''
  form.avatar = u.avatar || ''
}

async function loadStats() {
  const orders = (await groupbuyApi.myOrders()) || []
  stats.total = orders.length
  stats.success = orders.filter((o) => o.status === 2).length
  stats.paid = orders.filter((o) => o.status === 1 || o.status === 2).length
}

async function onAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const url = await authApi.uploadAvatar(file)
    form.avatar = url
    ElMessage.success('头像已上传，记得保存资料')
  } catch {
    // request 拦截器已提示错误
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await authApi.updateProfile({
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      avatar: form.avatar
    })
    await userStore.getInfo()
    ElMessage.success('资料已保存')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!pwd.oldPassword || !pwd.newPassword) {
    ElMessage.warning('请填写原密码和新密码')
    return
  }
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await authApi.changePassword(pwd.oldPassword, pwd.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
    await userStore.logout()
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadStats()
})
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: var(--sp-5);
  align-items: start;
}
.pf-card {
  padding: var(--sp-5);
  margin-bottom: var(--sp-5);
}
.pf-card-title {
  font-size: var(--fz-lg);
  font-weight: 700;
  margin-bottom: var(--sp-4);
  padding-bottom: var(--sp-3);
  border-bottom: 1px solid var(--c-border);
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  margin-bottom: var(--sp-5);
}
.pf-avatar {
  background: linear-gradient(135deg, var(--c-primary), var(--c-accent));
  font-size: 28px;
  flex-shrink: 0;
}
.avatar-tip {
  color: var(--c-text-3);
  font-size: var(--fz-xs);
  margin-top: var(--sp-2);
}
.pf-form {
  max-width: 420px;
}
.stat-card {
  text-align: center;
}
.stat-row {
  display: flex;
  justify-content: space-around;
  margin-bottom: var(--sp-4);
}
.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat-num {
  font-size: var(--fz-2xl);
  font-weight: 800;
  color: var(--c-text-1);
}
.stat-num.hl {
  color: var(--c-primary);
}
.stat-label {
  font-size: var(--fz-sm);
  color: var(--c-text-3);
}
</style>
