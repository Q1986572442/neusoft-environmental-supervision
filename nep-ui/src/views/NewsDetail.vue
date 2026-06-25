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

      <!-- 社交互动栏 -->
      <div class="social-bar">
        <el-button :type="liked ? 'danger' : 'default'" plain size="small" @click="handleLike" :loading="liking">
          <el-icon><StarFilled v-if="liked" /><Star v-else /></el-icon>
          {{ liked ? '已点赞' : '点赞' }} ({{ likeCount }})
        </el-button>
        <el-button :type="favorited ? 'warning' : 'default'" plain size="small" @click="handleFavorite">
          <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon>
          {{ favorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button plain size="small" @click="handleShare">
          <el-icon><Share /></el-icon> 分享
        </el-button>
      </div>

      <!-- 评论 -->
      <CommentSection target-type="NEWS" :target-id="Number(route.params.id)" />

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
import { toggleLike, toggleFavorite, checkLiked, checkFavorited } from '@/api/social'
import { rolePath } from '@/utils/roleRouter'
import { ElMessage } from 'element-plus'
import CommentSection from '@/components/CommentSection.vue'

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
    news.value.likeCount = likeCount.value
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
    ElMessage.success('链接已复制到剪贴板，分享给朋友吧！')
  }).catch(() => {
    ElMessage.info(`分享链接: ${url}`)
  })
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getNewsById(route.params.id)
    news.value = res.data
    likeCount.value = news.value.likeCount || 0
    await loadSocialStatus()
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
.social-bar { display: flex; gap: 10px; margin-top: 32px; padding: 16px 0; border-top: 1px solid rgba(28,36,33,0.06); border-bottom: 1px solid rgba(28,36,33,0.06); }
.article-footer { margin-top: 32px; padding-top: 20px; border-top: 1px solid rgba(28,36,33,0.06); }
.glass-panel { background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04); }
</style>
