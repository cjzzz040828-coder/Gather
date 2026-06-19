<template>
  <div class="home">
    <!-- 背景大图 + 遮罩 -->
    <div class="hero-bg"></div>
    <div class="hero-mask"></div>
    <div class="hero-motion" aria-hidden="true">
      <span class="motion-line motion-line-a"></span>
      <span class="motion-line motion-line-b"></span>
      <span class="motion-line motion-line-c"></span>
    </div>

    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="brand" @click="scrollTop">
          <span class="wordmark">GATHER</span>
          <span class="brand-cn">集萃</span>
        </div>

        <nav class="nav-links">
          <a @click="goShop">拼团商城</a>
          <a @click="goOrders">我的拼单</a>
        </nav>

        <div class="nav-right">
          <el-dropdown v-if="userStore.isLogin" popper-class="user-dropdown" @command="onCommand">
            <span class="user-trigger">
              <span class="username">{{ userStore.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="orders">我的拼单</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button v-else class="login-btn" @click="authDialog.open()">登 录</button>
        </div>
      </div>
    </header>

    <!-- 主视觉 -->
    <section class="hero">
      <p class="hero-eyebrow">CAMPUS GROUP BUYING</p>
      <h1 class="hero-title">集萃<br />好物 一起拼</h1>
      <p class="hero-desc">发起拼团 · 邀请同学 · 成团享低价</p>
      <button class="cta-btn" @click="goShop">逛 一 逛</button>
    </section>

    <!-- 向下箭头 -->
    <div class="scroll-hint" @click="goShop">
      <el-icon :size="28"><ArrowDownBold /></el-icon>
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDownBold } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAuthDialogStore } from '@/stores/authDialog'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const authDialog = useAuthDialogStore()

// 受保护页面未登录被拦截回首页时（旧链接兼容），自动弹出全局登录框
onMounted(() => {
  if (route.query.redirect && !userStore.isLogin) {
    authDialog.open({ redirect: route.query.redirect as string })
  }
})

function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 逛商城无需登录，直接进入
function goShop() {
  router.push('/goods')
}

// 我的拼单需登录：未登录就地弹登录，成功后进入
function goOrders() {
  authDialog.requireLogin(() => router.push('/my-orders'))
}

function onCommand(command: string) {
  if (command === 'orders') router.push('/my-orders')
  else if (command === 'logout') userStore.logout()
}
</script>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?auto=format&fit=crop&w=2400&q=80');
  background-size: cover;
  background-position: center;
  transform: scale(1.05);
  z-index: 0;
  animation: heroDrift 18s ease-in-out infinite alternate;
}
.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(225, 37, 27, 0.35) 0%,
    rgba(10, 12, 24, 0.7) 100%
  );
  z-index: 1;
}
.hero-mask::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(110deg, transparent 0%, rgba(255, 255, 255, 0.1) 42%, transparent 58%),
    repeating-linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.025) 0,
      rgba(255, 255, 255, 0.025) 1px,
      transparent 1px,
      transparent 78px
    );
  opacity: 0.34;
  mix-blend-mode: screen;
  animation: maskSheen 9s ease-in-out infinite;
}
.hero-motion {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  overflow: hidden;
}
.motion-line {
  position: absolute;
  width: 42vw;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.42), transparent);
  opacity: 0;
  transform: rotate(-18deg);
  filter: blur(0.2px);
  animation: lineFloat 8s ease-in-out infinite;
}
.motion-line-a {
  top: 22%;
  right: -10%;
  animation-delay: 0.4s;
}
.motion-line-b {
  top: 58%;
  right: 6%;
  width: 32vw;
  animation-delay: 2.2s;
}
.motion-line-c {
  top: 74%;
  left: -8%;
  width: 36vw;
  animation-delay: 4.1s;
}

/* 导航栏 */
.navbar {
  position: relative;
  z-index: 10;
  animation: navReveal 0.8s ease-out both;
}
.nav-inner {
  width: min(calc(100% - 160px), 1680px);
  margin: 0 auto;
  height: 84px;
  display: flex;
  align-items: center;
  gap: var(--sp-8);
}
.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}
.brand-cn {
  display: flex;
  align-items: center;
  align-self: stretch;
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 5px;
  color: #fff;
  padding-left: 16px;
  border-left: 1px solid rgba(255, 255, 255, 0.35);
  user-select: none;
}
.wordmark {
  display: flex;
  align-items: center;
  align-self: stretch;
  font-family: 'Arial Narrow', 'Helvetica Neue', Arial, sans-serif;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 11px;
  color: #fff;
  text-transform: uppercase;
  padding-left: 5px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
  user-select: none;
  transition: letter-spacing 0.3s ease;
}
.wordmark:hover {
  letter-spacing: 13px;
}

.nav-links {
  display: flex;
  gap: var(--sp-6);
  flex: 1;
}
.nav-links a {
  position: relative;
  color: rgba(255, 255, 255, 0.85);
  font-size: var(--fz-md);
  letter-spacing: 1px;
  cursor: pointer;
  transition: color 0.2s, transform 0.2s;
}
.nav-links a::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -8px;
  height: 1px;
  background: rgba(255, 255, 255, 0.85);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.25s ease;
}
.nav-links a:hover {
  color: #fff;
  transform: translateY(-1px);
}
.nav-links a:hover::after {
  transform: scaleX(1);
}

.nav-right {
  flex-shrink: 0;
}
.login-btn {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #fff;
  padding: 9px 28px;
  border-radius: 22px;
  cursor: pointer;
  font-size: var(--fz-md);
  letter-spacing: 2px;
  backdrop-filter: blur(8px);
  transition: background 0.2s, color 0.2s, border-color 0.2s, transform 0.2s;
}
.login-btn:hover {
  background: #fff;
  color: #000;
  border-color: #fff;
  transform: translateY(-1px);
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
  color: #fff;
}
.username {
  font-size: var(--fz-base);
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主视觉文案 */
.hero {
  position: relative;
  z-index: 5;
  flex: 1;
  width: min(calc(100% - 160px), 1680px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.hero-eyebrow {
  color: rgba(255, 255, 255, 0.7);
  font-size: var(--fz-md);
  letter-spacing: 6px;
  margin-bottom: var(--sp-4);
  animation: textReveal 0.75s 0.2s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
.hero-title {
  color: #fff;
  font-size: 64px;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: 4px;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
  margin-bottom: var(--sp-5);
  animation: textReveal 0.85s 0.35s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
.hero-desc {
  color: rgba(255, 255, 255, 0.88);
  font-size: var(--fz-lg);
  letter-spacing: 2px;
  margin-bottom: var(--sp-8);
  animation: textReveal 0.8s 0.52s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
.cta-btn {
  position: relative;
  align-self: flex-start;
  background: transparent;
  border: 1.5px solid #fff;
  color: #fff;
  padding: 14px 48px;
  font-size: var(--fz-md);
  letter-spacing: 4px;
  cursor: pointer;
  overflow: hidden;
  transition:
    background 0.25s,
    color 0.25s,
    transform 0.25s,
    box-shadow 0.25s;
  animation: textReveal 0.8s 0.68s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
.cta-btn::before {
  content: '';
  position: absolute;
  top: -30%;
  bottom: -30%;
  left: -45%;
  width: 36%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.65), transparent);
  transform: skewX(-18deg);
  transition: left 0.55s ease;
}
.cta-btn:hover {
  background: #fff;
  color: #000;
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(0, 0, 0, 0.2);
}
.cta-btn:hover::before {
  left: 112%;
}

/* 向下箭头 */
.scroll-hint {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  animation: bounce 2.2s ease-in-out infinite;
}
@keyframes heroDrift {
  0% {
    transform: scale(1.05) translate3d(0, 0, 0);
  }
  100% {
    transform: scale(1.1) translate3d(-18px, -10px, 0);
  }
}
@keyframes maskSheen {
  0%,
  100% {
    opacity: 0.22;
    transform: translateX(-4%);
  }
  50% {
    opacity: 0.38;
    transform: translateX(4%);
  }
}
@keyframes lineFloat {
  0%,
  100% {
    opacity: 0;
    transform: translate3d(-18px, 18px, 0) rotate(-18deg);
  }
  28%,
  64% {
    opacity: 0.42;
  }
  50% {
    transform: translate3d(28px, -14px, 0) rotate(-18deg);
  }
}
@keyframes navReveal {
  from {
    opacity: 0;
    transform: translateY(-14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes textReveal {
  from {
    opacity: 0;
    transform: translateY(22px);
    filter: blur(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}
@keyframes bounce {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(8px);
  }
}

@media (max-width: 640px) {
  .nav-inner,
  .hero {
    width: calc(100% - 40px);
  }

  .nav-links {
    display: none;
  }
  .hero-title {
    font-size: 40px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-bg,
  .hero-mask::after,
  .motion-line,
  .navbar,
  .hero-eyebrow,
  .hero-title,
  .hero-desc,
  .cta-btn,
  .scroll-hint {
    animation: none;
  }

  .hero-eyebrow,
  .hero-title,
  .hero-desc,
  .cta-btn,
  .navbar {
    opacity: 1;
    transform: none;
    filter: none;
  }
}
</style>
