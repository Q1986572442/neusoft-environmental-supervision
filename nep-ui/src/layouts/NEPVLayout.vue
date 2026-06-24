<template>
  <div class="nepv-layout">
    <header class="nepv-topbar">
      <div class="brand">📊 NEP 环保决策中枢</div>
      <div class="topbar-tabs">
        <router-link to="/nepv/dashboard" class="tab" active-class="tab-active">数据大屏</router-link>
        <router-link to="/nepv/statistics" class="tab" active-class="tab-active">详细统计</router-link>
        <router-link to="/nepv/news" class="tab" active-class="tab-active">环保资讯</router-link>
        <router-link to="/nepv/knowledge" class="tab" active-class="tab-active">知识库</router-link>
        <router-link to="/nepv/ai" class="tab" active-class="tab-active">AI 助手</router-link>
      </div>
      <div class="topbar-right">
        <span class="vip-badge">决策者</span>
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :size="32" icon="UserFilled" />
            <span>{{ userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <main class="nepv-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userName = ref(localStorage.getItem('userName') || '决策者')

function handleLogout() { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.nepv-layout { display:flex; flex-direction:column; height:100vh; background:#0a1628; color:#e0e0e0; }
.nepv-topbar { display:flex; justify-content:space-between; align-items:center; padding:0 32px; height:60px; background:rgba(255,255,255,0.03); border-bottom:1px solid rgba(255,255,255,0.08); flex-shrink:0; }
.brand { font-size:18px; font-weight:700; color:#fff; }
.topbar-tabs { display:flex; gap:4px; }
.tab { padding:8px 20px; color:#8899aa; text-decoration:none; font-size:14px; font-weight:500; border-radius:8px; transition:all 0.2s; }
.tab:hover { color:#fff; background:rgba(255,255,255,0.05); }
.tab-active { color:#409EFF !important; background:rgba(64,158,255,0.1) !important; }
.topbar-right { display:flex; align-items:center; gap:16px; }
.vip-badge { padding:4px 14px; background:linear-gradient(135deg,#409EFF,#AF52DE); color:#fff; border-radius:10px; font-size:12px; font-weight:600; }
.user-info { display:flex; align-items:center; gap:8px; cursor:pointer; color:#ccc; }

.nepv-main { flex:1; overflow-y:auto; }
</style>
