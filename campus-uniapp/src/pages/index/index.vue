<template>
	<view class="page">
		<!-- 背景装饰：柔和渐变 + 角落叶子 -->
		<view class="bg-deco">
			<text class="leaf leaf-tl">🌿</text>
			<text class="leaf leaf-tr">🍃</text>
		</view>

		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

		<!-- 顶部毛玻璃条 -->
		<view class="top-bar">
			<view class="top-pill" @tap="goMessage">
				<view class="top-logo"><text class="logo-emoji">🐱</text></view>
				<text class="top-title">集萃</text>
			</view>
			<view class="top-actions">
				<view class="action-dots"><text class="dots">···</text></view>
				<view class="action-round"><text class="round-dot">◉</text></view>
			</view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- Banner 主视觉 -->
			<view class="hero">
				<view class="hero-text">
					<text class="hero-l1">集萃</text>
					<text class="hero-l2">麻烦秒清空</text>
				</view>
				<text class="hero-art">🧑‍🎤</text>
			</view>

			<!-- 公告条（毛玻璃） -->
			<view class="notice-bar" @tap="onNotice">
				<view class="notice-ic"><u-icon name="volume" color="#3aa564" size="16"></u-icon></view>
				<text class="notice-text">点我可以弹窗、跳转小程序、网页等等</text>
			</view>

			<!-- 两个推荐大卡 -->
			<view class="big-cards">
				<view class="big-card c1" @tap="goHall(3)">
					<view class="bc-info">
						<text class="bc-title">扔垃圾</text>
						<view class="bc-btn">去下单</view>
					</view>
					<text class="bc-emoji">🗑️</text>
				</view>
				<view class="big-card c2" @tap="goHall(2)">
					<view class="bc-info">
						<text class="bc-title">团购专区</text>
						<view class="bc-btn">去下单</view>
					</view>
					<text class="bc-emoji">🛍️</text>
				</view>
			</view>

			<!-- 服务九宫格 -->
			<view class="grid-wrap">
				<view class="grid-item" v-for="(s, i) in services" :key="i" @tap="goHall(s.type)">
					<view class="grid-icon" :style="{ background: s.bg }">
						<text class="grid-emoji">{{ s.emoji }}</text>
					</view>
					<text class="grid-label">{{ s.label }}</text>
				</view>
			</view>

			<!-- 底部价格卡 -->
			<view class="price-cards">
				<view class="price-card pc1" @tap="onRecruit">
					<view class="pc-head">
						<text class="pc-title">打印卡</text>
						<text class="pc-en">PRINT CARD</text>
					</view>
					<text class="pc-desc">可低至全场8折优惠</text>
					<view class="pc-foot">
						<view class="pc-price"><text class="pc-num">¥ 2</text><text class="pc-unit">/次</text></view>
						<text class="pc-emoji">🖨️</text>
						<view class="pc-btn">去开卡</view>
					</view>
				</view>
				<view class="price-card pc2" @tap="onRecruit">
					<view class="pc-head">
						<text class="pc-title">回收卡</text>
						<text class="pc-en">RECYCLE CARD</text>
					</view>
					<text class="pc-desc">可低至全场8折优惠</text>
					<view class="pc-foot">
						<view class="pc-price"><text class="pc-num">¥ 5</text><text class="pc-unit">/次</text></view>
						<text class="pc-emoji">♻️</text>
						<view class="pc-btn">去开卡</view>
					</view>
				</view>
			</view>

			<view class="bottom-space"></view>
		</scroll-view>

		<tab-bar current="home"></tab-bar>
	</view>
</template>

<script>
	import { checkLogin } from '../../utils/auth.js'
	export default {
		data() {
			return {
				statusBarHeight: 20,
				services: [
					{ label: '取快递', emoji: '📦', bg: 'linear-gradient(135deg,#fff4e0,#ffe8c7)', type: 1 },
					{ label: '帮我买', emoji: '🛒', bg: 'linear-gradient(135deg,#e3f9e9,#cdf3da)', type: 2 },
					{ label: '取奶茶', emoji: '🧋', bg: 'linear-gradient(135deg,#fff1e6,#ffe0cc)', type: 2 },
					{ label: '取咖啡', emoji: '☕', bg: 'linear-gradient(135deg,#efe7df,#e3d5c8)', type: 2 },
					{ label: '打印服务', emoji: '🖨️', bg: 'linear-gradient(135deg,#e6f1ff,#d2e6ff)', type: 3 },
					{ label: '互帮互助', emoji: '🤝', bg: 'linear-gradient(135deg,#ffe9ef,#ffd6e2)', type: 3 },
					{ label: '清洁服务', emoji: '🧹', bg: 'linear-gradient(135deg,#e6fbfa,#cdf5f3)', type: 3 },
					{ label: '游戏开黑', emoji: '🎮', bg: 'linear-gradient(135deg,#fde8f4,#fbd2ea)', type: 3 },
					{ label: '旧物回收', emoji: '♻️', bg: 'linear-gradient(135deg,#e9f9e0,#d5f3c7)', type: 3 }
				]
			}
		},
		onLoad() {
			this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
		},
		onShow() {
			if (!checkLogin()) return
		},
		methods: {
			goMessage() { uni.navigateTo({ url: '/pages/message/index' }) },
			goHall(type) {
				const url = type ? `/pages/errand/list?type=${type}` : '/pages/errand/list'
				uni.navigateTo({ url })
			},
			onNotice() { uni.showToast({ title: '公告功能开发中', icon: 'none' }) },
			onRecruit() { uni.showToast({ title: '接单员招募开发中', icon: 'none' }) }
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		height: 100vh;
		display: flex;
		flex-direction: column;
		position: relative;
		overflow: hidden;
		background: linear-gradient(180deg, #c9f0d2 0%, #e6f8ea 22%, #f4faf0 46%, #f7f8f2 100%);
	}

	/* 背景装饰叶子 */
	.bg-deco { position: absolute; top: 0; left: 0; right: 0; height: 600rpx; pointer-events: none; z-index: 0; }
	.leaf { position: absolute; font-size: 120rpx; opacity: 0.5; }
	.leaf-tl { top: -20rpx; left: -24rpx; transform: rotate(-20deg); }
	.leaf-tr { top: 120rpx; right: -10rpx; transform: rotate(25deg); }

	.status-bar { width: 100%; position: relative; z-index: 2; }

	/* 顶部毛玻璃条 */
	.top-bar {
		display: flex; align-items: center; justify-content: space-between;
		padding: 8rpx 28rpx 12rpx; position: relative; z-index: 2; flex-shrink: 0;
	}
	.top-pill {
		display: flex; align-items: center;
		background: rgba(255,255,255,0.55); backdrop-filter: blur(10px);
		border-radius: 40rpx; padding: 8rpx 24rpx 8rpx 8rpx;
		box-shadow: 0 4rpx 16rpx rgba(58,165,100,0.1);
	}
	.top-logo {
		width: 48rpx; height: 48rpx; border-radius: 50%;
		background: linear-gradient(135deg,#1f1f1f,#3a3a3a);
		display: flex; align-items: center; justify-content: center; margin-right: 12rpx;
	}
	.logo-emoji { font-size: 26rpx; }
	.top-title { font-size: 28rpx; font-weight: 700; color: #2a3a2e; }
	.top-actions {
		display: flex; align-items: center;
		background: rgba(255,255,255,0.55); backdrop-filter: blur(10px);
		border-radius: 40rpx; padding: 6rpx 6rpx; box-shadow: 0 4rpx 16rpx rgba(58,165,100,0.1);
	}
	.action-dots { width: 56rpx; height: 48rpx; display: flex; align-items: center; justify-content: center; }
	.dots { font-size: 30rpx; color: #2a3a2e; letter-spacing: 2rpx; }
	.action-round {
		width: 48rpx; height: 48rpx; border-radius: 50%; background: #fff;
		display: flex; align-items: center; justify-content: center;
	}
	.round-dot { font-size: 24rpx; color: #2a3a2e; }

	.content-scroll { flex: 1; overflow: hidden; padding: 0 28rpx; box-sizing: border-box; position: relative; z-index: 1; }

	/* Hero 主视觉 */
	.hero {
		margin-top: 8rpx; height: 280rpx; position: relative;
		display: flex; align-items: center;
	}
	.hero-text { position: relative; z-index: 2; }
	.hero-l1 {
		font-size: 64rpx; font-weight: 900; color: #1f6b3a; display: block;
		letter-spacing: 2rpx; text-shadow: 0 2rpx 0 rgba(255,255,255,0.6);
	}
	.hero-l2 {
		font-size: 64rpx; font-weight: 900; color: #1f6b3a; display: block;
		margin-top: 8rpx; letter-spacing: 2rpx; text-shadow: 0 2rpx 0 rgba(255,255,255,0.6);
	}
	.hero-art {
		position: absolute; right: -10rpx; top: -20rpx; font-size: 240rpx;
		filter: drop-shadow(0 12rpx 20rpx rgba(0,0,0,0.12));
	}

	/* 公告条（毛玻璃） */
	.notice-bar {
		margin-top: 8rpx; display: flex; align-items: center;
		background: rgba(255,255,255,0.6); backdrop-filter: blur(8px);
		border-radius: 40rpx; padding: 18rpx 28rpx;
		box-shadow: 0 4rpx 18rpx rgba(58,165,100,0.08);
	}
	.notice-ic { margin-right: 14rpx; flex-shrink: 0; }
	.notice-text { font-size: 24rpx; color: #6a8a74; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

	/* 两个推荐大卡 */
	.big-cards { display: flex; gap: 20rpx; margin-top: 24rpx; }
	.big-card {
		flex: 1; height: 180rpx; border-radius: 28rpx; padding: 28rpx;
		box-sizing: border-box; display: flex; align-items: center; justify-content: space-between;
		position: relative; overflow: hidden;
		box-shadow: 0 8rpx 24rpx rgba(58,165,100,0.12);
	}
	.big-card.c1 { background: linear-gradient(135deg, #d4f3d9 0%, #b8ebc4 100%); }
	.big-card.c2 { background: linear-gradient(135deg, #ffe9c9 0%, #ffd9a8 100%); }
	.bc-info { display: flex; flex-direction: column; z-index: 1; }
	.bc-title { font-size: 32rpx; font-weight: 800; color: #2a3a2e; }
	.bc-btn {
		margin-top: 16rpx; align-self: flex-start;
		font-size: 22rpx; color: #fff; background: rgba(42,58,46,0.85);
		border-radius: 24rpx; padding: 6rpx 20rpx;
	}
	.bc-emoji { font-size: 90rpx; filter: drop-shadow(0 6rpx 10rpx rgba(0,0,0,0.12)); }

	/* 服务九宫格 */
	.grid-wrap {
		margin-top: 24rpx; background: rgba(255,255,255,0.7); backdrop-filter: blur(6px);
		border-radius: 32rpx; padding: 32rpx 12rpx; display: flex; flex-wrap: wrap;
		box-shadow: 0 8rpx 28rpx rgba(58,165,100,0.08);
	}
	.grid-item { width: 25%; display: flex; flex-direction: column; align-items: center; margin-bottom: 28rpx; }
	.grid-item:nth-last-child(-n+1) { margin-bottom: 0; }
	.grid-icon {
		width: 100rpx; height: 100rpx; border-radius: 28rpx;
		display: flex; align-items: center; justify-content: center; margin-bottom: 14rpx;
		box-shadow: 0 6rpx 14rpx rgba(0,0,0,0.06);
	}
	.grid-emoji { font-size: 52rpx; filter: drop-shadow(0 4rpx 6rpx rgba(0,0,0,0.1)); }
	.grid-label { font-size: 24rpx; color: #4a5a4e; }

	/* 底部价格卡 */
	.price-cards { display: flex; gap: 20rpx; margin-top: 24rpx; }
	.price-card {
		flex: 1; border-radius: 28rpx; padding: 24rpx 26rpx; box-sizing: border-box;
		position: relative; overflow: hidden;
		box-shadow: 0 8rpx 24rpx rgba(58,165,100,0.1);
	}
	.price-card.pc1 { background: linear-gradient(160deg, #fff6e3 0%, #ffedd0 100%); }
	.price-card.pc2 { background: linear-gradient(160deg, #e9f9ee 0%, #d6f3df 100%); }
	.pc-head { display: flex; align-items: baseline; }
	.pc-title { font-size: 30rpx; font-weight: 800; color: #2a3a2e; }
	.pc-en { font-size: 18rpx; color: #b3a98f; margin-left: 12rpx; letter-spacing: 1rpx; }
	.pc-desc { font-size: 20rpx; color: #9aa89e; margin-top: 8rpx; display: block; }
	.pc-foot { display: flex; align-items: center; margin-top: 18rpx; position: relative; }
	.pc-price { display: flex; align-items: baseline; }
	.pc-num { font-size: 36rpx; font-weight: 900; color: #fa5151; }
	.pc-unit { font-size: 20rpx; color: #fa5151; margin-left: 4rpx; }
	.pc-emoji { font-size: 56rpx; position: absolute; right: 70rpx; bottom: -6rpx; filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.12)); }
	.pc-btn {
		margin-left: auto; font-size: 20rpx; color: #fff;
		background: linear-gradient(135deg,#2bbd6a,#1f9d54);
		border-radius: 24rpx; padding: 8rpx 18rpx;
	}

	.bottom-space { height: 140rpx; }
</style>
