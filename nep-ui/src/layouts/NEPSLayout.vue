<template>
  <div class="swiss-spa-layout">
    <div class="ambient-background">
      <div class="ambient-glow glow-sage"></div>
      <div class="ambient-glow glow-mist"></div>
      <div class="ambient-glow glow-sand"></div>
      <div class="fine-grain-overlay"></div>
    </div>

    <header class="crystal-topbar">
      <div class="topbar-section left-section">
        <div class="brand-mark">
          <el-icon class="brand-icon"><Platform /></el-icon>
        </div>
        <span class="brand-name">NEP 环保中枢</span>
      </div>

      <div class="topbar-section center-section">
        <div class="status-indicator">
          <div class="pulse-dot"></div>
          <span class="status-message">系统运行稳定</span>
        </div>
      </div>

      <div class="topbar-section right-section">
        <button class="icon-action-btn"><el-icon><Search /></el-icon></button>
        <button class="icon-action-btn notification-btn" @click="$router.push('/ne/notifications')">
          <el-icon><Bell /></el-icon>
          <div class="notification-badge"></div>
        </button>
        
        <div class="separator"></div>

        <el-dropdown trigger="click" placement="bottom-end" :popper-class="'swiss-dropdown'">
          <div class="profile-capsule">
            <span class="profile-name">{{ userStore.user?.realName || '用户' }}</span>
            <el-avatar :size="30" class="profile-avatar" :src="avatarUrl">
              <el-icon v-if="!avatarUrl"><UserFilled /></el-icon>
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item class="info-row" style="pointer-events: none;">
                <span class="dropdown-role">{{ roleName }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="$router.push('/ne/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout" class="danger-action">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <aside class="minimal-dock">
      <nav class="dock-navigation">
        <el-tooltip
          v-for="item in dynamicMenuItems"
          :key="item.path"
          :content="item.title"
          placement="right"
          :show-after="300"
          popper-class="swiss-tooltip"
        >
          <router-link :to="item.path" class="dock-action" active-class="is-active">
            <div class="dock-icon-box">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
          </router-link>
        </el-tooltip>
      </nav>
    </aside>

    <main class="pristine-canvas">
      <div class="canvas-viewport">
        <router-view v-slot="{ Component, route }">
          <transition name="seamless-fade" mode="out-in">
            <div :key="route.path" class="viewport-content">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { 
  Platform, HomeFilled, Document, EditPen,
  ChatDotRound, Bell, Search, UserFilled,
  Reading, Collection
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 角色直接写死为公众监督员
const roleName = '公众监督员'

const avatarUrl = computed(() => {
  const av = userStore.user?.avatar
  return av && av.length > 0 ? av : ''
})

// 菜单项简化，专属于公众监督员，路径匹配 index.js 里的子路由
const dynamicMenuItems = computed(() => {
  return [
    { path: '/ne/home', title: '首页概览', icon: HomeFilled },
    { path: '/ne/submit', title: '提交反馈', icon: EditPen },
    { path: '/ne/feedbacks', title: '我的反馈', icon: Document },
    { path: '/ne/news', title: '环保新闻', icon: Reading },
    { path: '/ne/knowledge', title: '环保知识', icon: Collection },
    { path: '/ne/ai', title: 'AI助手', icon: ChatDotRound }
  ]
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出 NEP 环保系统吗？', '退出确认', {
    confirmButtonText: '退出登录',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'swiss-message-box'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  userStore.fetchUser()
})
</script>

<style scoped>
/* ========== Design System Variables ========== */
:root {
  --color-surface: #F4F6F5;
  --color-text-primary: #1C2421;
  --color-text-secondary: #74807B;
  --color-accent: #2A483A;
  
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: rgba(255, 255, 255, 0.8);
  --glass-shadow: 0 8px 32px -8px rgba(28, 36, 33, 0.05), 0 4px 16px -4px rgba(28, 36, 33, 0.03);
  
  --layout-spacing: 24px;
  --topbar-height: 56px;
  --dock-width: 64px;
}

/* ========== Global Layout ========== */
.swiss-spa-layout {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #F4F6F5;
  font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #1C2421;
}

/* ========== 1. Ambient Background ========== */
.ambient-background { position: absolute; inset: 0; z-index: 0; pointer-events: none; overflow: hidden; }
.ambient-glow {
  position: absolute; filter: blur(140px); opacity: 0.5;
  animation: ethereal-drift 30s infinite alternate cubic-bezier(0.4, 0, 0.6, 1);
}
.glow-sage { width: 55vw; height: 55vw; background: rgba(205, 222, 214, 0.8); top: -10%; left: -5%; }
.glow-mist { width: 45vw; height: 45vw; background: rgba(228, 233, 237, 0.8); bottom: -10%; right: -5%; animation-delay: -10s; }
.glow-sand { width: 50vw; height: 50vw; background: rgba(240, 236, 228, 0.6); top: 25%; left: 25%; animation-delay: -20s; }

@keyframes ethereal-drift {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(3%, 4%) scale(1.05); }
}

.fine-grain-overlay {
  position: absolute; inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.015'/%3E%3C/svg%3E");
}

/* ========== 2. Crystal Topbar (The Refined Island) ========== */
.crystal-topbar {
  position: absolute;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  height: 56px;
  
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-bottom-color: rgba(255, 255, 255, 0.5);
  border-radius: 28px;
  box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.8);
  z-index: 100;
  
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px 0 24px;
  gap: 32px;
  transition: box-shadow 0.4s ease;
}

.crystal-topbar:hover { box-shadow: 0 16px 40px -8px rgba(0, 0, 0, 0.06), inset 0 2px 4px rgba(255, 255, 255, 0.9); }

.topbar-section { display: flex; align-items: center; }

.left-section { gap: 12px; }
.brand-mark { color: #2A483A; font-size: 20px; display: flex; }
.brand-name { font-size: 15px; font-weight: 600; letter-spacing: 0.5px; color: #1C2421; }

.center-section { padding: 0 24px; border-left: 1px solid rgba(28, 36, 33, 0.08); border-right: 1px solid rgba(28, 36, 33, 0.08); }
.status-indicator { display: flex; align-items: center; gap: 8px; }
.pulse-dot {
  width: 6px; height: 6px; border-radius: 50%; background-color: #2A483A;
  box-shadow: 0 0 0 0 rgba(42, 72, 58, 0.4);
  animation: gentle-pulse 3s infinite;
}
@keyframes gentle-pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42, 72, 58, 0.4); }
  70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(42, 72, 58, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42, 72, 58, 0); }
}
.status-message { font-size: 13px; font-weight: 500; color: #74807B; letter-spacing: 0.3px; }

.right-section { gap: 4px; }
.icon-action-btn {
  width: 36px; height: 36px; border-radius: 50%; border: none; background: transparent;
  display: flex; justify-content: center; align-items: center;
  font-size: 16px; color: #74807B; cursor: pointer; transition: all 0.3s ease; position: relative;
}
.icon-action-btn:hover { background: rgba(28, 36, 33, 0.04); color: #1C2421; }

.notification-badge {
  position: absolute; top: 8px; right: 8px; width: 6px; height: 6px;
  background-color: #D9534F; border-radius: 50%; border: 1.5px solid #FFF;
}

.separator { width: 1px; height: 16px; background-color: rgba(28, 36, 33, 0.1); margin: 0 8px; }

.profile-capsule {
  display: flex; align-items: center; gap: 12px;
  padding: 4px 4px 4px 16px; border-radius: 20px;
  cursor: pointer; transition: background 0.3s ease;
}
.profile-capsule:hover { background: rgba(28, 36, 33, 0.03); }
.profile-name { font-size: 14px; font-weight: 500; color: #1C2421; }
.profile-avatar { background: #E4E9ED; color: #74807B; border: 1.5px solid #FFF; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

/* ========== 3. Minimalist Dock ========== */
.minimal-dock {
  position: absolute;
  top: 50%;
  left: 24px;
  transform: translateY(-50%);
  z-index: 100;
  
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 32px;
  box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6);
  
  padding: 12px 10px;
}

.dock-navigation { display: flex; flex-direction: column; gap: 8px; }

.dock-action {
  width: 44px; height: 44px;
  display: flex; justify-content: center; align-items: center;
  text-decoration: none; outline: none; position: relative;
}

.dock-icon-box {
  width: 40px; height: 40px; border-radius: 20px;
  display: flex; justify-content: center; align-items: center;
  font-size: 18px; color: #74807B;
  background: transparent;
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.dock-action:hover .dock-icon-box { color: #1C2421; background: rgba(255, 255, 255, 0.6); transform: scale(1.05); }

.dock-action.is-active .dock-icon-box {
  background: #FFFFFF; color: #2A483A;
  box-shadow: 0 4px 12px rgba(28, 36, 33, 0.06), inset 0 1px 2px rgba(255, 255, 255, 1);
}

/* ========== 4. Pristine Canvas ========== */
.pristine-canvas {
  position: absolute;
  top: calc(24px + 56px + 32px);
  left: calc(24px + 64px + 32px);
  right: 48px;
  bottom: 32px;
  z-index: 50;
  display: flex; flex-direction: column;
}

.canvas-viewport { flex: 1; overflow-y: auto; overflow-x: hidden; scrollbar-width: none; }
.canvas-viewport::-webkit-scrollbar { display: none; }

.viewport-content { height: 100%; }

/* Seamless Transition */
.seamless-fade-enter-active, .seamless-fade-leave-active { transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.2, 0.8, 0.2, 1); }
.seamless-fade-enter-from { opacity: 0; transform: translateY(10px); }
.seamless-fade-leave-to { opacity: 0; transform: translateY(-10px); }
</style>

<style>
/* Swiss Style Reset (Injected Globally) */
.swiss-dropdown.el-dropdown-menu {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(40px) saturate(180%) !important;
  border: 1px solid rgba(255, 255, 255, 0.9) !important;
  border-radius: 16px !important;
  padding: 8px !important;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.02) !important;
}
.swiss-dropdown .info-row { padding: 8px 16px; margin-bottom: 4px; }
.swiss-dropdown .dropdown-role { font-size: 12px; font-weight: 600; color: #74807B; text-transform: uppercase; letter-spacing: 0.5px; }
.swiss-dropdown .el-dropdown-menu__item { border-radius: 8px; margin: 2px 0; font-size: 14px; font-weight: 500; color: #1C2421; padding: 8px 16px; transition: all 0.2s; }
.swiss-dropdown .el-dropdown-menu__item:hover { background: rgba(28, 36, 33, 0.04) !important; color: #1C2421 !important; }
.swiss-dropdown .danger-action:hover { background: rgba(217, 83, 79, 0.08) !important; color: #D9534F !important; }

.swiss-tooltip.el-popper {
  background: rgba(28, 36, 33, 0.85) !important;
  backdrop-filter: blur(16px) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #FFFFFF !important;
  font-size: 13px !important;
  font-weight: 500 !important;
  letter-spacing: 0.5px !important;
  padding: 8px 14px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}
.swiss-tooltip.el-popper .el-popper__arrow::before { background: rgba(28, 36, 33, 0.85) !important; border: none !important; }

.swiss-message-box {
  border-radius: 20px !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(40px) !important;
  box-shadow: 0 24px 48px -12px rgba(0, 0, 0, 0.1) !important;
}
</style>