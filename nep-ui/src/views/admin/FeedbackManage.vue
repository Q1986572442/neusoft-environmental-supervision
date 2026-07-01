<template>
  <div class="swiss-page-container">
    
    <header class="action-console glass-panel fixed-section">
      <div class="console-left">
        <h2 class="page-title">全量监督流转</h2>
        <span class="page-subtitle">环境异常反馈的统一调度与全生命周期追踪</span>
      </div>

      <div class="console-right">
        <div class="search-capsule">
          <span class="filter-label">流转状态</span>
          <el-select v-model="statusFilter" placeholder="全景视图" clearable class="swiss-select" popper-class="swiss-select-dropdown" @change="handleFilterChange">
            <el-option label="全景视图" value="" />
            <el-option label="待指派" value="PENDING" />
            <el-option label="已指派" value="ASSIGNED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已升级" value="ESCALATED" />
          </el-select>
        </div>
        <el-button class="swiss-btn icon-btn" @click="handleFilterChange" title="刷新流转状态">
          <el-icon><RefreshRight /></el-icon>
        </el-button>
      </div>
    </header>

    <transition name="slide-down">
      <div v-if="selectedIds.length > 0" class="batch-console glass-panel">
        <div class="batch-left">
          <div class="selection-count">{{ selectedIds.length }}</div>
          <span class="batch-text">个工单已选中</span>
        </div>
        <div class="batch-right">
          <div class="minimal-input-wrapper">
            <el-icon><User /></el-icon>
            <input v-model="batchInspectorId" type="number" placeholder="输入网格员ID" class="minimal-input" />
          </div>
          <button class="action-btn primary" @click="handleBatchAssign">
            <el-icon><Position /></el-icon> 批量指派
          </button>
          <button class="action-btn outline" @click="selectedIds = []">取消选中</button>
        </div>
      </div>
    </transition>

    <main class="roster-workspace stretch-section glass-panel" v-loading="loading">
      
      <div class="roster-header-row roster-grid">
        <div class="col-checkbox">
          <el-checkbox :model-value="isAllSelected" :indeterminate="isIndeterminate" @change="toggleSelectAll" />
        </div>
        <div class="col-id">工单编号</div>
        <div class="col-supervisor">提报人</div>
        <div class="col-address">隐患地址</div>
        <div class="col-aqi">预估 AQI</div>
        <div class="col-status">流转状态</div>
        <div class="col-action">执行调度</div>
      </div>

      <div class="roster-scroll-area">
        
        <div v-if="feedbacks.length === 0 && !loading" class="empty-state">
          <el-icon class="empty-icon"><Tickets /></el-icon>
          <p>当前视图下没有任何监督工单</p>
        </div>

        <div class="roster-list" v-else>
          <div 
            class="roster-row roster-grid" 
            v-for="row in feedbacks" 
            :key="row.id"
            :class="{ 'is-timeout': isTimeout(row) }"
          >
            
            <div class="col-checkbox" @click.stop>
              <el-checkbox v-model="selectedIds" :label="row.id" :value="row.id"><span style="display:none"></span></el-checkbox>
            </div>

            <div class="col-id">
              <span class="mono-id">TKT-{{ String(row.id).padStart(4, '0') }}</span>
              <div v-if="isTimeout(row)" class="timeout-pulse" title="已超时 24 小时未处理"></div>
            </div>

            <div class="col-supervisor">
              <div class="user-pill"><el-icon><UserFilled /></el-icon> {{ row.supervisorId }}</div>
            </div>

            <div class="col-address">
              <el-tooltip :content="row.specificAddress" placement="top" popper-class="alpine-tooltip" :show-after="300">
                <span class="address-text">{{ row.specificAddress }}</span>
              </el-tooltip>
            </div>

            <div class="col-aqi">
              <span class="aqi-level-badge" :class="getAqiClass(row.estimatedAqiLevel)">
                L{{ row.estimatedAqiLevel }}
              </span>
            </div>

            <div class="col-status">
              <span class="status-pill" :class="row.status.toLowerCase()">
                <span class="pill-dot"></span>
                {{ getStatusText(row) }}
              </span>
            </div>

            <div class="col-action" @click.stop>
              
              <div v-if="row.status === 'PENDING'" class="inline-assign">
                <input v-model="row._inspectorId" type="number" placeholder="ID" class="inline-input" />
                <button class="icon-action-btn primary" title="直接指派" @click="handleAssign(row)">
                  <el-icon><Position /></el-icon>
                </button>
              </div>

              <div v-else-if="row.status === 'ASSIGNED'" class="inline-assign">
                <button class="action-btn outline small" @click="openTransfer(row)">
                  <el-icon><Switch /></el-icon> 转派
                </button>
              </div>
              
              <div v-else class="inline-assign placeholder"></div>

              <button class="icon-action-btn ghost" title="工单备忘录" @click="openNotes(row)">
                <el-icon><ChatDotRound /></el-icon>
              </button>

            </div>
          </div>
        </div>
      </div>

      <div class="list-pagination" v-if="total > size">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="prev, pager, next" class="swiss-pagination" @current-change="fetch" />
      </div>
    </main>

    <el-dialog v-model="transferVisible" title="工单转派" width="440px" class="swiss-dialog" destroy-on-close :close-on-click-modal="false">
      <div class="dialog-form-content">
        <div class="info-row">
          <span class="info-label">原负责网格员</span>
          <span class="info-value">ID: {{ currentTransferRow?.assignedInspectorId }}</span>
        </div>
        <div class="input-group">
          <label>新任网格员 ID</label>
          <div class="minimal-input-wrapper full">
            <el-icon><User /></el-icon>
            <input v-model="transferTargetId" type="number" placeholder="输入接手的网格员编号" class="minimal-input" />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-actions">
          <button class="action-btn ghost" @click="transferVisible = false">取消</button>
          <button class="action-btn primary" @click="handleTransfer"><el-icon><Switch /></el-icon> 确认转派</button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="notesVisible" title="工单追踪与备忘" width="560px" class="swiss-dialog" destroy-on-close :close-on-click-modal="false">
      <div class="notes-timeline">
        <div v-if="currentNotes.length === 0" class="empty-notes">暂无追踪记录</div>
        <div v-else class="note-bubble" v-for="note in currentNotes" :key="note.id">
          <div class="bubble-header">
            <span class="note-author">{{ note.userName }}</span>
            <span class="note-time">{{ formatTime(note.createTime) }}</span>
          </div>
          <div class="bubble-content">{{ note.content }}</div>
        </div>
      </div>
      <div class="note-composer">
        <el-input v-model="newNoteContent" type="textarea" :rows="2" placeholder="输入最新追踪进展..." class="swiss-textarea" />
        <button class="action-btn primary send-btn" @click="handleAddNote">
          <el-icon><Position /></el-icon>
        </button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFeedbackPage, assignInspector, transferFeedback, batchAssignFeedback } from '@/api/feedback'
import { getNotesByFeedback, addNote } from '@/api/feedbackNote'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  RefreshRight, User, Position, Tickets, UserFilled, 
  Switch, ChatDotRound
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const currentUserId = ref(Number(localStorage.getItem('userId') || 0))
const currentUserName = computed(() => userStore.userName || '管理员')

const feedbacks = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref('')

// 批量指派逻辑
const selectedIds = ref([])
const batchInspectorId = ref(null)

const isAllSelected = computed(() => feedbacks.value.length > 0 && selectedIds.value.length === feedbacks.value.length)
const isIndeterminate = computed(() => selectedIds.value.length > 0 && selectedIds.value.length < feedbacks.value.length)

const toggleSelectAll = (val) => {
  if (val) selectedIds.value = feedbacks.value.map(f => f.id)
  else selectedIds.value = []
}

// 转派状态
const transferVisible = ref(false)
const currentTransferRow = ref(null)
const transferTargetId = ref(null)

// 备注状态
const notesVisible = ref(false)
const currentNotesFeedbackId = ref(null)
const currentNotes = ref([])
const newNoteContent = ref('')

// ====== 辅助渲染函数 ======
function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function isTimeout(row) {
  if (row.status !== 'PENDING') return false
  const created = new Date(row.createTime).getTime()
  return (Date.now() - created) > 24 * 60 * 60 * 1000
}

function getAqiClass(level) {
  if (level <= 2) return 'aqi-safe'
  if (level <= 4) return 'aqi-warn'
  return 'aqi-danger'
}

function getStatusText(row) {
  const map = {
    'PENDING': isTimeout(row) ? '超时待办' : '等待指派',
    'ASSIGNED': `指派给 #${row.assignedInspectorId}`,
    'COMPLETED': '已处理完成',
    'REJECTED': '已被拒绝',
    'ESCALATED': '已升级警告'
  }
  return map[row.status] || row.status
}

// ====== 业务逻辑 ======
function handleFilterChange() {
  page.value = 1
  fetch()
}

async function fetch() {
  loading.value = true
  try {
    const r = await getFeedbackPage(page.value, size.value, statusFilter.value)
    // 注入前端 _inspectorId 供内联输入
    feedbacks.value = r.data.map(f => ({ ...f, _inspectorId: null }))
    total.value = r.total
    selectedIds.value = []
  } catch (e) {} finally { loading.value = false }
}

async function handleAssign(row) {
  if (!row._inspectorId) { ElMessage.warning('需输入承接网格员的数字 ID'); return }
  try {
    await assignInspector(row.id, row._inspectorId)
    ElMessage.success('工单调度成功')
    fetch()
  } catch (e) {}
}

async function handleBatchAssign() {
  if (!batchInspectorId.value) { ElMessage.warning('需输入承接网格员的数字 ID'); return }
  try {
    const res = await batchAssignFeedback(selectedIds.value, batchInspectorId.value)
    ElMessage.success(`矩阵调度完成: 成功 ${res.data.successCount} / 共 ${res.data.totalCount} 条`)
    batchInspectorId.value = null
    selectedIds.value = []
    fetch()
  } catch (e) {}
}

function openTransfer(row) {
  currentTransferRow.value = row
  transferTargetId.value = null
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferTargetId.value) { ElMessage.warning('需输入新任网格员 ID'); return }
  try {
    await transferFeedback(currentTransferRow.value.id, transferTargetId.value)
    ElMessage.success('工单转派成功')
    transferVisible.value = false
    fetch()
  } catch (e) {}
}

async function openNotes(row) {
  currentNotesFeedbackId.value = row.id
  notesVisible.value = true
  newNoteContent.value = ''
  try {
    const res = await getNotesByFeedback(row.id)
    currentNotes.value = res.data || []
  } catch (e) { currentNotes.value = [] }
}

async function handleAddNote() {
  if (!newNoteContent.value.trim()) { ElMessage.warning('备忘录内容不可为空'); return }
  try {
    await addNote({
      feedbackId: currentNotesFeedbackId.value,
      userId: currentUserId.value,
      userName: currentUserName.value,
      content: newNoteContent.value
    })
    ElMessage.success('追踪记录已归档')
    newNoteContent.value = ''
    const res = await getNotesByFeedback(currentNotesFeedbackId.value)
    currentNotes.value = res.data || []
  } catch (e) {}
}

onMounted(fetch)
</script>

<style scoped>
/* ========== 全局架构 ========== */
.swiss-page-container { max-width: 1440px; width: 100%; height: 100%; margin: 0 auto; display: flex; flex-direction: column; padding-bottom: 32px; box-sizing: border-box; color: #1C2421; position: relative; }
.fixed-section { flex-shrink: 0; margin-bottom: 24px; }
.stretch-section { flex: 1; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }
.glass-panel { background: rgba(255, 255, 255, 0.65); backdrop-filter: blur(24px) saturate(180%); -webkit-backdrop-filter: blur(24px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.9); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6); }

/* 控制台 */
.action-console { height: 100px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box; }
.console-left { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 600; margin: 0; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }
.console-right { display: flex; align-items: center; gap: 16px; }

.search-capsule { display: flex; align-items: center; gap: 12px; background: rgba(255, 255, 255, 0.5); padding: 8px 16px 8px 20px; border-radius: 100px; border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 4px 16px -4px rgba(0, 0, 0, 0.03); }
.filter-label { font-size: 13px; font-weight: 600; color: #74807B; }

.swiss-btn { border: none; outline: none; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.swiss-btn.icon-btn { width: 44px; height: 44px; background: rgba(255, 255, 255, 0.8); color: #1C2421; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); font-size: 20px; }
.swiss-btn.icon-btn:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }

/* ========== 悬浮批量操作舱 ========== */
.batch-console {
  position: absolute; top: 110px; left: 50%; transform: translateX(-50%); z-index: 50;
  display: flex; justify-content: space-between; align-items: center; padding: 12px 24px;
  border-radius: 100px; background: rgba(15, 23, 42, 0.85); border-color: rgba(255,255,255,0.1);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.15); width: 600px; color: white;
}
.batch-left { display: flex; align-items: center; gap: 12px; }
.selection-count { background: #2AA876; width: 28px; height: 28px; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-weight: 800; font-size: 14px; }
.batch-text { font-size: 14px; font-weight: 600; }
.batch-right { display: flex; align-items: center; gap: 12px; }

/* 极简自定义输入框 */
.minimal-input-wrapper { display: flex; align-items: center; gap: 8px; background: rgba(255,255,255,0.1); padding: 6px 12px; border-radius: 8px; transition: all 0.3s; }
.minimal-input-wrapper:focus-within { background: rgba(255,255,255,0.2); }
.minimal-input { border: none; background: transparent; color: white; font-size: 13px; width: 100px; outline: none; font-family: monospace; }
.minimal-input::placeholder { color: rgba(255,255,255,0.4); }
.minimal-input-wrapper.full { background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.08); width: 100%; }
.minimal-input-wrapper.full input { color: #1C2421; width: 100%; font-size: 14px; }

/* 动作按钮 */
.action-btn { height: 36px; padding: 0 16px; border-radius: 10px; display: inline-flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.3s; border: none; }
.action-btn.primary { background: #2AA876; color: white; }
.action-btn.primary:hover { background: #1f8a5f; }
.action-btn.outline { background: transparent; color: white; border: 1px solid rgba(255,255,255,0.2); }
.action-btn.outline:hover { background: rgba(255,255,255,0.1); }
.action-btn.outline.small { background: white; color: #1C2421; border-color: rgba(28,36,33,0.1); }
.action-btn.outline.small:hover { border-color: #1C2421; }
.action-btn.ghost { background: transparent; color: #74807B; }
.action-btn.ghost:hover { background: rgba(0,0,0,0.05); color: #1C2421; }

.icon-action-btn { width: 32px; height: 32px; border-radius: 8px; display: flex; justify-content: center; align-items: center; font-size: 16px; border: none; cursor: pointer; transition: all 0.2s; }
.icon-action-btn.primary { background: #1C2421; color: white; }
.icon-action-btn.primary:hover { background: #2A483A; }
.icon-action-btn.ghost { background: transparent; color: #A0AAB2; }
.icon-action-btn.ghost:hover { background: rgba(0,0,0,0.05); color: #1C2421; }

/* 动画 */
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translate(-50%, -20px) scale(0.95); }

/* ========== 3. 调度矩阵 (Roster Grid) ========== */
.roster-workspace { display: flex; flex-direction: column; }
.roster-grid {
  display: grid;
  /* 精确比例划分: 选 | ID | 提报人 | 地址 | AQI | 状态 | 操作 */
  grid-template-columns: 40px 100px 120px 2fr 100px 180px 180px;
  align-items: center; gap: 16px; padding: 0 32px;
}

.roster-header-row { height: 56px; border-bottom: 1px solid rgba(28, 36, 33, 0.08); flex-shrink: 0; font-size: 12px; font-weight: 600; color: #A0AAB2; letter-spacing: 0.5px; }
.roster-scroll-area { flex: 1; overflow-y: auto; padding-top: 8px; }
.roster-scroll-area::-webkit-scrollbar { display: none; }

.roster-row { height: 64px; border-radius: 12px; transition: all 0.3s ease; border: 1px solid transparent; }
.roster-row:hover { background: rgba(255, 255, 255, 0.8); border-color: rgba(28, 36, 33, 0.04); box-shadow: 0 4px 12px -4px rgba(0,0,0,0.02); }

/* 异常超时状态隐喻 */
.roster-row.is-timeout { background: rgba(225, 29, 72, 0.02); }
.roster-row.is-timeout:hover { background: rgba(225, 29, 72, 0.04); border-color: rgba(225, 29, 72, 0.1); }
.timeout-pulse { width: 8px; height: 8px; border-radius: 50%; background: #E11D48; display: inline-block; margin-left: 8px; box-shadow: 0 0 0 0 rgba(225, 29, 72, 0.4); animation: pulse-red 2s infinite; }
@keyframes pulse-red { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(225, 29, 72, 0.4); } 70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(225, 29, 72, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(225, 29, 72, 0); } }

/* 单元格内容排版 */
.mono-id { font-family: monospace; font-size: 13px; color: #1C2421; font-weight: 700; }
.user-pill { display: inline-flex; align-items: center; gap: 6px; background: rgba(28,36,33,0.04); padding: 4px 10px; border-radius: 8px; font-size: 12px; font-family: monospace; font-weight: 600; color: #74807B; }
.address-text { font-size: 14px; font-weight: 500; color: #1C2421; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: default; }

.aqi-level-badge { padding: 4px 10px; border-radius: 8px; font-size: 11px; font-weight: 800; font-family: monospace; border: 1px solid; }
.aqi-safe { background: #F0FDF4; color: #059669; border-color: #BBF7D0; }
.aqi-warn { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.aqi-danger { background: #FEF2F2; color: #E11D48; border-color: #FECDD3; }

/* 胶囊状态标识 */
.status-pill { display: inline-flex; align-items: center; gap: 8px; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 12px; background: rgba(28,36,33,0.04); color: #74807B; }
.pill-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.status-pill.pending { background: #FFFBEB; color: #D97706; }
.status-pill.assigned { background: #EFF6FF; color: #2563EB; }
.status-pill.completed { background: #F0FDF4; color: #059669; }
.status-pill.rejected, .status-pill.escalated { background: #FEF2F2; color: #E11D48; }

/* 操作区 */
.col-action { display: flex; align-items: center; gap: 12px; justify-content: flex-end; }
.inline-assign { display: flex; align-items: center; gap: 6px; }
.inline-assign.placeholder { width: 100px; } /* 占位防抖动 */
.inline-input { width: 60px; height: 32px; border-radius: 8px; border: 1px solid rgba(28,36,33,0.1); background: rgba(255,255,255,0.5); padding: 0 8px; font-family: monospace; font-size: 12px; text-align: center; outline: none; transition: border-color 0.3s; }
.inline-input:focus { border-color: #1C2421; background: white; }

.list-pagination { display: flex; justify-content: center; margin-top: 24px; padding: 16px; }
:deep(.swiss-pagination) { --el-pagination-bg-color: transparent; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120px 0; color: #A0AAB2; font-size: 14px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.3; }

/* ========== 弹窗深度定制 (Swiss Dialogs) ========== */
:deep(.swiss-dialog) { background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(32px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.9); border-radius: 24px; box-shadow: 0 24px 48px -12px rgba(0, 0, 0, 0.1); }
:deep(.swiss-dialog .el-dialog__header) { padding: 32px 32px 16px; margin: 0; }
:deep(.swiss-dialog .el-dialog__title) { font-size: 18px; font-weight: 700; color: #1C2421; }
:deep(.swiss-dialog .el-dialog__body) { padding: 0 32px; }
:deep(.swiss-dialog .el-dialog__footer) { padding: 24px 32px 32px; border-top: 1px solid rgba(28, 36, 33, 0.04); }

.dialog-form-content { display: flex; flex-direction: column; gap: 24px; padding: 16px 0; }
.info-row { display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; padding: 12px 16px; border-radius: 12px; }
.info-label { font-size: 13px; color: #74807B; font-weight: 600; }
.info-value { font-size: 14px; color: #1C2421; font-family: monospace; font-weight: 700; }
.input-group label { display: block; font-size: 13px; color: #74807B; font-weight: 600; margin-bottom: 8px; }

.dialog-actions { display: flex; justify-content: flex-end; gap: 12px; }

/* iMessage 级对话备忘录 */
.notes-timeline { display: flex; flex-direction: column; gap: 16px; max-height: 360px; overflow-y: auto; padding: 16px 0; }
.notes-timeline::-webkit-scrollbar { width: 4px; }
.notes-timeline::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 4px; }

.empty-notes { text-align: center; color: #A0AAB2; font-size: 13px; padding: 40px 0; }
.note-bubble { background: #F4F6F5; padding: 16px; border-radius: 16px; border-bottom-left-radius: 4px; max-width: 90%; align-self: flex-start; }
.bubble-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 6px; }
.note-author { font-size: 13px; font-weight: 700; color: #1C2421; }
.note-time { font-size: 11px; color: #A0AAB2; font-family: monospace; }
.bubble-content { font-size: 14px; color: #333; line-height: 1.6; }

.note-composer { display: flex; gap: 12px; margin-top: 16px; align-items: flex-end; }
.swiss-textarea :deep(.el-textarea__inner) { background: #F4F6F5; border: none; box-shadow: none; border-radius: 16px; padding: 12px 16px; font-size: 14px; color: #1C2421; resize: none; transition: background 0.3s; }
.swiss-textarea :deep(.el-textarea__inner:focus) { background: white; box-shadow: 0 0 0 1px #2AA876 inset; }
.send-btn { width: 44px; height: 44px; border-radius: 16px; flex-shrink: 0; padding: 0; display: flex; justify-content: center; align-items: center; font-size: 18px; }

/* 工具提示 */
.alpine-tooltip.el-popper { background: #1C2421 !important; color: white !important; font-weight: 600 !important; border: none !important; border-radius: 8px !important; padding: 8px 12px !important; box-shadow: 0 8px 24px rgba(28, 36, 33, 0.2) !important; }
.alpine-tooltip .el-popper__arrow::before { background: #1C2421 !important; border: none !important; }

/* 复选框深度美化 */
:deep(.el-checkbox__inner) { border-radius: 6px; border: 1px solid rgba(28,36,33,0.2); width: 18px; height: 18px; transition: all 0.2s; }
:deep(.el-checkbox__input.is-checked .el-checkbox__inner), :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) { background-color: #1C2421; border-color: #1C2421; }
:deep(.el-checkbox__inner::after) { top: 2px; left: 6px; width: 4px; height: 8px; }
</style>
<style>
.swiss-select { width: 140px; }
.swiss-select .el-input__wrapper { background: transparent !important; box-shadow: none !important; }
.swiss-select-dropdown { background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 12px !important; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important; }
.swiss-select-dropdown .el-select-dropdown__item.selected { color: #2A483A !important; font-weight: 600 !important; }
</style>