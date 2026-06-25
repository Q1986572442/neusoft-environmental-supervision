<template>
  <div class="notification-page">
    <div class="page-header glass-panel">
      <div class="header-left">
        <h2 class="page-title">🔔 通知中心</h2>
        <p class="page-subtitle">查看系统通知、反馈进度提醒</p>
      </div>
      <div class="header-right">
        <el-button type="primary" plain size="small" @click="handleReadAll" :disabled="unreadCount === 0">
          全部已读 ({{ unreadCount }})
        </el-button>
      </div>
    </div>

    <div class="notification-list" v-loading="loading">
      <div
        class="notif-item glass-panel"
        v-for="item in notifications"
        :key="item.id"
        :class="{ unread: item.isRead === 0 }"
        @click="handleRead(item)"
      >
        <div class="notif-icon">
          <el-icon :size="20">
            <Message v-if="item.type === 'SYSTEM'" />
            <Bell v-else-if="item.type === 'FEEDBACK'" />
            <Warning v-else-if="item.type === 'ASSIGN'" />
            <Document v-else />
          </el-icon>
        </div>
        <div class="notif-content">
          <div class="notif-title">
            {{ item.title }}
            <span v-if="item.isRead === 0" class="unread-dot"></span>
          </div>
          <div class="notif-text">{{ item.content || '' }}</div>
          <div class="notif-time">{{ formatTime(item.createTime) }}</div>
        </div>
        <div class="notif-action">
          <el-button size="small" text type="danger" @click.stop="handleDelete(item.id)">删除</el-button>
        </div>
      </div>
      <div v-if="!loading && notifications.length === 0" class="empty-guide">
        <el-icon :size="48"><Bell /></el-icon>
        <p>暂无通知消息</p>
        <span class="sub">当您的反馈有新的处理进度，或系统有重要公告时，会在这里通知您</span>
        <el-button type="primary" size="small" style="margin-top:16px" @click="$router.push(rolePath('/submit'))">去提交反馈</el-button>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" @current-change="fetchData" background />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNotificationPage, getUnreadCount, readNotification, readAllNotifications, deleteNotification } from '@/api/notification'
import { rolePath } from '@/utils/roleRouter'
import { ElMessage, ElMessageBox } from 'element-plus'

const notifications = ref([])
const loading = ref(false)
const unreadCount = ref(0)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const userId = ref(Number(localStorage.getItem('userId') || 0))

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function fetchData() {
  loading.value = true
  try {
    const [notifRes, countRes] = await Promise.all([
      getNotificationPage(page.value, size.value, userId.value),
      getUnreadCount(userId.value)
    ])
    notifications.value = notifRes.data || []
    total.value = notifRes.total || 0
    unreadCount.value = countRes.data?.unreadCount || 0
  } catch (e) {} finally { loading.value = false }
}

async function handleRead(item) {
  if (item.isRead === 0) {
    try {
      await readNotification(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {}
  }
}

async function handleReadAll() {
  try {
    await readAllNotifications(userId.value)
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该通知？', '提示', { type: 'warning' })
    await deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    ElMessage.success('已删除')
  } catch (e) {}
}

onMounted(fetchData)
</script>

<style scoped>
.notification-page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; padding: 32px; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 600; margin: 0 0 8px; color: #1C2421; }
.page-subtitle { font-size: 14px; color: #74807B; margin: 0; }
.notification-list { display: flex; flex-direction: column; gap: 12px; }
.notif-item { display: flex; align-items: center; gap: 16px; padding: 20px 24px; border-radius: 16px; cursor: pointer; transition: all 0.3s; }
.notif-item:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.notif-item.unread { border-left: 3px solid #409EFF; background: rgba(64,158,255,0.03); }
.notif-icon { width: 44px; height: 44px; border-radius: 12px; background: rgba(28,36,33,0.04); display: flex; align-items: center; justify-content: center; color: #2A483A; flex-shrink: 0; }
.notif-content { flex: 1; min-width: 0; }
.notif-title { font-size: 15px; font-weight: 600; color: #1C2421; margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: #409EFF; flex-shrink: 0; }
.notif-text { font-size: 13px; color: #74807B; margin-bottom: 6px; }
.notif-time { font-size: 12px; color: #A0AAB2; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
.glass-panel { background: rgba(255,255,255,0.6); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); box-shadow: 0 8px 32px -8px rgba(0,0,0,0.04); }

.empty-guide { display:flex; flex-direction:column; align-items:center; padding:48px 20px; color:#999; }
.empty-guide p { margin:12px 0 4px; font-size:14px; font-weight:500; color:#666; }
.empty-guide .sub { font-size:12px; color:#aaa; text-align:center; max-width:280px; }
.empty-guide .el-icon { color:#ccc; }
</style>
