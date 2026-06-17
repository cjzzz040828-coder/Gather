<template>
  <div class="layout">
    <!-- 顶部窄条 -->
    <div class="topbar">
      <div class="container topbar-inner">
        <span>集萃 · 省钱就要一起拼</span>
        <span class="topbar-right">
          <template v-if="userStore.isLogin">你好，{{ userStore.nickname }}</template>
          <template v-else>
            <a @click="router.push('/login')">请登录</a>
          </template>
        </span>
      </div>
    </div>

    <!-- 主导航 -->
    <header class="header">
      <div class="container header-inner">
        <div class="logo" @click="router.push('/')">
          <span class="wordmark">GATHER</span>
          <span class="brand-cn">集萃</span>
        </div>

        <div class="search">
          <input
            v-model="keyword"
            class="search-input"
            placeholder="搜索拼团好物"
            @keyup.enter="doSearch"
          />
          <button class="search-btn" @click="doSearch">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </button>
        </div>

        <nav class="nav">
          <router-link to="/goods" class="nav-item">首页</router-link>
          <router-link to="/my-orders" class="nav-item">我的拼单</router-link>
        </nav>

        <div class="user-area">
          <el-dropdown v-if="userStore.isLogin" @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="userStore.avatar">{{
                userStore.nickname.charAt(0)
              }}</el-avatar>
              <span class="username">{{ userStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的拼单</el-dropdown-item>
                <el-dropdown-item command="address">收货地址</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button v-else class="login-btn" @click="router.push('/login')">登录</button>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="container">
        <router-view />
      </div>
    </main>

    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-cols">
          <div class="fcol">
            <h4>购物指南</h4>
            <p>拼团流程</p>
            <p>成团规则</p>
          </div>
          <div class="fcol">
            <h4>支付方式</h4>
            <p>在线支付</p>
            <p>模拟支付</p>
          </div>
          <div class="fcol">
            <h4>关于我们</h4>
            <p>集萃商城</p>
            <p>联系客服</p>
          </div>
        </div>
        <p class="copyright">© 2026 集萃 · 仅用于教学演示</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

function doSearch() {
  router.push({ path: '/goods', query: keyword.value ? { q: keyword.value } : {} })
}

function onCommand(command: string) {
  if (command === 'profile') router.push('/profile')
  else if (command === 'orders') router.push('/my-orders')
  else if (command === 'address') router.push('/address')
  else if (command === 'logout') userStore.logout()
}
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 顶部窄条 */
.topbar {
  background: #f0f0f0;
  font-size: var(--fz-xs);
  color: var(--c-text-3);
  border-bottom: 1px solid #e7e7e7;
}
.topbar-inner {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.topbar-right a {
  cursor: pointer;
  color: var(--c-primary);
}

/* 主导航 */
.header {
  background: var(--c-white);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}
.header-inner {
  height: 80px;
  display: flex;
  align-items: center;
  gap: var(--sp-6);
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}
.wordmark {
  font-family: 'Arial Narrow', 'Helvetica Neue', Arial, sans-serif;
  font-size: 24px;
  font-weight: 500;
  letter-spacing: 8px;
  color: var(--c-text);
  text-transform: uppercase;
  padding-left: 4px;
  user-select: none;
}
.brand-cn {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 3px;
  color: var(--c-text-2);
  padding-left: 12px;
  border-left: 1px solid #d4d4d4;
  user-select: none;
}

.search {
  flex: 1;
  max-width: 540px;
  display: flex;
  height: 42px;
  border: 2px solid var(--c-primary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0 18px;
  font-size: var(--fz-base);
}
.search-btn {
  border: none;
  background: var(--c-primary);
  color: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: var(--fz-base);
  transition: background 0.2s;
}
.search-btn:hover {
  background: var(--c-primary-dark);
}

.nav {
  display: flex;
  gap: var(--sp-5);
  flex-shrink: 0;
}
.nav-item {
  color: var(--c-text-2);
  font-size: var(--fz-md);
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.nav-item:hover,
.nav-item.router-link-active {
  color: var(--c-primary);
  border-bottom-color: var(--c-primary);
}

.user-area {
  flex-shrink: 0;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.username {
  font-size: var(--fz-base);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.login-btn {
  background: var(--c-primary);
  color: #fff;
  border: none;
  padding: 8px 22px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--fz-base);
  transition: background 0.2s;
}
.login-btn:hover {
  background: var(--c-primary-dark);
}

.main {
  flex: 1;
  padding: var(--sp-5) 0 var(--sp-8);
}

/* footer */
.footer {
  background: #2b2b2b;
  color: #aaa;
  padding: var(--sp-8) 0 var(--sp-5);
  margin-top: auto;
}
.footer-cols {
  display: flex;
  gap: var(--sp-8);
  justify-content: center;
  margin-bottom: var(--sp-6);
}
.fcol h4 {
  color: #fff;
  font-size: var(--fz-base);
  margin-bottom: var(--sp-3);
}
.fcol p {
  font-size: var(--fz-sm);
  line-height: 2;
  cursor: pointer;
}
.fcol p:hover {
  color: #fff;
}
.copyright {
  text-align: center;
  font-size: var(--fz-xs);
  color: #777;
  border-top: 1px solid #3a3a3a;
  padding-top: var(--sp-4);
}
</style>
