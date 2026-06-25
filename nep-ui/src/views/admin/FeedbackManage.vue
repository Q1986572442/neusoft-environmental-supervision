<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>反馈管理（指派网格员）</span>
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:140px" @change="handleFilterChange">
          <el-option label="待指派" value="PENDING" />
          <el-option label="已指派" value="ASSIGNED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已升级" value="ESCALATED" />
        </el-select>
      </div>
    </template>

    <!-- 批量指派操作栏 -->
    <div v-if="selectedIds.length > 0" class="batch-bar">
      <span class="batch-info">已选 <strong>{{ selectedIds.length }}</strong> 条</span>
      <el-input-number v-model="batchInspectorId" :min="1" size="small" placeholder="网格员ID" style="width:140px" />
      <el-button type="primary" size="small" @click="handleBatchAssign">批量指派</el-button>
      <el-button size="small" @click="selectedIds = []">取消选择</el-button>
    </div>

    <el-table :data="feedbacks" v-loading="loading" stripe
      :row-class-name="tableRowClassName"
      @selection-change="handleSelectionChange" ref="tableRef">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="编号" width="70"/>
      <el-table-column prop="supervisorId" label="监督员ID" width="90"/>
      <el-table-column prop="cityId" label="城市" width="70"/>
      <el-table-column prop="specificAddress" label="地址" show-overflow-tooltip/>
      <el-table-column prop="estimatedAqiLevel" label="预估AQI" width="90">
        <template #default="{row}">
          <el-tag :type="row.estimatedAqiLevel<=2?'success':row.estimatedAqiLevel<=4?'warning':'danger'">{{ row.estimatedAqiLevel }}级</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{row}">
          <el-tag v-if="row.status==='PENDING'" :type="isTimeout(row) ? 'danger' : 'warning'" effect="dark">
            {{ isTimeout(row) ? '超时' : '待指派' }}
          </el-tag>
          <el-tag v-else-if="row.status==='ASSIGNED'" type="primary">已指派(ID:{{ row.assignedInspectorId }})</el-tag>
          <el-tag v-else-if="row.status==='COMPLETED'" type="success">已完成</el-tag>
          <el-tag v-else-if="row.status==='REJECTED'" type="danger">已拒绝</el-tag>
          <el-tag v-else-if="row.status==='ESCALATED'" type="danger">已升级</el-tag>
          <el-tag v-else type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{row}">
          <template v-if="row.status==='PENDING'">
            <el-input-number v-model="row._inspectorId" :min="1" size="small" placeholder="网格员ID" style="width:100px"/>
            <el-button size="small" type="primary" @click="handleAssign(row)" style="margin-left:6px">指派</el-button>
          </template>
          <template v-else-if="row.status==='ASSIGNED'">
            <el-button size="small" type="warning" @click="openTransfer(row)">转派</el-button>
          </template>
          <el-button size="small" @click="openNotes(row)">备注</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top:16px;text-align:right">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="fetch"/>
    </div>

    <!-- 转派对话框 -->
    <el-dialog v-model="transferVisible" title="转派反馈" width="400px">
      <el-form>
        <el-form-item label="当前网格员">
          <el-tag type="primary">ID: {{ currentTransferRow?.assignedInspectorId }}</el-tag>
        </el-form-item>
        <el-form-item label="新网格员ID" required>
          <el-input-number v-model="transferTargetId" :min="1" size="small" style="width:200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer">确认转派</el-button>
      </template>
    </el-dialog>

    <!-- 备注对话框 -->
    <el-dialog v-model="notesVisible" title="内部备注" width="550px">
      <div class="notes-list">
        <div v-if="currentNotes.length === 0" style="color:#999;text-align:center;padding:20px">暂无备注</div>
        <div v-for="note in currentNotes" :key="note.id" class="note-item">
          <div class="note-header">
            <span class="note-user">{{ note.userName }}</span>
            <span class="note-time">{{ formatTime(note.createTime) }}</span>
          </div>
          <p class="note-content">{{ note.content }}</p>
        </div>
      </div>
      <el-divider />
      <div class="note-input">
        <el-input v-model="newNoteContent" type="textarea" :rows="2" placeholder="添加备注..." />
        <el-button type="primary" size="small" @click="handleAddNote" style="margin-top:8px">添加备注</el-button>
      </div>
      <template #footer>
        <el-button @click="notesVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFeedbackPage, assignInspector, transferFeedback, batchAssignFeedback } from '@/api/feedback'
import { getNotesByFeedback, addNote } from '@/api/feedbackNote'
import { ElMessage } from 'element-plus'

const feedbacks = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref('')
const tableRef = ref(null)

// 批量指派
const selectedIds = ref([])
const batchInspectorId = ref(null)

// 转派
const transferVisible = ref(false)
const currentTransferRow = ref(null)
const transferTargetId = ref(null)

// 备注
const notesVisible = ref(false)
const currentNotesFeedbackId = ref(null)
const currentNotes = ref([])
const newNoteContent = ref('')

const currentUserId = ref(Number(localStorage.getItem('userId') || 0))
const currentUserName = ref(localStorage.getItem('userName') || '管理员')

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function isTimeout(row) {
  if (row.status !== 'PENDING') return false
  const created = new Date(row.createTime).getTime()
  return (Date.now() - created) > 24 * 60 * 60 * 1000
}

function tableRowClassName({ row }) {
  if (isTimeout(row)) return 'timeout-row'
  return ''
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(s => s.id)
}

function handleFilterChange() {
  page.value = 1
  fetch()
}

async function fetch() {
  loading.value = true
  try {
    const r = await getFeedbackPage(page.value, size.value, statusFilter.value)
    feedbacks.value = r.data.map(f => ({ ...f, _inspectorId: null }))
    total.value = r.total
    selectedIds.value = []
  } catch (e) {} finally { loading.value = false }
}

async function handleAssign(row) {
  if (!row._inspectorId) { ElMessage.warning('请输入网格员ID'); return }
  try {
    await assignInspector(row.id, row._inspectorId)
    ElMessage.success('指派成功')
    fetch()
  } catch (e) {}
}

// 批量指派
async function handleBatchAssign() {
  if (!batchInspectorId.value) { ElMessage.warning('请输入网格员ID'); return }
  if (selectedIds.value.length === 0) { ElMessage.warning('请先选择反馈'); return }
  try {
    const res = await batchAssignFeedback(selectedIds.value, batchInspectorId.value)
    ElMessage.success(`批量指派完成: ${res.data.successCount}/${res.data.totalCount} 条`)
    batchInspectorId.value = null
    selectedIds.value = []
    fetch()
  } catch (e) {}
}

// 转派
function openTransfer(row) {
  currentTransferRow.value = row
  transferTargetId.value = null
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferTargetId.value) { ElMessage.warning('请输入新网格员ID'); return }
  try {
    await transferFeedback(currentTransferRow.value.id, transferTargetId.value)
    ElMessage.success('转派成功')
    transferVisible.value = false
    fetch()
  } catch (e) {}
}

// 备注
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
  if (!newNoteContent.value.trim()) { ElMessage.warning('请输入备注内容'); return }
  try {
    await addNote({
      feedbackId: currentNotesFeedbackId.value,
      userId: currentUserId.value,
      userName: currentUserName.value,
      content: newNoteContent.value
    })
    ElMessage.success('备注已添加')
    newNoteContent.value = ''
    const res = await getNotesByFeedback(currentNotesFeedbackId.value)
    currentNotes.value = res.data || []
  } catch (e) {}
}

onMounted(fetch)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  margin-bottom: 12px;
  background: #ecf5ff;
  border-radius: 8px;
  border: 1px solid #d9ecff;
}

.batch-info {
  font-size: 14px;
  color: #409EFF;
}

:deep(.timeout-row) {
  background-color: rgba(245, 108, 108, 0.08) !important;
}

:deep(.timeout-row:hover td) {
  background-color: rgba(245, 108, 108, 0.12) !important;
}

.notes-list {
  max-height: 300px;
  overflow-y: auto;
}

.note-item {
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 8px;
}

.note-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.note-user {
  font-weight: 600;
  font-size: 13px;
  color: #409EFF;
}

.note-time {
  font-size: 12px;
  color: #999;
}

.note-content {
  margin: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}
</style>
