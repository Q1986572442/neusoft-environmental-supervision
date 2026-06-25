<template>
  <div class="news-page">
    <div class="page-header glass-panel">
      <div class="header-left">
        <h2 class="page-title">🌍 环保资讯</h2>
        <p class="page-subtitle">了解最新环保政策、新闻动态与系统公告</p>
      </div>
      <div class="header-right">
        <el-radio-group v-model="filterType" @change="onFilterChange" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="NEWS">环保新闻</el-radio-button>
          <el-radio-button value="POLICY">政策法规</el-radio-button>
          <el-radio-button value="NOTICE">系统公告</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="news-grid" v-loading="loading">
      <div class="news-card glass-panel" v-for="item in newsList" :key="item.id" @click="$router.push(rolePath(`/news/${item.id}`))">
        <div class="card-cover" v-if="item.coverImage">
          <img :src="item.coverImage" :alt="item.title" />
        </div>
        <div class="card-body">
          <div class="card-tags">
            <el-tag size="small" :type="item.newsType === 'POLICY' ? 'success' : item.newsType === 'NOTICE' ? 'warning' : 'primary'">
              {{ typeLabel(item.newsType) }}
            </el-tag>
          </div>
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-summary">{{ item.summary || '暂无摘要' }}</p>
          <div class="card-footer">
            <span class="footer-time">📅 {{ formatTime(item.publishTime) }}</span>
            <span class="footer-views">👁 {{ item.viewCount || 0 }} 次阅读</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && newsList.length === 0" description="暂无新闻资讯" />
    </div>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchNews"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNewsPage } from '@/api/news'
import { rolePath } from '@/utils/roleRouter'

const newsList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(9)
const total = ref(0)
const filterType = ref('')

function typeLabel(type) {
  return type === 'NEWS' ? '环保新闻' : type === 'POLICY' ? '政策法规' : '系统公告'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

function onFilterChange() {
  page.value = 1
  fetchNews()
}

async function fetchNews() {
  loading.value = true
  try {
    const params = filterType.value ? { newsType: filterType.value } : {}
    const res = await getNewsPage(page.value, size.value, params)
    newsList.value = res.data || []
    total.value = res.total || 0
  } catch (e) {} finally { loading.value = false }
}

onMounted(fetchNews)
</script>

<style scoped>
.news-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; padding: 32px; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 600; margin: 0 0 8px 0; color: #1C2421; }
.page-subtitle { font-size: 14px; color: #74807B; margin: 0; }
.news-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.news-card { border-radius: 20px; overflow: hidden; cursor: pointer; transition: all 0.3s; }
.news-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); }
.card-cover { height: 180px; overflow: hidden; background: linear-gradient(135deg, #e8f5e9, #e0f2f1); }
.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.card-body { padding: 20px; }
.card-tags { margin-bottom: 10px; }
.card-title { font-size: 16px; font-weight: 600; color: #1C2421; margin: 0 0 10px 0; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-summary { font-size: 13px; color: #74807B; line-height: 1.6; margin: 0 0 16px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-footer { display: flex; justify-content: space-between; font-size: 12px; color: #A0AAB2; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; }

.glass-panel {
  background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8);
  border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04);
}

@media (max-width: 900px) { .news-grid { grid-template-columns: 1fr; } }
</style>
