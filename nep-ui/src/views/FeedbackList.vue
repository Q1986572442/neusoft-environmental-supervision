<template>
  <div class="swiss-page-container">
    
    <header class="action-console glass-panel fixed-section">
      <div class="console-left">
        <h2 class="page-title">监督反馈网络</h2>
        <span class="page-subtitle">环境问题流转与实时追踪</span>
      </div>

      <div class="console-right">
        <div class="search-capsule">
          <span class="filter-label">流转阶段</span>
          <el-select 
            v-model="searchForm.status" 
            placeholder="全景视图" 
            clearable 
            class="swiss-select"
            popper-class="swiss-select-dropdown"
            @change="handleSearch"
          >
            <el-option label="全景视图" value="" />
            <el-option label="待指派" value="PENDING" />
            <el-option label="网格员处理中" value="ASSIGNED" />
            <el-option label="案件已归档" value="COMPLETED" />
          </el-select>
        </div>

        <div class="divider"></div>

        <button class="swiss-btn icon-btn" @click="handleReset" title="重置视图">
          <el-icon><RefreshRight /></el-icon>
        </button>
        <button class="swiss-btn primary-btn" @click="$router.push('/ne/submit')">
          <el-icon><Plus /></el-icon> 提报新案件
        </button>
      </div>
    </header>

    <main class="spatial-split-view stretch-section">
      
      <aside class="master-list-panel glass-panel scrollable-card">
        <div class="list-header panel-header">
          <span class="header-text">近期案件</span>
          <span class="item-count">{{ total }} 宗</span>
        </div>
        
        <div class="card-flow scroll-area" v-loading="loading">
          <div 
            v-for="item in tableData" 
            :key="item.id"
            class="feed-ticket-card"
            :class="{ 'is-active': selectedFeedback?.id === item.id }"
            @click="selectFeedback(item)"
          >
            <div class="ticket-top">
              <span class="ticket-id">#{{ String(item.id).padStart(5, '0') }}</span>
              <div class="mini-status" :class="'status-' + item.status.toLowerCase()"></div>
            </div>
            <div class="ticket-main">
              <div class="ticket-title">{{ item.description || '常规环境监测记录' }}</div>
              <div class="ticket-meta">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ item.specificAddress || `网格 #${item.cityId}` }}</span>
              </div>
            </div>
            <div class="ticket-bottom">
              <span class="time-stamp">{{ formatTime(item.createTime) }}</span>
              <div class="aqi-badge" :class="'aqi-level-' + item.estimatedAqiLevel">
                AQI {{ item.estimatedAqiLevel }}
              </div>
            </div>
          </div>
          
          <div class="empty-state-wrapper" v-if="!loading && tableData.length === 0">
            <el-empty description="暂无追踪档案" :image-size="60" />
          </div>
        </div>

        <div class="list-pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            class="swiss-pagination"
            @current-change="handleSearch"
          />
        </div>
      </aside>

      <section class="detail-view-panel glass-panel scrollable-card">
        <template v-if="selectedFeedback">
          <div class="detail-header panel-header">
            <div class="header-left">
              <div class="detail-id-badge">案件 #{{ String(selectedFeedback.id).padStart(5, '0') }}</div>
            </div>
            <div class="header-right">
              <span class="sub-time">立案于 {{ formatTime(selectedFeedback.createTime) }}</span>
            </div>
          </div>

          <div class="detail-body scroll-area">
            <div class="flow-tracker-container">
              <h3 class="bento-title">生命周期追踪</h3>
              <div class="timeline-track">
                <div 
                  v-for="(step, index) in flowSteps" 
                  :key="index"
                  class="track-step"
                  :class="{ 
                    'is-completed': index < currentStepIndex,
                    'is-active': index === currentStepIndex,
                    'is-pending': index > currentStepIndex
                  }"
                >
                  <div class="step-line" v-if="index < flowSteps.length - 1"></div>
                  
                  <div class="step-node">
                    <div class="node-inner"></div>
                  </div>
                  <div class="step-label">{{ step.label }}</div>
                </div>
              </div>
            </div>

            <div class="detail-bento-grid">
              
              <div class="bento-card col-span-2">
                <div class="bento-icon-header"><el-icon><Location /></el-icon> <span>定位坐标</span></div>
                <div class="bento-main-text">{{ selectedFeedback.specificAddress || '未记录详细物理坐标' }}</div>
                <div class="bento-sub-text">行政区划代码: {{ selectedFeedback.provinceId }} - {{ selectedFeedback.cityId }}</div>
              </div>

              <div class="bento-card">
                <div class="bento-icon-header"><el-icon><DataLine /></el-icon> <span>AQI 评估</span></div>
                <div class="aqi-massive-display" :class="'aqi-text-' + selectedFeedback.estimatedAqiLevel">
                  {{ selectedFeedback.estimatedAqiLevel }} <span class="unit">级</span>
                </div>
              </div>

              <div class="bento-card">
                <div class="bento-icon-header"><el-icon><User /></el-icon> <span>人员调度</span></div>
                <div class="personnel-info">
                  <div class="avatar-placeholder"><el-icon><Avatar /></el-icon></div>
                  <div class="personnel-text">
                    <span class="p-name">{{ selectedFeedback.assignedInspectorId ? 'ID: ' + selectedFeedback.assignedInspectorId : '待指派' }}</span>
                    <span class="p-role">负责网格员</span>
                  </div>
                </div>
              </div>

              <div class="bento-card col-span-2">
                <div class="bento-icon-header"><el-icon><Document /></el-icon> <span>现场勘测描述</span></div>
                <div class="description-box">
                  {{ selectedFeedback.description || '监督员仅上传了坐标，未提供详细文字描述。' }}
                </div>
              </div>

            </div>
          </div>
        </template>
        
        <div class="empty-state-view" v-else>
          <el-icon class="placeholder-icon"><Document /></el-icon>
          <p>请在左侧列表选择一宗案件查看详情</p>
        </div>
      </section>
      
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getFeedbackPage } from '@/api/feedback'
import { 
  RefreshRight, Plus, LocationInformation, 
  Location, DataLine, User, Avatar, Document
} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(15) 
const total = ref(0)
const searchForm = ref({ status: '' })
const selectedFeedback = ref(null)

const flowSteps = [
  { label: '立案' },
  { label: '派发' },
  { label: '接办' },
  { label: '勘测' },
  { label: '处理' },
  { label: '验收' }
]

const currentStepIndex = computed(() => {
  if (!selectedFeedback.value) return 0
  const s = selectedFeedback.value.status
  if (s === 'PENDING') return 0 
  if (s === 'ASSIGNED') return 2 
  if (s === 'COMPLETED') return 5 
  return 0
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function selectFeedback(item) {
  selectedFeedback.value = item
}

async function handleSearch() {
  loading.value = true
  try {
    const userId = localStorage.getItem('userId')
    const role = localStorage.getItem('userRole')
    let res
    if (role === 'NEPS' && userId) {
      const { getMyFeedback } = await import('@/api/feedback')
      res = await getMyFeedback(Number(userId))
      const allData = res.data || []
      let filtered = allData
      if (searchForm.value.status) {
        filtered = allData.filter(f => f.status === searchForm.value.status)
      }
      tableData.value = filtered
      total.value = filtered.length
    } else {
      res = await getFeedbackPage(currentPage.value, pageSize.value, searchForm.value.status)
      tableData.value = res.data
      total.value = res.total
    }
    if (tableData.value.length > 0 && !selectedFeedback.value) {
      selectedFeedback.value = tableData.value[0]
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.value.status = ''
  currentPage.value = 1
  selectedFeedback.value = null
  handleSearch()
}

const route = useRoute()

// 每次进入页面都刷新数据
onMounted(() => { handleSearch() })
onActivated(() => { handleSearch() })
// 监听路由变化（同一组件不同参数时刷新）
watch(() => route.fullPath, () => { handleSearch() })
</script>

<style scoped>
/* ========== 全局主容器 ========== */
.swiss-page-container {
  max-width: 1440px;
  width: 100%;
  height: 100%; 
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 32px;
  box-sizing: border-box;
  color: #1C2421;
}

.fixed-section { flex-shrink: 0; }
.stretch-section { flex: 1; min-height: 0; }

.glass-panel {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

.scrollable-card {
  height: 100%; 
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
  min-height: 300px; 
}
.scroll-area::-webkit-scrollbar { display: none; }

.panel-header {
  flex-shrink: 0;
  display: flex; justify-content: space-between; align-items: center;
  padding: 24px 28px 16px;
  border-bottom: 1px solid rgba(28, 36, 33, 0.06);
}

/* ========== 1. 悬浮控制台 (经典的左右分布) ========== */
.action-console {
  height: 120px; 
  padding: 0 48px; 
  display: flex;
  justify-content: space-between; /* 恢复纯粹的左右顶格布局 */
  align-items: center;
  box-sizing: border-box;
}

.console-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 500; margin: 0; letter-spacing: 0.5px; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }

.console-right { display: flex; justify-content: flex-end; align-items: center; gap: 16px; }

/* 右侧集成的筛选胶囊 */
.search-capsule {
  display: flex; align-items: center; gap: 12px;
  background: rgba(255, 255, 255, 0.5);
  padding: 8px 16px 8px 20px;
  border-radius: 100px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px -4px rgba(0,0,0,0.03);
}
.filter-label { font-size: 13px; font-weight: 600; color: #74807B; }
.divider { width: 1px; height: 24px; background: rgba(28, 36, 33, 0.1); margin: 0 4px; }

.swiss-btn {
  border: none; outline: none; border-radius: 12px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  font-size: 14px; font-weight: 600; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.swiss-btn.icon-btn { 
  width: 40px; height: 40px; background: rgba(255, 255, 255, 0.8); color: #74807B; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.swiss-btn.icon-btn:hover { background: #FFF; color: #1C2421; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.swiss-btn.primary-btn { 
  padding: 0 20px; height: 40px; background: #2A483A; color: #FFF;
  box-shadow: 0 4px 12px rgba(42, 72, 58, 0.2); 
}
.swiss-btn.primary-btn:hover { background: #1C2421; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(42, 72, 58, 0.3); }

/* ========== 2. 主从分栏结构 ========== */
.spatial-split-view {
  display: grid;
  grid-template-columns: 360px 1fr; 
  gap: 24px;
  align-items: stretch; 
}

.master-list-panel { background: rgba(255, 255, 255, 0.55); }
.list-header .header-text { font-size: 14px; font-weight: 600; color: #1C2421; }
.list-header .item-count { font-size: 12px; background: rgba(28, 36, 33, 0.05); padding: 2px 10px; border-radius: 12px; font-weight: 600; color: #74807B; }

.card-flow { padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.feed-ticket-card {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 16px; padding: 16px; cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.feed-ticket-card:hover { background: rgba(255, 255, 255, 0.9); transform: translateY(-2px); box-shadow: 0 6px 16px -4px rgba(0, 0, 0, 0.05); }
.feed-ticket-card.is-active {
  background: #FFFFFF; border-color: rgba(42, 72, 58, 0.15);
  box-shadow: 0 8px 24px -6px rgba(42, 72, 58, 0.1), inset 0 2px 4px rgba(255, 255, 255, 1);
}

.ticket-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.ticket-id { font-family: "SF Mono", Consolas, monospace; font-size: 13px; font-weight: 600; color: #2A483A; }
.mini-status { width: 8px; height: 8px; border-radius: 50%; }
.status-pending { background: #F5A623; box-shadow: 0 0 6px rgba(245, 166, 35, 0.4); }
.status-assigned { background: #409EFF; box-shadow: 0 0 6px rgba(64, 158, 255, 0.4); }
.status-completed { background: #2AA876; box-shadow: 0 0 6px rgba(42, 168, 118, 0.4); }

.ticket-main { margin-bottom: 12px; }
.ticket-title { font-size: 14px; font-weight: 600; color: #1C2421; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ticket-meta { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #74807B; }

.ticket-bottom { display: flex; justify-content: space-between; align-items: center; }
.time-stamp { font-size: 12px; font-weight: 500; color: #A0AAB2; }
.aqi-badge { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 8px; background: rgba(28, 36, 33, 0.04); }

.empty-state-wrapper { height: 100%; display: flex; align-items: center; justify-content: center; }
.list-pagination { padding: 12px; border-top: 1px solid rgba(28, 36, 33, 0.06); display: flex; justify-content: center; flex-shrink: 0; }
:deep(.swiss-pagination) { --el-pagination-bg-color: transparent; }

/* === 右栏：详情视图 === */
.detail-header { padding: 24px 32px 20px; }
.detail-id-badge { font-size: 16px; font-weight: 700; color: #2A483A; background: rgba(42, 72, 58, 0.08); padding: 6px 14px; border-radius: 12px; letter-spacing: 0.5px; }
.sub-time { font-size: 13px; font-weight: 500; color: #74807B; }

.detail-body { padding: 0 32px 32px; }

.flow-tracker-container { margin: 24px 0 40px 0; }
.bento-title { font-size: 13px; font-weight: 600; color: #74807B; letter-spacing: 0.5px; margin-bottom: 24px; text-transform: uppercase; }

.timeline-track {
  display: flex;
  width: 100%; 
  align-items: flex-start;
}

.track-step {
  flex: 1; 
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  position: relative; z-index: 2;
}

.step-node {
  width: 24px; height: 24px; border-radius: 50%;
  background: #F4F6F5; 
  border: 2px solid rgba(28, 36, 33, 0.1);
  display: flex; justify-content: center; align-items: center;
  transition: all 0.4s ease;
}
.node-inner { width: 8px; height: 8px; border-radius: 50%; transition: all 0.4s ease; }

.step-label { 
  font-size: 12px; font-weight: 600; color: #A0AAB2; 
  text-align: center; white-space: normal; 
}

.step-line {
  position: absolute;
  top: 12px; left: 50%; width: 100%; height: 2px;
  background: rgba(28, 36, 33, 0.08);
  z-index: -1; 
}

.track-step.is-completed .step-node { border-color: #2AA876; background: rgba(42, 168, 118, 0.1); }
.track-step.is-completed .node-inner { background: #2AA876; }
.track-step.is-completed .step-label { color: #1C2421; }
.track-step.is-completed .step-line { background: #2AA876; }

.track-step.is-active .step-node { 
  border-color: #409EFF; background: rgba(64, 158, 255, 0.15); 
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
  transform: scale(1.15);
}
.track-step.is-active .node-inner { background: #409EFF; box-shadow: 0 0 8px rgba(64, 158, 255, 0.6); }
.track-step.is-active .step-label { color: #409EFF; font-weight: 700; }
.track-step.is-active .step-line { background: linear-gradient(90deg, #409EFF, rgba(28, 36, 33, 0.08)); }

.detail-bento-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.bento-card { background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 16px; padding: 20px; }
.bento-card.col-span-2 { grid-column: 1 / -1; }

.bento-icon-header { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: #74807B; margin-bottom: 12px; }
.bento-icon-header .el-icon { color: #2A483A; font-size: 16px; }

.bento-main-text { font-size: 15px; font-weight: 600; color: #1C2421; margin-bottom: 4px; line-height: 1.4; }
.bento-sub-text { font-size: 13px; color: #74807B; }

.aqi-massive-display { font-size: 32px; font-weight: 700; font-family: "SF Pro Display", sans-serif; }
.aqi-massive-display .unit { font-size: 15px; font-weight: 600; opacity: 0.7; }
.aqi-text-1, .aqi-text-2 { color: #2AA876; }
.aqi-text-3, .aqi-text-4 { color: #F5A623; }
.aqi-text-5, .aqi-text-6 { color: #D9534F; }

.personnel-info { display: flex; align-items: center; gap: 12px; }
.avatar-placeholder { width: 40px; height: 40px; border-radius: 50%; background: rgba(42, 72, 58, 0.1); color: #2A483A; display: flex; justify-content: center; align-items: center; font-size: 18px; }
.personnel-text { display: flex; flex-direction: column; }
.p-name { font-size: 15px; font-weight: 600; color: #1C2421; }
.p-role { font-size: 12px; color: #74807B; font-weight: 500; margin-top: 2px; }

.description-box { font-size: 14px; line-height: 1.6; color: #1C2421; background: rgba(28, 36, 33, 0.03); padding: 16px; border-radius: 12px; border: 1px solid rgba(28, 36, 33, 0.05); }

.empty-state-view { height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 20px; }
.placeholder-icon { font-size: 48px; color: rgba(28, 36, 33, 0.15); }
.empty-state-view p { font-size: 14px; font-weight: 500; color: #A0AAB2; }
</style>

<style>
.swiss-select { width: 140px; }
.swiss-select .el-input__wrapper { background: transparent !important; box-shadow: none !important; }
.swiss-select .el-input__wrapper.is-focus { box-shadow: none !important; }
.swiss-select-dropdown { background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 12px !important; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important; }
.swiss-select-dropdown .el-select-dropdown__item.selected { color: #2A483A !important; font-weight: 600 !important; }
</style>