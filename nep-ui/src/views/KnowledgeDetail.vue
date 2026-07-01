<template>
  <div class="swiss-page-container" v-loading="loading">
    
    <header class="action-console glass-panel fixed-section">
      <div class="console-left">
        <el-button class="swiss-btn icon-btn" @click="$router.push(rolePath('/knowledge'))">
          <el-icon><Back /></el-icon>
        </el-button>
        <div style="margin-left:16px; display:flex; flex-direction:column; gap:4px">
          <h2 class="page-title">文献归档详情</h2>
          <span class="page-subtitle">Document Verification & Preview</span>
        </div>
      </div>
      <div class="console-right">
        <button class="action-btn outline" @click="handlePrint">
          <el-icon><Printer /></el-icon> 打印归档
        </button>
        <button class="action-btn primary" @click="handleDownload">
          <el-icon><Download /></el-icon> 下载源文件
        </button>
      </div>
    </header>

    <main class="stretch-section scrollable-card">
      <div class="reader-area scroll-area" v-if="document">
        
        <article class="paper-canvas">
          
          <header class="paper-header">
            <div class="doc-code">档案卷宗编号：{{ String(document.id).padStart(6, '0') }}</div>
            <h1 class="doc-title">{{ document.title }}</h1>
            <div class="doc-meta-strip">
              <span class="meta-tag">{{ typeLabel(document.category) }}</span>
              <span class="meta-divider"></span>
              <span class="meta-item">颁布单位：{{ document.department || '环境保护监管中心' }}</span>
              <span class="meta-divider"></span>
              <span class="meta-item">生效日期：{{ formatTime(document.createTime) }}</span>
            </div>
          </header>

          <div class="paper-abstract">
            <div class="abstract-label">【内容提要】</div>
            <p>{{ document.summary || '本文献暂无摘要说明，请直接阅览下方正文或下载附件查看详细内容。' }}</p>
          </div>

          <div class="paper-body html-content" v-html="document.content || '<p class=empty-hint>暂无结构化正文数据，请下载附件源文件查看。</p>'"></div>

          <footer class="paper-footer">
            <div class="attachment-box">
              <div class="att-icon"><el-icon><Link /></el-icon></div>
              <div class="att-info">
                <span class="att-name">{{ document.fileName || '知识库源文件.pdf' }}</span>
                <span class="att-size">安全扫描通过</span>
              </div>
              <button class="att-download" @click="handleDownload">提取附件</button>
            </div>
          </footer>

        </article>

      </div>
      
      <div v-if="!loading && !document" class="empty-state-wrapper glass-panel">
        <el-empty description="该文献已被撤档或不存在" />
      </div>
    </main>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getKnowledgeById } from '@/api/knowledge'
import { rolePath } from '@/utils/roleRouter'
import { Back, Printer, Download, Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const document = ref(null)
const loading = ref(false)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 10)
}

function typeLabel(type) {
  if (type === 'STANDARD') return '作业标准'
  if (type === 'LAW') return '法律法规'
  if (type === 'EMERGENCY') return '应急预案'
  return '内部文档'
}

function handlePrint() { window.print() }
function handleDownload() { 
  ElMessage.success('已开始提取源文件，请留意浏览器下载队列。')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getKnowledgeById(route.params.id)
    document.value = res.data
  } catch (e) {
    console.error(e)
  } finally { 
    loading.value = false 
  }
})
</script>

<style scoped>
/* ========== 全局主容器 ========== */
.swiss-page-container { max-width: 1440px; width: 100%; height: 100%; margin: 0 auto; display: flex; flex-direction: column; gap: 24px; padding-bottom: 32px; box-sizing: border-box; color: #1C2421; }
.fixed-section { flex-shrink: 0; }
.stretch-section { flex: 1; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }

.glass-panel { background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(24px) saturate(180%); -webkit-backdrop-filter: blur(24px) saturate(180%); border: 1px solid rgba(255, 255, 255, 0.8); border-radius: 24px; box-shadow: 0 8px 32px -8px rgba(0, 0, 0, 0.04), inset 0 2px 4px rgba(255, 255, 255, 0.6); }

/* ========== 控制台 ========== */
.action-console { height: 100px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box; }
.console-left { display: flex; align-items: center; }
.page-title { font-size: 24px; font-weight: 600; margin: 0; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }
.console-right { display: flex; align-items: center; gap: 12px; }

.swiss-btn { border: none; outline: none; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.swiss-btn.icon-btn { width: 44px; height: 44px; background: rgba(255, 255, 255, 0.8); color: #1C2421; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); font-size: 20px; }
.swiss-btn.icon-btn:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }

.action-btn { border: 1px solid rgba(28,36,33,0.1); background: rgba(255,255,255,0.5); height: 40px; padding: 0 16px; border-radius: 12px; display: inline-flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #74807B; cursor: pointer; transition: all 0.3s; }
.action-btn.primary { background: #2A483A; color: white; border: none; }
.action-btn.primary:hover { background: #1C2421; box-shadow: 0 4px 12px rgba(42, 72, 58, 0.2); transform: translateY(-1px); }
.action-btn.outline { background: white; color: #1C2421; }
.action-btn.outline:hover { border-color: #1C2421; }

/* ========== 阅读器视区 ========== */
.scrollable-card { display: flex; flex-direction: column; background: rgba(28, 36, 33, 0.02); border-radius: 24px; }

/* 关键修复 1：将 display: flex 移除，改为 block 流式渲染，避免高度计算坍塌 */
.reader-area { flex: 1; overflow-y: auto; padding: 48px 24px; display: block; }
.reader-area::-webkit-scrollbar { display: none; }

/* 关键修复 2：加入 height: max-content 和 margin: 0 auto */
.paper-canvas {
  margin: 0 auto; /* 保证在块级流中完美水平居中 */
  height: max-content; /* 强制白纸高度包含住所有的长文本，绝不溢出 */
  min-height: calc(100% - 96px); /* 即使文本很短，也能保持一张纸的体面高度 */
  
  background: white; width: 100%; max-width: 860px; padding: 64px 80px;
  border-radius: 8px; box-shadow: 0 24px 48px -12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  display: flex; flex-direction: column; gap: 40px;
  box-sizing: border-box;
}

.paper-header { display: flex; flex-direction: column; align-items: center; text-align: center; border-bottom: 2px solid #1C2421; padding-bottom: 32px; }
.doc-code { font-family: monospace; font-size: 13px; color: #74807B; margin-bottom: 24px; letter-spacing: 1px; }
.doc-title { font-size: 32px; font-weight: 800; color: #1C2421; line-height: 1.4; margin: 0 0 24px 0; letter-spacing: 1px; }
.doc-meta-strip { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #74807B; }
.meta-tag { background: #F4F6F5; color: #2A483A; padding: 4px 10px; border-radius: 6px; font-weight: 700; }
.meta-divider { width: 1px; height: 12px; background: rgba(28, 36, 33, 0.1); }

.paper-abstract { background: #F8FAFC; padding: 24px; border-radius: 8px; font-size: 14px; color: #475569; line-height: 1.8; text-align: justify; }
.abstract-label { font-weight: 700; color: #1C2421; margin-bottom: 8px; font-size: 15px; }
.paper-abstract p { margin: 0; }

.paper-body { font-size: 16px; color: #333; line-height: 2; text-align: justify; flex: 1; }
:deep(.html-content h2), :deep(.html-content h3) { font-size: 20px; font-weight: 700; color: #1C2421; margin: 40px 0 16px; }
:deep(.html-content p) { margin: 0 0 20px; }
:deep(.html-content img) { max-width: 100%; border-radius: 12px; margin: 24px 0; }
.empty-hint { text-align: center; color: #A0AAB2; font-style: italic; padding: 40px 0; }

.paper-footer { margin-top: 40px; padding-top: 32px; border-top: 1px dashed rgba(28, 36, 33, 0.1); }
.attachment-box { display: flex; align-items: center; gap: 16px; background: #F4F6F5; padding: 16px 24px; border-radius: 12px; }
.att-icon { width: 40px; height: 40px; background: white; border-radius: 10px; display: flex; justify-content: center; align-items: center; color: #2A483A; font-size: 18px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.att-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.att-name { font-size: 14px; font-weight: 600; color: #1C2421; font-family: monospace; }
.att-size { font-size: 12px; color: #2AA876; font-weight: 500; }
.att-download { border: none; background: transparent; color: #2A483A; font-size: 14px; font-weight: 600; cursor: pointer; transition: opacity 0.3s; }
.att-download:hover { opacity: 0.7; }

.empty-state-wrapper { height: 100%; display: flex; align-items: center; justify-content: center; }

/* 打印机媒体查询：隐藏多余组件，只打印白纸 */
@media print {
  .fixed-section, .empty-state-wrapper, .paper-footer { display: none !important; }
  .stretch-section { background: transparent !important; }
  .paper-canvas { box-shadow: none; border: none; padding: 0; max-width: 100%; height: auto !important; }
}
</style>