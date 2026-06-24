<template>
  <div class="swiss-dashboard">
    
    <section class="action-console glass-panel fixed-section">
      <div class="console-left">
        <button class="icon-nav-btn" @click="$router.push('/home')" title="返回概览">
          <el-icon><Back /></el-icon>
        </button>
        <div class="title-group">
          <h2 class="page-title">大气成分勘测中枢</h2>
          <span class="page-subtitle">多维气体指标录入与综合空气质量评估</span>
        </div>
      </div>
      
      <div class="console-right">
        <div class="live-aqi-indicator" v-if="realTimeFinalAqi >= 0">
          <span class="label">预估综合 AQI</span>
          <div class="value-badge" :style="{ backgroundColor: getAqiColor(realTimeFinalAqi) }">
            {{ realTimeFinalAqi }} 
          </div>
        </div>

        <button class="swiss-btn ghost-btn" @click="resetForm">重置仪轨</button>
        <button class="swiss-btn primary-btn" @click="handleSubmit" :class="{ 'is-loading': submitting }">
          <el-icon v-if="!submitting"><Upload /></el-icon>
          <el-icon v-else class="is-spinning"><Loading /></el-icon>
          数据归档入库
        </button>
      </div>
    </section>

    <section class="content-split-grid stretch-section form-split">
      
      <div class="form-panel glass-panel scrollable-card">
        <div class="panel-header">
          <h3 class="panel-title"><el-icon><Odometer /></el-icon> 勘测仪轨刻度</h3>
          <span class="sensor-status"><span class="pulse-dot"></span> 传感器在线</span>
        </div>

        <div class="panel-body scroll-area">
          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-position="top" 
            class="mac-inset-form"
          >
            <el-form-item label="锚定关联案件 (Feedback ID)" prop="feedbackId">
              <el-input-number 
                v-model="form.feedbackId" 
                :min="1" 
                placeholder="请输入反馈案件 ID" 
                class="mac-number-input"
                controls-position="right"
              />
            </el-form-item>

            <div class="section-divider">化学成分微观指标录入</div>

            <div class="chemical-control-group">
              <div class="chem-header">
                <div class="chem-title">
                  <span class="chem-symbol">SO₂</span>
                  <span class="chem-name">二氧化硫 AQI</span>
                </div>
                <div class="chem-value-display" :style="{ color: getAqiColor(form.so2Aqi) }">
                  {{ form.so2Aqi || 0 }} <span class="unit">AQI</span>
                </div>
              </div>
              <el-form-item prop="so2Aqi" class="margin-bottom-0">
                <el-slider 
                  v-model="form.so2Aqi" 
                  :max="500" 
                  :show-tooltip="false"
                  class="swiss-slider"
                  :style="{ '--slider-color': getAqiColor(form.so2Aqi) }"
                />
              </el-form-item>
            </div>

            <div class="chemical-control-group">
              <div class="chem-header">
                <div class="chem-title">
                  <span class="chem-symbol">CO</span>
                  <span class="chem-name">一氧化碳 AQI</span>
                </div>
                <div class="chem-value-display" :style="{ color: getAqiColor(form.coAqi) }">
                  {{ form.coAqi || 0 }} <span class="unit">AQI</span>
                </div>
              </div>
              <el-form-item prop="coAqi" class="margin-bottom-0">
                <el-slider 
                  v-model="form.coAqi" 
                  :max="500" 
                  :show-tooltip="false"
                  class="swiss-slider"
                  :style="{ '--slider-color': getAqiColor(form.coAqi) }"
                />
              </el-form-item>
            </div>

            <div class="chemical-control-group">
              <div class="chem-header">
                <div class="chem-title">
                  <span class="chem-symbol pm25">PM<span class="sub">2.5</span></span>
                  <span class="chem-name">悬浮颗粒物 AQI</span>
                </div>
                <div class="chem-value-display massive-value" :style="{ color: getAqiColor(form.pm25Aqi) }">
                  {{ form.pm25Aqi || 0 }} <span class="unit">AQI</span>
                </div>
              </div>
              <el-form-item prop="pm25Aqi" class="margin-bottom-0">
                <el-slider 
                  v-model="form.pm25Aqi" 
                  :max="500" 
                  :show-tooltip="false"
                  class="swiss-slider"
                  :style="{ '--slider-color': getAqiColor(form.pm25Aqi) }"
                />
              </el-form-item>
            </div>

            <div class="section-divider">附加信息</div>

            <el-form-item label="勘测附言 (选填)" prop="remark" class="margin-bottom-0">
              <el-input 
                v-model="form.remark" 
                type="textarea" 
                :rows="4" 
                placeholder="可在此记录当天的气象条件、风向风速或传感器校准状态..." 
                class="mac-inset-input"
              />
            </el-form-item>

          </el-form>
        </div>
      </div>

      <div class="ref-panel glass-panel scrollable-card">
        <div class="panel-header">
          <h3 class="panel-title"><el-icon><Compass /></el-icon> 历史勘测档案</h3>
          <span class="item-count">共 {{ total }} 条记录</span>
        </div>

        <div class="panel-body scroll-area" v-loading="loading">
          <div class="record-flow">
            <div v-for="record in tableData" :key="record.id" class="record-ticket">
              
              <div class="ticket-left">
                <div class="synthesis-ring" :style="{ borderColor: getAqiColor(record.finalAqi) }">
                  <div class="synthesis-core" :style="{ backgroundColor: getAqiColor(record.finalAqi) }"></div>
                </div>
                <div class="final-score">
                  <span class="score-num" :style="{ color: getAqiColor(record.finalAqi) }">{{ record.finalAqi }}</span>
                  <span class="score-label">综合 AQI</span>
                </div>
              </div>

              <div class="ticket-divider"></div>

              <div class="ticket-center">
                <div class="ticket-meta-info">
                  <span class="record-id">单号 #{{ String(record.id).padStart(5, '0') }}</span>
                  <span class="meta-link"><el-icon><Link /></el-icon> 关联案件: {{ record.feedbackId }}</span>
                  <span class="meta-link"><el-icon><Avatar /></el-icon> 测绘员 ID: {{ record.inspectorId }}</span>
                </div>
                
                <div class="chem-badges">
                  <div class="mini-badge">
                    <span class="mini-sym">SO₂</span>
                    <span class="mini-val" :style="{ color: getAqiColor(record.so2Aqi) }">{{ record.so2Aqi }}</span>
                  </div>
                  <div class="mini-badge">
                    <span class="mini-sym">CO</span>
                    <span class="mini-val" :style="{ color: getAqiColor(record.coAqi) }">{{ record.coAqi }}</span>
                  </div>
                  <div class="mini-badge">
                    <span class="mini-sym">PM2.5</span>
                    <span class="mini-val" :style="{ color: getAqiColor(record.pm25Aqi) }">{{ record.pm25Aqi }}</span>
                  </div>
                </div>
              </div>

              <div class="ticket-right">
                <span class="time-text">{{ formatTime(record.detectionTime) }}</span>
              </div>
            </div>

            <div class="empty-state-wrapper" v-if="!loading && tableData.length === 0">
              <el-empty description="暂无勘测归档记录" :image-size="60" />
            </div>
          </div>
        </div>

        <div class="list-pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            class="swiss-pagination"
            @current-change="fetchList"
          />
        </div>
      </div>

    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { submitAqi, getAqiPage } from '@/api/aqi'
import { ElMessage } from 'element-plus'
import { 
  Back, Upload, Loading, Odometer, 
  Compass, Link, Avatar, Connection, Document
} from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10) // 契合高度锁定后的视野
const total = ref(0)

const form = ref({
  feedbackId: null,
  so2Aqi: 0,
  coAqi: 0,
  pm25Aqi: 0,
  remark: '',
  inspectorId: 2 
})

const rules = {
  feedbackId: [{ required: true, message: '请提供需要关联的案件 ID', trigger: 'blur' }]
}

// 严选色彩管理：基于高级瑞士色板的污染预警色
function getAqiColor(val) {
  const num = Number(val) || 0
  if (num <= 50) return '#2AA876'  // 优 - 鼠尾草绿
  if (num <= 100) return '#85C77A' // 良 - 晨雾绿
  if (num <= 150) return '#F5A623' // 轻度 - 警示橙
  if (num <= 200) return '#E87A31' // 中度 - 赤陶色
  if (num <= 300) return '#D9534F' // 重度 - 碳霞红
  return '#A03232'                 // 严重 - 深绛红
}

// 动态合成 AQI (最高项决定整体污染等级)
const realTimeFinalAqi = computed(() => {
  return Math.max(form.value.so2Aqi || 0, form.value.coAqi || 0, form.value.pm25Aqi || 0)
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 16)
}

function resetForm() {
  form.value = { feedbackId: null, so2Aqi: 0, coAqi: 0, pm25Aqi: 0, remark: '', inspectorId: form.value.inspectorId }
  if (formRef.value) formRef.value.clearValidate()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await submitAqi(form.value)
    ElMessage.success({ message: '化学成分勘测数据已成功加密入库', customClass: 'swiss-message' })
    resetForm()
    currentPage.value = 1
    fetchList()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAqiPage(currentPage.value, pageSize.value)
    tableData.value = res.data
    total.value = res.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
  const uid = localStorage.getItem('userId')
  if (uid) form.value.inspectorId = parseInt(uid)
})
</script>

<style scoped>
/* =========================================
   核心空间调度 (严格继承系统整体基调)
   ========================================= */

.swiss-dashboard {
  max-width: 1440px; width: 100%; height: 100%; 
  margin: 0 auto; display: flex; flex-direction: column; gap: 24px; color: #1C2421;
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

/* 局部滑动核心区 */
.scroll-area { flex: 1; overflow-y: auto; scrollbar-width: none; -ms-overflow-style: none; min-height: 300px;}
.scroll-area::-webkit-scrollbar { display: none; }
.panel-body { padding: 24px 28px 32px; }

/* =========================================
   1. 顶部悬浮控制台 (严苛的高低差抹平，固定120px)
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

/* 实时合成指示徽章 */
.live-aqi-indicator {
  display: flex; align-items: center; gap: 12px; background: rgba(255, 255, 255, 0.6); padding: 6px 8px 6px 16px;
  border-radius: 100px; border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 2px 4px rgba(255,255,255,1), 0 4px 12px rgba(0,0,0,0.02);
  margin-right: 12px;
}
.live-aqi-indicator .label { font-size: 13px; font-weight: 600; color: #74807B; letter-spacing: 0.5px;}
.value-badge {
  padding: 4px 14px; border-radius: 20px; color: #FFF; font-weight: 700; font-size: 15px; font-family: "SF Pro Display", sans-serif;
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.3); transition: background-color 0.4s ease;
}

.swiss-btn {
  border: none; outline: none; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px; font-weight: 600; transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.swiss-btn.ghost-btn { padding: 0 20px; height: 40px; background: rgba(255, 255, 255, 0.6); color: #1C2421; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.swiss-btn.ghost-btn:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.swiss-btn.primary-btn { padding: 0 24px; height: 40px; background: #2A483A; color: #FFF; box-shadow: 0 2px 10px rgba(42, 72, 58, 0.3); }
.swiss-btn.primary-btn:hover:not(.is-loading) { background: #1C2421; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(42, 72, 58, 0.4); }
.is-spinning { animation: spin 1s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }

/* =========================================
   2. 并排主面板区 
   ========================================= */
.form-split { display: grid; grid-template-columns: 1fr 1.2fr; gap: 24px; align-items: stretch; }

/* === 左侧：化学制剂仪表盘 === */
.sensor-status { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; color: #2AA876; background: rgba(42, 168, 118, 0.1); padding: 4px 10px; border-radius: 12px; }
.pulse-dot { width: 6px; height: 6px; border-radius: 50%; background: #2AA876; box-shadow: 0 0 6px #2AA876; animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

.section-divider { font-size: 12px; font-weight: 700; color: #86868B; text-transform: uppercase; margin: 32px 0 16px 0; letter-spacing: 0.5px; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 8px;}

/* 拟物输入框基调 */
.margin-bottom-0 { margin-bottom: 0 !important; }
.mac-inset-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #74807B; padding-bottom: 8px; line-height: 1; }

.mac-number-input { width: 100% !important; }
.mac-number-input :deep(.el-input__wrapper) { background: rgba(0, 0, 0, 0.03) !important; box-shadow: inset 0 1px 3px rgba(0,0,0,0.04), 0 1px 0 rgba(255,255,255,0.7) !important; border-radius: 10px !important; border: none; height: 44px;}
.mac-number-input :deep(.el-input__wrapper.is-focus) { background: #FFF !important; box-shadow: inset 0 0 0 2px #2A483A !important; }

.mac-inset-input :deep(.el-textarea__inner) {
  background: rgba(0, 0, 0, 0.03) !important; box-shadow: inset 0 1px 3px rgba(0,0,0,0.04), 0 1px 0 rgba(255,255,255,0.7) !important;
  border-radius: 10px !important; border: none; transition: all 0.3s; padding: 12px 16px; font-size: 14px; color: #1D1D1F;
}
.mac-inset-input :deep(.el-textarea__inner:focus) { background: #FFF !important; box-shadow: inset 0 0 0 2px #2A483A !important; }

/* 化学仪轨交互组件 (核心) */
.chemical-control-group {
  background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px; padding: 16px 20px; margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02); transition: transform 0.2s;
}
.chemical-control-group:hover { transform: scale(1.01); background: rgba(255,255,255,0.7); }

.chem-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 16px; }
.chem-title { display: flex; flex-direction: column; gap: 2px; }
.chem-symbol { font-size: 20px; font-weight: 700; font-family: "SF Pro Display", sans-serif; color: #1C2421; letter-spacing: 0.5px; }
.chem-symbol.pm25 { font-size: 18px; }
.chem-symbol .sub { font-size: 11px; font-weight: 600; }
.chem-name { font-size: 12px; font-weight: 600; color: #74807B; }

.chem-value-display { font-size: 28px; font-weight: 700; font-family: "SF Pro Display", sans-serif; transition: color 0.4s ease; line-height: 1;}
.massive-value { font-size: 32px; }
.chem-value-display .unit { font-size: 12px; font-weight: 600; opacity: 0.6; }

/* 滑块组件美化：CSS 变量注入，实现无缝变色 */
.swiss-slider { --el-slider-main-bg-color: var(--slider-color); --el-slider-runway-bg-color: rgba(0,0,0,0.06); margin: 0 8px; }
.swiss-slider :deep(.el-slider__bar) { transition: background-color 0.4s ease; }
.swiss-slider :deep(.el-slider__button) { border: 2px solid #FFF; box-shadow: 0 2px 8px rgba(0,0,0,0.15); background-color: var(--slider-color); transition: background-color 0.4s ease; width: 18px; height: 18px; }


/* === 右侧：健康档案式监测流 (History Flow) === */
.item-count { font-size: 12px; background: rgba(28, 36, 33, 0.05); padding: 4px 12px; border-radius: 12px; font-weight: 600; color: #74807B; }

.record-flow { display: flex; flex-direction: column; gap: 16px; }
.record-ticket {
  background: rgba(255, 255, 255, 0.6); border: 1px solid rgba(255, 255, 255, 0.9); border-radius: 16px;
  padding: 16px 20px; display: flex; align-items: center; gap: 20px;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.record-ticket:hover { background: #FFF; transform: translateX(2px); box-shadow: 0 6px 16px -4px rgba(0, 0, 0, 0.05); }

/* 左：合成环 */
.ticket-left { display: flex; align-items: center; gap: 16px; width: 110px; flex-shrink: 0; }
.synthesis-ring { width: 14px; height: 14px; border-radius: 50%; border: 3px solid; display: flex; justify-content: center; align-items: center; }
.synthesis-core { width: 6px; height: 6px; border-radius: 50%; }
.final-score { display: flex; flex-direction: column; }
.score-num { font-size: 22px; font-weight: 700; font-family: "SF Pro Display", sans-serif; line-height: 1; }
.score-label { font-size: 10px; font-weight: 600; color: #A0AAB2; margin-top: 4px; text-transform: uppercase; }

.ticket-divider { width: 1px; height: 36px; background: rgba(28, 36, 33, 0.08); }

/* 中：气泡 */
.ticket-center { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.ticket-meta-info { display: flex; align-items: center; gap: 12px; }
.record-id { font-family: "SF Mono", Consolas, monospace; font-size: 12px; font-weight: 600; color: #1C2421; }
.meta-link { font-size: 11px; font-weight: 600; color: #74807B; display: flex; align-items: center; gap: 4px; }
.meta-link .el-icon { color: #A0AAB2; }

.chem-badges { display: flex; gap: 8px; flex-wrap: wrap; }
.mini-badge { display: flex; align-items: center; background: rgba(255, 255, 255, 0.8); border: 1px solid rgba(0,0,0,0.04); border-radius: 8px; padding: 4px 8px; gap: 6px; box-shadow: 0 1px 2px rgba(0,0,0,0.02);}
.mini-sym { font-size: 10px; font-weight: 600; color: #86868B; }
.mini-val { font-size: 13px; font-weight: 700; font-family: "SF Pro Display", sans-serif; }

/* 右：时间 */
.ticket-right { flex-shrink: 0; }
.time-text { font-size: 11px; font-weight: 500; color: #A0AAB2; }

.empty-state-wrapper { height: 100%; display: flex; align-items: center; justify-content: center; }

/* 极简底部固定条 */
.list-pagination { padding: 16px 24px; border-top: 1px solid rgba(28, 36, 33, 0.06); display: flex; justify-content: center; flex-shrink: 0; }
:deep(.swiss-pagination) { --el-pagination-bg-color: transparent; }

@media (max-width: 1024px) {
  .form-split { grid-template-columns: 1fr; }
  .action-console { padding: 0 24px; }
}
</style>

<style>
.swiss-message { background: rgba(255, 255, 255, 0.85) !important; backdrop-filter: blur(24px) !important; border: 1px solid rgba(255, 255, 255, 0.9) !important; border-radius: 12px !important; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08) !important; color: #1C2421 !important; font-weight: 600 !important; }
</style>