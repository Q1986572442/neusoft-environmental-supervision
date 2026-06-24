<template>
  <div class="swiss-dashboard">
    <section class="hero-section glass-panel fixed-section">
      <div class="hero-content">
        <h1 class="hero-greeting">{{ greeting }} <span class="hero-name">{{ userStore.user?.realName || '用户' }}</span></h1>
        <p class="hero-date">{{ today }} · 环保监测网络运行良好</p>
      </div>
      <div class="hero-status">
        <div class="status-icon-ring">
          <el-icon><Odometer /></el-icon>
        </div>
        <div class="status-info">
          <div class="status-title">实时监测中</div>
          <div class="status-subtitle">已覆盖全国 106 座城市</div>
        </div>
      </div>
    </section>

    <section class="stats-grid fixed-section">
      <div class="stat-glass-card" v-for="card in statCards" :key="card.label">
        <div class="stat-header">
          <span class="stat-label">{{ card.label }}</span>
          <div class="stat-icon-wrapper">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
        </div>
        <div class="stat-body">
          <div class="stat-value">
            <span class="value-number">{{ card.value }}</span>
            <span class="value-unit">{{ card.unit }}</span>
          </div>
        </div>
        <div class="stat-footer">
          <div class="trend-indicator" :class="card.trend > 0 ? 'is-positive' : 'is-negative'">
            <el-icon><component :is="card.trend > 0 ? 'TopRight' : 'BottomRight'" /></el-icon>
            <span>{{ Math.abs(card.trend) }}%</span>
          </div>
          <span class="trend-context">较上周对比</span>
        </div>
      </div>
    </section>

    <section class="content-split-grid stretch-section">
      
      <div class="action-bento glass-panel scrollable-card">
        <div class="panel-header">
          <h3 class="panel-title">快捷操作</h3>
        </div>
        <div class="bento-grid scroll-area">
          <div class="bento-item" v-for="act in quickActions" :key="act.label" @click="$router.push(act.path)">
            <div class="bento-icon"><el-icon><component :is="act.icon" /></el-icon></div>
            <span class="bento-label">{{ act.label }}</span>
          </div>
        </div>
      </div>

      <div class="feedback-stream glass-panel scrollable-card">
        <div class="panel-header">
          <h3 class="panel-title">最新监督反馈</h3>
          <button class="text-link-btn" @click="$router.push('/feedback')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <div class="stream-list scroll-area" v-loading="feedLoading">
          <div
            class="stream-item"
            v-for="fb in recentFeedbacks"
            :key="fb.id"
            @click="$router.push('/feedback')"
          >
            <div class="aqi-indicator" :class="'aqi-level-' + fb.estimatedAqiLevel">
               <div class="aqi-dot"></div>
            </div>
            
            <div class="stream-content">
              <div class="stream-desc">{{ fb.description || '常规空气质量记录' }}</div>
              <div class="stream-meta">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ fb.specificAddress || '网格区域 #' + fb.cityId }}</span>
              </div>
            </div>

            <div class="stream-status">
              <span class="status-pill" :class="'status-' + fb.status.toLowerCase()">
                {{ statusLabel(fb.status) }}
              </span>
              <span class="stream-time">{{ formatTime(fb.createTime) }}</span>
            </div>
          </div>
          <el-empty v-if="!feedLoading && recentFeedbacks.length === 0" description="暂无最新反馈" :image-size="60" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getFeedbackPage } from '@/api/feedback'
import { 
  User, DataAnalysis, CircleCheck, Location, ArrowRight,
  TopRight, BottomRight, Odometer, LocationInformation,
  EditPen, Document, ChatDotRound, PieChart, Setting
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const feedLoading = ref(false)
const recentFeedbacks = ref([])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，'
  if (h < 12) return '早上好，'
  if (h < 14) return '中午好，'
  if (h < 18) return '下午好，'
  return '晚上好，'
})

const today = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    month: 'long', day: 'numeric', weekday: 'long'
  })
})

const statCards = ref([
  { label: '公众监督员', value: '12,846', unit: '人', icon: User, trend: 12 },
  { label: 'AQI 监测总数', value: '35,862', unit: '次', icon: DataAnalysis, trend: 8 },
  { label: '反馈处理率', value: '87.5', unit: '%', icon: CircleCheck, trend: -3 },
  { label: '覆盖城市', value: '106', unit: '座', icon: Location, trend: 15 },
])

const quickActions = [
  { label: '提交反馈', icon: EditPen, path: '/feedback/submit' },
  { label: '反馈列表', icon: Document, path: '/feedback' },
  { label: 'AI 助手', icon: ChatDotRound, path: '/ai' },
  { label: '数据大盘', icon: PieChart, path: '/statistics' },
  { label: '个人中心', icon: User, path: '/profile' },
  { label: '管理后台', icon: Setting, path: '/admin/dashboard' },
]

function statusLabel(s) {
  return s === 'PENDING' ? '待指派' : s === 'ASSIGNED' ? '处理中' : '已完成'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  feedLoading.value = true
  try {
    const res = await getFeedbackPage(1, 10) // 请求更多数据以便演示滑动效果
    recentFeedbacks.value = res.data || []
  } catch (e) {} finally { feedLoading.value = false }
})
</script>

<style scoped>
/* ========== 核心空间调度 (Spatial Layout) ========== */
.swiss-dashboard {
  max-width: 1440px;
  width: 100%;
  height: 100%; /* 填满父容器 */
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  color: #1C2421;
}

/* 锁定部分：禁止压缩 */
.fixed-section {
  flex-shrink: 0;
}

/* 伸缩部分：填满剩余空间 */
.stretch-section {
  flex: 1;
  min-height: 0; /* 这是让 flex 子元素能够在内部发生滚动的绝对关键 */
}

/* ========== 通用玻璃面板 ========== */
.glass-panel {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

/* ========== 1. 欢迎区 ========== */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 36px 48px;
}
.hero-greeting { font-size: 26px; font-weight: 400; margin: 0 0 8px 0; letter-spacing: 0.5px; color: #74807B; }
.hero-name { font-weight: 600; color: #1C2421; }
.hero-date { font-size: 14px; font-weight: 500; color: #74807B; margin: 0; letter-spacing: 0.5px; }

.hero-status {
  display: flex; align-items: center; gap: 16px; padding: 12px 24px;
  background: rgba(255, 255, 255, 0.5); border-radius: 100px; border: 1px solid rgba(255, 255, 255, 0.6);
}
.status-icon-ring {
  width: 36px; height: 36px; border-radius: 50%; background: #2A483A; color: #FFF;
  display: flex; justify-content: center; align-items: center; font-size: 18px;
}
.status-title { font-size: 14px; font-weight: 600; color: #1C2421; }
.status-subtitle { font-size: 12px; color: #74807B; margin-top: 2px; }

/* ========== 2. 统计数据网格 ========== */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.stat-glass-card {
  background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 24px;
  box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04);
  padding: 24px; transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); cursor: pointer;
}
.stat-glass-card:hover {
  background: rgba(255, 255, 255, 0.85); transform: translateY(-4px); box-shadow: 0 16px 40px -8px rgba(0, 0, 0, 0.08);
}
.stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.stat-label { font-size: 14px; font-weight: 600; color: #74807B; letter-spacing: 0.5px; }
.stat-icon-wrapper {
  width: 32px; height: 32px; border-radius: 10px; background: rgba(28, 36, 33, 0.04); color: #2A483A;
  display: flex; justify-content: center; align-items: center; font-size: 16px;
}
.stat-body { margin-bottom: 16px; }
.value-number { font-size: 32px; font-weight: 700; color: #1C2421; letter-spacing: 0.5px; }
.value-unit { font-size: 14px; color: #74807B; margin-left: 6px; font-weight: 500; }
.stat-footer { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 500; }
.trend-indicator { display: flex; align-items: center; gap: 4px; padding: 4px 8px; border-radius: 6px; }
.trend-indicator.is-positive { background: rgba(42, 168, 118, 0.1); color: #2AA876; }
.trend-indicator.is-negative { background: rgba(217, 83, 79, 0.1); color: #D9534F; }
.trend-context { color: #A0AAB2; }

/* ========== 3. 操作与反馈分栏 (滑动核心区) ========== */
.content-split-grid {
  display: grid;
  grid-template-columns: 1fr 2fr; 
  gap: 24px;
}

.scrollable-card {
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止内部结构撑爆玻璃面板 */
}

.panel-header {
  flex-shrink: 0; /* 标题头不允许被挤压 */
  display: flex; justify-content: space-between; align-items: center;
  padding: 24px 28px 16px;
  border-bottom: 1px solid rgba(28, 36, 33, 0.06);
}
.panel-title { font-size: 16px; font-weight: 600; color: #1C2421; margin: 0; }
.text-link-btn {
  background: transparent; border: none; cursor: pointer;
  font-size: 13px; font-weight: 600; color: #2A483A;
  display: flex; align-items: center; gap: 4px; transition: opacity 0.2s;
}
.text-link-btn:hover { opacity: 0.7; }

/* 卡片内部滑动核心设置 */
.scroll-area {
  flex: 1; /* 吸干剩下的全部高度 */
  overflow-y: auto; /* 超出区域才产生滑动 */
  scrollbar-width: none; /* 隐藏火狐浏览器的丑陋滚动条 */
  -ms-overflow-style: none; /* 隐藏 IE 滚动条 */
}
.scroll-area::-webkit-scrollbar { 
  display: none; /* 隐藏 Chrome/Safari 的滚动条，保持纯净玻璃感 */
}

/* 便当盒控制矩阵 */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 24px 28px;
  align-content: start; /* 即使内容不够，也从顶部排列 */
}
.bento-item {
  background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 16px;
  padding: 20px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px;
  cursor: pointer; transition: all 0.3s;
}
.bento-item:hover {
  background: #FFFFFF; box-shadow: 0 8px 24px -6px rgba(0, 0, 0, 0.06); transform: scale(1.02);
}
.bento-icon { font-size: 24px; color: #2A483A; }
.bento-label { font-size: 13px; font-weight: 600; color: #1C2421; }

/* 极简反馈流 */
.stream-list { padding: 12px 28px 28px; }
.stream-item {
  display: flex; align-items: center; gap: 16px; padding: 16px 0;
  border-bottom: 1px solid rgba(28, 36, 33, 0.06); cursor: pointer; transition: padding 0.3s;
}
.stream-item:last-child { border-bottom: none; }
.stream-item:hover { 
  padding-left: 8px; padding-right: 8px; 
  background: rgba(255, 255, 255, 0.4); border-radius: 12px; border-bottom-color: transparent; 
}

.aqi-indicator { width: 40px; height: 40px; border-radius: 12px; background: rgba(28, 36, 33, 0.03); display: flex; justify-content: center; align-items: center; flex-shrink: 0; }
.aqi-dot { width: 12px; height: 12px; border-radius: 50%; }
.aqi-level-1 .aqi-dot, .aqi-level-2 .aqi-dot { background: #2AA876; box-shadow: 0 0 10px rgba(42, 168, 118, 0.4); }
.aqi-level-3 .aqi-dot { background: #F5A623; box-shadow: 0 0 10px rgba(245, 166, 35, 0.4); }
.aqi-level-4 .aqi-dot, .aqi-level-5 .aqi-dot, .aqi-level-6 .aqi-dot { background: #D9534F; box-shadow: 0 0 10px rgba(217, 83, 79, 0.4); }

.stream-content { flex: 1; min-width: 0; }
.stream-desc { font-size: 15px; font-weight: 500; color: #1C2421; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
.stream-meta { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #74807B; }

.stream-status { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0; }
.status-pill { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; letter-spacing: 0.5px; }
.status-pending { background: rgba(245, 166, 35, 0.15); color: #F5A623; }
.status-assigned { background: rgba(64, 158, 255, 0.15); color: #409EFF; }
.status-resolved { background: rgba(42, 168, 118, 0.15); color: #2AA876; }

.stream-time { font-size: 12px; color: #A0AAB2; font-weight: 500; }

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .content-split-grid { grid-template-columns: 1fr; }
}
</style>