<template>
  <div class="neps-layout">
    <!-- 顶部导航 -->
    <header class="neps-topbar">
      <div class="brand">🌿 东软环保公众监督</div>
      <div class="topbar-right">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-button link @click="$router.push('/ne/notifications')">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :size="32" icon="UserFilled" />
            <span class="user-name">{{ userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>👤 公众监督员</el-dropdown-item>
              <el-dropdown-item divided @click="$router.push('/ne/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="neps-body">
      <!-- 侧边导航 -->
      <aside class="neps-sidebar">
        <nav>
          <router-link to="/ne/home" class="nav-item" active-class="active">
            <el-icon><HomeFilled /></el-icon><span>首页概览</span>
          </router-link>
          <router-link to="/ne/submit" class="nav-item" active-class="active">
            <el-icon><EditPen /></el-icon><span>提交反馈</span>
          </router-link>
          <router-link to="/ne/feedbacks" class="nav-item" active-class="active">
            <el-icon><Document /></el-icon><span>我的反馈</span>
          </router-link>
          <div class="nav-divider"></div>
          <router-link to="/ne/news" class="nav-item" active-class="active">
            <el-icon><Reading /></el-icon><span>环保资讯</span>
          </router-link>
          <router-link to="/ne/knowledge" class="nav-item" active-class="active">
            <el-icon><Collection /></el-icon><span>环保知识库</span>
          </router-link>
          <router-link to="/ne/ai" class="nav-item" active-class="active">
            <el-icon><ChatDotRound /></el-icon><span>AI 助手</span>
          </router-link>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="neps-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const userName = ref(localStorage.getItem('userName') || '监督员')
const unreadCount = ref(0)
let timer = null

async function fetchCount() {
  const uid = localStorage.getItem('userId')
  if (!uid) return
  try { const r = await getUnreadCount(Number(uid)); unreadCount.value = r.data?.unreadCount || 0 } catch(e) {}
}

function handleLogout() {
  localStorage.clear()
  router.push('/login')
}

onMounted(() => { fetchCount(); timer = setInterval(fetchCount, 30000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.neps-layout { display:flex; flex-direction:column; height:100vh; background:#f0f4f0; }
.neps-topbar { display:flex; justify-content:space-between; align-items:center; padding:0 24px; height:56px; background:#fff; border-bottom:1px solid #e8e8e8; flex-shrink:0; }
.brand { font-size:16px; font-weight:700; color:#2A483A; }
.topbar-right { display:flex; align-items:center; gap:16px; }
.user-info { display:flex; align-items:center; gap:8px; cursor:pointer; }
.user-name { font-size:14px; color:#333; }

.neps-body { display:flex; flex:1; overflow:hidden; }
.neps-sidebar { width:200px; background:#fff; border-right:1px solid #e8e8e8; padding:16px 0; flex-shrink:0; }
.nav-item { display:flex; align-items:center; gap:10px; padding:12px 20px; color:#555; text-decoration:none; font-size:14px; transition:all 0.2s; border-left:3px solid transparent; }
.nav-item:hover { background:#f5f7f5; color:#2A483A; }
.nav-item.active { background:#e8f5e9; color:#2A483A; font-weight:600; border-left-color:#2A483A; }
.nav-divider { height:1px; background:#eee; margin:8px 16px; }

.neps-main { flex:1; overflow-y:auto; padding:24px; }
</style>
