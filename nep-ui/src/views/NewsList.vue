<template>
  <div class="swiss-page-container">
    
    <header class="action-console glass-panel fixed-section">
      <div class="console-left">
        <h2 class="page-title">环保资讯</h2>
        <span class="page-subtitle">了解最新环保政策、新闻动态与系统公告</span>
      </div>

      <div class="console-right">
        <div class="search-capsule">
          <el-icon><Search /></el-icon>
          <el-input
            v-model="searchQuery"
            placeholder="搜索资讯标题..."
            clearable
            class="keyword-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </div>
        <div class="search-capsule">
          <span class="filter-label">分类</span>
          <el-select v-model="filterType" placeholder="全部资讯" clearable class="swiss-select" popper-class="swiss-select-dropdown" @change="handleSearch">
            <el-option label="全部资讯" value="" />
            <el-option label="环保新闻" value="NEWS" />
            <el-option label="政策法规" value="POLICY" />
            <el-option label="系统公告" value="NOTICE" />
          </el-select>
        </div>
        <el-button class="swiss-btn icon-btn" @click="handleReset" title="重置视图">
          <el-icon><RefreshRight /></el-icon>
        </el-button>
      </div>
    </header>

    <main class="stretch-section" v-loading="loading">
      
      <div v-if="newsList.length === 0 && !loading" class="empty-state-wrapper glass-panel">
        <el-empty description="暂无新闻资讯" :image-size="80" />
      </div>

      <div v-else class="news-grid">
        <div 
          v-for="item in newsList" 
          :key="item.id" 
          class="news-card glass-panel"
          @click="$router.push(rolePath(`/news/${item.id}`))"
        >
          <div class="card-cover">
            <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" />
            <div v-else class="fallback-cover"><el-icon><PictureRounded /></el-icon></div>
            <div class="cover-overlay">
              <span class="swiss-blur-tag" :class="item.newsType.toLowerCase()">
                {{ typeLabel(item.newsType) }}
              </span>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-summary">{{ item.summary || '暂无摘要描述' }}</p>
            <div class="card-footer">
              <span class="footer-meta"><el-icon><Calendar /></el-icon> {{ formatTime(item.publishTime) }}</span>
              <span class="footer-meta"><el-icon><View /></el-icon> {{ item.viewCount || 0 }} 阅读</span>
            </div>
          </div>
        </div>
      </div>

      <div class="list-pagination" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="prev, pager, next"
          class="swiss-pagination"
          @current-change="fetchNews"
        />
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNewsPage } from '@/api/news'
import { rolePath } from '@/utils/roleRouter'
import { Search, RefreshRight, PictureRounded, Calendar, View } from '@element-plus/icons-vue'

const newsList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(9)
const total = ref(0)

const filterType = ref('')
const searchQuery = ref('')

function typeLabel(type) {
  return type === 'NEWS' ? '环保新闻' : type === 'POLICY' ? '政策法规' : '系统公告'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

function handleSearch() {
  page.value = 1
  fetchNews()
}

function handleReset() {
  filterType.value = ''
  searchQuery.value = ''
  handleSearch()
}

async function fetchNews() {
  loading.value = true
  try {
    const params = {}
    if (filterType.value) params.newsType = filterType.value
    if (searchQuery.value) params.keyword = searchQuery.value
    const res = await getNewsPage(page.value, size.value, params)
    newsList.value = res.data || []
    total.value = res.total || 0
  } catch (e) {} finally { loading.value = false }
}

onMounted(() => fetchNews())
</script>

<style scoped>
/* ========== 全局主容器 (对标 FeedbackList) ========== */
.swiss-page-container {
  max-width: 1440px; width: 100%; height: 100%; margin: 0 auto;
  display: flex; flex-direction: column; gap: 24px; padding-bottom: 32px; box-sizing: border-box; color: #1C2421;
}
.fixed-section { flex-shrink: 0; }
.stretch-section { flex: 1; min-height: 0; display: flex; flex-direction: column; }

.glass-panel {
  background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

/* ========== 控制台 ========== */
.action-console { height: 100px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box; }
.console-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 600; margin: 0; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }
.console-right { display: flex; justify-content: flex-end; align-items: center; gap: 16px; }

.search-capsule { display: flex; align-items: center; gap: 12px; background: rgba(255, 255, 255, 0.5); padding: 8px 16px 8px 20px; border-radius: 100px; border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 4px 16px -4px rgba(0, 0, 0, 0.03); }
.filter-label { font-size: 13px; font-weight: 600; color: #74807B; }
.keyword-input { width: 200px; }
.keyword-input :deep(.el-input__wrapper) { background: transparent !important; box-shadow: none !important; border-radius: 0; }

.swiss-btn { border: none; outline: none; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.swiss-btn.icon-btn { width: 40px; height: 40px; background: rgba(255, 255, 255, 0.8); color: #74807B; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); font-size: 18px; }
.swiss-btn.icon-btn:hover { background: #FFF; color: #1C2421; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }

/* ========== 网格卡片 ========== */
.news-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }
.news-card { border-radius: 20px; overflow: hidden; cursor: pointer; transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); display: flex; flex-direction: column; }
.news-card:hover { transform: translateY(-6px); box-shadow: 0 16px 32px -12px rgba(0, 0, 0, 0.08); border-color: rgba(42, 72, 58, 0.15); }

.card-cover { position: relative; height: 180px; overflow: hidden; display: flex; justify-content: center; align-items: center; }
.cover-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.6s ease; }
.news-card:hover .cover-image { transform: scale(1.05); }
.fallback-cover { width: 100%; height: 100%; background: linear-gradient(135deg, #E8F5E9 0%, #E0F2F1 100%); color: rgba(42, 72, 58, 0.2); font-size: 64px; display: flex; justify-content: center; align-items: center; }

.cover-overlay { position: absolute; top: 16px; left: 16px; }
.swiss-blur-tag { background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(8px); font-size: 12px; font-weight: 700; padding: 6px 12px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.swiss-blur-tag.policy { color: #2AA876; }
.swiss-blur-tag.notice { color: #E6A23C; }
.swiss-blur-tag.news { color: #409EFF; }

.card-body { padding: 20px; flex: 1; display: flex; flex-direction: column; }
.card-title { font-size: 16px; font-weight: 700; color: #1C2421; margin: 0 0 8px 0; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-summary { font-size: 13px; color: #74807B; line-height: 1.6; margin: 0 0 16px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; flex: 1; }
.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 16px; border-top: 1px solid rgba(28, 36, 33, 0.06); }
.footer-meta { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #A0AAB2; font-weight: 600; }

.list-pagination { display: flex; justify-content: center; margin-top: 32px; padding: 16px; }
:deep(.swiss-pagination) { --el-pagination-bg-color: transparent; }

.empty-state-wrapper { flex: 1; display: flex; justify-content: center; align-items: center; }
</style>
<style>
.swiss-select { width: 140px; }
.swiss-select .el-input__wrapper { background: transparent !important; box-shadow: none !important; }
.swiss-select-dropdown { background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 12px !important; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important; }
.swiss-select-dropdown .el-select-dropdown__item.selected { color: #2A483A !important; font-weight: 600 !important; }
</style>