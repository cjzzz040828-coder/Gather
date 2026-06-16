<template>
  <div class="goods-list">
    <!-- 促销轮播 banner -->
    <el-carousel height="200px" class="banner" :interval="4000" arrow="hover">
      <el-carousel-item v-for="(b, i) in banners" :key="i">
        <div class="banner-slide" :style="{ background: b.bg }">
          <div class="banner-text">
            <p class="banner-eyebrow">{{ b.eyebrow }}</p>
            <h2 class="banner-title">{{ b.title }}</h2>
            <p class="banner-sub">{{ b.sub }}</p>
          </div>
          <div class="banner-price">{{ b.price }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 横向分类条 -->
    <div class="cat-bar">
      <span
        class="cat-item"
        :class="{ active: selectedCat === '' }"
        @click="selectedCat = ''"
        >全部</span
      >
      <span
        v-for="cat in categories"
        :key="cat"
        class="cat-item"
        :class="{ active: selectedCat === cat }"
        @click="selectedCat = cat"
        >{{ cat }}</span
      >
    </div>

    <!-- 标题 -->
    <div class="section-title">
      <span>{{ keyword ? `“${keyword}” 的搜索结果` : '为你推荐' }}</span>
    </div>

    <!-- 商品网格 -->
    <div v-loading="loading" class="grid">
      <el-empty
        v-if="!loading && filteredGoods.length === 0"
        description="暂无在售拼团商品"
      />
      <div
        v-for="item in filteredGoods"
        :key="item.id"
        class="goods-card jd-card hoverable"
        @click="goDetail(item.id)"
      >
        <div class="cover">
          <el-image :src="item.cover" fit="cover" class="cover-img" lazy>
            <template #error>
              <div class="cover-placeholder">{{ item.title?.charAt(0) || '商' }}</div>
            </template>
          </el-image>
          <span class="corner">拼团</span>
        </div>
        <div class="info">
          <div class="goods-title">{{ item.title }}</div>
          <div class="price-line">
            <span class="price"><span class="symbol">¥</span>拼团价</span>
            <span v-if="item.category" class="cat-tag">{{ item.category }}</span>
          </div>
          <button class="buy-btn" @click.stop="goDetail(item.id)">立即拼团</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { groupbuyApi, type GbGoods } from '@/api/groupbuy'

const router = useRouter()
const route = useRoute()
const goods = ref<GbGoods[]>([])
const keyword = ref((route.query.q as string) || '')
const loading = ref(false)
const selectedCat = ref('')

const banners = [
  { eyebrow: '限时拼团', title: '省钱就要一起拼', sub: '邀好友组团，拼得越多省得越多', price: '¥99 起', bg: 'linear-gradient(120deg,#e1251b,#ff6a00)' },
  { eyebrow: '成团包邮', title: '不成团 · 秒退款', sub: '放心拼，凑不齐自动原路退回', price: '0 风险', bg: 'linear-gradient(120deg,#c81623,#e1251b)' },
  { eyebrow: '精选好物', title: '同学都在拼的优惠', sub: '实时进度看得见，成团享低价', price: '一起省', bg: 'linear-gradient(120deg,#ff6a00,#ff9a3d)' }
]

// 从商品中去重出分类
const categories = computed(() => {
  const set = new Set<string>()
  goods.value.forEach((g) => g.category && set.add(g.category))
  return [...set]
})

// 按分类前端筛选（关键词由后端 load 处理）
const filteredGoods = computed(() => {
  if (!selectedCat.value) return goods.value
  return goods.value.filter((g) => g.category === selectedCat.value)
})

async function load() {
  loading.value = true
  try {
    goods.value = (await groupbuyApi.goodsList(keyword.value || undefined)) || []
    selectedCat.value = ''
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/goods/${id}`)
}

watch(
  () => route.query.q,
  (q) => {
    keyword.value = (q as string) || ''
    load()
  }
)

load()
</script>

<style scoped>
/* 轮播 banner */
.banner {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.banner-slide {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 56px;
  color: #fff;
}
.banner-eyebrow {
  display: inline-block;
  background: rgba(0, 0, 0, 0.18);
  padding: 3px 12px;
  border-radius: var(--radius-sm);
  font-size: var(--fz-sm);
  margin-bottom: var(--sp-3);
}
.banner-title {
  font-size: var(--fz-2xl);
  font-weight: 800;
  margin-bottom: var(--sp-2);
}
.banner-sub {
  font-size: var(--fz-md);
  opacity: 0.92;
}
.banner-price {
  font-size: 64px;
  font-weight: 900;
  text-shadow: 0 4px 18px rgba(0, 0, 0, 0.22);
}

/* 分类条贴在 banner 下 */
.cat-bar {
  margin-top: var(--sp-4);
}

/* 商品网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--sp-3);
  min-height: 200px;
}
.goods-card {
  overflow: hidden;
  cursor: pointer;
}
.cover {
  position: relative;
  height: 220px;
  background: #f5f5f5;
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-placeholder {
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  color: #d0d0d0;
  background: linear-gradient(135deg, #fafafa, #f0f0f0);
}
.corner {
  position: absolute;
  left: 0;
  top: 10px;
  background: var(--c-primary);
  color: #fff;
  font-size: var(--fz-xs);
  padding: 2px 10px;
  font-weight: 600;
}
.info {
  padding: var(--sp-3);
}
.goods-title {
  font-size: var(--fz-base);
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.price-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: var(--sp-2) 0;
  min-height: 24px;
}
.price-line .price {
  font-size: var(--fz-lg);
}
.cat-tag {
  font-size: var(--fz-xs);
  color: var(--c-text-3);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 1px 6px;
}
.buy-btn {
  width: 100%;
  background: var(--c-primary);
  color: #fff;
  border: none;
  padding: 8px 0;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--fz-base);
  transition: background 0.2s;
}
.buy-btn:hover {
  background: var(--c-primary-dark);
}
</style>
