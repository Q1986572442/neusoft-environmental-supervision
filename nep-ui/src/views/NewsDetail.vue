<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-container glass-panel" v-if="news">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: rolePath('/news') }">环保资讯</el-breadcrumb-item>
          <el-breadcrumb-item>{{ news.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <article class="article">
        <h1 class="article-title">{{ news.title }}</h1>
        <div class="article-meta">
          <span><el-icon><Clock /></el-icon> {{ formatTime(news.publishTime) }}</span>
          <span><el-icon><View /></el-icon> {{ news.viewCount || 0 }} 次阅读</span>
          <el-tag size="small" :type="news.newsType === 'POLICY' ? 'success' : news.newsType === 'NOTICE' ? 'warning' : 'primary'">
            {{ news.newsType === 'NEWS' ? '环保新闻' : news.newsType === 'POLICY' ? '政策法规' : '系统公告' }}
          </el-tag>
        </div>
        <div class="article-content" v-html="news.content || '暂无详细内容'"></div>
      </article>

      <div class="article-footer">
        <el-button @click="$router.push(rolePath('/news'))">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
      </div>
    </div>
    <el-empty v-if="!loading && !news" description="新闻不存在或已被删除" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNewsById } from '@/api/news'
import { rolePath } from '@/utils/roleRouter'

const route = useRoute()
const news = ref(null)
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNewsById(route.params.id)
    news.value = res.data
  } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.detail-page { max-width: 900px; margin: 0 auto; padding: 24px; }
.detail-container { padding: 40px; }
.breadcrumb { margin-bottom: 24px; }
.article-title { font-size: 28px; font-weight: 700; color: #1C2421; line-height: 1.4; margin: 0 0 16px 0; }
.article-meta { display: flex; align-items: center; gap: 20px; font-size: 13px; color: #A0AAB2; margin-bottom: 32px; padding-bottom: 20px; border-bottom: 1px solid rgba(28,36,33,0.06); }
.article-content { font-size: 15px; color: #333; line-height: 1.8; }
.article-content :deep(h3) { font-size: 18px; font-weight: 600; margin: 24px 0 12px; color: #1C2421; }
.article-content :deep(p) { margin: 0 0 12px; }
.article-footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid rgba(28,36,33,0.06); }
.glass-panel { background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04); }
</style>
