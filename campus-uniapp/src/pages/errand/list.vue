<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">跑腿</text>
		</view>

		<scroll-view scroll-x class="cate-bar" :show-scrollbar="false">
			<view class="cate-item" :class="{ active: type === null }" @tap="switchType(null)">全部</view>
			<view class="cate-item" :class="{ active: type === 1 }" @tap="switchType(1)">代取快递</view>
			<view class="cate-item" :class="{ active: type === 2 }" @tap="switchType(2)">代买</view>
			<view class="cate-item" :class="{ active: type === 3 }" @tap="switchType(3)">其他</view>
		</scroll-view>

		<scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
			<view v-for="e in list" :key="e.id" class="task-card" @tap="goDetail(e.id)">
				<view class="card-head">
					<text class="type-tag">{{ typeLabel(e.type) }}</text>
					<text class="title">{{ e.title }}</text>
					<text class="reward">¥{{ e.reward }}</text>
				</view>
				<text class="content">{{ e.content }}</text>
				<view class="addr-row" v-if="e.pickupAddr || e.deliverAddr">
					<text class="addr"><u-icon name="map" size="12" color="#999"></u-icon> {{ e.pickupAddr || '—' }} → {{ e.deliverAddr || '—' }}</text>
				</view>
				<view class="card-foot">
					<text class="time">{{ formatTime(e.createTime) }}</text>
					<text class="accept-btn">去接单</text>
				</view>
			</view>
			<view v-if="!loading && list.length === 0" class="empty">暂无可接任务</view>
			<view v-if="loading" class="tip">加载中...</view>
			<view v-else-if="finished && list.length > 0" class="tip">没有更多了</view>
		</scroll-view>

		<view class="fab" @tap="goPublish"><u-icon name="plus" color="#fff" size="26"></u-icon></view>
	</view>
</template>

<script>
	import { getErrandList } from '@/utils/api.js'
	export default {
		data() { return { statusBarHeight: 20, list: [], page: 1, pageSize: 10, type: null, loading: false, finished: false } },
		onLoad() { this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20; this.loadData(true) },
		onShow() { if (this._needRefresh) { this._needRefresh = false; this.loadData(true) } },
		onPullDownRefresh() { this.loadData(true) },
		methods: {
			typeLabel(t) { return { 1: '代取快递', 2: '代买', 3: '其他' }[t] || '其他' },
			formatTime(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' },
			switchType(t) { if (this.type === t) return; this.type = t; this.loadData(true) },
			async loadData(reset = false) {
				if (this.loading) return
				if (reset) { this.page = 1; this.finished = false; this.list = [] }
				this.loading = true
				try {
					const res = await getErrandList({ page: this.page, pageSize: this.pageSize, type: this.type === null ? undefined : this.type })
					const rows = (res.data && res.data.list) || []
					this.list = reset ? rows : this.list.concat(rows)
					if (rows.length < this.pageSize) this.finished = true
				} catch (e) {} finally { this.loading = false; uni.stopPullDownRefresh() }
			},
			loadMore() { if (this.finished || this.loading) return; this.page++; this.loadData(false) },
			goDetail(id) { this._needRefresh = true; uni.navigateTo({ url: `/pages/errand/detail?id=${id}` }) },
			goPublish() { this._needRefresh = true; uni.navigateTo({ url: '/pages/errand/publish' }) },
			goBack() { uni.navigateBack() }
		}
	}
</script>

<style lang="scss" scoped>
	.page { min-height: 100vh; background: #f5f6f8; }
	.nav-bar { height: 44px; display: flex; align-items: center; position: relative; background: #fff; }
	.nav-back { position: absolute; left: 12px; padding: 6px; }
	.nav-title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #222; }
	.cate-bar { white-space: nowrap; background: #fff; padding: 8px 12px; border-bottom: 1px solid #f0f0f0; }
	.cate-item { display: inline-block; padding: 6px 16px; margin-right: 8px; font-size: 14px; color: #666; background: #f2f3f5; border-radius: 16px; }
	.cate-item.active { color: #fff; background: #07C160; }
	.list-scroll { height: calc(100vh - 44px - 53px); padding: 12px; box-sizing: border-box; }
	.task-card { background: #fff; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
	.card-head { display: flex; align-items: center; }
	.type-tag { font-size: 11px; color: #07C160; background: rgba(7,193,96,0.1); border-radius: 4px; padding: 2px 6px; margin-right: 8px; flex-shrink: 0; }
	.title { flex: 1; font-size: 15px; font-weight: 600; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
	.reward { font-size: 18px; color: #fa5151; font-weight: 700; margin-left: 8px; }
	.content { font-size: 13px; color: #888; margin-top: 8px; line-height: 1.5; }
	.addr-row { margin-top: 8px; }
	.addr { font-size: 12px; color: #999; }
	.card-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f5f5f5; }
	.time { font-size: 12px; color: #bbb; }
	.accept-btn { font-size: 13px; color: #fff; background: #07C160; padding: 4px 14px; border-radius: 14px; }
	.empty, .tip { text-align: center; color: #999; font-size: 13px; padding: 24px 0; }
	.fab { position: fixed; right: 20px; bottom: 40px; width: 52px; height: 52px; border-radius: 50%; background: #07C160; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(7,193,96,0.4); }
</style>
