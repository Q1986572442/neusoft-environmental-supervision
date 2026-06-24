<template>
  <div class="neps-home">
    <div class="welcome-card">
      <h1>{{ greeting }}，{{ userName }}</h1>
      <p>欢迎使用东软环保公众监督平台，您的每一次反馈都在守护碧水蓝天</p>
    </div>

    <div class="stats-row">
      <div class="stat-card clickable" @click="$router.push('/ne/feedbacks')"><div class="num">{{ stats.myFeedbacks }}</div><div class="label">我的反馈</div></div>
      <div class="stat-card clickable" @click="$router.push('/ne/feedbacks')"><div class="num">{{ stats.pendingCount }}</div><div class="label">待处理</div></div>
      <div class="stat-card clickable" @click="$router.push('/ne/feedbacks')"><div class="num">{{ stats.completedCount }}</div><div class="label">已办结</div></div>
      <div class="stat-card"><div class="num">{{ stats.aqiTotal }}</div><div class="label">全国AQI检测</div></div>
    </div>

    <div class="action-grid">
      <div class="action-card" @click="$router.push('/ne/submit')">
        <el-icon :size="32"><EditPen /></el-icon>
        <h3>提交监督反馈</h3>
        <p>观测身边空气质量，提交AQI等级预估和描述</p>
      </div>
      <div class="action-card" @click="$router.push('/ne/feedbacks')">
        <el-icon :size="32"><Document /></el-icon>
        <h3>我的反馈记录</h3>
        <p>查看已提交的反馈及其处理进度</p>
      </div>
      <div class="action-card" @click="$router.push('/ne/news')">
        <el-icon :size="32"><Reading /></el-icon>
        <h3>环保资讯</h3>
        <p>了解最新环保政策与新闻动态</p>
      </div>
      <div class="action-card" @click="$router.push('/ne/knowledge')">
        <el-icon :size="32"><Collection /></el-icon>
        <h3>环保知识库</h3>
        <p>学习环保知识，提升环境保护意识</p>
      </div>
    </div>

    <div class="recent-section">
      <h3>最近提交的反馈</h3>
      <el-table v-if="recentFeedbacks.length > 0" :data="recentFeedbacks" stripe size="small">
        <el-table-column prop="id" label="编号" width="70"/>
        <el-table-column prop="specificAddress" label="地址" show-overflow-tooltip/>
        <el-table-column prop="estimatedAqiLevel" label="预估AQI" width="90">
          <template #default="{row}">
            <el-tag :type="row.estimatedAqiLevel <= 2 ? 'success' : row.estimatedAqiLevel <= 4 ? 'warning' : 'danger'">{{ row.estimatedAqiLevel }}级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'ASSIGNED' ? 'primary' : 'warning'">
              {{ row.status === 'PENDING' ? '待指派' : row.status === 'ASSIGNED' ? '处理中' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-guide">
        <el-icon :size="48"><Folder /></el-icon>
        <p>您还没有提交过监督反馈</p>
        <el-button type="primary" @click="$router.push('/ne/submit')">👉 立即提交第一条反馈</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { getMyFeedback } from '@/api/feedback'
import { getOverview } from '@/api/statistics'

const userName = ref(localStorage.getItem('userName') || '监督员')
const recentFeedbacks = ref([])
const stats = ref({ myFeedbacks: 0, pendingCount: 0, completedCount: 0, aqiTotal: 0 })

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function formatTime(t) { if (!t) return ''; return t.replace('T', ' ').substring(0, 16) }

onMounted(async () => {
  const uid = localStorage.getItem('userId')
  if (uid) {
    try {
      const res = await getMyFeedback(Number(uid))
      const data = res.data || []
      recentFeedbacks.value = data.slice(0, 5)
      stats.value.myFeedbacks = data.length
      stats.value.pendingCount = data.filter(f => f.status === 'PENDING').length
      stats.value.completedCount = data.filter(f => f.status === 'COMPLETED').length
    } catch(e) {}
  }
  try {
    const r = await getOverview()
    if (r.data) stats.value.aqiTotal = r.data.totalDetections || 0
  } catch(e) {}
})
</script>

<style scoped>
.neps-home { max-width:1000px; margin:0 auto; }
.welcome-card { background:linear-gradient(135deg,#0c8c3f,#1a6b3a); color:#fff; padding:40px; border-radius:16px; margin-bottom:24px; }
.welcome-card h1 { margin:0 0 8px; font-size:24px; }
.welcome-card p { margin:0; opacity:0.85; font-size:14px; }

.stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:24px; }
.stat-card { background:#fff; padding:20px; border-radius:12px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.stat-card.clickable { cursor:pointer; transition:all 0.2s; }
.stat-card.clickable:hover { transform:translateY(-2px); box-shadow:0 6px 16px rgba(0,0,0,0.08); }
.stat-card .num { font-size:28px; font-weight:700; color:#2A483A; }
.stat-card .label { font-size:13px; color:#888; margin-top:4px; }

.action-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:16px; margin-bottom:24px; }
.action-card { background:#fff; padding:24px; border-radius:12px; cursor:pointer; transition:all 0.3s; box-shadow:0 2px 8px rgba(0,0,0,0.04); border:1px solid #eee; }
.action-card:hover { transform:translateY(-2px); box-shadow:0 8px 24px rgba(0,0,0,0.08); }
.action-card h3 { margin:12px 0 4px; font-size:16px; color:#333; }
.action-card p { margin:0; font-size:13px; color:#888; }
.action-card .el-icon { color:#2A483A; }

.recent-section { background:#fff; padding:24px; border-radius:12px; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.recent-section h3 { margin:0 0 16px; font-size:16px; color:#333; }
.empty-guide { text-align:center; padding:40px 0; color:#999; }
.empty-guide p { margin:12px 0 16px; font-size:14px; }
.empty-guide .el-icon { color:#ccc; }
</style>
