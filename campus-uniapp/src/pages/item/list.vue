<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">失物·二手</text>
		</view>

		<scroll-view scroll-x class="cate-bar" :show-scrollbar="false">
			<view class="cate-item" :class="{ active: type === null }" @tap="switchType(null)">全部</view>
			<view class="cate-item" :class="{ active: type === 1 }" @tap="switchType(1)">寻物</view>
			<view class="cate-item" :class="{ active: type === 2 }" @tap="switchType(2)">招领</view>
			<view class="cate-item" :class="{ active: type === 3 }" @tap="switchType(3)">二手</view>
		</scroll-view>

		<scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
			<view v-for="item in list" :key="item.id" class="item-card" @tap="goDetail(item.id)">
				<image v-if="firstImg(item.images)" class="thumb" :src="firstImg(item.images)" mode="aspectFill"></image>
				<view v-else class="thumb placeholder"><u-icon name="photo" color="#ccc" size="28"></u-icon></view>
				<view class="info">
					<view class="row1">
						<text class="type-tag" :class="'t' + item.type">{{ typeLabel(item.type) }}</text>
						<text class="title">{{ item.title }}</text>
					</view>
					<text class="desc">{{ item.content }}</text>
					<view class="row3">
						<text v-if="item.type === 3 && item.price != null" class="price">¥{{ item.price }}</text>
						<text v-else class="cate">{{ item.category || '' }}</text>
						<text class="loc">{{ item.location || '' }}</text>
					</view>
				</view>
			</view>
			<view v-if="!loading && list.length === 0" class="empty">暂无信息</view>
			<view v-if="loading" class="tip">加载中...</view>
			<view v-else-if="finished && list.length > 0" class="tip">没有更多了</view>
		</scroll-view>

		<view class="fab" @tap="goPublish">
			<u-icon name="plus" color="#fff" size="26"></u-icon>
		</view>
	</view>
</template>

<script>
	import { getItemList } from '@/utils/api.js'
	export default {
		data() {
			return { statusBarHeight: 20, list: [], page: 1, pageSize: 10, type: null, loading: false, finished: false }
		},
		onLoad() {
			this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
			this.loadData(true)
		},
		onShow() {
			// 发帖返回后刷新
			if (this._needRefresh) { this._needRefresh = false; this.loadData(true) }
		},
		onPullDownRefresh() { this.loadData(true) },
		methods: {
			typeLabel(t) { return { 1: '寻物', 2: '招领', 3: '二手' }[t] || '' },
			firstImg(s) { return s ? s.split(',').filter(Boolean)[0] : '' },
			switchType(t) { if (this.type === t) return; this.type = t; this.loadData(true) },
			async loadData(reset = false) {
				if (this.loading) return
				if (reset) { this.page = 1; this.finished = false; this.list = [] }
				this.loading = true
				try {
					const res = await getItemList({ page: this.page, pageSize: this.pageSize, type: this.type === null ? undefined : this.type })
					const rows = (res.data && res.data.list) || []
					this.list = reset ? rows : this.list.concat(rows)
					if (rows.length < this.pageSize) this.finished = true
				} catch (e) {} finally { this.loading = false; uni.stopPullDownRefresh() }
			},
			loadMore() { if (this.finished || this.loading) return; this.page++; this.loadData(false) },
			goDetail(id) { uni.navigateTo({ url: `/pages/item/detail?id=${id}` }) },
			goPublish() { this._needRefresh = true; uni.navigateTo({ url: '/pages/item/publish' }) },
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
	.item-card { display: flex; background: #fff; border-radius: 10px; padding: 12px; margin-bottom: 12px; }
	.thumb { width: 84px; height: 84px; border-radius: 8px; margin-right: 12px; flex-shrink: 0; }
	.thumb.placeholder { display: flex; align-items: center; justify-content: center; background: #f2f3f5; }
	.info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
	.row1 { display: flex; align-items: center; }
	.type-tag { font-size: 11px; color: #fff; border-radius: 4px; padding: 1px 6px; margin-right: 6px; flex-shrink: 0; }
	.type-tag.t1 { background: #faad14; }
	.type-tag.t2 { background: #07C160; }
	.type-tag.t3 { background: #1890ff; }
	.title { font-size: 15px; font-weight: 600; color: #222; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
	.desc { font-size: 13px; color: #888; margin-top: 6px; line-height: 1.5; max-height: 40px; overflow: hidden; }
	.row3 { display: flex; align-items: center; margin-top: auto; padding-top: 8px; }
	.price { font-size: 16px; color: #fa5151; font-weight: 600; margin-right: 12px; }
	.cate { font-size: 12px; color: #07C160; margin-right: 12px; }
	.loc { font-size: 12px; color: #bbb; }
	.empty, .tip { text-align: center; color: #999; font-size: 13px; padding: 24px 0; }
	.fab { position: fixed; right: 20px; bottom: 40px; width: 52px; height: 52px; border-radius: 50%; background: #07C160; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(7,193,96,0.4); }
</style>
