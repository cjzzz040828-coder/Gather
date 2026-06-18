<template>
  <div class="goods-list">
    <!-- 主视觉：极简大留白横幅 -->
    <el-carousel
      height="220px"
      class="hero"
      :interval="6000"
      arrow="never"
      indicator-position="none"
      @change="(i: number) => (heroIdx = i)"
    >
      <el-carousel-item v-for="(b, i) in banners" :key="i">
        <div class="hero-slide">
          <div class="hero-inner">
            <span class="hero-eyebrow">{{ b.eyebrow }}</span>
            <h1 class="hero-title">{{ b.title }}</h1>
            <p class="hero-sub">{{ b.sub }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <div class="hero-dots">
      <span
        v-for="(b, i) in banners"
        :key="i"
        class="hero-dot"
        :class="{ active: heroIdx === i }"
      ></span>
    </div>

    <!-- 分类条 -->
    <div class="cat-bar">
      <span
        class="cat-item"
        :class="{ active: selectedCat === '' }"
        @click="selectCat('')"
        >全部</span
      >
      <span
        v-for="cat in categories"
        :key="cat"
        class="cat-item"
        :class="{ active: selectedCat === cat }"
        @click="selectCat(cat)"
        >{{ cat }}</span
      >
    </div>

    <!-- 标题 + 排序 -->
    <div class="list-head">
      <h2 class="head-title">{{ keyword ? `“${keyword}” 的结果` : '为你精选' }}</h2>
      <div class="sort-bar">
        <span
          class="sort-item"
          :class="{ active: sort === 'newest' }"
          @click="changeSort('newest')"
          >最新</span
        >
        <span
          class="sort-item"
          :class="{ active: sort === 'price_asc' }"
          @click="changeSort('price_asc')"
          >价格 升</span
        >
        <span
          class="sort-item"
          :class="{ active: sort === 'price_desc' }"
          @click="changeSort('price_desc')"
          >价格 降</span
        >
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-loading="loading" class="grid">
      <el-empty
        v-if="!loading && goods.length === 0"
        description="暂无在售拼团商品"
      />
      <div
        v-for="item in goods"
        :key="item.id"
        class="goods-card"
        @click="goDetail(item.id)"
      >
        <div class="cover">
          <el-image :src="item.cover" fit="cover" class="cover-img" lazy>
            <template #error>
              <div class="cover-placeholder">{{ item.title?.charAt(0) || '集' }}</div>
            </template>
          </el-image>
          <span v-if="item.category" class="cover-tag">{{ item.category }}</span>
        </div>
        <div class="info">
          <div class="goods-title">{{ item.title }}</div>
          <div class="price-line">
            <span class="price">
              <span class="symbol">¥</span><span class="price-num">{{ item.minPrice != null ? item.minPrice : '—' }}</span>
              <span class="price-suffix">起</span>
            </span>
            <span class="go-detail">拼团 →</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { groupbuyApi, type GbGoods } from '@/api/groupbuy'

const router = useRouter()
const route = useRoute()
const goods = ref<GbGoods[]>([])
const keyword = ref((route.query.q as string) || '')
const loading = ref(false)
const selectedCat = ref('')
const categories = ref<string[]>([])
const sort = ref('newest')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const heroIdx = ref(0)

const banners = [
  { eyebrow: 'GATHER · 集萃', title: '好物，值得一起拥有', sub: '邀三两好友，从容拼成一单' },
  { eyebrow: '成团 · 安心', title: '不成团，原路退', sub: '凑不齐自动退回，零负担尝试' },
  { eyebrow: '精选 · 严选', title: '挑剔之选', sub: '每一件，都经得起细看' }
]

async function loadCategories() {
  categories.value = (await groupbuyApi.goodsCategories()) || []
}

async function load() {
  loading.value = true
  try {
    const res = await groupbuyApi.goodsPage({
      page: page.value,
      pageSize: pageSize.value,
      title: keyword.value || undefined,
      category: selectedCat.value || undefined,
      sort: sort.value
    })
    goods.value = res?.list || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

function selectCat(cat: string) {
  selectedCat.value = cat
  page.value = 1
  load()
}

function changeSort(s: string) {
  sort.value = s
  page.value = 1
  load()
}

function onPageChange(p: number) {
  page.value = p
  load()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goDetail(id: number) {
  const { href } = router.resolve(`/goods/${id}`)
  window.open(href, '_blank')
}

watch(
  () => route.query.q,
  (q) => {
    keyword.value = (q as string) || ''
    page.value = 1
    load()
  }
)

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.goods-list {
  --ink: #1c1c1e;
  --ink-2: #6b6b70;
  --ink-3: #a1a1a6;
  --line: #ececec;
  --paper: #faf9f7;
}

/* ===== 主视觉 ===== */
.hero {
  border-radius: 14px;
  overflow: hidden;
}
.hero-slide {
  height: 100%;
  display: flex;
  align-items: center;
  /* 极简米色/墨色调，不用刺眼渐变 */
  background:
    radial-gradient(120% 140% at 85% 15%, #f3efe9 0%, #e9e4dc 55%, #ded7cc 100%);
  position: relative;
}
.hero-slide::after {
  /* 一道极淡的高光，制造质感 */
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, rgba(255, 255, 255, 0.5), transparent 42%);
  pointer-events: none;
}
.hero-inner {
  padding: 0 64px;
  position: relative;
  z-index: 1;
}
.hero-eyebrow {
  display: inline-block;
  font-size: 12px;
  letter-spacing: 3px;
  color: var(--ink-2);
  text-transform: uppercase;
  margin-bottom: 12px;
}
.hero-title {
  font-size: 30px;
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--ink);
  line-height: 1.25;
  margin-bottom: 10px;
}
.hero-sub {
  font-size: 15px;
  color: var(--ink-2);
  letter-spacing: 1px;
  font-weight: 300;
}
.hero-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin: 14px 0 0;
}
.hero-dot {
  width: 22px;
  height: 3px;
  border-radius: 2px;
  background: var(--line);
  transition: background 0.3s;
}
.hero-dot.active {
  background: var(--ink);
}

/* ===== 分类条 ===== */
.cat-bar {
  display: flex;
  gap: 28px;
  margin: 36px 0 4px;
  border-bottom: 1px solid var(--line);
}
.cat-item {
  position: relative;
  padding: 0 0 14px;
  font-size: 14px;
  color: var(--ink-2);
  cursor: pointer;
  letter-spacing: 1px;
  transition: color 0.2s;
}
.cat-item:hover {
  color: var(--ink);
}
.cat-item.active {
  color: var(--ink);
  font-weight: 500;
}
.cat-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2px;
  background: var(--ink);
}

/* ===== 标题 + 排序 ===== */
.list-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 32px 0 20px;
}
.head-title {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
}
.sort-bar {
  display: flex;
  gap: 22px;
}
.sort-item {
  font-size: 13px;
  color: var(--ink-3);
  cursor: pointer;
  letter-spacing: 1px;
  transition: color 0.2s;
}
.sort-item:hover {
  color: var(--ink);
}
.sort-item.active {
  color: var(--ink);
  font-weight: 500;
}

/* ===== 商品网格 ===== */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(248px, 1fr));
  gap: 28px 22px;
  min-height: 200px;
}
.goods-card {
  cursor: pointer;
  background: transparent;
}
.cover {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  overflow: hidden;
  background: var(--paper);
}
.cover-img {
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.goods-card:hover .cover-img {
  transform: scale(1.04);
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 300;
  color: #cfc9bf;
  background: linear-gradient(135deg, #f7f4ef, #ece7df);
}
.cover-tag {
  position: absolute;
  left: 12px;
  top: 12px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(6px);
  color: var(--ink);
  font-size: 11px;
  letter-spacing: 1px;
  padding: 4px 10px;
  border-radius: 999px;
}
.info {
  padding: 14px 2px 0;
}
.goods-title {
  font-size: 14px;
  line-height: 1.5;
  color: var(--ink);
  letter-spacing: 0.5px;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.price-line {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 10px;
}
.price {
  color: var(--ink);
  display: flex;
  align-items: baseline;
}
.price .symbol {
  font-size: 13px;
  margin-right: 1px;
}
.price-num {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.price-suffix {
  font-size: 12px;
  color: var(--ink-3);
  margin-left: 3px;
}
.go-detail {
  font-size: 12px;
  color: var(--ink-3);
  letter-spacing: 1px;
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.25s;
}
.goods-card:hover .go-detail {
  opacity: 1;
  transform: translateX(0);
  color: var(--ink);
}

/* ===== 分页 ===== */
.pager {
  display: flex;
  justify-content: center;
  margin: 48px 0 24px;
}
:deep(.el-pagination.is-background .el-pager li),
:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: transparent;
  color: var(--ink-2);
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--ink);
  color: #fff;
}
</style>
