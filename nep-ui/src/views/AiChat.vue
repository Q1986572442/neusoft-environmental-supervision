<template>
  <div class="swiss-dashboard">
    <section class="action-console glass-panel fixed-section">
      <div class="console-left">
        <button class="icon-nav-btn" @click="$router.push('/home')" title="返回概览">
          <el-icon><Back /></el-icon>
        </button>
        <div class="title-group">
          <h2 class="page-title">NEP 智能解析中枢</h2>
          <span class="page-subtitle">基于 DeepSeek 引擎的私有化环保决策大脑</span>
        </div>
      </div>
      <div class="console-right">
        <button class="swiss-btn ghost-btn" @click="clearChat" :disabled="streaming">
          <el-icon><Delete /></el-icon> 重置神经元
        </button>
      </div>
    </section>

    <section class="content-split-grid stretch-section chat-split">
      
      <aside class="chat-sidebar glass-panel scrollable-card">
        <div class="panel-header">
          <h3 class="panel-title"><el-icon><Cpu /></el-icon> 中枢控制台</h3>
        </div>
        <div class="sidebar-body scroll-area">
          
          <div class="sidebar-section">
            <div class="section-title"><el-icon><Lightning /></el-icon> 高频预设指令</div>
            <div class="prompt-list">
              <div class="prompt-item" v-for="p in quickPrompts" :key="p" @click="sendSuggestion(p)">
                {{ p }}
              </div>
            </div>
          </div>
          
          <div class="sidebar-divider"></div>
          
          <div class="sidebar-section">
            <div class="section-title"><el-icon><Clock /></el-icon> 会话快照记录</div>
            <div class="history-list">
              <div class="history-item" v-for="(h, i) in history" :key="i" :class="{ active: i === history.length - 1 }">
                <div class="h-dot"></div>
                <span>{{ h.title }}</span>
              </div>
              <el-empty v-if="history.length === 0" description="暂无历史快照" :image-size="40" />
            </div>
          </div>

        </div>
      </aside>

      <main class="chat-workspace glass-panel scrollable-card">
        
        <div class="workspace-header panel-header">
          <div class="ai-identity">
            <div class="ai-orb-mini" :class="{ 'is-thinking': waiting, 'is-speaking': streaming }"></div>
            <div class="ai-info">
              <span class="ai-name">DeepSeek Engine</span>
              <span class="ai-status">{{ statusText }}</span>
            </div>
          </div>
        </div>

        <div class="chat-scroll-viewport scroll-area" ref="msgBox">
          
          <div v-if="messages.length === 0" class="empty-state">
            <div class="ai-orb-hero">
              <div class="orb-core"></div>
              <div class="orb-ring ring-1"></div>
              <div class="orb-ring ring-2"></div>
            </div>
            <h2 class="hero-title">如何为您效劳？</h2>
            <p class="hero-subtitle">NEP 智能中枢已就绪，随时为您解析多维环境数据与法规。</p>
            <div class="suggestion-bento">
              <div class="bento-item" v-for="s in suggestions" :key="s.title" @click="sendSuggestion(s.prompt)">
                <el-icon class="bento-icon"><component :is="s.icon"/></el-icon>
                <div class="bento-text">
                  <span class="b-title">{{ s.title }}</span>
                  <span class="b-desc">{{ s.desc }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="message-list" v-else>
            <div v-for="(msg, idx) in messages" :key="idx" class="msg-wrapper">
              
              <div v-if="msg.role === 'user'" class="msg-row user-row">
                <div class="msg-actions"></div>
                <div class="msg-bubble user-bubble">
                  <div class="msg-text">{{ msg.content }}</div>
                </div>
                <div class="user-avatar-mini"><el-icon><User /></el-icon></div>
              </div>

              <div v-else class="msg-row ai-row">
                <div class="ai-avatar-mini"><div class="orb-mini"></div></div>
                <div class="msg-bubble ai-bubble">
                  <div class="msg-text" v-html="renderContent(msg.content)"></div>
                  <span v-if="idx === messages.length-1 && streaming" class="typing-cursor"></span>
                </div>
                <div v-if="msg.content && !streaming" class="msg-actions">
                  <button class="action-icon-btn" @click="copyText(msg.content)" title="拷贝入板">
                    <el-icon><CopyDocument /></el-icon>
                  </button>
                </div>
              </div>

            </div>

            <div v-if="waiting" class="msg-row ai-row">
              <div class="ai-avatar-mini"><div class="orb-mini"></div></div>
              <div class="msg-bubble ai-bubble thinking-bubble">
                <div class="typing-dots"><span></span><span></span><span></span></div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input-dock">
          <div class="input-capsule">
            <el-input
              ref="inputRef"
              v-model="input"
              :disabled="streaming"
              placeholder="输入环境分析指令，Enter 发送，Shift+Enter 换行"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 6 }"
              class="mac-chat-input"
              @keydown.enter.exact.prevent="handleEnter"
            />
            <button class="mac-send-btn" :disabled="!input.trim() || streaming" @click="send">
              <el-icon v-if="!streaming"><Promotion /></el-icon>
              <el-icon v-else><VideoPause /></el-icon>
            </button>
          </div>
          <div class="input-hint">
            <span>智能中枢可能产生不可预期的推演结果，请核实关键环保数据。</span>
            <span>{{ input.length }} / 2000</span>
          </div>
        </div>
      </main>
      
    </section>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { chat, chatStream } from '@/api/ai'
import { ElMessage } from 'element-plus'
import {
  Back, Delete, Promotion, VideoPause, CopyDocument,
  Lightning, Clock, DataAnalysis, Warning, Monitor, TrendCharts,
  Cpu, User
} from '@element-plus/icons-vue'

const input = ref('')
const streaming = ref(false)
const waiting = ref(false)
const messages = ref([])
const msgBox = ref(null)
const inputRef = ref(null)
const history = ref([])

// OS 级组件欢迎语设计
const suggestions = [
  { icon: DataAnalysis, title: 'AQI指数解析', desc: '空气质量指数是如何计算的？', prompt: '请详细解释AQI空气质量指数的计算方法与对应危害' },
  { icon: Warning, title: '微观颗粒物追踪', desc: '深度解析 PM2.5 对人体的损害', prompt: 'PM2.5对人体健康具体有哪些危害？如何在重度污染下进行防护？' },
  { icon: Monitor, title: '公众环保赋能', desc: '普通人如何参与环保监督网络？', prompt: '作为公众，我们如何有效参与到城市的环保监督网络中？' },
  { icon: TrendCharts, title: '宏观趋势推演', desc: '推演近年全国空气质量走向', prompt: '请分析中国近年来空气质量改善的宏观数据情况，并推演未来趋势' },
]

const quickPrompts = [
  '详述空气质量指数(AQI)的分级标准',
  '二氧化硫(SO₂)的主要工业来源是什么？',
  '一氧化碳(CO)超标时的应急预案',
  '系统网格员的主要调度职责是什么？'
]

// 智能状态文本
const statusText = computed(() => {
  if (streaming.value) return '神经网络正在解析与渲染...'
  if (waiting.value) return '正在连接中枢节点...'
  return '在线 · DeepSeek Engine'
})

// Markdown 深度解析渲染
function renderContent(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
    .replace(/(\d+)\.\s/g, '<br>$1. ')
  return html
}

async function sendSuggestion(prompt) {
  input.value = prompt
  await send()
}

async function handleEnter() {
  if (!streaming.value && input.value.trim()) {
    await send()
  }
}

async function send() {
  const msg = input.value.trim()
  if (!msg || streaming.value) return

  input.value = ''
  waiting.value = true
  messages.value.push({ role: 'user', content: msg })
  await scrollToBottom()

  try {
    streaming.value = true
    waiting.value = false
    messages.value.push({ role: 'ai', content: '' })
    await scrollToBottom()

    const response = await chatStream(msg)
    if (!response.ok) throw new Error('Stream failed')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const data = line.substring(5).trim()
        if (data === '[DONE]') break
        if (data.startsWith('[ERROR]')) {
          messages.value[messages.value.length - 1].content = '中枢服务请求中断：' + data.substring(7)
          break
        }
        if (data) {
          messages.value[messages.value.length - 1].content += data
          await scrollToBottom()
        }
      }
    }
  } catch (e) {
    try {
      const res = await chat(msg)
      messages.value[messages.value.length - 1].content = res.data || '中枢节点网络波动，暂不可用。'
    } catch (e2) {
      messages.value[messages.value.length - 1].content = '中枢服务请求失败，请检查加密链路配置。'
    }
  } finally {
    streaming.value = false
    waiting.value = false
    saveHistory()
    await scrollToBottom()
    nextTick(() => inputRef.value?.focus())
  }
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success({ message: '解析数据已拷贝至剪贴板', customClass: 'swiss-message' })
  }).catch(() => {
    ElMessage.info({ message: '拦截失败，请手动选择', customClass: 'swiss-message' })
  })
}

function clearChat() {
  messages.value = []
  ElMessage.success({ message: '神经元历史记录已重置', customClass: 'swiss-message' })
}

function saveHistory() {
  const firstUserMsg = messages.value.find(m => m.role === 'user')
  if (firstUserMsg) {
    history.value.push({
      title: firstUserMsg.content.substring(0, 20) + (firstUserMsg.content.length > 20 ? '...' : '')
    })
  }
}

async function scrollToBottom() {
  await nextTick()
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}

watch(messages, () => scrollToBottom(), { deep: true })

onMounted(() => {
  nextTick(() => inputRef.value?.focus())
})
</script>

<style scoped>
/* =========================================
   全局空间调度 (继承系统主框架规范)
   ========================================= */
.swiss-dashboard {
  max-width: 1440px; width: 100%; height: 100%; 
  margin: 0 auto; display: flex; flex-direction: column; gap: 24px;
  padding-bottom: 32px; box-sizing: border-box; color: #1C2421;
}

.fixed-section { flex-shrink: 0; }
.stretch-section { flex: 1; min-height: 0; }

.glass-panel {
  background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6);
}

.scrollable-card { display: flex; flex-direction: column; overflow: hidden; height: 100%; }
.panel-header {
  flex-shrink: 0; display: flex; justify-content: space-between; align-items: center;
  padding: 24px 28px 16px; border-bottom: 1px solid rgba(28, 36, 33, 0.06);
}
.panel-title { font-size: 16px; font-weight: 600; color: #1C2421; margin: 0; display: flex; align-items: center; gap: 8px; }
.panel-title .el-icon { color: #2A483A; font-size: 18px; }

.scroll-area { flex: 1; overflow-y: auto; scrollbar-width: none; -ms-overflow-style: none; }
.scroll-area::-webkit-scrollbar { display: none; }

/* =========================================
   1. 悬浮控制台 (严苛对齐，固定 120px)
   ========================================= */
.action-console {
  height: 120px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box;
}

.console-left { display: flex; align-items: center; gap: 20px; }
.icon-nav-btn {
  width: 44px; height: 44px; border-radius: 50%; border: 1px solid rgba(255,255,255,0.9);
  background: rgba(255, 255, 255, 0.5); color: #1C2421; font-size: 20px;
  display: flex; justify-content: center; align-items: center; cursor: pointer; transition: all 0.3s; box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.icon-nav-btn:hover { background: #FFF; transform: scale(1.05); }

.title-group { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 500; margin: 0; letter-spacing: 0.5px; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }

.console-right { display: flex; align-items: center; gap: 16px; }

.swiss-btn { border: none; outline: none; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px; font-weight: 600; transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1); }
.swiss-btn.ghost-btn { padding: 0 20px; height: 40px; background: rgba(255, 255, 255, 0.6); color: #1C2421; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.swiss-btn.ghost-btn:hover:not(:disabled) { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.swiss-btn.ghost-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* =========================================
   2. 主从分栏结构 (侧边栏 280px，主视窗 1fr)
   ========================================= */
.chat-split { display: grid; grid-template-columns: 280px 1fr; gap: 24px; align-items: stretch; }

/* === 左栏：侧边栏 === */
.chat-sidebar { background: rgba(255, 255, 255, 0.55); }
.sidebar-body { padding: 20px 24px; }
.section-title { font-size: 12px; font-weight: 700; color: #86868B; text-transform: uppercase; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; letter-spacing: 0.5px;}

.prompt-list { display: flex; flex-direction: column; gap: 8px; }
.prompt-item {
  font-size: 13px; font-weight: 500; color: #1C2421; background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8); padding: 10px 14px; border-radius: 12px;
  cursor: pointer; transition: all 0.2s; line-height: 1.4; box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}
.prompt-item:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); color: #2A483A; }

.sidebar-divider { height: 1px; background: rgba(0,0,0,0.05); margin: 24px 0; }

.history-list { display: flex; flex-direction: column; gap: 4px; }
.history-item {
  display: flex; align-items: center; gap: 10px; font-size: 13px; color: #515154; font-weight: 500;
  padding: 8px 12px; border-radius: 10px; cursor: pointer; transition: all 0.2s;
}
.history-item:hover { background: rgba(0,0,0,0.03); color: #1C2421; }
.history-item.active { background: rgba(42, 72, 58, 0.08); color: #2A483A; font-weight: 600; }
.h-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; opacity: 0.5; }
.history-item span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* === 右栏：主交互区 === */
.chat-workspace { display: flex; flex-direction: column; }

/* 头部状态指示 */
.workspace-header { padding: 16px 28px; }
.ai-identity { display: flex; align-items: center; gap: 12px; }
.ai-orb-mini {
  width: 32px; height: 32px; border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #409EFF, #AF52DE, #5E5CE6);
  box-shadow: 0 2px 12px rgba(175, 82, 222, 0.4); transition: all 0.4s;
}
.ai-orb-mini.is-thinking { animation: orb-pulse 1s infinite alternate; }
.ai-orb-mini.is-speaking { animation: orb-spin 3s linear infinite; background: radial-gradient(circle at 30% 30%, #34C759, #409EFF); box-shadow: 0 2px 12px rgba(52, 199, 89, 0.4); }
@keyframes orb-pulse { 0% { transform: scale(0.9); opacity: 0.8; } 100% { transform: scale(1.1); opacity: 1; box-shadow: 0 4px 16px rgba(175, 82, 222, 0.6); } }
@keyframes orb-spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.ai-info { display: flex; flex-direction: column; }
.ai-name { font-size: 15px; font-weight: 700; color: #1C2421; }
.ai-status { font-size: 11px; font-weight: 600; color: #86868B; }

/* 聊天滚动区 */
.chat-scroll-viewport { padding: 24px 32px; }

/* 空状态：悬浮呼吸球与便当盒 */
.empty-state { text-align: center; padding: 40px 0; display: flex; flex-direction: column; align-items: center;}
.ai-orb-hero {
  position: relative; width: 100px; height: 100px; margin-bottom: 24px;
  display: flex; justify-content: center; align-items: center;
}
.orb-core {
  width: 56px; height: 56px; border-radius: 50%; z-index: 2;
  background: radial-gradient(circle at 30% 30%, #409EFF, #AF52DE);
  box-shadow: 0 0 40px rgba(175, 82, 222, 0.6);
  animation: core-pulse 4s ease-in-out infinite alternate;
}
.orb-ring { position: absolute; border-radius: 50%; border: 1px solid rgba(175, 82, 222, 0.3); z-index: 1; }
.ring-1 { width: 80px; height: 80px; animation: ring-spin 8s linear infinite; border-left-color: #409EFF; }
.ring-2 { width: 100px; height: 100px; animation: ring-spin-reverse 12s linear infinite; border-top-color: #AF52DE; }
@keyframes core-pulse { 0% { transform: scale(0.9); filter: blur(2px); } 100% { transform: scale(1.1); filter: blur(6px); } }
@keyframes ring-spin { 100% { transform: rotate(360deg); } }
@keyframes ring-spin-reverse { 100% { transform: rotate(-360deg); } }

.hero-title { font-size: 24px; font-weight: 600; color: #1C2421; margin: 0 0 8px 0; }
.hero-subtitle { font-size: 14px; color: #74807B; margin-bottom: 40px; }

.suggestion-bento { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; width: 100%; max-width: 600px; }
.bento-item {
  background: rgba(255, 255, 255, 0.4); border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 20px; border-radius: 20px; text-align: left; cursor: pointer; transition: all 0.3s;
  display: flex; flex-direction: column; gap: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}
.bento-item:hover { background: #FFF; transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.bento-icon { font-size: 22px; color: #2A483A; }
.b-title { font-size: 14px; font-weight: 600; color: #1C2421; }
.b-desc { font-size: 12px; color: #74807B; line-height: 1.4; }

/* 聊天气泡体系 */
.message-list { display: flex; flex-direction: column; gap: 24px; padding-bottom: 24px; }
.msg-row { display: flex; gap: 16px; align-items: flex-end; }
.user-row { justify-content: flex-end; }
.ai-row { justify-content: flex-start; }

.user-avatar-mini {
  width: 32px; height: 32px; border-radius: 50%; background: rgba(42, 72, 58, 0.1); color: #2A483A;
  display: flex; justify-content: center; align-items: center; font-size: 16px; flex-shrink: 0;
}
.ai-avatar-mini {
  width: 32px; height: 32px; border-radius: 50%; background: transparent;
  display: flex; justify-content: center; align-items: center; flex-shrink: 0;
}
.orb-mini { width: 24px; height: 24px; border-radius: 50%; background: radial-gradient(circle at 30% 30%, #409EFF, #AF52DE); box-shadow: 0 2px 8px rgba(175, 82, 222, 0.4); }

.msg-bubble { max-width: 75%; padding: 14px 20px; font-size: 14px; line-height: 1.6; }
.user-bubble { background: #1C2421; color: #FFF; border-radius: 20px 20px 4px 20px; box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.ai-bubble { background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(255, 255, 255, 0.9); color: #1D1D1F; border-radius: 20px 20px 20px 4px; box-shadow: 0 4px 16px rgba(0,0,0,0.03); }

/* Markdown 深度美化 */
.ai-bubble :deep(strong) { color: #2A483A; font-weight: 600; }
.ai-bubble :deep(code) { background: rgba(28, 36, 33, 0.05); padding: 2px 6px; border-radius: 6px; font-family: "SF Mono", Consolas, monospace; font-size: 13px; }
.ai-bubble :deep(pre) { background: #1C2421; color: #F4F6F5; padding: 16px; border-radius: 12px; overflow-x: auto; margin: 12px 0; }
.ai-bubble :deep(pre code) { background: none; color: inherit; padding: 0; }

.typing-cursor { display: inline-block; width: 6px; height: 14px; background: #007AFF; vertical-align: middle; margin-left: 4px; animation: cursor-blink 0.8s infinite; }
@keyframes cursor-blink { 0%,100%{opacity:1} 50%{opacity:0} }

.msg-actions { opacity: 0; transition: opacity 0.2s; padding-bottom: 4px; }
.msg-row:hover .msg-actions { opacity: 1; }
.action-icon-btn { background: transparent; border: none; color: #A0AAB2; font-size: 16px; cursor: pointer; transition: color 0.2s; }
.action-icon-btn:hover { color: #1C2421; }

.typing-dots { display: flex; gap: 4px; padding: 4px 2px; }
.typing-dots span { width: 6px; height: 6px; border-radius: 50%; background: #A0AAB2; animation: dot-bounce 1.4s infinite; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-bounce { 0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; } 40% { transform: scale(1); opacity: 1; } }

/* === 沉浸式底部输入胶囊 === */
.chat-input-dock { padding: 16px 32px 24px; background: transparent; flex-shrink: 0; }
.input-capsule {
  display: flex; align-items: flex-end; gap: 12px;
  background: rgba(255, 255, 255, 0.75); backdrop-filter: blur(40px) saturate(200%); -webkit-backdrop-filter: blur(40px) saturate(200%);
  border: 1px solid rgba(255, 255, 255, 0.9); padding: 10px 10px 10px 24px;
  border-radius: 24px; box-shadow: 0 12px 32px -8px rgba(0,0,0,0.08); transition: box-shadow 0.3s;
}
.input-capsule:focus-within { box-shadow: 0 16px 48px -8px rgba(0,0,0,0.12), inset 0 0 0 1px rgba(42, 72, 58, 0.1); }

.mac-chat-input :deep(.el-textarea__inner) {
  background: transparent !important; box-shadow: none !important; border: none !important;
  padding: 8px 0; font-size: 15px; color: #1D1D1F; line-height: 1.5; resize: none;
}

.mac-send-btn {
  width: 40px; height: 40px; border-radius: 50%; background: #1C2421; color: #FFF;
  display: flex; justify-content: center; align-items: center; border: none; cursor: pointer; transition: all 0.2s; flex-shrink: 0; font-size: 18px;
}
.mac-send-btn:hover:not(:disabled) { background: #2A483A; transform: scale(1.05); }
.mac-send-btn:disabled { background: rgba(28, 36, 33, 0.1); color: rgba(28, 36, 33, 0.3); cursor: not-allowed; }

.input-hint { display: flex; justify-content: space-between; margin-top: 12px; font-size: 11px; font-weight: 500; color: #A0AAB2; padding: 0 16px; }

@media (max-width: 1024px) {
  .chat-split { grid-template-columns: 1fr; }
  .chat-sidebar { display: none; }
  .action-console { padding: 0 24px; }
}
</style>

<style>
/* 全局消息弹窗覆盖 */
.swiss-message { background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 12px !important; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important; color: #1C2421 !important; font-weight: 600 !important; }
</style>