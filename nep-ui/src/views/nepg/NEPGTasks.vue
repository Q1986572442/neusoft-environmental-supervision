<template>
  <div class="tasks-page">
    <div class="page-header">
      <h2>📋 指派给我的检测任务</h2>
      <span class="task-count">共 {{ tasks.length }} 个待检测任务</span>
    </div>

    <!-- 任务列表 -->
    <div v-if="tasks.length > 0">
      <div v-for="task in tasks" :key="task.id" class="task-card" :class="{ expanded: expandId === task.id }">
        <div class="task-summary" @click="toggleExpand(task)">
          <div class="summary-left">
            <span class="case-id">案件 #{{ task.id }}</span>
            <span class="case-address">{{ task.specificAddress || '地址未记录' }}</span>
            <el-tag :type="task.estimatedAqiLevel <= 2 ? 'success' : task.estimatedAqiLevel <= 4 ? 'warning' : 'danger'" size="small">
              预估 AQI {{ task.estimatedAqiLevel }} 级
            </el-tag>
          </div>
          <div class="summary-right">
            <span class="assign-time">{{ formatTime(task.assignTime) }}</span>
            <el-icon><ArrowDown v-if="expandId !== task.id" /><ArrowUp v-else /></el-icon>
          </div>
        </div>

        <!-- 展开后显示AQI检测表单 -->
        <div v-if="expandId === task.id" class="task-detail">
          <el-divider />
          <p class="detail-desc">{{ task.description || '监督员未提供详细描述' }}</p>
          <el-divider />
          <h4>📝 录入实测 AQI 数据</h4>
          <el-form :model="form" label-width="200px">
            <el-form-item label="SO₂ 二氧化硫 AQI">
              <el-slider v-model="form.so2Aqi" :max="500" show-input style="width:400px" />
            </el-form-item>
            <el-form-item label="CO 一氧化碳 AQI">
              <el-slider v-model="form.coAqi" :max="500" show-input style="width:400px" />
            </el-form-item>
            <el-form-item label="PM2.5 悬浮颗粒物 AQI">
              <el-slider v-model="form.pm25Aqi" :max="500" show-input style="width:400px" />
            </el-form-item>
            <el-form-item label="最终AQI = MAX(SO₂,CO,PM2.5)">
              <span class="final-aqi" :style="{color:getAqiColor(finalAqi)}">{{ finalAqi }}</span>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="检测备注..." style="width:400px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit(task)">提交检测数据</el-button>
              <el-button type="danger" plain @click="openReject(task)">拒绝此任务</el-button>
              <el-button @click="expandId = null">取消</el-button>
            </el-form-item>

            <!-- 内部备注区域 -->
            <el-divider />
            <h4>💬 内部备注</h4>
            <div class="notes-section">
              <div v-if="currentNotes.length === 0" style="color:#999;font-size:13px;padding:8px 0">暂无备注</div>
              <div v-for="note in currentNotes" :key="note.id" class="note-bubble">
                <div class="note-meta">
                  <span class="note-author">{{ note.userName }}</span>
                  <span class="note-time">{{ formatTime(note.createTime) }}</span>
                </div>
                <p class="note-text">{{ note.content }}</p>
              </div>
            </div>
            <div class="note-input-row">
              <el-input v-model="newNote" type="textarea" :rows="2" placeholder="添加内部备注..." size="small" />
              <el-button size="small" type="primary" @click="handleAddNote(task.id)" style="margin-top:6px">添加备注</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无待检测任务" />

    <!-- 拒绝反馈对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝反馈任务" width="450px">
      <el-form>
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请详细填写拒绝该任务的原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFeedbackPage, rejectFeedback } from '@/api/feedback'
import { submitAqi } from '@/api/aqi'
import { getNotesByFeedback, addNote } from '@/api/feedbackNote'
import { ElMessage } from 'element-plus'

const tasks = ref([])
const expandId = ref(null)
const submitting = ref(false)
const currentUserId = ref(Number(localStorage.getItem('userId') || 0))
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
const currentUserName = computed(() => userStore.userName || '网格员')

// 拒绝
const rejectVisible = ref(false)
const currentRejectTask = ref(null)
const rejectReason = ref('')

// 备注
const currentNotes = ref([])
const newNote = ref('')

const form = ref({ so2Aqi: 0, coAqi: 0, pm25Aqi: 0, remark: '' })

const finalAqi = computed(() => Math.max(form.value.so2Aqi, form.value.coAqi, form.value.pm25Aqi))

function formatTime(t) { if (!t) return ''; return t.replace('T', ' ').substring(0, 16) }

function toggleExpand(task) {
  if (expandId.value === task.id) {
    expandId.value = null
  } else {
    expandId.value = task.id
    newNote.value = ''
    loadNotes(task.id)
  }
}

function getAqiColor(v) {
  if (v <= 50) return '#2AA876'; if (v <= 100) return '#85C77A'
  if (v <= 150) return '#F5A623'; if (v <= 200) return '#E87A31'
  if (v <= 300) return '#D9534F'; return '#A03232'
}

async function handleSubmit(task) {
  submitting.value = true
  try {
    await submitAqi({
      feedbackId: task.id,
      inspectorId: currentUserId.value,
      provinceId: task.provinceId,
      cityId: task.cityId,
      so2Aqi: form.value.so2Aqi,
      coAqi: form.value.coAqi,
      pm25Aqi: form.value.pm25Aqi,
      remark: form.value.remark
    })
    ElMessage.success('检测数据提交成功！')
    expandId.value = null
    form.value = { so2Aqi: 0, coAqi: 0, pm25Aqi: 0, remark: '' }
    loadTasks()
  } catch(e) {} finally { submitting.value = false }
}

async function loadTasks() {
  try {
    const res = await getFeedbackPage(1, 50, 'ASSIGNED', currentUserId.value)
    tasks.value = res.data || []
  } catch(e) {}
}

// 拒绝反馈
function openReject(task) {
  currentRejectTask.value = task
  rejectReason.value = ''
  rejectVisible.value = true
}

async function handleReject() {
  if (!rejectReason.value.trim()) { ElMessage.warning('请填写拒绝原因'); return }
  try {
    await rejectFeedback(currentRejectTask.value.id, currentUserId.value, rejectReason.value)
    ElMessage.success('已拒绝该反馈')
    rejectVisible.value = false
    expandId.value = null
    loadTasks()
  } catch(e) {}
}

// 备注
async function loadNotes(feedbackId) {
  try {
    const res = await getNotesByFeedback(feedbackId)
    currentNotes.value = res.data || []
  } catch(e) { currentNotes.value = [] }
}

async function handleAddNote(feedbackId) {
  if (!newNote.value.trim()) { ElMessage.warning('请输入备注内容'); return }
  try {
    await addNote({
      feedbackId,
      userId: currentUserId.value,
      userName: currentUserName.value,
      content: newNote.value
    })
    ElMessage.success('备注已添加')
    newNote.value = ''
    loadNotes(feedbackId)
  } catch(e) {}
}

onMounted(loadTasks)
</script>

<style scoped>
.tasks-page { max-width:900px; margin:0 auto; }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
.page-header h2 { margin:0; font-size:20px; }
.task-count { font-size:14px; color:#888; }

.task-card { background:#fff; border:1px solid #eee; border-radius:12px; margin-bottom:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.03); }
.task-card.expanded { border-color:#409EFF; }
.task-summary { display:flex; justify-content:space-between; align-items:center; padding:16px 20px; cursor:pointer; transition:background 0.2s; }
.task-summary:hover { background:#fafcff; }
.summary-left { display:flex; align-items:center; gap:16px; }
.case-id { font-weight:700; color:#333; }
.case-address { color:#666; font-size:14px; }
.assign-time { font-size:13px; color:#999; margin-right:8px; }
.task-detail { padding:0 20px 20px; }
.detail-desc { color:#666; font-size:14px; line-height:1.6; }
.final-aqi { font-size:32px; font-weight:700; }

/* 备注样式 */
.notes-section {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 8px;
}

.note-bubble {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #f8f9fb;
  border-radius: 8px;
  border-left: 3px solid #409EFF;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.note-author {
  font-weight: 600;
  font-size: 12px;
  color: #409EFF;
}

.note-time {
  font-size: 11px;
  color: #999;
}

.note-text {
  margin: 0;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
}
</style>
