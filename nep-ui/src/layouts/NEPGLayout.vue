<template>
  <div class="nepg-layout">
    <header class="nepg-topbar">
      <div class="brand">🔬 NEP 网格员工作台</div>
      <div class="topbar-center">
        <span class="employee-badge">员工编号: {{ employeeCode }}</span>
        <span class="work-area">负责区域: {{ workArea }}</span>
      </div>
      <div class="topbar-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-button link @click="$router.push('/nepg/notifications')">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :size="32" icon="UserFilled" />
            <span>{{ userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>🔬 网格员</el-dropdown-item>
              <el-dropdown-item divided @click="$router.push('/nepg/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="nepg-body">
      <aside class="nepg-sidebar">
        <nav>
          <router-link to="/nepg/home" class="nav-item" active-class="active">
            <el-icon><HomeFilled /></el-icon><span>工作台首页</span>
          </router-link>
          <router-link to="/nepg/tasks" class="nav-item" active-class="active">
            <el-icon><List /></el-icon><span>检测任务</span>
            <el-badge v-if="taskCount > 0" :value="taskCount" class="nav-badge" />
          </router-link>
          <router-link to="/nepg/records" class="nav-item" active-class="active">
            <el-icon><DataLine /></el-icon><span>历史检测记录</span>
          </router-link>
          <div class="nav-divider"></div>
          <router-link to="/nepg/news" class="nav-item" active-class="active">
            <el-icon><Reading /></el-icon><span>环保资讯</span>
          </router-link>
          <router-link to="/nepg/knowledge" class="nav-item" active-class="active">
            <el-icon><Collection /></el-icon><span>知识库</span>
          </router-link>
          <router-link to="/nepg/ai" class="nav-item" active-class="active">
            <el-icon><ChatDotRound /></el-icon><span>AI 助手</span>
          </router-link>
        </nav>
      </aside>

      <main class="nepg-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUnreadCount } from '@/api/notification'
import { getFeedbackPage } from '@/api/feedback'

const router = useRouter()
const userName = ref(localStorage.getItem('userName') || '网格员')
const employeeCode = ref(localStorage.getItem('employeeCode') || '-')
const unreadCount = ref(0)
const taskCount = ref(0)
const workArea = ref('北京')
let timer = null

async function fetchData() {
  const uid = localStorage.getItem('userId')
  if (!uid) return
  try {
    const [notifRes, taskRes] = await Promise.all([
      getUnreadCount(Number(uid)),
      getFeedbackPage(1, 1, 'ASSIGNED', Number(uid))
    ])
    unreadCount.value = notifRes.data?.unreadCount || 0
    taskCount.value = taskRes.total || 0
  } catch(e) {}
}

function handleLogout() { localStorage.clear(); router.push('/login') }

onMounted(() => { fetchData(); timer = setInterval(fetchData, 60000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.nepg-layout { display:flex; flex-direction:column; height:100vh; background:#f4f6f9; }
.nepg-topbar { display:flex; justify-content:space-between; align-items:center; padding:0 24px; height:56px; background:#fff; border-bottom:2px solid #409EFF; flex-shrink:0; }
.brand { font-size:16px; font-weight:700; color:#409EFF; }
.topbar-center { display:flex; gap:20px; font-size:13px; color:#666; }
.employee-badge { background:#ecf5ff; color:#409EFF; padding:2px 12px; border-radius:10px; font-weight:600; }
.work-area { font-weight:500; }
.topbar-right { display:flex; align-items:center; gap:16px; }
.user-info { display:flex; align-items:center; gap:8px; cursor:pointer; }

.nepg-body { display:flex; flex:1; overflow:hidden; }
.nepg-sidebar { width:200px; background:#fff; border-right:1px solid #e8e8e8; padding:16px 0; flex-shrink:0; }
.nav-item { display:flex; align-items:center; gap:10px; padding:12px 20px; color:#555; text-decoration:none; font-size:14px; transition:all 0.2s; border-left:3px solid transparent; position:relative; }
.nav-item:hover { background:#ecf5ff; color:#409EFF; }
.nav-item.active { background:#ecf5ff; color:#409EFF; font-weight:600; border-left-color:#409EFF; }
.nav-badge { position:absolute; right:12px; }
.nav-divider { height:1px; background:#eee; margin:8px 16px; }

.nepg-main { flex:1; overflow-y:auto; padding:24px; }
</style>
