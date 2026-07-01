<template>
  <div class="alpine-tasks-canvas">
    <!-- 顶部控制台 -->
    <header class="tasks-header">
      <div class="header-left">
        <h1 class="page-title">{{ dynamicText.pageTitle }}</h1>
        <span class="task-counter">{{ filteredTasks.length }} {{ dynamicText.taskUnit }}</span>
      </div>
      
      <div class="header-right">
        <div class="alpine-search">
          <el-icon class="search-icon"><Search /></el-icon>
          <input 
            type="text" 
            v-model="searchQuery" 
            :placeholder="dynamicText.searchPlaceholder"
            class="search-input"
          >
        </div>
        
        <div class="alpine-segments">
          <button 
            v-for="tab in filterTabs" 
            :key="tab.value"
            class="segment-btn"
            :class="{ active: currentTab === tab.value }"
            @click="currentTab = tab.value"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
    </header>

    <!-- 主工作区 -->
    <main class="tasks-workspace">
      <!-- 左侧：任务流 -->
      <aside class="task-list-pane" v-loading="isLoading">
        <div v-if="filteredTasks.length === dynamicText.zeroCount && !isLoading" class="empty-state-list">
          <el-icon class="empty-icon"><List /></el-icon>
          <p>{{ dynamicText.emptyListDesc }}</p>
        </div>
        
        <div 
          v-else
          v-for="task in filteredTasks" 
          :key="task.id"
          class="task-card"
          :class="{ 'is-selected': selectedTaskId === task.id }"
          @click="selectTask(task)"
        >
          <div class="card-header">
            <span class="task-id">{{ task.displayId }}</span>
            <span class="alpine-dot" :class="task.statusCode"></span>
          </div>
          <h3 class="task-title">{{ task.title }}</h3>
          <div class="task-meta">
            <span class="meta-item"><el-icon><Location /></el-icon> {{ task.address }}</span>
            <span class="meta-item"><el-icon><Calendar /></el-icon> {{ task.date }}</span>
          </div>
        </div>
      </aside>

      <!-- 右侧：详情画板 -->
      <section class="task-detail-pane" v-loading="isLoadingDetail">
        <div v-if="!selectedTask" class="empty-state-detail">
          <div class="glass-placeholder">
            <el-icon><Briefcase /></el-icon>
          </div>
          <h3>{{ dynamicText.emptyDetailTitle }}</h3>
          <p>{{ dynamicText.emptyDetailDesc }}</p>
        </div>

        <div v-else class="detail-content">
          <div class="detail-scroll-area">
            <div class="detail-header">
              <div class="tag-group">
                <span class="alpine-tag solid" :class="selectedTask.levelCode">{{ selectedTask.levelText }}</span>
                <span class="alpine-tag outline" :class="selectedTask.statusCode">{{ selectedTask.statusText }}</span>
              </div>
              <h2 class="detail-title">{{ selectedTask.title }}</h2>
              <p class="detail-id">{{ dynamicText.idPrefix }}{{ selectedTask.displayId }}</p>
            </div>

            <div class="detail-section">
              <h4 class="section-title"><el-icon><LocationInformation /></el-icon> {{ dynamicText.locationTitle }}</h4>
              <p class="section-text">{{ selectedTask.address }}</p>
              <div class="map-placeholder">
                <el-icon><MapLocation /></el-icon>
                <span>{{ dynamicText.mapPlaceholder }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h4 class="section-title"><el-icon><Document /></el-icon> {{ dynamicText.descTitle }}</h4>
              <p class="section-text">{{ selectedTask.description }}</p>
            </div>
            
            <div class="detail-section">
              <h4 class="section-title"><el-icon><Timer /></el-icon> {{ dynamicText.timeTitle }}</h4>
              <div class="time-grid">
                <div class="time-box">
                  <span class="time-label">{{ dynamicText.assignTimeLabel }}</span>
                  <span class="time-value">{{ selectedTask.date }}</span>
                </div>
                <div class="time-box">
                  <span class="time-label">{{ dynamicText.deadlineLabel }}</span>
                  <span class="time-value">{{ selectedTask.deadline || dynamicText.noDeadlineText }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-actions">
            <button class="alpine-btn ghost" @click="handleCancel">
              {{ dynamicText.actionCancel }}
            </button>
            <button 
              class="alpine-btn primary" 
              :disabled="selectedTask.statusCode === 'completed'"
              @click="handlePrimaryAction"
            >
              <el-icon><Position /></el-icon> 
              {{ selectedTask.statusCode === 'pending' ? dynamicText.actionStart : (selectedTask.statusCode === 'active' ? dynamicText.actionSubmit : dynamicText.actionDone) }}
            </button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getFeedbackPage } from '@/api/feedback' 
import { ElMessage } from 'element-plus'
import {
  Search, List, Location, Calendar, Briefcase,
  LocationInformation, MapLocation, Document, Timer, Position
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// ==========================================
// 1. 全局文案
// ==========================================
const dynamicText = ref({
  pageTitle: '检测任务',
  taskUnit: '项任务',
  searchPlaceholder: '搜索任务编号、地点或关键字...',
  zeroCount: 0,
  emptyListDesc: '当前分类下暂无需要处理的任务',
  emptyDetailTitle: '未选择任何任务',
  emptyDetailDesc: '请从左侧列表中选择一个任务以查阅详情并进行跟进操作',
  idPrefix: '系统工单：',
  locationTitle: '巡查地点',
  descTitle: '任务/反馈描述',
  timeTitle: '时间节点',
  assignTimeLabel: '管理员下发时间',
  deadlineLabel: '要求完成时间',
  noDeadlineText: '按常规流程推进',
  mapPlaceholder: '环境监管网格坐标已锁定',
  actionCancel: '取消选中',
  actionStart: '接收并开始执行',
  actionSubmit: '提交检测报告',
  actionDone: '任务已闭环'
})

// ==========================================
// 2. 响应式状态
// ==========================================
const searchQuery = ref('')
const currentTab = ref('all')
const selectedTaskId = ref(null)
const isLoading = ref(false)
const isLoadingDetail = ref(false)

const filterTabs = ref([
  { label: '全部', value: 'all' },
  { label: '待处理', value: 'pending' },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' }
])

const rawTasks = ref([])

// ==========================================
// 3. 状态与优先级映射
// ==========================================
const statusMap = {
  'ASSIGNED': { code: 'pending', text: '待处理' },
  'PROCESSING': { code: 'active', text: '进行中' },
  'COMPLETED': { code: 'completed', text: '已完成' },
  'CLOSED': { code: 'completed', text: '已闭环' }
}

const levelMap = {
  1: { code: 'info', text: '常规' },
  2: { code: 'warning', text: '中优' },
  3: { code: 'danger', text: '紧急' }
}

// ==========================================
// 4. 核心数据请求（关键修正点）
// ==========================================
const fetchAssignedTasks = async () => {
  // 兼容用户身份获取
  const userId = userStore.user?.id || Number(localStorage.getItem('userId'))
  if (!userId) {
    console.warn('未获取到用户ID，无法加载任务')
    return
  }
  
  isLoading.value = true
  try {
    // 明确获取指派状态的任务，兼容旧接口逻辑
    const res = await getFeedbackPage(1, 100, 'ASSIGNED', userId)
    
    // 兼容多种后端返回格式：data 直接是数组 || data.records 分页对象
    let taskList = []
    if (Array.isArray(res.data)) {
      taskList = res.data
    } else if (res.data?.records) {
      taskList = res.data.records
    } else if (Array.isArray(res)) {
      // 某些拦截器可能直接返回数组
      taskList = res
    }
    
    rawTasks.value = taskList.map(item => ({
      id: item.id,
      displayId: `TSK-${new Date(item.createTime || item.assignTime).getFullYear()}-${String(item.id).padStart(4, '0')}`,
      title: item.title || item.feedbackType || '未命名指派任务',
      // 地址字段兼容
      address: item.specificAddress || item.address || '未指定明确地点',
      date: item.assignTime || item.createTime || '未知时间',
      deadline: item.deadline || null,
      description: item.description || item.content || '管理员暂未提供详细描述',
      statusCode: statusMap[item.status]?.code || 'pending',
      statusText: statusMap[item.status]?.text || '待处理',
      levelCode: levelMap[item.estimatedAqiLevel]?.code || levelMap[item.priority]?.code || 'info',
      levelText: levelMap[item.estimatedAqiLevel]?.text || levelMap[item.priority]?.text || '常规'
    }))
  } catch (error) {
    console.error('任务加载失败:', error)
    ElMessage.error('无法同步管理员委派任务，请检查网络连接')
  } finally {
    isLoading.value = false
  }
}

// ==========================================
// 5. 计算属性与交互
// ==========================================
const filteredTasks = computed(() => {
  return rawTasks.value.filter(task => {
    const matchTab = currentTab.value === 'all' || task.statusCode === currentTab.value
    const matchSearch = 
      task.title.includes(searchQuery.value) || 
      task.displayId.includes(searchQuery.value) || 
      task.address.includes(searchQuery.value)
    return matchTab && matchSearch
  })
})

const selectedTask = computed(() => {
  return rawTasks.value.find(t => t.id === selectedTaskId.value)
})

const selectTask = (task) => {
  isLoadingDetail.value = true
  selectedTaskId.value = task.id
  setTimeout(() => { isLoadingDetail.value = false }, 300)
}

const handlePrimaryAction = async () => {
  if (!selectedTask.value) return
  // 实际项目中应调用后端状态更新接口
  if (selectedTask.value.statusCode === 'pending') {
    selectedTask.value.statusCode = 'active'
    selectedTask.value.statusText = '进行中'
    ElMessage.success('已接收该任务，请尽快前往现场')
  } else if (selectedTask.value.statusCode === 'active') {
    selectedTask.value.statusCode = 'completed'
    selectedTask.value.statusText = '已完成'
    ElMessage.success('报告提交成功，任务已闭环')
    selectedTaskId.value = null 
  }
}

const handleCancel = () => {
  selectedTaskId.value = null
}

// ==========================================
// 6. 初始化
// ==========================================
onMounted(async () => {
  // 确保用户信息已加载
  if (!userStore.user) {
    await userStore.fetchUser()
  }
  fetchAssignedTasks()
})
</script>

<style scoped>
/* =======================================================
   顶级画布约束 (Strict Canvas)
======================================================= */
.alpine-tasks-canvas {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden; 
}

/* =======================================================
   顶部控制台 (Header Console)
======================================================= */
.tasks-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  margin-bottom: 24px;
}
.header-left { display: flex; align-items: baseline; gap: 12px; }
.page-title { font-size: 24px; font-weight: 700; color: #0F172A; margin: 0; }
.task-counter { font-size: 14px; color: #64748B; font-weight: 500; }

.header-right { display: flex; align-items: center; gap: 24px; }

.alpine-search {
  display: flex; align-items: center; gap: 8px;
  background: white; border: 1px solid rgba(15, 23, 42, 0.08);
  padding: 8px 16px; border-radius: 12px; width: 280px;
  transition: all 0.3s;
}
.alpine-search:focus-within { border-color: #0284C7; box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1); }
.search-icon { color: #94A3B8; font-size: 16px; }
.search-input { border: none; outline: none; font-size: 14px; width: 100%; color: #0F172A; background: transparent; }
.search-input::placeholder { color: #94A3B8; }

.alpine-segments {
  display: flex; background: rgba(15, 23, 42, 0.04);
  padding: 4px; border-radius: 12px;
}
.segment-btn {
  border: none; background: transparent; padding: 6px 16px;
  border-radius: 8px; font-size: 13px; font-weight: 600; color: #64748B;
  cursor: pointer; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.segment-btn.active { background: white; color: #0F172A; box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06); }

/* =======================================================
   主工作区 (Master-Detail Workspace)
======================================================= */
.tasks-workspace {
  flex: 1;
  display: flex;
  gap: 24px;
  min-height: 0; /* 防止 Flex 子项溢出 */
}

/* =======================================================
   左侧任务流 (Master List)
======================================================= */
.task-list-pane {
  width: 380px; flex-shrink: 0;
  display: flex; flex-direction: column; gap: 12px;
  overflow-y: auto; padding-right: 8px; 
}
.task-list-pane::-webkit-scrollbar { width: 4px; }
.task-list-pane::-webkit-scrollbar-thumb { background: rgba(15, 23, 42, 0.1); border-radius: 4px; }

.task-card {
  background: white; border-radius: 16px; padding: 20px;
  border: 1px solid rgba(15, 23, 42, 0.04);
  cursor: pointer; transition: all 0.2s ease;
  display: flex; flex-direction: column; gap: 12px;
}
.task-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px -8px rgba(15, 23, 42, 0.06); }
.task-card.is-selected { border-color: #0284C7; box-shadow: 0 0 0 1px #0284C7, 0 8px 24px -8px rgba(2, 132, 199, 0.15); }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.task-id { font-size: 12px; color: #94A3B8; font-weight: 600; font-family: monospace; }
.alpine-dot { width: 8px; height: 8px; border-radius: 50%; }
.alpine-dot.pending { background: #EF4444; box-shadow: 0 0 0 3px #FEF2F2; }
.alpine-dot.active { background: #F59E0B; box-shadow: 0 0 0 3px #FFFBEB; }
.alpine-dot.completed { background: #10B981; box-shadow: 0 0 0 3px #F0FDF4; }

.task-title { font-size: 15px; font-weight: 600; color: #0F172A; margin: 0; line-height: 1.4; }
.task-meta { display: flex; flex-direction: column; gap: 6px; }
.meta-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748B; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* =======================================================
   右侧详情画板 (Detail Canvas)
======================================================= */
.task-detail-pane {
  flex: 1;
  background: white; border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.04);
  box-shadow: 0 4px 24px -8px rgba(15, 23, 42, 0.03);
  display: flex; flex-direction: column;
  overflow: hidden;
}

.detail-content {
  flex: 1; display: flex; flex-direction: column; min-height: 0;
}
.detail-scroll-area {
  flex: 1; overflow-y: auto; padding: 40px;
}
.detail-scroll-area::-webkit-scrollbar { display: none; }

.detail-header { margin-bottom: 40px; border-bottom: 1px solid rgba(15, 23, 42, 0.04); padding-bottom: 24px; }
.tag-group { display: flex; gap: 8px; margin-bottom: 16px; }
.alpine-tag { padding: 4px 10px; border-radius: 8px; font-size: 12px; font-weight: 600; }
.alpine-tag.solid.danger { background: #FEF2F2; color: #EF4444; }
.alpine-tag.solid.warning { background: #FFFBEB; color: #F59E0B; }
.alpine-tag.solid.info { background: #F1F5F9; color: #64748B; }
.alpine-tag.outline { background: transparent; border: 1px solid; }
.alpine-tag.outline.pending { border-color: #EF4444; color: #EF4444; }
.alpine-tag.outline.active { border-color: #F59E0B; color: #F59E0B; }
.alpine-tag.outline.completed { border-color: #10B981; color: #10B981; }

.detail-title { font-size: 24px; font-weight: 700; color: #0F172A; margin: 0 0 8px 0; line-height: 1.3; }
.detail-id { font-size: 13px; color: #94A3B8; font-family: monospace; margin: 0; }

.detail-section { margin-bottom: 32px; }
.section-title { font-size: 14px; font-weight: 600; color: #0F172A; margin: 0 0 12px 0; display: flex; align-items: center; gap: 8px; }
.section-title .el-icon { color: #0284C7; font-size: 16px; }
.section-text { font-size: 14px; color: #475569; line-height: 1.6; margin: 0; }

.map-placeholder {
  margin-top: 16px; height: 120px; border-radius: 12px;
  background: rgba(14, 165, 233, 0.03); border: 1px dashed rgba(14, 165, 233, 0.2);
  display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 8px;
  color: #0284C7; font-size: 13px; font-weight: 500;
}
.map-placeholder .el-icon { font-size: 24px; opacity: 0.5; }

.time-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.time-box { background: #F8FAFC; padding: 16px; border-radius: 12px; display: flex; flex-direction: column; gap: 4px; }
.time-label { font-size: 12px; color: #64748B; font-weight: 500; }
.time-value { font-size: 14px; color: #0F172A; font-weight: 600; }

.detail-actions {
  flex-shrink: 0; padding: 24px 40px; background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px); border-top: 1px solid rgba(15, 23, 42, 0.04);
  display: flex; justify-content: flex-end; gap: 16px;
}

.alpine-btn {
  padding: 12px 24px; border-radius: 12px; font-size: 14px; font-weight: 600;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px; 
  cursor: pointer; border: none; transition: all 0.3s;
}
.alpine-btn.primary { background: #0284C7; color: white; box-shadow: 0 4px 12px rgba(2, 132, 199, 0.2); }
.alpine-btn.primary:hover:not(:disabled) { transform: translateY(-1px); background: #0369A1; }
.alpine-btn.primary:disabled { background: #94A3B8; box-shadow: none; cursor: not-allowed; }
.alpine-btn.ghost { background: transparent; color: #64748B; }
.alpine-btn.ghost:hover { background: #F1F5F9; color: #0F172A; }

/* =======================================================
   空状态 (Empty States)
======================================================= */
.empty-state-list { padding: 40px 20px; text-align: center; color: #94A3B8; font-size: 13px; }
.empty-state-detail { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #64748B; }
.glass-placeholder {
  width: 80px; height: 80px; border-radius: 24px; margin-bottom: 24px;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%);
  display: flex; justify-content: center; align-items: center;
  font-size: 32px; color: #0284C7; box-shadow: 0 12px 32px rgba(2, 132, 199, 0.1);
}
.empty-state-detail h3 { font-size: 18px; color: #0F172A; margin: 0 0 8px 0; font-weight: 600; }
.empty-state-detail p { font-size: 14px; margin: 0; max-width: 260px; text-align: center; line-height: 1.5; }
</style>