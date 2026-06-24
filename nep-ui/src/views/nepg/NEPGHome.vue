<template>
  <div class="nepg-home">
    <div class="welcome-card">
      <h1>{{ greeting }}，{{ userName }}</h1>
      <p>员工编号: {{ employeeCode }} | 今日待检测任务: <strong>{{ assignedTasks.length }}</strong> 个</p>
    </div>

    <div class="stats-row">
      <div class="stat-card urgent"><div class="num">{{ assignedTasks.length }}</div><div class="label">待检测任务</div></div>
      <div class="stat-card"><div class="num">{{ todayCompleted }}</div><div class="label">今日已完成</div></div>
      <div class="stat-card"><div class="num">{{ myTotalDetections }}</div><div class="label">累计检测</div></div>
      <div class="stat-card"><div class="num">{{ myAvgAqi }}</div><div class="label">平均AQI</div></div>
    </div>

    <div class="task-section" v-if="assignedTasks.length > 0">
      <div class="section-header">
        <h3>📋 指派给我的检测任务</h3>
        <el-button type="primary" @click="$router.push('/nepg/tasks')">查看全部</el-button>
      </div>
      <div class="task-grid">
        <div v-for="task in assignedTasks.slice(0, 4)" :key="task.id" class="task-card" @click="$router.push('/nepg/tasks')">
          <div class="task-header">
            <span class="task-id">案件 #{{ task.id }}</span>
            <el-tag :type="task.estimatedAqiLevel <= 2 ? 'success' : task.estimatedAqiLevel <= 4 ? 'warning' : 'danger'" size="small">
              AQI {{ task.estimatedAqiLevel }}级
            </el-tag>
          </div>
          <div class="task-address"><el-icon><Location /></el-icon> {{ task.specificAddress || '地址待确认' }}</div>
          <div class="task-desc">{{ task.description || '暂无详细描述' }}</div>
          <div class="task-time">指派时间: {{ formatTime(task.assignTime) }}</div>
        </div>
      </div>
    </div>
    <div v-else class="empty-tasks">
      <el-empty description="暂无待检测任务，请等待管理员指派" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getFeedbackPage } from '@/api/feedback'
import { getMyAqiRecords } from '@/api/aqi'

const userName = ref(localStorage.getItem('userName') || '网格员')
const employeeCode = ref(localStorage.getItem('employeeCode') || '-')
const assignedTasks = ref([])
const myTotalDetections = ref(0)
const myAvgAqi = ref('-')
const todayCompleted = ref(0)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '辛苦了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function formatTime(t) { if (!t) return ''; return t.replace('T', ' ').substring(0, 16) }

onMounted(async () => {
  const uid = localStorage.getItem('userId')
  if (!uid) return
  try {
    const res = await getFeedbackPage(1, 50, 'ASSIGNED', Number(uid))
    assignedTasks.value = res.data || []
  } catch(e) {}
  try {
    const records = await getMyAqiRecords(Number(uid))
    const data = records.data || []
    myTotalDetections.value = data.length
    if (data.length > 0) {
      const sum = data.reduce((s, r) => s + (r.finalAqi || 0), 0)
      myAvgAqi.value = Math.round(sum / data.length)
    }
    const today = new Date().toISOString().substring(0, 10)
    todayCompleted.value = data.filter(r => r.createTime && r.createTime.startsWith(today)).length
  } catch(e) {}
})
</script>

<style scoped>
.nepg-home { max-width:1000px; margin:0 auto; }
.welcome-card { background:linear-gradient(135deg,#409EFF,#337ecc); color:#fff; padding:40px; border-radius:16px; margin-bottom:24px; }
.welcome-card h1 { margin:0 0 8px; font-size:24px; }
.welcome-card p { margin:0; opacity:0.9; font-size:14px; }

.stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:24px; }
.stat-card { background:#fff; padding:20px; border-radius:12px; text-align:center; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.stat-card.urgent { border:2px solid #E6A23C; }
.stat-card .num { font-size:28px; font-weight:700; color:#409EFF; }
.stat-card.urgent .num { color:#E6A23C; }
.stat-card .label { font-size:13px; color:#888; margin-top:4px; }

.task-section { background:#fff; padding:24px; border-radius:12px; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.section-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.section-header h3 { margin:0; font-size:16px; }
.task-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:12px; }
.task-card { border:1px solid #eee; padding:16px; border-radius:10px; cursor:pointer; transition:all 0.2s; }
.task-card:hover { border-color:#409EFF; background:#fafcff; }
.task-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.task-id { font-weight:700; color:#333; }
.task-address { font-size:13px; color:#666; margin-bottom:6px; display:flex; align-items:center; gap:4px; }
.task-desc { font-size:13px; color:#999; margin-bottom:6px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.task-time { font-size:12px; color:#bbb; }
.empty-tasks { padding:60px 0; }
</style>
