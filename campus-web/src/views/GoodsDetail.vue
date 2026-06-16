<template>
  <div v-loading="loading" class="goods-detail">
    <!-- 面包屑 -->
    <div class="crumb-bar">
      <a @click="router.push('/goods')">首页</a>
      <span class="sep">›</span>
      <template v-if="data?.goods.category">
        <a @click="router.push('/goods')">{{ data.goods.category }}</a>
        <span class="sep">›</span>
      </template>
      <span class="cur">{{ data?.goods.title || '商品详情' }}</span>
    </div>

    <template v-if="data">
      <div class="detail-card jd-card">
        <!-- 左：主图 + 缩略图 strip -->
        <div class="gallery">
          <el-image :src="activeImage" fit="cover" class="main-img">
            <template #error>
              <div class="img-placeholder">{{ data.goods.title?.charAt(0) || '商' }}</div>
            </template>
          </el-image>
          <div class="thumbs">
            <div
              v-for="(img, i) in galleryImages"
              :key="i"
              class="thumb"
              :class="{ active: activeImage === img }"
              @mouseenter="activeImage = img"
            >
              <el-image :src="img" fit="cover">
                <template #error>
                  <div class="thumb-ph">{{ data.goods.title?.charAt(0) || '商' }}</div>
                </template>
              </el-image>
            </div>
          </div>
        </div>

        <!-- 右：信息 -->
        <div class="summary">
          <!-- 京东红促销条（置顶，在商品名上方） -->
          <div class="promo-bar">
            <div class="promo-left">
              <span class="promo-tag">拼团</span>
              <span class="promo-text">多人拼团 · 立省更多</span>
            </div>
            <div class="promo-right">买贵双倍赔 · 包邮</div>
          </div>

          <h1 class="title">{{ data.goods.title }}</h1>
          <p class="subtitle">多人成团享拼团优惠 · 库存有限先到先得</p>

          <!-- 价格面板 -->
          <div class="price-box">
            <div class="price-main">
              <span class="label">拼团价</span>
              <span class="price big"><span class="symbol">¥</span>{{ displayPrice }}</span>
              <span class="origin-price" v-if="hasDiscount">¥{{ displayPrice }}</span>
            </div>
            <div class="price-extra">
              <span class="rate-item">
                <em>{{ totalStock }}</em> 件库存
              </span>
              <span class="rate-sep">|</span>
              <span class="rate-item" v-if="primaryActivity">
                <em>{{ primaryActivity.targetCount }}</em> 人成团
              </span>
            </div>
          </div>

          <!-- 优惠行（拼团活动入口提示，真实数据驱动） -->
          <div class="coupon-row" v-if="primaryActivity">
            <span class="coupon-label">可参与</span>
            <span class="coupon-chip">{{ primaryActivity.activityName }}</span>
          </div>

          <!-- 系列品 / 规格：真实 SKU 网格 -->
          <div class="sku-block">
            <div class="sku-label">规格</div>
            <div class="sku-grid">
              <div
                v-for="sku in data.skus"
                :key="sku.id"
                class="sku-card"
                :class="{ off: sku.stock <= 0, sel: selectedSkuId === sku.id }"
                @click="sku.stock > 0 && (selectedSkuId = sku.id)"
              >
                <el-image :src="data.goods.cover" fit="cover" class="sku-thumb">
                  <template #error>
                    <div class="sku-thumb-ph">{{ data.goods.title?.charAt(0) || '商' }}</div>
                  </template>
                </el-image>
                <div class="sku-info">
                  <span class="sku-name">{{ sku.skuName }}</span>
                  <span class="sku-price">¥{{ sku.originalPrice }}</span>
                  <span class="sku-stock">库存 {{ sku.stock }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 数量 -->
          <div class="qty-block">
            <div class="qty-label">数量</div>
            <el-input-number v-model="qty" :min="1" :max="99" />
          </div>

          <!-- 去拼团 -->
          <div class="buy-block">
            <el-button
              type="danger"
              size="large"
              class="go-btn"
              :disabled="!primaryActivity"
              @click="goActivity"
            >
              去 拼 团
            </el-button>
          </div>
          <p class="entry-tip">
            <template v-if="primaryActivity">
              当前拼团活动：{{ primaryActivity.activityName }}（{{ primaryActivity.targetCount }} 人成团）
            </template>
            <template v-else>该商品暂无进行中的拼团活动</template>
          </p>
        </div>
      </div>

      <!-- 图文详情 -->
      <div class="detail-tab jd-card">
        <div class="tab-head"><span class="tab active">商品详情</span></div>
        <div class="rich" v-html="data.goods.detail || '暂无详情'"></div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { groupbuyApi, type GoodsDetailVO, type GbActivity } from '@/api/groupbuy'

const route = useRoute()
const router = useRouter()
const data = ref<GoodsDetailVO | null>(null)
const loading = ref(false)
const activities = ref<GbActivity[]>([])
const qty = ref(1)
const selectedSkuId = ref<number | null>(null)
const activeImage = ref('')

// 主图 + 缩略图列表：goods.images 逗号分隔，回退到 cover
const galleryImages = computed(() => {
  if (!data.value) return []
  const imgs = (data.value.goods.images || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
  if (imgs.length) return imgs
  return data.value.goods.cover ? [data.value.goods.cover] : []
})

const selectedSku = computed(
  () => data.value?.skus.find((s) => s.id === selectedSkuId.value) ?? null
)

const totalStock = computed(
  () => data.value?.skus.reduce((sum, s) => sum + (s.stock || 0), 0) ?? 0
)

const minPrice = computed(() => {
  if (!data.value?.skus?.length) return '-'
  return Math.min(...data.value.skus.map((s) => Number(s.originalPrice)))
})

// 选中 SKU 则显示其价格，否则显示最低价
const displayPrice = computed(() =>
  selectedSku.value ? selectedSku.value.originalPrice : minPrice.value
)

const hasDiscount = computed(() => !!primaryActivity.value)

// 当前商品进行中的活动（取第一个作为去拼团目标）
const primaryActivity = computed(() => activities.value[0] ?? null)

async function load() {
  loading.value = true
  try {
    const goodsId = Number(route.params.id)
    data.value = await groupbuyApi.goodsDetail(goodsId)
    const first = data.value?.skus?.find((s) => s.stock > 0) || data.value?.skus?.[0]
    selectedSkuId.value = first?.id ?? null
    activities.value = await groupbuyApi.goodsActivities(goodsId)
  } finally {
    loading.value = false
  }
}

watch(
  galleryImages,
  (imgs) => {
    if (imgs.length) activeImage.value = imgs[0]
  },
  { immediate: true }
)

function goActivity() {
  if (!primaryActivity.value) return
  router.push(`/activity/${primaryActivity.value.id}`)
}

onMounted(load)
</script>

<style scoped>
.detail-card {
  padding: var(--sp-6);
  display: flex;
  gap: var(--sp-8);
}
/* 左图廊 */
.gallery {
  width: 400px;
  flex-shrink: 0;
}
.main-img {
  width: 400px;
  height: 400px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  background: #f5f5f5;
}
.img-placeholder {
  width: 400px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 90px;
  color: #d0d0d0;
  background: linear-gradient(135deg, #fafafa, #f0f0f0);
}
.thumbs {
  display: flex;
  gap: var(--sp-2);
  margin-top: var(--sp-3);
  flex-wrap: wrap;
}
.thumb {
  width: 60px;
  height: 60px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
}
.thumb.active {
  border-color: var(--c-primary);
}
.thumb :deep(.el-image) {
  width: 100%;
  height: 100%;
}
.thumb-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d0d0d0;
  background: #fafafa;
}

/* 右信息 */
.summary {
  flex: 1;
  min-width: 0;
}
.title {
  font-size: var(--fz-xl);
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: var(--sp-2);
}
.subtitle {
  font-size: var(--fz-sm);
  color: var(--c-accent);
  margin-bottom: var(--sp-4);
}

/* 京东红促销条（置顶，独立圆角块） */
.promo-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, var(--c-primary), var(--c-accent));
  color: #fff;
  border-radius: var(--radius-sm);
  padding: 10px var(--sp-4);
  margin-bottom: var(--sp-4);
}
.promo-left {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}
.promo-tag {
  background: rgba(255, 255, 255, 0.25);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  font-size: var(--fz-xs);
  font-weight: 700;
}
.promo-text {
  font-size: var(--fz-md);
  font-weight: 700;
}
.promo-right {
  font-size: var(--fz-xs);
  opacity: 0.95;
}

/* 价格面板：京东到手价风 */
.price-box {
  background: var(--c-bg-warm);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: var(--sp-4) var(--sp-5);
  margin-bottom: var(--sp-4);
}
.price-main {
  display: flex;
  align-items: baseline;
  gap: var(--sp-3);
}
.price-main .label {
  font-size: var(--fz-sm);
  background: var(--c-primary);
  color: #fff;
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}
.price.big {
  font-size: 38px;
  line-height: 1;
}
.origin-price {
  color: var(--c-text-3);
  text-decoration: line-through;
  font-size: var(--fz-sm);
}
.price-extra {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-top: var(--sp-3);
  font-size: var(--fz-xs);
  color: var(--c-text-3);
}
.price-extra em {
  font-style: normal;
  color: var(--c-text-2);
  font-weight: 700;
}
.rate-sep {
  color: #ddd;
}

/* 优惠/活动行 */
.coupon-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}
.coupon-label {
  width: 48px;
  color: var(--c-text-3);
  font-size: var(--fz-sm);
  flex-shrink: 0;
}
.coupon-chip {
  border: 1px solid var(--c-primary);
  color: var(--c-primary);
  background: var(--c-primary-light);
  border-radius: var(--radius-sm);
  padding: 3px 10px;
  font-size: var(--fz-xs);
}

/* 系列品 SKU 网格 */
.sku-block {
  display: flex;
  margin-bottom: var(--sp-4);
}
.sku-label,
.qty-label {
  width: 48px;
  color: var(--c-text-3);
  flex-shrink: 0;
  padding-top: 6px;
  font-size: var(--fz-sm);
}
.sku-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(180px, 1fr));
  gap: var(--sp-2);
  flex: 1;
}
.sku-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 8px 10px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.sku-card:hover {
  border-color: var(--c-primary);
}
.sku-card.sel {
  border-color: var(--c-primary);
  background: var(--c-primary-light);
}
.sku-card.off {
  opacity: 0.45;
  cursor: not-allowed;
}
.sku-thumb {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border-radius: var(--radius-sm);
  background: #f5f5f5;
}
.sku-thumb-ph {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d0d0d0;
  background: #fafafa;
  font-size: 20px;
}
.sku-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.sku-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sku-price {
  color: var(--c-price);
  font-size: var(--fz-sm);
  font-weight: 700;
}
.sku-stock {
  color: var(--c-text-3);
  font-size: var(--fz-xs);
}

/* 数量 */
.qty-block {
  display: flex;
  align-items: center;
  margin-bottom: var(--sp-5);
}

/* 去拼团 */
.buy-block {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--c-border);
}
.go-btn {
  height: 48px;
  padding: 0 64px;
  font-size: var(--fz-md);
  letter-spacing: 4px;
  --el-button-bg-color: var(--c-primary);
  --el-button-border-color: var(--c-primary);
  --el-button-hover-bg-color: var(--c-primary-dark);
  --el-button-hover-border-color: var(--c-primary-dark);
  --el-button-active-bg-color: var(--c-primary-dark);
  --el-button-active-border-color: var(--c-primary-dark);
}
.entry-tip {
  margin-top: var(--sp-2);
  font-size: var(--fz-xs);
  color: var(--c-text-3);
}

/* 详情 tab */
.detail-tab {
  margin-top: var(--sp-4);
  padding: 0;
}
.tab-head {
  border-bottom: 1px solid var(--c-border);
  background: #fafafa;
  padding: 0 var(--sp-5);
}
.tab {
  display: inline-block;
  padding: 12px 4px;
  font-weight: 600;
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: var(--c-primary);
  border-bottom-color: var(--c-primary);
}
.rich {
  padding: var(--sp-6);
  line-height: 1.9;
  color: var(--c-text-2);
}
.rich :deep(img) {
  max-width: 100%;
}
</style>
