<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">发布跑腿</text>
		</view>

		<scroll-view scroll-y class="form-scroll">
			<view class="card">
				<view class="type-seg">
					<view class="seg-item" :class="{ active: form.type === 1 }" @tap="form.type = 1">代取快递</view>
					<view class="seg-item" :class="{ active: form.type === 2 }" @tap="form.type = 2">代买</view>
					<view class="seg-item" :class="{ active: form.type === 3 }" @tap="form.type = 3">其他</view>
				</view>
				<view class="field">
					<text class="label">标题</text>
					<input class="input" v-model="form.title" placeholder="如：帮取菜鸟驿站快递" maxlength="50" />
				</view>
				<view class="field">
					<text class="label">取件地</text>
					<input class="input" v-model="form.pickupAddr" placeholder="如：东门菜鸟驿站" />
				</view>
				<view class="field">
					<text class="label">送达地</text>
					<input class="input" v-model="form.deliverAddr" placeholder="如：3号宿舍楼 502" />
				</view>
				<view class="field column">
					<text class="label">任务说明</text>
					<textarea class="textarea" v-model="form.content" placeholder="取件码、物品描述、特殊要求等" maxlength="300" />
				</view>
			</view>

			<view class="card">
				<view class="field reward-field">
					<text class="label">赏金 ¥</text>
					<input class="input reward-input" type="digit" v-model="form.reward" placeholder="0.00" />
				</view>
				<view class="quick-reward">
					<text v-for="r in [2, 3, 5, 8]" :key="r" class="qr-chip" @tap="form.reward = String(r)">¥{{ r }}</text>
				</view>
			</view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="pay-info">
				<text class="pay-label">需支付赏金</text>
				<text class="pay-amount">¥{{ form.reward || '0.00' }}</text>
			</view>
			<button class="submit-btn" :loading="submitting" @tap="submit">支付并发布</button>
		</view>
	</view>
</template>

<script>
	import { publishErrand } from '@/utils/api.js'
	export default {
		data() {
			return { statusBarHeight: 20, submitting: false, form: { type: 1, title: '', pickupAddr: '', deliverAddr: '', content: '', reward: '' } }
		},
		onLoad() { this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20 },
		methods: {
			async submit() {
				if (!this.form.title.trim()) { uni.showToast({ title: '请填写标题', icon: 'none' }); return }
				const reward = Number(this.form.reward)
				if (!this.form.reward || isNaN(reward) || reward <= 0) { uni.showToast({ title: '请填写有效赏金', icon: 'none' }); return }

				// 模拟支付流程（真实接入时此处应拉起 campus-pay 微信支付）
				uni.showModal({
					title: '模拟支付',
					content: `确认支付赏金 ¥${reward.toFixed(2)} 并发布任务？`,
					confirmText: '确认支付',
					success: async (r) => {
						if (!r.confirm) return
						this.submitting = true
						uni.showLoading({ title: '支付中...' })
						try {
							await publishErrand({
								type: this.form.type,
								title: this.form.title.trim(),
								pickupAddr: this.form.pickupAddr,
								deliverAddr: this.form.deliverAddr,
								content: this.form.content,
								reward: reward
							})
							uni.hideLoading()
							uni.showToast({ title: '支付成功，已发布', icon: 'success' })
							setTimeout(() => uni.navigateBack(), 900)
						} catch (e) {
							uni.hideLoading()
						} finally { this.submitting = false }
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
	.form-scroll { flex: 1; }
	.card { background: #fff; margin: 12px; border-radius: 12px; padding: 4px 16px; }
	.type-seg { display: flex; padding: 12px 0; gap: 8px; }
	.seg-item { flex: 1; text-align: center; padding: 8px 0; font-size: 14px; color: #666; background: #f2f3f5; border-radius: 8px; }
	.seg-item.active { color: #fff; background: #07C160; }
	.field { display: flex; align-items: center; padding: 12px 0; border-top: 1px solid #f5f5f5; }
	.field.column { flex-direction: column; align-items: stretch; }
	.label { width: 72px; font-size: 14px; color: #333; flex-shrink: 0; }
	.field.column .label { margin-bottom: 8px; }
	.input { flex: 1; font-size: 14px; color: #222; }
	.textarea { width: 100%; height: 120rpx; font-size: 14px; color: #222; }
	.reward-field .reward-input { font-size: 20px; font-weight: 700; color: #fa5151; }
	.quick-reward { display: flex; gap: 10px; padding: 4px 0 12px; }
	.qr-chip { padding: 4px 16px; font-size: 14px; color: #fa5151; background: #fff1f0; border-radius: 14px; }
	.bottom-bar { display: flex; align-items: center; padding: 10px 16px; background: #fff; border-top: 1px solid #f0f0f0; }
	.pay-info { flex: 1; display: flex; flex-direction: column; }
	.pay-label { font-size: 12px; color: #999; }
	.pay-amount { font-size: 20px; color: #fa5151; font-weight: 700; }
	.submit-btn { flex: 0 0 160px; height: 44px; line-height: 44px; background: #07C160; color: #fff; border-radius: 22px; font-size: 16px; margin: 0; }
</style>
