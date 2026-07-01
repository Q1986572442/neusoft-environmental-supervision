<template>
  <div class="alpine-admin-layout">
    
    <!-- 1. 灵动底色 -->
    <div class="ambient-bg"></div>

    <!-- 2. 内嵌式呼吸侧栏 (Solid Breathing Sidebar) -->
    <aside class="alpine-sidebar glass-panel" :class="{ 'is-collapsed': isCollapsed }">
      
      <div class="sidebar-header">
        <div class="brand-zone">
          <div class="brand-logo">
            <el-icon><Platform /></el-icon>
          </div>
          <span class="brand-text">NEP Admin</span>
        </div>
        <button class="collapse-btn" @click="toggleSidebar" :title="isCollapsed ? '展开菜单' : '收起菜单'">
          <el-icon><Fold v-if="!isCollapsed"/><Expand v-else/></el-icon>
        </button>
      </div>

      <nav class="sidebar-nav">
        <el-tooltip content="数据总控大屏" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/dashboard" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><Odometer /></el-icon>
            <span class="nav-text">数据总控大屏</span>
          </router-link>
        </el-tooltip>

        <el-tooltip content="深度统计洞察" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/statistics" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><DataAnalysis /></el-icon>
            <span class="nav-text">深度统计洞察</span>
          </router-link>
        </el-tooltip>

        <div class="nav-divider"></div>

        <el-tooltip content="系统账户与权限" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/users" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><User /></el-icon>
            <span class="nav-text">系统账户与权限</span>
          </router-link>
        </el-tooltip>

        <el-tooltip content="全量监督流转" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/feedbacks" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><Tickets /></el-icon>
            <span class="nav-text">全量监督流转</span>
          </router-link>
        </el-tooltip>

        <div class="nav-divider"></div>

        <el-tooltip content="环保资讯发布" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/news" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><Reading /></el-icon>
            <span class="nav-text">环保资讯发布</span>
          </router-link>
        </el-tooltip>

        <el-tooltip content="知识库与规章" placement="right" :disabled="!isCollapsed" popper-class="alpine-tooltip">
          <router-link to="/admin/knowledge" class="nav-item" active-class="is-active">
            <el-icon class="nav-icon"><Collection /></el-icon>
            <span class="nav-text">知识库与规章</span>
          </router-link>
        </el-tooltip>
      </nav>

      <div class="sidebar-footer">
        <el-dropdown trigger="click" placement="top-start" popper-class="alpine-admin-dropdown">
          <div class="profile-card">
            <el-avatar :size="40" class="admin-avatar" :src="userStore.user?.avatar">
              <el-icon v-if="!userStore.user?.avatar"><UserFilled /></el-icon>
            </el-avatar>
            <div class="profile-info">
              <span class="admin-name">{{ userName }}</span>
              <span class="admin-role">System Root</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="navTo('/admin/profile')">
                <el-icon><Setting /></el-icon> 系统偏好设置
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout" class="danger-action">
                <el-icon><SwitchButton /></el-icon> 安全登出
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

    </aside>

    <!-- 3. 右侧核心舞台 (Main Canvas) -->
    <main class="main-canvas">
      
      <!-- 【核心重构 1】去除冗余标题，改为极简的系统状态悬浮带 -->
      <header class="utility-strip">
        <div class="strip-left">
          <div class="system-status">
            <div class="pulse-dot"></div>
            <span class="status-text">NEP Core Engine v2.0 · 运行平稳</span>
          </div>
        </div>
        <div class="strip-right">
          <!-- 真实的唤出式调度器 -->
          <button class="command-trigger" @click="openCmdPalette" title="或按下 ⌘K / Ctrl+K">
            <el-icon><Search /></el-icon>
            <span>全局系统调度</span>
            <kbd>⌘ K</kbd>
          </button>
          <button class="utility-btn" title="系统通知">
            <el-icon><Bell /></el-icon>
          </button>
        </div>
      </header>

      <!-- 【核心重构 2】去除多余玻璃框，全权交给独立页面的容器 -->
      <div class="viewport-wrapper">
        <router-view v-slot="{ Component, route }">
          <transition name="spatial-fade" mode="out-in">
            <div :key="route.path" class="viewport-content">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </div>

    </main>

    <!-- 【核心重构 3】全局命令控制台 (Command Palette) -->
    <transition name="cmd-fade">
      <div class="cmd-overlay" v-if="cmdVisible" @click.self="closeCmdPalette">
        <div class="cmd-palette glass-cmd">
          
          <div class="cmd-header">
            <el-icon class="cmd-search-icon"><Search /></el-icon>
            <input 
              ref="cmdInput"
              v-model="cmdQuery" 
              class="cmd-input" 
              placeholder="搜索功能模块、输入快捷指令..." 
              @keyup.esc="closeCmdPalette"
            >
            <div class="cmd-hint"><kbd>ESC</kbd> 退出</div>
          </div>

          <div class="cmd-body">
            <div class="cmd-group">
              <div class="cmd-group-title">建议操作</div>
              <div class="cmd-option" @click="navTo('/admin/feedbacks')">
                <div class="cmd-icon-box"><el-icon><Position /></el-icon></div>
                <div class="cmd-option-text">指派最新流转工单</div>
                <el-icon class="cmd-enter-icon"><TopRight /></el-icon>
              </div>
              <div class="cmd-option" @click="navTo('/admin/users')">
                <div class="cmd-icon-box"><el-icon><User /></el-icon></div>
                <div class="cmd-option-text">管理账户与权限</div>
                <el-icon class="cmd-enter-icon"><TopRight /></el-icon>
              </div>
              <div class="cmd-option" @click="navTo('/admin/news')">
                <div class="cmd-icon-box"><el-icon><Edit /></el-icon></div>
                <div class="cmd-option-text">发布环保资讯或系统公告</div>
                <el-icon class="cmd-enter-icon"><TopRight /></el-icon>
              </div>
            </div>

            <div class="cmd-group" v-if="cmdQuery">
              <div class="cmd-group-title">检索结果</div>
              <div class="cmd-option empty-search">
                对不起，全局业务数据搜索引擎尚未并网。
              </div>
            </div>
          </div>

        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Platform, Odometer, DataAnalysis, User, Tickets, 
  Reading, Collection, UserFilled, Setting, SwitchButton, 
  Fold, Expand, Search, Bell, Position, TopRight, Edit
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userName = computed(() => userStore.user?.realName || 'Administrator')

// 侧栏折叠控制
const isCollapsed = ref(false)
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 通用跳转
const navTo = (path) => {
  cmdVisible.value = false
  router.push(path)
}

// 登出逻辑
const handleLogout = () => {
  ElMessageBox.confirm('确认要挂起管理员会话并登出系统吗？', '系统安全', {
    confirmButtonText: '安全登出',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'alpine-message-box'
  }).then(() => {
    localStorage.clear()
    router.push('/login')
  }).catch(() => {})
}

// ==================== 全局命令控制台 (Command Palette) 逻辑 ====================
const cmdVisible = ref(false)
const cmdQuery = ref('')
const cmdInput = ref(null)

const openCmdPalette = () => {
  cmdQuery.value = ''
  cmdVisible.value = true
}

const closeCmdPalette = () => {
  cmdVisible.value = false
}

// 监听快捷键 (Cmd+K 或 Ctrl+K)
const handleKeydown = (e) => {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    if (cmdVisible.value) closeCmdPalette()
    else openCmdPalette()
  }
}

// 弹窗出现时自动聚焦输入框
watch(cmdVisible, (val) => {
  if (val) {
    setTimeout(() => {
      cmdInput.value?.focus()
    }, 50)
  }
})

onMounted(() => {
  if (!userStore.user) userStore.fetchUser()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* ========== 1. 基础布局与底色 ========== */
.alpine-admin-layout {
  position: relative; width: 100vw; height: 100vh;
  background-color: #F4F6F5;
  overflow: hidden; display: flex; padding: 24px; gap: 24px;
  box-sizing: border-box; font-family: "SF Pro Display", -apple-system, BlinkMacSystemFont, sans-serif;
}
.ambient-bg {
  position: absolute; inset: 0; z-index: 0; pointer-events: none;
  background-image: radial-gradient(circle at 50% 0%, rgba(255,255,255,0.8) 0%, transparent 70%);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 32px -12px rgba(28, 36, 33, 0.06), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

/* ========== 2. 内嵌式呼吸侧栏 ========== */
.alpine-sidebar {
  position: relative; z-index: 10; flex-shrink: 0;
  width: 280px; height: 100%; border-radius: 24px;
  display: flex; flex-direction: column;
  transition: width 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
  overflow: hidden;
}

.alpine-sidebar.is-collapsed { width: 88px; }

.sidebar-header {
  height: 88px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;
  border-bottom: 1px solid rgba(28, 36, 33, 0.04); flex-shrink: 0;
  transition: justify-content 0.4s;
}

.brand-zone { display: flex; align-items: center; gap: 12px; overflow: hidden; }
.brand-logo {
  width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
  background: linear-gradient(135deg, #1C2421 0%, #2A483A 100%); color: white;
  display: flex; justify-content: center; align-items: center; font-size: 20px;
  box-shadow: 0 4px 12px rgba(42, 72, 58, 0.2);
}
.brand-text {
  font-size: 18px; font-weight: 800; color: #1C2421; white-space: nowrap;
  max-width: 150px; opacity: 1; transition: max-width 0.3s ease, opacity 0.3s ease;
}
.is-collapsed .brand-zone { display: none; }
.is-collapsed .sidebar-header { justify-content: center; padding: 0; }

.collapse-btn {
  width: 36px; height: 36px; border-radius: 10px; border: none; background: rgba(28, 36, 33, 0.04);
  color: #74807B; display: flex; justify-content: center; align-items: center; font-size: 16px;
  cursor: pointer; transition: all 0.3s; flex-shrink: 0;
}
.collapse-btn:hover { background: rgba(28, 36, 33, 0.08); color: #1C2421; }
.is-collapsed .collapse-btn { width: 44px; height: 44px; font-size: 20px; border-radius: 12px; background: white; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

/* 侧栏导航 */
.sidebar-nav { flex: 1; padding: 24px 16px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; overflow-x: hidden; }
.sidebar-nav::-webkit-scrollbar { display: none; }
.nav-divider { height: 1px; background: rgba(28, 36, 33, 0.06); margin: 8px 12px; }

.nav-item {
  position: relative; height: 48px; border-radius: 14px;
  display: flex; align-items: center; padding: 0 16px; gap: 14px;
  color: #74807B; text-decoration: none; cursor: pointer;
  transition: color 0.3s ease, justify-content 0.3s; z-index: 1; overflow: hidden;
}
.nav-icon { font-size: 20px; flex-shrink: 0; transition: transform 0.3s; }
.nav-text {
  font-size: 15px; font-weight: 600; white-space: nowrap; overflow: hidden;
  max-width: 200px; opacity: 1; transition: max-width 0.3s ease, opacity 0.3s ease;
}

.is-collapsed .nav-item { padding: 0; justify-content: center; }
.is-collapsed .nav-text { max-width: 0; opacity: 0; display: none; }
.nav-item:hover { color: #1C2421; }
.nav-item:hover .nav-icon { transform: scale(1.1); }

.nav-item::before {
  content: ''; position: absolute; inset: 0; border-radius: 14px; background: #1C2421; z-index: -1;
  transform: scaleX(0); transform-origin: left; transition: transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.nav-item.is-active { color: white; }
.nav-item.is-active::before { transform: scaleX(1); }

/* 侧栏底部 */
.sidebar-footer { padding: 16px; border-top: 1px solid rgba(28, 36, 33, 0.04); transition: padding 0.3s; }
.is-collapsed .sidebar-footer { display: flex; justify-content: center; padding: 16px 8px; }

.profile-card {
  display: flex; align-items: center; gap: 12px; padding: 8px; border-radius: 16px;
  cursor: pointer; transition: background 0.3s; overflow: hidden; width: 100%; box-sizing: border-box;
}
.profile-card:hover { background: rgba(28, 36, 33, 0.04); }

.admin-avatar { flex-shrink: 0; border: 2px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.profile-info { 
  flex: 1; display: flex; flex-direction: column; white-space: nowrap; overflow: hidden;
  max-width: 150px; opacity: 1; transition: max-width 0.3s ease, opacity 0.3s ease;
}
.admin-name { font-size: 14px; font-weight: 700; color: #1C2421; overflow: hidden; text-overflow: ellipsis;}
.admin-role { font-size: 11px; color: #74807B; font-weight: 600; margin-top: 2px; }

.is-collapsed .profile-card { padding: 8px 0; justify-content: center; }
.is-collapsed .profile-info { max-width: 0; opacity: 0; display: none; }

/* ========== 3. 右侧舞台 ========== */
.main-canvas { flex: 1; display: flex; flex-direction: column; min-width: 0; position: relative; z-index: 10; }

/* 系统状态悬浮带 (无边界感设计) */
.utility-strip {
  height: 48px; flex-shrink: 0; display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding: 0 8px; /* 极简边距，让位于内部容器 */
}

.system-status { display: flex; align-items: center; gap: 8px; }
.status-text { font-size: 13px; font-weight: 600; color: #74807B; font-family: monospace; letter-spacing: 0.5px; }
.pulse-dot { width: 6px; height: 6px; border-radius: 50%; background: #2AA876; box-shadow: 0 0 0 0 rgba(42, 168, 118, 0.4); animation: pulse-green 2s infinite; }
@keyframes pulse-green { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42, 168, 118, 0.4); } 70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(42, 168, 118, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(42, 168, 118, 0); } }

.strip-right { display: flex; align-items: center; gap: 16px; }

/* 极其隐喻的 Command 触发器 */
.command-trigger {
  display: flex; align-items: center; gap: 10px; padding: 6px 10px 6px 12px;
  background: rgba(28, 36, 33, 0.04); border: 1px solid rgba(28, 36, 33, 0.06);
  border-radius: 10px; cursor: pointer; color: #74807B; transition: all 0.3s;
}
.command-trigger:hover { background: rgba(255, 255, 255, 0.6); box-shadow: 0 2px 8px rgba(0,0,0,0.02); color: #1C2421; }
.command-trigger span { font-size: 13px; font-weight: 500; width: 100px; text-align: left;}
.command-trigger kbd { font-size: 11px; font-weight: 700; background: white; padding: 2px 6px; border-radius: 6px; border: 1px solid rgba(28,36,33,0.1); color: #1C2421; }

.utility-btn {
  width: 32px; height: 32px; border-radius: 8px; border: none; background: transparent;
  color: #74807B; font-size: 18px; display: flex; justify-content: center; align-items: center;
  cursor: pointer; transition: all 0.3s;
}
.utility-btn:hover { background: rgba(28, 36, 33, 0.05); color: #1C2421; }

/* 画板容器：完全释放边框，由路由子组件全权控制玻璃态和内边距 */
.viewport-wrapper { flex: 1; display: flex; flex-direction: column; overflow: hidden; position: relative; }
.viewport-content { flex: 1; height: 100%; display: flex; flex-direction: column; }

/* 空间转场动画 */
.spatial-fade-enter-active, .spatial-fade-leave-active { transition: opacity 0.4s ease, transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); }
.spatial-fade-enter-from { opacity: 0; transform: translateY(16px) scale(0.99); }
.spatial-fade-leave-to { opacity: 0; transform: translateY(-16px) scale(0.99); }

/* ========== 4. 全局命令面板 (Command Palette) ========== */
.cmd-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(28, 36, 33, 0.4); backdrop-filter: blur(8px);
  display: flex; justify-content: center; align-items: flex-start; padding-top: 15vh;
}

.glass-cmd {
  background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.9); border-radius: 20px;
  box-shadow: 0 32px 64px -16px rgba(0, 0, 0, 0.2), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

.cmd-palette { width: 100%; max-width: 640px; display: flex; flex-direction: column; overflow: hidden; }

.cmd-header {
  display: flex; align-items: center; padding: 20px 24px;
  border-bottom: 1px solid rgba(28, 36, 33, 0.06); gap: 16px;
}
.cmd-search-icon { font-size: 24px; color: #1C2421; }
.cmd-input { flex: 1; font-size: 20px; font-weight: 500; color: #1C2421; border: none; background: transparent; outline: none; }
.cmd-input::placeholder { color: #A0AAB2; font-weight: 400; }
.cmd-hint { font-size: 12px; color: #74807B; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.cmd-hint kbd { background: rgba(28, 36, 33, 0.05); padding: 2px 6px; border-radius: 6px; font-family: monospace; }

.cmd-body { padding: 16px; display: flex; flex-direction: column; gap: 16px; max-height: 400px; overflow-y: auto; }
.cmd-body::-webkit-scrollbar { display: none; }

.cmd-group-title { font-size: 12px; font-weight: 700; color: #A0AAB2; padding: 8px 12px; letter-spacing: 0.5px; }

.cmd-option {
  display: flex; align-items: center; gap: 16px; padding: 12px 16px; border-radius: 12px;
  cursor: pointer; transition: background 0.2s; color: #1C2421;
}
.cmd-option:hover { background: rgba(28, 36, 33, 0.04); }

.cmd-icon-box {
  width: 32px; height: 32px; border-radius: 8px; background: white; border: 1px solid rgba(28, 36, 33, 0.06);
  display: flex; justify-content: center; align-items: center; font-size: 16px; color: #2A483A; box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.cmd-option-text { flex: 1; font-size: 15px; font-weight: 600; }
.cmd-enter-icon { color: #A0AAB2; opacity: 0; transition: opacity 0.2s; }
.cmd-option:hover .cmd-enter-icon { opacity: 1; }

.empty-search { color: #A0AAB2; justify-content: center; padding: 24px; font-size: 14px; font-style: italic; cursor: default; }
.empty-search:hover { background: transparent; }

/* 调度台动画 */
.cmd-fade-enter-active, .cmd-fade-leave-active { transition: opacity 0.3s, transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.cmd-fade-enter-from, .cmd-fade-leave-to { opacity: 0; transform: scale(0.95) translateY(-20px); }
</style>

<style>
/* 悬浮菜单覆盖 */
.alpine-admin-dropdown.el-dropdown-menu {
  background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important;
  border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 16px !important;
  padding: 8px !important; box-shadow: 0 16px 40px -8px rgba(28, 36, 33, 0.15) !important;
}
.alpine-admin-dropdown .el-dropdown-menu__item { border-radius: 10px; font-size: 14px; padding: 10px 16px; font-weight: 500; color: #1C2421; transition: all 0.2s; }
.alpine-admin-dropdown .el-dropdown-menu__item:hover { background: #F4F6F5 !important; font-weight: 600; }
.alpine-admin-dropdown .danger-action { color: #E11D48 !important; }
.alpine-admin-dropdown .danger-action:hover { background: #FFF1F2 !important; color: #E11D48 !important; }

/* 模态框 */
.alpine-message-box { border-radius: 24px !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(40px) !important; box-shadow: 0 32px 64px -16px rgba(28, 36, 33, 0.15) !important; }
.alpine-message-box .el-message-box__title { font-weight: 700; color: #1C2421; }
</style>