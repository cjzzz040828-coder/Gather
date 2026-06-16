<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">任务详情</text>
		</view>

		<scroll-view scroll-y class="content-scroll" v-if="e">
			<view class="status-banner" :class="'s' + e.status">
				<text class="status-text">{{ statusLabel(e.status) }}</text>
				<text class="reward">赏金 ¥{{ e.reward }}</text>
			</view>

			<view class="card">
				<view class="head">
					<text class="type-tag">{{ typeLabel(e.type) }}</text>
					<text class="title">{{ e.title }}</text>
				</view>
				<view class="meta" v-if="e.pickupAddr"><text class="ml">取件地</text><text class="mv">{{ e.pickupAddr }}</text></view>
				<view class="meta" v-if="e.deliverAddr"><text class="ml">送达地</text><text class="mv">{{ e.deliverAddr }}</text></view>
				<view class="meta" v-if="e.expectTime"><text class="ml">期望时间</text><text class="mv">{{ formatTime(e.expectTime) }}</text></view>
				<view class="desc-box">
					<text class="section">任务说明</text>
					<text class="desc">{{ e.content || '无' }}</text>
				</view>
			</view>

			<view class="card timeline">
				<view class="tl-item"><text class="dot done"></text><text class="tl-label">发布于 {{ formatTime(e.createTime) }}</text></view>
				<view class="tl-item" v-if="e.acceptTime"><text class="dot done"></text><text class="tl-label">接单于 {{ formatTime(e.acceptTime) }}</text></view>
				<view class="tl-item" v-if="e.finishTime"><text class="dot done"></text><text class="tl-label">完成于 {{ formatTime(e.finishTime) }}</text></view>
			</view>
		</scroll-view>

		<view class="bottom-bar" v-if="e && actionBtn">
			<button class="action-btn" :class="actionBtn.cls" :loading="busy" @tap="actionBtn.handler">{{ actionBtn.text }}</button>
			<button v-if="canCancel" class="action-btn ghost" @tap="onCancel">取消任务</button>
		</view>
	</view>
</template>

<script>
	import { getErrandDetail, acceptErrand, finishErrand, confirmErrand, cancelErrand, reviewErrand } from '@/utils/api.js'
	export default {
		data() { return { statusBarHeight: 20, e: null, myId: null, busy: false } },
		computed: {
			isPublisher() { return this.e && String(this.e.publisherId) === String(this.myId) },
			isRunner() { return this.e && this.e.runnerId && String(this.e.runnerId) === String(this.myId) },
			// 发单方在待接单/待支付可取消
			canCancel() { return this.isPublisher && (this.e.status === 0 || this.e.status === 1) },
			actionBtn() {
				if (!this.e) return null
				const s = this.e.status
				// 非相关人：待接单 → 可接单
				if (!this.isPublisher && !this.isRunner && s === 1) {
					return { text: '立即接单', cls: 'primary', handler: this.onAccept }
				}
				// 接单方：进行中 → 标记送达
				if (this.isRunner && s === 2) {
					return { text: '标记已送达', cls: 'primary', handler: this.onFinish }
				}
				// 发单方：已送达 → 确认完成
				if (this.isPublisher && s === 3) {
					return { text: '确认完成', cls: 'primary', handler: this.onConfirm }
				}
				// 发单方：已完成 → 评价
				if (this.isPublisher && s === 4) {
					return { text: '评价接单人', cls: 'warning', handler: this.onReview }
				}
				return null
			}
		},
		onLoad(options) {
			this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
			const u = uni.getStorageSync('userInfo')
			this.myId = u ? (u.userId || u.id) : null
			if (options.id) this.load(options.id)
		},
		methods: {
			typeLabel(t) { return { 1: '代取快递', 2: '代买', 3: '其他' }[t] || '其他' },
			statusLabel(s) { return { 0: '待支付', 1: '待接单', 2: '进行中', 3: '已送达', 4: '已完成', 5: '已取消' }[s] || '' },
			formatTime(t) { return t ? String(t).replace('T', ' ').substring(0, 16) : '' },
			async load(id) { try { const res = await getErrandDetail(id); this.e = res.data } catch (e) {} },
			async run(fn, okMsg) {
				this.busy = true
				try { await fn(); uni.showToast({ title: okMsg, icon: 'success' }); this.load(this.e.id) }
				catch (e) {} finally { this.busy = false }
			},
			onAccept() {
				uni.showModal({ title: '接单', content: '确认接下这个任务？', success: (r) => { if (r.confirm) this.run(() => acceptErrand(this.e.id), '接单成功') } })
			},
			onFinish() { this.run(() => finishErrand(this.e.id), '已标记送达') },
			onConfirm() {
				uni.showModal({ title: '确认完成', content: '确认对方已完成任务？', success: (r) => { if (r.confirm) this.run(() => confirmErrand(this.e.id), '已完成') } })
			},
			onCancel() {
				uni.showModal({ title: '取消任务', content: '确认取消该任务？', success: (r) => { if (r.confirm) this.run(() => cancelErrand(this.e.id), '已取消') } })
			},
			onReview() {
				uni.showActionSheet({
					itemList: ['5分 非常满意', '4分 满意', '3分 一般', '2分 较差', '1分 差'],
					success: (res) => {
						const score = 5 - res.tapIndex
						uni.showModal({
							title: '评价', editable: true, placeholderText: '说点什么（选填）',
							success: (r) => {
								if (!r.confirm) return
								this.run(() => reviewErrand(this.e.id, { score, content: r.content || '' }), '评价成功')
							}
						})
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
	.status-banner { display: flex; align-items: center; justify-content: space-between; padding: 20px 16px; background: linear-gradient(135deg, #07C160, #03a050); }
	.status-banner.s5 { background: linear-gradient(135deg, #bbb, #999); }
	.status-banner.s4 { background: linear-gradient(135deg, #1890ff, #0e6fd6); }
	.status-text { font-size: 20px; color: #fff; font-weight: 700; }
	.reward { font-size: 16px; color: #fff; }
	.card { background: #fff; margin: 10px 12px; border-radius: 12px; padding: 16px; }
	.head { display: flex; align-items: center; margin-bottom: 12px; }
	.type-tag { font-size: 12px; color: #07C160; background: rgba(7,193,96,0.1); border-radius: 4px; padding: 2px 8px; margin-right: 8px; }
	.title { font-size: 17px; font-weight: 700; color: #1a1a1a; }
	.meta { display: flex; padding: 6px 0; }
	.ml { width: 72px; font-size: 14px; color: #999; }
	.mv { flex: 1; font-size: 14px; color: #333; }
	.desc-box { margin-top: 12px; padding-top: 12px; border-top: 1px solid #f2f2f2; }
	.section { font-size: 14px; font-weight: 600; color: #333; display: block; margin-bottom: 8px; }
	.desc { font-size: 15px; color: #444; line-height: 1.7; }
	.timeline .tl-item { display: flex; align-items: center; padding: 6px 0; }
	.dot { width: 8px; height: 8px; border-radius: 50%; background: #07C160; margin-right: 10px; }
	.tl-label { font-size: 13px; color: #666; }
	.bottom-bar { display: flex; gap: 10px; padding: 10px 12px; background: #fff; border-top: 1px solid #f0f0f0; }
	.action-btn { flex: 1; height: 44px; line-height: 44px; border-radius: 22px; font-size: 16px; margin: 0; }
	.action-btn.primary { background: #07C160; color: #fff; }
	.action-btn.warning { background: #faad14; color: #fff; }
	.action-btn.ghost { background: #f2f3f5; color: #666; flex: 0 0 110px; }
</style>
