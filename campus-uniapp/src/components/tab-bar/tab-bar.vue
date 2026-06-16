<template>
	<view class="tab-bar">
		<view class="tab-item" :class="{ active: current === 'home' }" @tap="go('home')">
			<u-icon :name="current === 'home' ? 'home-fill' : 'home'" :color="current === 'home' ? '#07C160' : '#999'" size="22"></u-icon>
			<text class="tab-label">首页</text>
		</view>
		<view class="tab-item" :class="{ active: current === 'orders' }" @tap="go('orders')">
			<u-icon name="order" :color="current === 'orders' ? '#07C160' : '#999'" size="22"></u-icon>
			<text class="tab-label">订单</text>
		</view>
		<view class="tab-item" :class="{ active: current === 'profile' }" @tap="go('profile')">
			<u-icon :name="current === 'profile' ? 'account-fill' : 'account'" :color="current === 'profile' ? '#07C160' : '#999'" size="22"></u-icon>
			<text class="tab-label">我的</text>
		</view>
	</view>
</template>

<script>
	const ROUTES = {
		home: '/pages/index/index',
		orders: '/pages/errand/orders',
		profile: '/pages/profile/index'
	}
	export default {
		name: 'tab-bar',
		props: {
			current: { type: String, default: 'home' }
		},
		methods: {
			go(key) {
				if (key === this.current) return
				uni.reLaunch({ url: ROUTES[key] })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.tab-bar {
		position: fixed; left: 0; right: 0; bottom: 0; z-index: 100;
		height: 100rpx; display: flex;
		background: #fff; border-top: 1rpx solid #f0f0f0;
		padding-bottom: env(safe-area-inset-bottom);
		box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.04);
	}
	.tab-item {
		flex: 1; display: flex; flex-direction: column;
		align-items: center; justify-content: center;
	}
	.tab-label { font-size: 22rpx; color: #999; margin-top: 4rpx; }
	.tab-item.active .tab-label { color: #07C160; }
</style>
