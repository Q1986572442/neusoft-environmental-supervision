<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-container glass-panel" v-if="knowledge">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: rolePath('/knowledge') }">环保知识库</el-breadcrumb-item>
          <el-breadcrumb-item>{{ knowledge.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <article class="article">
        <h1 class="article-title">{{ knowledge.title }}</h1>
        <div class="article-meta">
          <span><el-icon><Clock /></el-icon> {{ formatTime(knowledge.createTime) }}</span>
          <span>👁 {{ knowledge.viewCount || 0 }} 次阅读</span>
          <span>❤ {{ knowledge.likeCount || 0 }} 次点赞</span>
          <el-tag size="small">{{ categoryLabel(knowledge.category) }}</el-tag>
          <span v-if="knowledge.source">来源: {{ knowledge.source }}</span>
        </div>
        <div class="article-content" v-html="knowledge.content || '暂无详细内容'"></div>
      </article>

      <div class="article-actions">
        <el-button type="danger" plain @click="handleLike">
          <el-icon><Star /></el-icon> 点赞 ({{ knowledge.likeCount || 0 }})
        </el-button>
        <el-button @click="$router.push(rolePath('/knowledge'))">
          <el-icon><ArrowLeft /></el-icon> 返回知识库
        </el-button>
      </div>
    </div>
    <el-empty v-if="!loading && !knowledge" description="文章不存在或已被删除" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getKnowledgeById, likeKnowledge } from '@/api/knowledge'
import { rolePath } from '@/utils/roleRouter'
import { ElMessage } from 'element-plus'

const route = useRoute()
const knowledge = ref(null)
const loading = ref(false)

function categoryLabel(c) {
  const map = { AIR: '大气环境', WATER: '水环境', SOIL: '土壤环境', NOISE: '噪声污染', ECOLOGY: '生态保护' }
  return map[c] || c
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function handleLike() {
  try {
    await likeKnowledge(route.params.id)
    knowledge.value.likeCount = (knowledge.value.likeCount || 0) + 1
    ElMessage.success('点赞成功！')
  } catch (e) {}
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getKnowledgeById(route.params.id)
    knowledge.value = res.data
  } catch (e) {} finally { loading.value = false }
})
</script>

<style scoped>
.detail-page { max-width: 900px; margin: 0 auto; padding: 24px; }
.detail-container { padding: 40px; }
.breadcrumb { margin-bottom: 24px; }
.article-title { font-size: 28px; font-weight: 700; color: #1C2421; line-height: 1.4; margin: 0 0 16px; }
.article-meta { display: flex; align-items: center; gap: 20px; font-size: 13px; color: #A0AAB2; margin-bottom: 32px; padding-bottom: 20px; border-bottom: 1px solid rgba(28,36,33,0.06); flex-wrap: wrap; }
.article-content { font-size: 15px; color: #333; line-height: 1.8; }
.article-content :deep(h3) { font-size: 18px; font-weight: 600; margin: 24px 0 12px; color: #1C2421; }
.article-content :deep(p) { margin: 0 0 12px; }
.article-actions { margin-top: 40px; padding-top: 20px; border-top: 1px solid rgba(28,36,33,0.06); display: flex; gap: 12px; }
.glass-panel { background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04); }
</style>
