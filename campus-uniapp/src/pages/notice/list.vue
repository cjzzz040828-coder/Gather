<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<text class="nav-title">公告</text>
		</view>

		<!-- 分类筛选 -->
		<scroll-view scroll-x class="cate-bar" :show-scrollbar="false">
			<view class="cate-item" :class="{ active: category === null }" @tap="switchCate(null)">全部</view>
			<view class="cate-item" :class="{ active: category === 1 }" @tap="switchCate(1)">通知</view>
			<view class="cate-item" :class="{ active: category === 2 }" @tap="switchCate(2)">活动</view>
			<view class="cate-item" :class="{ active: category === 3 }" @tap="switchCate(3)">资讯</view>
		</scroll-view>

		<scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
			<view v-for="item in list" :key="item.id" class="notice-card" @tap="goDetail(item.id)">
				<image v-if="item.cover" class="cover" :src="item.cover" mode="aspectFill"></image>
				<view class="info">
					<view class="title-row">
						<text v-if="item.top === 1" class="top-tag">置顶</text>
						<text class="title">{{ item.title }}</text>
					</view>
					<view class="meta">
						<text class="cate-label">{{ cateLabel(item.category) }}</text>
						<text class="time">{{ formatTime(item.publishTime) }}</text>
						<text class="views">浏览 {{ item.viewCount || 0 }}</text>
					</view>
				</view>
			</view>

			<view v-if="!loading && list.length === 0" class="empty">
				<text>暂无公告</text>
			</view>
			<view v-if="loading" class="loading-tip">加载中...</view>
			<view v-else-if="finished && list.length > 0" class="loading-tip">没有更多了</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getCampusNoticeList } from '@/utils/api.js'

	export default {
		data() {
			return {
				statusBarHeight: 20,
				list: [],
				page: 1,
				pageSize: 10,
				category: null,
				loading: false,
				finished: false
			}
		},
		onLoad() {
			const sys = uni.getSystemInfoSync()
			this.statusBarHeight = sys.statusBarHeight || 20
			this.loadData(true)
		},
		onPullDownRefresh() {
			this.loadData(true)
		},
		methods: {
			cateLabel(c) {
				return { 1: '通知', 2: '活动', 3: '资讯' }[c] || '资讯'
			},
			formatTime(t) {
				if (!t) return ''
				return String(t).replace('T', ' ').substring(0, 16)
			},
			switchCate(c) {
				if (this.category === c) return
				this.category = c
				this.loadData(true)
			},
			async loadData(reset = false) {
				if (this.loading) return
				if (reset) {
					this.page = 1
					this.finished = false
					this.list = []
				}
				this.loading = true
				try {
					const res = await getCampusNoticeList({
						page: this.page,
						pageSize: this.pageSize,
						category: this.category === null ? undefined : this.category
					})
					const data = res.data || {}
					const rows = data.list || []
					this.list = reset ? rows : this.list.concat(rows)
					if (rows.length < this.pageSize) this.finished = true
				} catch (e) {
					// 错误已在请求层提示
				} finally {
					this.loading = false
					uni.stopPullDownRefresh()
				}
			},
			loadMore() {
				if (this.finished || this.loading) return
				this.page++
				this.loadData(false)
			},
			goDetail(id) {
				uni.navigateTo({ url: `/pages/notice/detail?id=${id}` })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		background: #f5f6f8;
	}
	.nav-bar {
		height: 44px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #fff;
	}
	.nav-title {
		font-size: 17px;
		font-weight: 600;
		color: #222;
	}
	.cate-bar {
		white-space: nowrap;
		background: #fff;
		padding: 8px 12px;
		border-bottom: 1px solid #f0f0f0;
	}
	.cate-item {
		display: inline-block;
		padding: 6px 16px;
		margin-right: 8px;
		font-size: 14px;
		color: #666;
		background: #f2f3f5;
		border-radius: 16px;
	}
	.cate-item.active {
		color: #fff;
		background: #07C160;
	}
	.list-scroll {
		height: calc(100vh - 44px - 53px);
		padding: 12px;
		box-sizing: border-box;
	}
	.notice-card {
		display: flex;
		background: #fff;
		border-radius: 10px;
		padding: 12px;
		margin-bottom: 12px;
	}
	.cover {
		width: 100px;
		height: 72px;
		border-radius: 8px;
		margin-right: 12px;
		flex-shrink: 0;
	}
	.info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		min-width: 0;
	}
	.title-row {
		display: flex;
		align-items: center;
	}
	.top-tag {
		font-size: 11px;
		color: #fff;
		background: #fa5151;
		border-radius: 4px;
		padding: 1px 5px;
		margin-right: 6px;
		flex-shrink: 0;
	}
	.title {
		font-size: 15px;
		font-weight: 600;
		color: #222;
		line-height: 1.4;
	}
	.meta {
		display: flex;
		align-items: center;
		margin-top: 8px;
	}
	.cate-label {
		font-size: 12px;
		color: #07C160;
		margin-right: 10px;
	}
	.time {
		font-size: 12px;
		color: #999;
		margin-right: 10px;
	}
	.views {
		font-size: 12px;
		color: #bbb;
	}
	.empty, .loading-tip {
		text-align: center;
		color: #999;
		font-size: 13px;
		padding: 24px 0;
	}
</style>
