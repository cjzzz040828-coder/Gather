<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack">
				<u-icon name="arrow-left" color="#222" size="20"></u-icon>
			</view>
			<text class="nav-title">公告详情</text>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view v-if="notice" class="detail">
				<text class="title">{{ notice.title }}</text>
				<view class="meta">
					<text class="cate">{{ cateLabel(notice.category) }}</text>
					<text class="time">{{ formatTime(notice.publishTime) }}</text>
					<text class="views">浏览 {{ notice.viewCount || 0 }}</text>
				</view>
				<image v-if="notice.cover" class="cover" :src="notice.cover" mode="widthFix"></image>
				<rich-text class="rich" :nodes="notice.content || '<p>暂无内容</p>'"></rich-text>
			</view>
			<view v-else-if="!loading" class="empty">公告不存在或已下架</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getCampusNoticeDetail } from '@/utils/api.js'

	export default {
		data() {
			return {
				statusBarHeight: 20,
				notice: null,
				loading: false
			}
		},
		onLoad(options) {
			const sys = uni.getSystemInfoSync()
			this.statusBarHeight = sys.statusBarHeight || 20
			if (options.id) this.loadDetail(options.id)
		},
		methods: {
			cateLabel(c) {
				return { 1: '通知', 2: '活动', 3: '资讯' }[c] || '资讯'
			},
			formatTime(t) {
				if (!t) return ''
				return String(t).replace('T', ' ').substring(0, 16)
			},
			async loadDetail(id) {
				this.loading = true
				try {
					const res = await getCampusNoticeDetail(id)
					this.notice = res.data
				} catch (e) {
					// 错误已在请求层提示
				} finally {
					this.loading = false
				}
			},
			goBack() {
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		background: #fff;
	}
	.nav-bar {
		height: 44px;
		display: flex;
		align-items: center;
		position: relative;
		background: #fff;
		border-bottom: 1px solid #f0f0f0;
	}
	.nav-back {
		position: absolute;
		left: 12px;
		padding: 6px;
	}
	.nav-title {
		flex: 1;
		text-align: center;
		font-size: 17px;
		font-weight: 600;
		color: #222;
	}
	.content-scroll {
		height: calc(100vh - 44px);
	}
	.detail {
		padding: 16px;
	}
	.title {
		font-size: 20px;
		font-weight: 700;
		color: #1a1a1a;
		line-height: 1.5;
		display: block;
	}
	.meta {
		display: flex;
		align-items: center;
		margin: 12px 0 16px;
		padding-bottom: 14px;
		border-bottom: 1px solid #f2f2f2;
	}
	.cate {
		font-size: 12px;
		color: #07C160;
		margin-right: 12px;
	}
	.time {
		font-size: 12px;
		color: #999;
		margin-right: 12px;
	}
	.views {
		font-size: 12px;
		color: #bbb;
	}
	.cover {
		width: 100%;
		border-radius: 8px;
		margin-bottom: 16px;
	}
	.rich {
		font-size: 15px;
		color: #333;
		line-height: 1.8;
	}
	.empty {
		text-align: center;
		color: #999;
		padding: 60px 0;
	}
</style>
