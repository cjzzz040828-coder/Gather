<template>
	<view class="page">
		<view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
		<view class="nav-bar">
			<view class="nav-back" @tap="goBack"><u-icon name="arrow-left" color="#222" size="20"></u-icon></view>
			<text class="nav-title">发布信息</text>
		</view>

		<scroll-view scroll-y class="form-scroll">
			<view class="card">
				<view class="type-seg">
					<view class="seg-item" :class="{ active: form.type === 1 }" @tap="form.type = 1">寻物</view>
					<view class="seg-item" :class="{ active: form.type === 2 }" @tap="form.type = 2">招领</view>
					<view class="seg-item" :class="{ active: form.type === 3 }" @tap="form.type = 3">二手出售</view>
				</view>

				<view class="field">
					<text class="label">标题</text>
					<input class="input" v-model="form.title" placeholder="一句话描述" maxlength="50" />
				</view>

				<view class="field">
					<text class="label">分类</text>
					<scroll-view scroll-x class="cate-scroll" :show-scrollbar="false">
						<text v-for="c in categories" :key="c" class="cate-chip" :class="{ active: form.category === c }" @tap="form.category = c">{{ c }}</text>
					</scroll-view>
				</view>

				<view class="field" v-if="form.type === 3">
					<text class="label">价格(¥)</text>
					<input class="input" type="digit" v-model="form.price" placeholder="0.00" />
				</view>

				<view class="field column">
					<text class="label">详细描述</text>
					<textarea class="textarea" v-model="form.content" placeholder="补充物品/交易的详细信息" maxlength="500" />
				</view>

				<view class="field column">
					<text class="label">图片</text>
					<view class="img-grid">
						<view v-for="(img, i) in form.imageList" :key="i" class="img-box">
							<image class="up-img" :src="img" mode="aspectFill" @tap="preview(i)"></image>
							<view class="img-del" @tap.stop="removeImg(i)"><u-icon name="close" color="#fff" size="12"></u-icon></view>
						</view>
						<view v-if="form.imageList.length < 6" class="img-add" @tap="chooseImage">
							<u-icon name="camera" color="#bbb" size="26"></u-icon>
						</view>
					</view>
				</view>

				<view class="field">
					<text class="label">地点</text>
					<input class="input" v-model="form.location" placeholder="如：二食堂、图书馆" />
				</view>
				<view class="field">
					<text class="label">联系方式</text>
					<input class="input" v-model="form.contact" placeholder="微信/手机号" />
				</view>
			</view>
		</scroll-view>

		<view class="bottom-bar">
			<button class="submit-btn" :loading="submitting" @tap="submit">发布</button>
		</view>
	</view>
</template>

<script>
	import { publishItem, uploadAppImage } from '@/utils/api.js'
	export default {
		data() {
			return {
				statusBarHeight: 20,
				submitting: false,
				categories: ['电子产品', '书籍资料', '卡证钥匙', '生活用品', '其他'],
				form: { type: 1, title: '', category: '', price: '', content: '', location: '', contact: '', imageList: [] }
			}
		},
		onLoad() { this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20 },
		methods: {
			chooseImage() {
				uni.chooseImage({
					count: 6 - this.form.imageList.length,
					success: async (res) => {
						uni.showLoading({ title: '上传中' })
						try {
							for (const path of res.tempFilePaths) {
								const r = await uploadAppImage(path)
								if (r && r.data) this.form.imageList.push(r.data)
							}
						} catch (e) {
							uni.showToast({ title: '上传失败', icon: 'none' })
						} finally { uni.hideLoading() }
					}
				})
			},
			removeImg(i) { this.form.imageList.splice(i, 1) },
			preview(i) { uni.previewImage({ current: i, urls: this.form.imageList }) },
			async submit() {
				if (!this.form.title.trim()) { uni.showToast({ title: '请填写标题', icon: 'none' }); return }
				if (this.form.type === 3 && this.form.price && isNaN(Number(this.form.price))) {
					uni.showToast({ title: '价格格式不正确', icon: 'none' }); return
				}
				this.submitting = true
				try {
					await publishItem({
						type: this.form.type,
						title: this.form.title.trim(),
						content: this.form.content,
						category: this.form.category,
						price: this.form.type === 3 && this.form.price ? Number(this.form.price) : null,
						location: this.form.location,
						contact: this.form.contact,
						images: this.form.imageList.join(',')
					})
					uni.showToast({ title: '发布成功', icon: 'success' })
					setTimeout(() => uni.navigateBack(), 800)
				} catch (e) {} finally { this.submitting = false }
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
	.cate-scroll { flex: 1; white-space: nowrap; }
	.cate-chip { display: inline-block; padding: 4px 12px; margin-right: 8px; font-size: 13px; color: #666; background: #f2f3f5; border-radius: 14px; }
	.cate-chip.active { color: #fff; background: #07C160; }
	.img-grid { display: flex; flex-wrap: wrap; gap: 10px; }
	.img-box { position: relative; width: 150rpx; height: 150rpx; }
	.up-img { width: 150rpx; height: 150rpx; border-radius: 8px; }
	.img-del { position: absolute; top: -6px; right: -6px; width: 20px; height: 20px; border-radius: 50%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; }
	.img-add { width: 150rpx; height: 150rpx; border: 1px dashed #ddd; border-radius: 8px; display: flex; align-items: center; justify-content: center; background: #fafafa; }
	.bottom-bar { padding: 10px 16px; background: #fff; border-top: 1px solid #f0f0f0; }
	.submit-btn { height: 44px; line-height: 44px; background: #07C160; color: #fff; border-radius: 22px; font-size: 16px; }
</style>
