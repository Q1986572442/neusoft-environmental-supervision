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
        <el-tooltip content="全局搜索" placement="bottom">
          <button class="icon-action-btn" @click="showSearch = true">
            <el-icon><Search /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip :content="unreadCount > 0 ? `${unreadCount} 条未读通知` : '通知中心'" placement="bottom">
          <button class="icon-action-btn notification-btn" @click="$router.push('/notifications')">
            <el-icon><Bell /></el-icon>
            <div class="notification-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</div>
          </button>
        </el-tooltip>

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
              <el-dropdown-item divided @click="$router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="$router.push('/notifications')">
                🔔 通知中心
                <span v-if="unreadCount > 0" style="color:#D9534F;margin-left:8px">({{ unreadCount }})</span>
              </el-dropdown-item>
              <el-dropdown-item @click="handleLogout" class="danger-action">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 全局搜索面板（Command Palette 风格浮层） -->
    <Teleport to="body">
      <div v-if="showSearch" class="search-overlay" @click.self="showSearch = false" @keydown.esc="showSearch = false">
        <div class="search-panel glass-panel-strong" @click.stop>
          <!-- 输入行 -->
          <div class="search-input-row">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              ref="searchInputRef"
              v-model="searchKeyword"
              placeholder="搜索新闻、知识..."
              class="search-field"
              @keyup.enter="handleSearch"
              @keydown.esc="showSearch = false"
            />
            <button v-if="searchKeyword" class="search-clear" @click="searchKeyword='';searchResults=[];searchStatus='idle'">
              <el-icon><Close /></el-icon>
            </button>
            <button class="search-close" @click="showSearch = false">ESC</button>
          </div>

          <!-- 搜索结果列表 -->
          <div class="search-body" v-if="searchStatus === 'done' && searchResults.length > 0">
            <div class="search-result-summary">{{ searchResults.length }} 条结果</div>
            <div
              class="search-result-item"
              v-for="item in searchResults"
              :key="item.type + '-' + item.id"
              @click="goToDetail(item)"
            >
              <div class="result-header">
                <span class="result-type-badge" :class="item.type">{{ item.typeLabel }}</span>
                <span class="result-date" v-if="item.publishTime || item.createTime">{{ formatSearchTime(item.publishTime || item.createTime) }}</span>
              </div>
              <div class="result-title" v-html="highlightKeyword(item.title, searchKeyword)"></div>
              <div class="result-summary" v-if="item.summary">{{ item.summary }}</div>
            </div>
          </div>

          <!-- 搜索中 -->
          <div class="search-status-box" v-else-if="searchStatus === 'searching'">
            <el-icon class="is-loading" :size="20"><Loading /></el-icon>
            <span>正在搜索...</span>
          </div>

          <!-- 无结果 -->
          <div class="search-status-box" v-else-if="searchStatus === 'no-results'">
            <el-icon :size="32"><Warning /></el-icon>
            <span class="no-results-title">未找到相关内容</span>
            <span class="no-results-hint">请尝试其他关键词，如：环保、空气质量、碳中和</span>
          </div>

          <!-- 初始提示 -->
          <div class="search-hints" v-else>
            <div class="hint-label">快速搜索</div>
            <div class="hint-tags">
              <span class="hint-tag" v-for="tag in quickSearchTags" :key="tag" @click="quickSearch(tag)">{{ tag }}</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

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
import { computed, onMounted, ref, markRaw, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUnreadCount } from '@/api/notification'
import { getNewsPage } from '@/api/news'
import { getKnowledgePage } from '@/api/knowledge'
import {
  Platform, HomeFilled, Document, DataLine, EditPen,
  PieChart, ChatDotRound, Setting, Bell, Search, UserFilled,
  Reading, Collection, Notification, Close, Loading, Warning
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const role = computed(() => userStore.getRole())

const roleName = computed(() => {
  const map = { 'NEPS': '公众监督员', 'NEPG': '网格员', 'NEPM': '系统管理员', 'NEPV': '决策者' }
  return map[role.value] || '系统用户'
})

const avatarUrl = computed(() => {
  const av = userStore.user?.avatar
  return av && av.length > 0 ? av : ''
})

// 通知未读数量
const unreadCount = ref(0)
let unreadTimer = null

async function fetchUnreadCount() {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return
    const res = await getUnreadCount(Number(userId))
    unreadCount.value = res.data?.unreadCount || 0
  } catch (e) {}
}

// 全局搜索
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searchStatus = ref('idle') // idle | searching | done | no-results
const searchInputRef = ref(null)

const quickSearchTags = ['PM2.5', '碳中和', '垃圾分类', '空气质量', '环保政策', '绿色出行']

// 打开搜索面板时重置状态
watch(showSearch, async (val) => {
  if (val) {
    await nextTick()
    searchInputRef.value?.focus()
    searchKeyword.value = ''
    searchResults.value = []
    searchStatus.value = 'idle'
  }
})

/** 快速搜索标签点击 */
function quickSearch(tag) {
  searchKeyword.value = tag
  handleSearch()
}

/** 高亮搜索关键词 */
function highlightKeyword(text, keyword) {
  if (!text || !keyword) return text || ''
  const escaped = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return text.replace(regex, '<em class="search-highlight">$1</em>')
}

/** 格式化时间 */
function formatSearchTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

/** 执行搜索 */
async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  searchStatus.value = 'searching'
  searchResults.value = []
  try {
    const [newsRes, knowledgeRes] = await Promise.all([
      getNewsPage(1, 5, { keyword: kw }),
      getKnowledgePage(1, 5, { keyword: kw })
    ])
    const results = []
    if (newsRes.data) {
      newsRes.data.forEach(n => results.push({ ...n, type: 'news', typeLabel: '📰 新闻' }))
    }
    if (knowledgeRes.data) {
      knowledgeRes.data.forEach(k => results.push({ ...k, type: 'knowledge', typeLabel: '📚 知识' }))
    }
    searchResults.value = results
    searchStatus.value = results.length > 0 ? 'done' : 'no-results'
  } catch (e) {
    searchStatus.value = 'no-results'
  }
}

/** 跳转到详情 */
function goToDetail(item) {
  showSearch.value = false
  if (item.type === 'news') {
    router.push(`/news/${item.id}`)
  } else {
    router.push(`/knowledge/${item.id}`)
  }
}

const dynamicMenuItems = computed(() => {
  const items = [{ path: '/home', title: '全局概览', icon: markRaw(HomeFilled) }]

  if (role.value === 'NEPS') {
    // 公众监督员：提交反馈 + 查看自己的反馈记录
    items.push({ path: '/feedback/submit', title: '提交反馈', icon: markRaw(EditPen) })
    items.push({ path: '/feedback', title: '我的反馈', icon: markRaw(Document) })
  }

  if (role.value === 'NEPG') {
    // 网格员：查看指派给自己的任务
    items.push({ path: '/aqi', title: '检测任务', icon: markRaw(DataLine) })
  }

  if (role.value === 'NEPM') {
    // 管理员：管理后台(含反馈管理+AQI管理+统计) + 数据大盘
    items.push({ path: '/admin/dashboard', title: '管理后台', icon: markRaw(Setting) })
    items.push({ path: '/statistics', title: '数据大盘', icon: markRaw(PieChart) })
  }

  if (role.value === 'NEPV') {
    // 决策者：只看统计数据大屏
    items.push({ path: '/statistics', title: '数据大屏', icon: markRaw(PieChart) })
  }

  // 所有角色都能访问的公共服务
  items.push({ path: '/news', title: '环保资讯', icon: markRaw(Reading) })
  items.push({ path: '/knowledge', title: '环保知识库', icon: markRaw(Collection) })
  items.push({ path: '/notifications', title: '通知中心', icon: markRaw(Notification) })
  items.push({ path: '/ai', title: 'AI 助手', icon: markRaw(ChatDotRound) })

  return items
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出 NEP 环保系统吗？', '退出确认', {
    confirmButtonText: '退出登录',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'swiss-message-box'
  }).then(() => {
    if (unreadTimer) clearInterval(unreadTimer)
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  userStore.fetchUser()
  fetchUnreadCount()
  // 每30秒轮询未读通知数
  unreadTimer = setInterval(fetchUnreadCount, 30000)
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
  position: absolute; top: 4px; right: 4px; min-width: 16px; height: 16px; padding: 0 4px;
  background-color: #D9534F; border-radius: 10px; border: 1.5px solid #FFF;
  color: #FFF; font-size: 10px; font-weight: 600; line-height: 14px; text-align: center;
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

/* ========== 全局搜索面板（Command Palette 风格） ========== */
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  display: flex;
  justify-content: center;
  padding-top: 16vh;
}

.search-panel {
  width: 560px;
  max-height: 480px;
  align-self: flex-start;
  border-radius: 16px;
  overflow: hidden;
  animation: searchIn 0.2s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes searchIn {
  from { opacity: 0; transform: translateY(-12px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.glass-panel-strong {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(30px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(0, 0, 0, 0.06);
}

/* --- 输入行 --- */
.search-input-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.search-icon {
  font-size: 18px;
  color: #86868B;
  flex-shrink: 0;
}

.search-field {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  font-weight: 500;
  color: #1C2421;
  background: transparent;
  font-family: inherit;
}

.search-field::placeholder {
  color: #A0AAB2;
}

.search-clear {
  width: 28px; height: 28px;
  border: none; background: rgba(0,0,0,0.06);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #86868B; font-size: 14px;
  transition: background 0.2s;
}

.search-clear:hover { background: rgba(0,0,0,0.12); }

.search-close {
  padding: 4px 10px; border: 1px solid rgba(0,0,0,0.12);
  background: transparent; border-radius: 6px;
  font-size: 11px; font-weight: 600; color: #86868B;
  cursor: pointer; letter-spacing: 0.5px;
  transition: all 0.2s;
}

.search-close:hover { background: rgba(0,0,0,0.06); }

/* --- 搜索结果列表 --- */
.search-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 12px;
}

.search-result-summary {
  padding: 12px 0 8px;
  font-size: 11px;
  font-weight: 600;
  color: #86868B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  margin-bottom: 4px;
}

.search-result-item {
  padding: 14px 0;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.2s;
}

.search-result-item:last-child { border-bottom: none; }

.search-result-item:hover {
  padding-left: 10px;
  padding-right: 4px;
  background: rgba(64, 158, 255, 0.04);
  border-radius: 10px;
  border-bottom-color: transparent;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.result-type-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.result-type-badge.news {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.result-type-badge.knowledge {
  background: rgba(42, 168, 118, 0.1);
  color: #2AA876;
}

.result-date {
  font-size: 11px;
  color: #A0AAB2;
}

.result-title {
  font-size: 14px;
  font-weight: 600;
  color: #1C2421;
  line-height: 1.4;
  margin-bottom: 4px;
}

.result-title :deep(.search-highlight) {
  background: #FFF3CD;
  color: #856404;
  padding: 1px 3px;
  border-radius: 2px;
  font-weight: 700;
}

.result-summary {
  font-size: 12px;
  color: #86868B;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* --- 搜索状态框（搜索中 / 无结果） --- */
.search-status-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 20px;
  color: #86868B;
  font-size: 14px;
}

.search-status-box .is-loading {
  animation: rotate 1s linear infinite;
  color: #409EFF;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.no-results-title {
  font-size: 15px;
  font-weight: 600;
  color: #1C2421;
}

.no-results-hint {
  font-size: 12px;
  color: #86868B;
}

/* --- 初始提示（快速搜索标签） --- */
.search-hints {
  padding: 20px;
}

.hint-label {
  font-size: 12px;
  font-weight: 600;
  color: #86868B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.hint-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hint-tag {
  display: inline-block;
  padding: 6px 14px;
  background: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.hint-tag:hover {
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.3);
  color: #409EFF;
}
</style>