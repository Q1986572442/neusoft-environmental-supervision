<template>
  <div class="mac-library-container">
    
    <div class="control-island">
      <div class="island-brand">
        <div class="brand-icon"><el-icon><Collection /></el-icon></div>
        <div class="brand-text">
          <h2>数字文献检索室</h2>
          <p>管理与发布操作手册、监测指南与制度规范</p>
        </div>
      </div>
      <div class="island-actions">
        <button class="apple-btn primary-btn" @click="showDialog(null)">
          <el-icon><DocumentAdd /></el-icon> 归档新文献
        </button>
      </div>
    </div>

    <div class="data-dome">
      <el-table 
        :data="knowledgeList" 
        v-loading="loading" 
        class="finder-table"
        :row-class-name="'finder-row'"
      >
        <el-table-column prop="id" label="档案编号" width="100">
          <template #default="{row}">
            <span class="mono-id">#{{ String(row.id || 0).padStart(3, '0') }}</span>
          </template>
        </el-table-column>

        <el-table-column label="文献档案 (Document)" min-width="340">
          <template #default="{row}">
            <div class="doc-cell">
              <div class="doc-icon-wrapper"><el-icon><Notebook /></el-icon></div>
              <div class="doc-title-group">
                <span class="doc-title">{{ row.title }}</span>
                <span class="doc-meta">导读精要：{{ row.summary || '（未提供文献导读）' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="category" label="所属类目" width="140">
          <template #default="{row}">
            <div class="library-ribbon" :class="(row.category || 'default').toLowerCase()">
              {{ row.category === 'MANUAL' ? '操作手册' : row.category === 'GUIDE' ? '监测指南' : row.category === 'POLICY' ? '制度规范' : '未分类' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="开放权限" width="140">
          <template #default="{row}">
            <div class="auth-status" :class="row.status === 1 ? 'is-public' : 'is-internal'">
              <el-icon><Unlock v-if="row.status === 1" /><Lock v-else /></el-icon>
              <span>{{ row.status === 1 ? '全网公开查阅' : '限内部调阅' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="viewCount" label="检索频次" width="120">
          <template #default="{row}">
            <span class="mono-metric">{{ row.viewCount || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="140" align="right">
          <template #default="{row}">
            <div class="action-dock">
              <el-tooltip content="修订文献" placement="top" :show-after="200" effect="light">
                <button class="dock-btn edit" @click="showDialog(row)"><el-icon><EditPen /></el-icon></button>
              </el-tooltip>
              <el-tooltip content="销毁记录" placement="top" :show-after="200" effect="light">
                <button class="dock-btn delete" @click="handleDelete(row.id)"><el-icon><Delete /></el-icon></button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer" v-if="total > size">
        <el-pagination 
          v-model:current-page="page" 
          :page-size="size" 
          :total="total" 
          layout="prev, pager, next" 
          @current-change="fetchData" 
          class="apple-pagination" 
        />
      </div>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      width="840px" 
      destroy-on-close 
      class="zen-dialog"
      :show-close="false"
      align-center
    >
      <div class="zen-header">
        <div class="zen-status">
          <span class="pulse-indicator"></span> {{ isEdit ? '文献修订与版本更新模式' : '新文献起草台' }}
        </div>
        <button class="zen-close" @click="dialogVisible = false">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <el-form :model="form" class="zen-form" @submit.prevent>
        <el-form-item prop="title" class="zen-title-item">
          <el-input v-model="form.title" class="zen-title-input" placeholder="输入无标题文献..." />
        </el-form-item>
        
        <div class="zen-meta-row">
          <el-form-item prop="category" class="zen-meta-item flex-1">
            <el-select v-model="form.category" class="zen-select" placeholder="选择文献所属领域..." popper-class="premium-popper">
              <el-option label="操作手册" value="MANUAL" />
              <el-option label="监测指南" value="GUIDE" />
              <el-option label="制度规范" value="POLICY" />
            </el-select>
          </el-form-item>
          <div class="flex-2"></div>
        </div>

        <el-form-item prop="summary" class="zen-summary-item">
          <el-input v-model="form.summary" type="textarea" :rows="2" class="zen-summary-input" placeholder="在此添加一段简短的文献导读或核心精要..." />
        </el-form-item>
        
        <el-form-item prop="content" class="zen-content-item">
          <el-input v-model="form.content" type="textarea" :rows="8" class="zen-content-input" placeholder="正文撰写区域（支持构建复杂的 HTML 文本排版）..." />
        </el-form-item>
        
        <el-form-item prop="attachment" class="zen-attachment-item">
          <div class="airdrop-zone">
            <el-upload 
              :show-file-list="false" 
              :before-upload="handleFileUpload" 
              accept=".pdf,.doc,.docx,.xls,.xlsx"
              class="airdrop-trigger"
            >
              <div class="airdrop-box" :class="{'box-uploading': uploading}">
                 <div class="airdrop-icon">
                   <el-icon v-if="!uploading"><UploadFilled /></el-icon>
                   <el-icon v-else class="is-loading"><Loading /></el-icon>
                 </div>
                 <div class="airdrop-text">
                   <h4>{{ uploading ? '文献源文件上载中...' : '点击或将文献文件拖拽至此' }}</h4>
                   <p>{{ form.attachment ? '已挂载实体附件：' + form.attachment : '支持 PDF, DOCX, XLSX 格式，最高 50MB' }}</p>
                 </div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>

      <div class="zen-footer">
        <button class="apple-btn ghost-btn" @click="dialogVisible = false">保存草稿并退出</button>
        <button class="apple-btn primary-btn" @click="handleSave">提交入库</button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getKnowledgePage, createKnowledge, updateKnowledge, deleteKnowledge } from '@/api/knowledge'
// 导入正确的附件上传接口 uploadDoc
import { uploadDoc } from '@/api/file'
import { ElMessage, ElMessageBox } from 'element-plus'
// 确保所有 Icon 合法可用
import { 
  Collection, Notebook, DocumentAdd, EditPen, Delete, Close, Lock, Unlock, UploadFilled, Loading
} from '@element-plus/icons-vue'

const knowledgeList = ref([])
const loading = ref(false)
const uploading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({ title: '', category: 'MANUAL', summary: '', content: '', attachment: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getKnowledgePage(page.value, size.value)
    knowledgeList.value = res.data || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function showDialog(row) {
  if (row) {
    isEdit.value = true; editId.value = row.id; form.value = { ...row }
  } else {
    isEdit.value = false; editId.value = null; form.value = { title: '', category: 'MANUAL', summary: '', content: '', attachment: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    isEdit.value ? await updateKnowledge(editId.value, form.value) : await createKnowledge(form.value)
    ElMessage.success(isEdit.value ? '文献档案修订完毕' : '全新文献已入库')
    dialogVisible.value = false; fetchData()
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('执行此操作将从检索室中永久抹除该文献记录，是否继续？', '高危操作确认', { 
      type: 'error', customClass: 'premium-message-box', confirmButtonText: '强制抹除', cancelButtonText: '终止'
    })
    await deleteKnowledge(id)
    ElMessage.success('文献记录已销毁'); fetchData()
  } catch (e) {}
}

async function handleFileUpload(file) {
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('超大文献请分卷压缩后上载 (上限 50MB)')
    return false
  }
  uploading.value = true
  try {
    // 调用正确的 API 方法
    const res = await uploadDoc(file)
    form.value.attachment = res.data
    ElMessage.success('源文件挂载成功')
  } catch (e) {
    ElMessage.error('上载通讯中断，请重试')
  } finally { 
    uploading.value = false 
  }
  return false 
}

onMounted(fetchData)
</script>

<style scoped>
/* =========================================================================
   瑞士极简与 Finder 档案库美学 (Swiss Spa & macOS Finder)
   ========================================================================= */

.mac-library-container { 
  background: transparent !important; 
  padding: 24px 0 0 0; 
  min-height: 100%; 
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif; 
  color: #1D1D1F; 
}

/* ---------- 1. Finder 级控制岛 ---------- */
.control-island {
  display: flex; justify-content: space-between; align-items: center;
  background: #FFFFFF; padding: 24px 32px; border-radius: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04); margin-bottom: 24px;
}
.island-brand { display: flex; align-items: center; gap: 20px; }
.brand-icon {
  width: 52px; height: 52px;
  background: linear-gradient(135deg, #0284C7, #0369A1);
  color: #FFFFFF; border-radius: 16px;
  display: flex; justify-content: center; align-items: center; font-size: 24px;
  box-shadow: 0 8px 20px rgba(2, 132, 199, 0.2);
}
.brand-text h2 { margin: 0 0 6px 0; font-size: 22px; font-weight: 700; letter-spacing: -0.5px; }
.brand-text p { margin: 0; font-size: 13px; color: #86868B; font-weight: 500; }

.apple-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  padding: 12px 24px; border-radius: 14px; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.primary-btn { background: #1D1D1F; color: #FFFFFF; }
.primary-btn:hover { background: #434344; transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.12); }
.primary-btn:active { transform: translateY(1px); }
.ghost-btn { background: transparent; color: #86868B; }
.ghost-btn:hover { background: #F1F5F9; color: #1D1D1F; }

/* ---------- 2. 穹顶数据表 ---------- */
.data-dome {
  background: #FFFFFF; border-radius: 24px; padding: 12px 24px 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.03);
}

:deep(.finder-table) { --el-table-border-color: transparent; --el-table-header-bg-color: #FFFFFF; border-radius: 16px; }
:deep(.finder-table .el-table__inner-wrapper::before) { display: none; }
:deep(.finder-table th.el-table__cell) {
  padding: 20px 0; border-bottom: 1px solid rgba(0,0,0,0.03) !important;
  font-size: 13px; font-weight: 600; color: #86868B;
}
:deep(.finder-table td.el-table__cell) {
  padding: 20px 0; border-bottom: 1px solid rgba(0,0,0,0.02) !important; transition: background 0.3s ease;
}
:deep(.finder-table__row:hover > td.el-table__cell) { background-color: #F8FAFC !important; }

/* 复合文献档案列 */
.doc-cell { display: flex; align-items: center; gap: 16px; }
.doc-icon-wrapper {
  width: 44px; height: 44px; border-radius: 12px;
  background: #F1F5F9; color: #64748B;
  display: flex; justify-content: center; align-items: center; font-size: 20px;
}
.doc-title-group { display: flex; flex-direction: column; gap: 4px; }
.doc-title { font-size: 15px; font-weight: 600; color: #1D1D1F; letter-spacing: -0.01em; }
.doc-meta { font-size: 12px; color: #94A3B8; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 320px; }

.mono-id { font-family: "SF Pro Text", monospace; color: #94A3B8; font-weight: 600; font-size: 13px; }
.mono-metric { font-family: "SF Pro Text", monospace; font-feature-settings: "tnum"; color: #475569; font-weight: 600; font-size: 14px; }

/* 类目缎带 */
.library-ribbon {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 6px 14px; border-radius: 8px; font-size: 12px; font-weight: 600; color: #475569;
  background: #F8FAFC;
}
.library-ribbon.manual { border-left: 3px solid #0284C7; }
.library-ribbon.guide { border-left: 3px solid #D97706; }
.library-ribbon.policy { border-left: 3px solid #059669; }
.library-ribbon.default { border-left: 3px solid #94A3B8; }

.auth-status { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; }
.is-public { color: #10B981; } .is-internal { color: #86868B; }

.action-dock { display: flex; justify-content: flex-end; gap: 8px; padding-right: 12px; }
.dock-btn {
  width: 36px; height: 36px; border-radius: 10px; border: none; background: #F1F5F9; color: #64748B; cursor: pointer;
  display: flex; justify-content: center; align-items: center; font-size: 16px; transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.dock-btn:hover { background: #1D1D1F; color: #FFFFFF; transform: scale(1.08) translateY(-2px); }
.dock-btn.delete:hover { background: #FEF2F2; color: #EF4444; }

.pagination-footer { margin-top: 32px; display: flex; justify-content: center; }
:deep(.apple-pagination .el-pager li) { background: transparent !important; font-weight: 600; color: #86868B; border-radius: 10px; min-width: 36px; height: 36px; line-height: 36px; }
:deep(.apple-pagination .el-pager li.is-active) { background: #1D1D1F !important; color: #FFF; }
:deep(.apple-pagination .btn-prev), :deep(.apple-pagination .btn-next) { background: transparent !important; border-radius: 10px; }

/* ---------- 3. Notion 风格数字纸张撰写台 ---------- */
:deep(.zen-dialog) { border-radius: 28px; box-shadow: 0 40px 100px rgba(0,0,0,0.15); overflow: hidden; background: #FFFFFF; padding: 0; }
:deep(.zen-dialog .el-dialog__header), :deep(.zen-dialog .el-dialog__body), :deep(.zen-dialog .el-dialog__footer) { padding: 0; display: block; }

.zen-header { padding: 24px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.03); background: #FAFAFC; }
.zen-status { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: #475569; }
.pulse-indicator { width: 8px; height: 8px; border-radius: 50%; background: #0284C7; box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.2); }
.zen-close { width: 32px; height: 32px; border-radius: 16px; border: none; background: transparent; color: #86868B; display: flex; justify-content: center; align-items: center; cursor: pointer; font-size: 18px; transition: all 0.2s; }
.zen-close:hover { background: #E2E8F0; color: #1D1D1F; transform: rotate(90deg); }

.zen-form { padding: 40px 48px; }
.zen-form .el-form-item { margin-bottom: 24px; }
.zen-meta-row { display: flex; margin-bottom: 32px; gap: 24px; }
.flex-1 { flex: 1; } .flex-2 { flex: 2; }

.zen-title-item { margin-bottom: 16px !important; }
:deep(.zen-title-input .el-input__wrapper) { background: transparent !important; box-shadow: none !important; padding: 0 !important; }
:deep(.zen-title-input .el-input__inner) { font-size: 32px; font-weight: 800; color: #1D1D1F; letter-spacing: -0.02em; height: auto; }
:deep(.zen-title-input .el-input__inner::placeholder) { color: #CBD5E1; font-weight: 700; }

:deep(.zen-select .el-input__wrapper) { background: #F8FAFC !important; box-shadow: none !important; border-radius: 12px; padding: 10px 14px; }
:deep(.zen-summary-input .el-textarea__inner) { background: transparent !important; box-shadow: none !important; border-left: 3px solid #E2E8F0; border-radius: 0; padding: 4px 16px; font-size: 15px; color: #64748B; font-weight: 500; transition: border-color 0.3s; }
:deep(.zen-summary-input .el-textarea__inner:focus) { border-color: #0284C7; }
:deep(.zen-content-input .el-textarea__inner) { background: transparent !important; box-shadow: none !important; padding: 0; font-size: 15px; line-height: 1.6; color: #1D1D1F; border: none; }

/* AirDrop 级投递区 */
.zen-attachment-item { margin-top: 48px; margin-bottom: 0 !important;}
.airdrop-trigger { width: 100%; }
.airdrop-box { display: flex; align-items: center; gap: 20px; padding: 24px 32px; border-radius: 20px; background: #F8FAFC; border: 2px dashed rgba(0,0,0,0.06); cursor: pointer; transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1); }
.airdrop-box:hover, .box-uploading { background: #F0F9FF; border-color: rgba(2, 132, 199, 0.3); }
.airdrop-icon { font-size: 32px; color: #0284C7; background: #FFFFFF; padding: 14px; border-radius: 16px; box-shadow: 0 8px 24px rgba(2, 132, 199, 0.12); }
.airdrop-text { text-align: left; line-height: 1.5; }
.airdrop-text h4 { margin: 0 0 6px 0; font-size: 16px; font-weight: 600; color: #1D1D1F; }
.airdrop-text p { margin: 0; font-size: 13px; color: #64748B; font-weight: 500; }

.zen-footer { padding: 24px 48px 32px; background: #FFFFFF; display: flex; justify-content: flex-end; gap: 12px; }
</style>

<style>
/* 重写组件原生质感 */
.premium-popper { border-radius: 16px !important; box-shadow: 0 16px 40px rgba(0,0,0,0.1) !important; border: 1px solid rgba(0,0,0,0.04) !important; padding: 8px !important; }
.premium-message-box { border-radius: 24px !important; padding: 32px !important; border: none !important; box-shadow: 0 32px 80px rgba(0,0,0,0.12) !important; }
.premium-message-box .el-message-box__btns .el-button--primary { background: #1D1D1F !important; border: none !important; border-radius: 10px !important; padding: 10px 20px !important; height: auto !important; }
</style>