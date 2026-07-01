<template>
  <div class="knowledge-page">
    <div class="page-header glass-panel">
      <div class="header-left">
        <h2 class="page-title">📚 环保知识库</h2>
        <p class="page-subtitle">学习环保知识，提升环境保护意识</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索知识标题..."
          clearable
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="onFilterChange"
          @clear="onFilterChange"
        />
      </div>
    </div>

    <div class="category-tabs">
      <el-radio-group v-model="filterCategory" @change="onFilterChange" size="small">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="AIR">🌬 大气环境</el-radio-button>
        <el-radio-button value="WATER">💧 水环境</el-radio-button>
        <el-radio-button value="SOIL">🌱 土壤环境</el-radio-button>
        <el-radio-button value="NOISE">🔊 噪声污染</el-radio-button>
        <el-radio-button value="ECOLOGY">🌳 生态保护</el-radio-button>
      </el-radio-group>
    </div>

    <div class="knowledge-grid" v-loading="loading">
      <div class="knowledge-card glass-panel" v-for="item in knowledgeList" :key="item.id" @click="$router.push(rolePath(`/knowledge/${item.id}`))">
        <div class="card-body">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-summary">{{ item.summary || '暂无摘要' }}</p>
          <div class="card-tags">
            <el-tag size="small" type="info">{{ categoryLabel(item.category) }}</el-tag>
          </div>
          <div class="card-footer">
            <span>👁 {{ item.viewCount || 0 }} 阅读</span>
            <span>❤ {{ item.likeCount || 0 }} 点赞</span>
            <span>📅 {{ formatTime(item.createTime) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && knowledgeList.length === 0" description="暂无知识文章" />
    </div>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" background />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getKnowledgePage } from '@/api/knowledge'
import { Search } from '@element-plus/icons-vue'

const knowledgeList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(9)
const total = ref(0)
const filterCategory = ref('')
const searchKeyword = ref('')

function categoryLabel(c) {
  const map = { AIR: '大气环境', WATER: '水环境', SOIL: '土壤环境', NOISE: '噪声污染', ECOLOGY: '生态保护' }
  return map[c] || c
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

function onFilterChange() { page.value = 1; fetchData() }

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    if (filterCategory.value) params.category = filterCategory.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getKnowledgePage(page.value, size.value, params)
    knowledgeList.value = res.data || []
    total.value = res.total || 0
  } catch (e) {} finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.knowledge-page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { padding: 32px; margin-bottom: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-size: 24px; font-weight: 600; margin: 0 0 8px; color: #1C2421; }
.page-subtitle { font-size: 14px; color: #74807B; margin: 0; }
.header-right { flex-shrink: 0; }
.search-input { width: 260px; }
.category-tabs { margin-bottom: 24px; display: flex; justify-content: center; }
.knowledge-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.knowledge-card { border-radius: 20px; padding: 24px; cursor: pointer; transition: all 0.3s; }
.knowledge-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); }
.card-title { font-size: 16px; font-weight: 600; color: #1C2421; margin: 0 0 12px; }
.card-summary { font-size: 13px; color: #74807B; line-height: 1.6; margin: 0 0 16px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.card-tags { margin-bottom: 12px; }
.card-footer { display: flex; gap: 16px; font-size: 12px; color: #A0AAB2; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; }
.glass-panel { background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04); }
@media (max-width: 900px) { .knowledge-grid { grid-template-columns: 1fr; } }
</style>
