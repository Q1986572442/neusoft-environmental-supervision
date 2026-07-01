<template>
  <div class="swiss-page-container" v-loading="loading">
    
    <header class="action-console glass-panel fixed-section">
      <div class="console-left">
        <el-button class="swiss-btn icon-btn" @click="$router.push(rolePath('/news'))">
          <el-icon><Back /></el-icon>
        </el-button>
        <div style="margin-left:16px; display:flex; flex-direction:column; gap:4px">
          <h2 class="page-title">资讯详情</h2>
          <span class="page-subtitle">Information Detail</span>
        </div>
      </div>
      <div class="console-right">
        <button class="action-btn" :class="{ 'is-liked': liked }" @click="handleLike" :disabled="liking">
          <el-icon><StarFilled v-if="liked" /><Star v-else /></el-icon>
          <span>{{ likeCount }}</span>
        </button>
        <button class="action-btn" :class="{ 'is-fav': favorited }" @click="handleFavorite">
          <el-icon><CollectionTag /></el-icon>
        </button>
        <button class="action-btn" @click="handleShare" title="分享">
          <el-icon><Share /></el-icon>
        </button>
      </div>
    </header>

    <main class="stretch-section glass-panel scrollable-card">
      <div class="detail-body scroll-area" v-if="news">
        
        <article class="swiss-article">
          <header class="article-header">
            <h1 class="article-title">{{ news.title }}</h1>
            <div class="article-meta">
              <span class="meta-tag">{{ typeLabel(news.newsType) }}</span>
              <span class="meta-item"><el-icon><Clock /></el-icon> {{ formatTime(news.publishTime) }}</span>
              <span class="meta-item"><el-icon><View /></el-icon> {{ news.viewCount || 0 }} 次阅读</span>
            </div>
          </header>

          <div class="article-content" v-html="news.content || '<p>暂无详细内容</p>'"></div>
        </article>

        <div class="comments-section">
          <h3 class="section-title">讨论与评论</h3>
          <CommentSection target-type="NEWS" :target-id="Number(route.params.id)" />
        </div>

      </div>
      
      <div v-if="!loading && !news" class="empty-state-wrapper">
        <el-empty description="新闻不存在或已被删除" />
      </div>
    </main>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getNewsById } from '@/api/news'
import { toggleLike, toggleFavorite, checkLiked, checkFavorited } from '@/api/social'
import { rolePath } from '@/utils/roleRouter'
import { ElMessage } from 'element-plus'
import CommentSection from '@/components/CommentSection.vue'
import { Back, Star, StarFilled, CollectionTag, Share, Clock, View } from '@element-plus/icons-vue'

const route = useRoute()
const news = ref(null)
const loading = ref(false)
const liking = ref(false)

const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const currentUserId = Number(localStorage.getItem('userId') || 0)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function typeLabel(type) {
  return type === 'NEWS' ? '环保新闻' : type === 'POLICY' ? '政策法规' : '系统公告'
}

async function loadSocialStatus() {
  const id = Number(route.params.id)
  try {
    const [lr, fr] = await Promise.all([
      checkLiked(currentUserId, 'NEWS', id),
      checkFavorited(currentUserId, 'NEWS', id)
    ])
    liked.value = lr.data.liked
    favorited.value = fr.data.favorited
  } catch (e) {}
}

async function handleLike() {
  liking.value = true
  try {
    const res = await toggleLike(currentUserId, 'NEWS', Number(route.params.id))
    liked.value = res.data.liked
    likeCount.value = res.data.likeCount
    if (news.value) news.value.likeCount = likeCount.value
  } catch (e) {} finally { liking.value = false }
}

async function handleFavorite() {
  try {
    const res = await toggleFavorite(currentUserId, 'NEWS', Number(route.params.id))
    favorited.value = res.data.favorited
    ElMessage.success(favorited.value ? '已收藏' : '已取消收藏')
  } catch (e) {}
}

function handleShare() {
  const url = window.location.href
  navigator.clipboard?.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板，分享给同事吧！')
  }).catch(() => {
    ElMessage.info(`分享链接: ${url}`)
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNewsById(route.params.id)
    news.value = res.data
    likeCount.value = news.value?.likeCount || 0
    await loadSocialStatus()
  } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
/* ========== 全局主容器 ========== */
.swiss-page-container { max-width: 1440px; width: 100%; height: 100%; margin: 0 auto; display: flex; flex-direction: column; gap: 24px; padding-bottom: 32px; box-sizing: border-box; color: #1C2421; }
.fixed-section { flex-shrink: 0; }
.stretch-section { flex: 1; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }

.glass-panel { background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(24px) saturate(180%); -webkit-backdrop-filter: blur(24px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6); }

/* ========== 控制台 ========== */
.action-console { height: 100px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box; }
.console-left { display: flex; align-items: center; }
.page-title { font-size: 24px; font-weight: 600; margin: 0; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }
.console-right { display: flex; align-items: center; gap: 12px; }

.swiss-btn { border: none; outline: none; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.swiss-btn.icon-btn { width: 44px; height: 44px; background: rgba(255, 255, 255, 0.8); color: #1C2421; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); font-size: 20px; }
.swiss-btn.icon-btn:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }

/* 社交按钮 */
.action-btn { border: 1px solid rgba(28,36,33,0.1); background: rgba(255,255,255,0.5); height: 40px; padding: 0 16px; border-radius: 12px; display: inline-flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #74807B; cursor: pointer; transition: all 0.3s; }
.action-btn:hover { background: white; color: #1C2421; }
.action-btn.is-liked { background: #FFF1F2; color: #E11D48; border-color: #FECDD3; }
.action-btn.is-fav { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }

/* ========== 阅读区 ========== */
.scrollable-card { display: flex; flex-direction: column; }
.scroll-area { flex: 1; overflow-y: auto; padding: 48px; }
.scroll-area::-webkit-scrollbar { display: none; }

.swiss-article { max-width: 800px; margin: 0 auto; display: flex; flex-direction: column; }

.article-header { border-bottom: 1px solid rgba(28, 36, 33, 0.06); padding-bottom: 24px; margin-bottom: 32px; }
.article-title { font-size: 32px; font-weight: 700; color: #1C2421; line-height: 1.4; margin: 0 0 16px 0; }

.article-meta { display: flex; align-items: center; gap: 16px; }
.meta-tag { background: rgba(42, 72, 58, 0.08); color: #2A483A; padding: 4px 12px; border-radius: 8px; font-size: 13px; font-weight: 700; }
.meta-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #A0AAB2; font-weight: 500; }

.article-content { font-size: 16px; color: #333; line-height: 1.9; }
:deep(.article-content h2), :deep(.article-content h3) { font-size: 20px; font-weight: 600; margin: 32px 0 16px; color: #1C2421; }
:deep(.article-content p) { margin: 0 0 16px; }
:deep(.article-content img) { max-width: 100%; border-radius: 16px; margin: 24px 0; }

.comments-section { max-width: 800px; margin: 64px auto 0; padding-top: 32px; border-top: 1px solid rgba(28, 36, 33, 0.06); }
.section-title { font-size: 18px; font-weight: 600; color: #1C2421; margin-bottom: 24px; }

.empty-state-wrapper { height: 100%; display: flex; align-items: center; justify-content: center; }
</style>