<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" v-if="canBack" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">我的订单</text>
		</view>

		<view class="tabs">
			<view class="tab" :class="{ active: role === 'publish' }" @tap="switchRole('publish')">我发布的</view>
			<view class="tab" :class="{ active: role === 'accept' }" @tap="switchRole('accept')">我接的单</view>
		</view>

		<scroll-view scroll-x class="cate-bar" :show-scrollbar="false">
			<view class="cate-item" :class="{ active: type === null }" @tap="switchType(null)">全部</view>
			<view class="cate-item" :class="{ active: type === 1 }" @tap="switchType(1)">代取快递</view>
			<view class="cate-item" :class="{ active: type === 2 }" @tap="switchType(2)">代买</view>
			<view class="cate-item" :class="{ active: type === 3 }" @tap="switchType(3)">其他</view>
		</scroll-view>

		<scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
			<!-- 骨架屏：首次加载且无数据时显示 -->
			<view v-if="loading && list.length === 0" class="skeleton">
				<view class="sk-card" v-for="n in 4" :key="n">
					<view class="sk-head">
						<view class="sk-tag"></view>
						<view class="sk-title"></view>
						<view class="sk-status"></view>
					</view>
					<view class="sk-foot">
						<view class="sk-reward"></view>
						<view class="sk-time"></view>
					</view>
				</view>
			</view>

			<template v-else>
				<view v-for="e in list" :key="e.id" class="task-card" @tap="goDetail(e.id)">
					<view class="card-head">
						<text class="type-tag">{{ typeLabel(e.type) }}</text>
						<text class="title">{{ e.title }}</text>
						<text class="status-tag" :class="'s' + e.status">{{ statusLabel(e.status) }}</text>
					</view>
					<view class="card-foot">
						<text class="reward">¥{{ e.reward }}</text>
						<text class="time">{{ formatTime(e.createTime) }}</text>
					</view>
				</view>
				<view v-if="!loading && list.length === 0" class="empty">暂无记录</view>
				<view v-if="loading && list.length > 0" class="tip">加载中...</view>
				<view v-else-if="finished && list.length > 0" class="tip">没有更多了</view>
			</template>
		</scroll-view>

		<tab-bar current="orders"></tab-bar>
	</view>
</template>

<script>
	import { getMyErrands } from '@/utils/api.js'
	export default {
		data() { return { statusBarHeight: 20, canBack: false, role: 'publish', type: null, list: [], page: 1, pageSize: 10, loading: true, finished: false } },
		onLoad() { this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20; this.canBack = getCurrentPages().length > 1; this.loadData(true) },
		onShow() { if (this._loaded) this.loadData(true); this._loaded = true },
		methods: {
			typeLabel(t) { return { 1: '代取快递', 2: '代买', 3: '其他' }[t] || '其他' },
			statusLabel(s) { return { 0: '待支付', 1: '待接单', 2: '进行中', 3: '已送达', 4: '已完成', 5: '已取消' }[s] || '' },
			formatTime(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' },
			switchRole(r) { if (this.role === r) return; this.role = r; this.loadData(true) },
			switchType(t) { if (this.type === t) return; this.type = t; this.loadData(true) },
			async loadData(reset = false) {
				if (this.loading && !reset && this.list.length) return
				if (reset) { this.page = 1; this.finished = false; this.list = [] }
				this.loading = true
				try {
					const res = await getMyErrands({ page: this.page, pageSize: this.pageSize, role: this.role, type: this.type === null ? undefined : this.type })
					const rows = (res.data && res.data.list) || []
					this.list = reset ? rows : this.list.concat(rows)
					if (rows.length < this.pageSize) this.finished = true
				} catch (e) {} finally { this.loading = false }
			},
			loadMore() { if (this.finished || this.loading) return; this.page++; this.loadData(false) },
			goDetail(id) { uni.navigateTo({ url: `/pages/errand/detail?id=${id}` }) },
			goBack() { uni.navigateBack() }
		}
	}
</script>

<style lang="scss" scoped>
	.page { min-height: 100vh; background: #f5f6f8; }
	.nav-bar { height: 44px; display: flex; align-items: center; position: relative; background: #fff; }
	.nav-back { position: absolute; left: 12px; padding: 6px; }
	.nav-title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #222; }
	.tabs { display: flex; background: #fff; border-bottom: 1px solid #f0f0f0; }
	.tab { flex: 1; text-align: center; padding: 12px 0; font-size: 15px; color: #666; position: relative; }
	.tab.active { color: #07C160; font-weight: 600; }
	.tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 32px; height: 3px; background: #07C160; border-radius: 2px; }
	.cate-bar { white-space: nowrap; background: #fff; padding: 8px 12px; border-bottom: 1px solid #f0f0f0; }
	.cate-item { display: inline-block; padding: 6px 16px; margin-right: 8px; font-size: 14px; color: #666; background: #f2f3f5; border-radius: 16px; }
	.cate-item.active { color: #fff; background: #07C160; }
	.list-scroll { height: calc(100vh - 44px - 49px - 53px - 50px); padding: 12px; box-sizing: border-box; }
	.task-card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
	.card-head { display: flex; align-items: center; }
	.type-tag { font-size: 11px; color: #07C160; background: rgba(7,193,96,0.1); border-radius: 4px; padding: 2px 6px; margin-right: 8px; flex-shrink: 0; }
	.title { flex: 1; font-size: 15px; font-weight: 600; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
	.status-tag { font-size: 12px; padding: 2px 8px; border-radius: 4px; flex-shrink: 0; }
	.status-tag.s0 { color: #faad14; background: rgba(250,173,20,0.1); }
	.status-tag.s1 { color: #1890ff; background: rgba(24,144,255,0.1); }
	.status-tag.s2 { color: #1890ff; background: rgba(24,144,255,0.1); }
	.status-tag.s3 { color: #07C160; background: rgba(7,193,96,0.1); }
	.status-tag.s4 { color: #07C160; background: rgba(7,193,96,0.1); }
	.status-tag.s5 { color: #999; background: #f2f3f5; }
	.card-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f5f5f5; }
	.reward { font-size: 16px; color: #fa5151; font-weight: 700; }
	.time { font-size: 12px; color: #bbb; }
	.empty, .tip { text-align: center; color: #999; font-size: 13px; padding: 24px 0; }

	/* 骨架屏 */
	.skeleton { padding-top: 0; }
	.sk-card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
	.sk-head { display: flex; align-items: center; }
	.sk-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 10px; border-top: 1px solid #f5f5f5; }
	.sk-tag, .sk-title, .sk-status, .sk-reward, .sk-time {
		border-radius: 4px;
		background: linear-gradient(90deg, #eef0f2 25%, #f6f7f8 37%, #eef0f2 63%);
		background-size: 400% 100%;
		animation: sk-shimmer 1.4s ease infinite;
	}
	.sk-tag { width: 48px; height: 16px; margin-right: 8px; }
	.sk-title { flex: 1; height: 16px; }
	.sk-status { width: 44px; height: 16px; margin-left: 8px; }
	.sk-reward { width: 60px; height: 18px; }
	.sk-time { width: 90px; height: 14px; }
	@keyframes sk-shimmer {
		0% { background-position: 100% 50%; }
		100% { background-position: 0 50%; }
	}
</style>
