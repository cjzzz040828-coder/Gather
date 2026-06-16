<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">详情</text>
		</view>

		<scroll-view scroll-y class="content-scroll" v-if="item">
			<swiper v-if="images.length" class="banner" :indicator-dots="images.length > 1" circular>
				<swiper-item v-for="(img, i) in images" :key="i">
					<image class="banner-img" :src="img" mode="aspectFill" @tap="preview(i)"></image>
				</swiper-item>
			</swiper>

			<view class="body">
				<view class="title-row">
					<text class="type-tag" :class="'t' + item.type">{{ typeLabel(item.type) }}</text>
					<text class="title">{{ item.title }}</text>
				</view>
				<text v-if="item.type === 3 && item.price != null" class="price">¥{{ item.price }}</text>

				<view class="meta-list">
					<view class="meta" v-if="item.category"><text class="ml">分类</text><text class="mv">{{ item.category }}</text></view>
					<view class="meta" v-if="item.location"><text class="ml">地点</text><text class="mv">{{ item.location }}</text></view>
					<view class="meta" v-if="item.contact"><text class="ml">联系</text><text class="mv">{{ item.contact }}</text></view>
				</view>

				<view class="desc-box">
					<text class="section-title">详细描述</text>
					<text class="desc">{{ item.content || '暂无描述' }}</text>
				</view>
			</view>
		</scroll-view>

		<view class="bottom-bar" v-if="item">
			<template v-if="isOwner">
				<button v-if="item.status === 1" class="btn ghost" @tap="onFinish">标记结束</button>
				<button class="btn danger" @tap="onDelete">删除</button>
			</template>
			<template v-else>
				<button class="btn ghost" @tap="onReport">举报</button>
				<button class="btn primary" @tap="onContact">联系 TA</button>
			</template>
		</view>
	</view>
</template>

<script>
	import { getItemDetail, finishItem, deleteItem, reportItem } from '@/utils/api.js'
	export default {
		data() {
			return { statusBarHeight: 20, item: null, myId: null }
		},
		computed: {
			images() { return this.item && this.item.images ? this.item.images.split(',').filter(Boolean) : [] },
			isOwner() { return this.item && this.myId && String(this.item.userId) === String(this.myId) }
		},
		onLoad(options) {
			this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
			const u = uni.getStorageSync('userInfo')
			this.myId = u ? (u.userId || u.id) : null
			if (options.id) this.load(options.id)
		},
		methods: {
			typeLabel(t) { return { 1: '寻物', 2: '招领', 3: '二手' }[t] || '' },
			async load(id) {
				try { const res = await getItemDetail(id); this.item = res.data } catch (e) {}
			},
			preview(idx) { uni.previewImage({ current: idx, urls: this.images }) },
			onContact() {
				const c = this.item.contact
				if (!c) { uni.showToast({ title: '对方未留联系方式', icon: 'none' }); return }
				uni.setClipboardData({ data: c, success: () => uni.showToast({ title: '联系方式已复制', icon: 'none' }) })
			},
			onFinish() {
				uni.showModal({
					title: '提示', content: '确认标记为已结束？',
					success: async (r) => {
						if (!r.confirm) return
						await finishItem(this.item.id)
						uni.showToast({ title: '已结束', icon: 'success' })
						this.load(this.item.id)
					}
				})
			},
			onDelete() {
				uni.showModal({
					title: '提示', content: '确认删除该帖子？',
					success: async (r) => {
						if (!r.confirm) return
						await deleteItem(this.item.id)
						uni.showToast({ title: '已删除', icon: 'success' })
						setTimeout(() => uni.navigateBack(), 800)
					}
				})
			},
			onReport() {
				uni.showModal({
					title: '举报', editable: true, placeholderText: '请输入举报理由',
					success: async (r) => {
						if (!r.confirm) return
						await reportItem(this.item.id, r.content || '违规内容')
						uni.showToast({ title: '举报已提交', icon: 'success' })
					}
				})
			},
			goBack() { uni.navigateBack() }
		}
	}
</script>

<style lang="scss" scoped>
	.page { min-height: 100vh; background: #f5f6f8; display: flex; flex-direction: column; }
	.nav-bar { height: 44px; display: flex; align-items: center; position: relative; background: #fff; }
	.nav-back { position: absolute; left: 12px; padding: 6px; }
	.nav-title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #222; }
	.content-scroll { flex: 1; }
	.banner { width: 100%; height: 300rpx; }
	.banner-img { width: 100%; height: 300rpx; }
	.body { background: #fff; padding: 16px; margin-bottom: 10px; }
	.title-row { display: flex; align-items: center; }
	.type-tag { font-size: 12px; color: #fff; border-radius: 4px; padding: 2px 8px; margin-right: 8px; }
	.type-tag.t1 { background: #faad14; } .type-tag.t2 { background: #07C160; } .type-tag.t3 { background: #1890ff; }
	.title { font-size: 18px; font-weight: 700; color: #1a1a1a; }
	.price { font-size: 22px; color: #fa5151; font-weight: 700; display: block; margin-top: 10px; }
	.meta-list { margin-top: 14px; }
	.meta { display: flex; padding: 6px 0; }
	.ml { width: 56px; font-size: 14px; color: #999; }
	.mv { flex: 1; font-size: 14px; color: #333; }
	.desc-box { margin-top: 14px; padding-top: 14px; border-top: 1px solid #f2f2f2; }
	.section-title { font-size: 14px; font-weight: 600; color: #333; display: block; margin-bottom: 8px; }
	.desc { font-size: 15px; color: #444; line-height: 1.7; }
	.bottom-bar { display: flex; padding: 10px 12px; background: #fff; border-top: 1px solid #f0f0f0; gap: 10px; }
	.btn { flex: 1; height: 40px; line-height: 40px; border-radius: 20px; font-size: 15px; margin: 0; }
	.btn.primary { background: #07C160; color: #fff; }
	.btn.ghost { background: #f2f3f5; color: #333; }
	.btn.danger { background: #fff1f0; color: #fa5151; }
</style>
