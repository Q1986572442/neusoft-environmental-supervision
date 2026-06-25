<template>
  <div class="nepv-dashboard" v-loading="loading">
    <div class="hero-banner">
      <h1>🌍 全国环境质量监测大屏</h1>
      <p>实时数据驱动 · 科学环保决策</p>
    </div>

    <div class="kpi-grid">
      <div class="kpi"><div class="val">{{ overview.totalUsers || 0 }}</div><div class="lbl">注册监督员</div></div>
      <div class="kpi"><div class="val">{{ overview.totalFeedbacks || 0 }}</div><div class="lbl">监督反馈总数</div></div>
      <div class="kpi"><div class="val">{{ overview.totalDetections || 0 }}</div><div class="lbl">AQI检测总数</div></div>
      <div class="kpi"><div class="val">{{ overview.totalCities || 0 }}</div><div class="lbl">覆盖城市</div></div>
      <div class="kpi"><div class="val">{{ overview.completedFeedbacks || 0 }}</div><div class="lbl">已完成反馈</div></div>
      <div class="kpi warn"><div class="val">{{ overview.pendingFeedbacks || 0 }}</div><div class="lbl">待处理反馈</div></div>
    </div>

    <div class="charts-grid">
      <div class="chart-card">
        <h3>📊 反馈状态分布</h3>
        <div ref="pieChart" style="height:300px"></div>
      </div>
      <div class="chart-card">
        <h3>📈 月度反馈趋势</h3>
        <div ref="lineChart" style="height:300px"></div>
      </div>
      <div class="chart-card span-2">
        <h3>🗺️ 各省份反馈统计</h3>
        <div ref="barChart" style="height:300px"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, markRaw } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getFeedbackStatus, getAqiDistribution, getProvinceFeedback, getMonthlyTrend } from '@/api/statistics'

const loading = ref(false)
const overview = ref({})
const pieChart = ref(null), lineChart = ref(null), barChart = ref(null)
let charts = []

onMounted(async () => {
  loading.value = true
  try {
    const [ov, fs, pf, mt] = await Promise.all([
      getOverview(), getFeedbackStatus(), getProvinceFeedback(), getMonthlyTrend()
    ])
    overview.value = ov.data || {}
    initPie(fs.data)
    initBar(pf.data)
    initLine(mt.data)
  } catch(e) {} finally { loading.value = false }
})

function initPie(data) {
  if (!pieChart.value) return
  const c = markRaw(echarts.init(pieChart.value))
  c.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: [
        { value: data?.pending || 0, name: '待指派', itemStyle: { color: '#F5A623' } },
        { value: data?.assigned || 0, name: '处理中', itemStyle: { color: '#409EFF' } },
        { value: data?.completed || 0, name: '已完成', itemStyle: { color: '#2AA876' } }
      ],
      label: { color: '#8899aa' }
    }]
  })
  charts.push(c)
}

function initBar(data) {
  if (!barChart.value) return
  const c = markRaw(echarts.init(barChart.value))
  const list = (data || []).slice(0, 10)
  c.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: list.map(d => d.provinceName), axisLabel: { color: '#8899aa' } },
    yAxis: { type: 'value', axisLabel: { color: '#8899aa' } },
    series: [{ type: 'bar', data: list.map(d => d.count), itemStyle: { color: '#409EFF', borderRadius: [6,6,0,0] } }]
  })
  charts.push(c)
}

function initLine(data) {
  if (!lineChart.value) return
  const c = markRaw(echarts.init(lineChart.value))
  c.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: (data||[]).map(d => d.month), axisLabel: { color: '#8899aa' } },
    yAxis: { type: 'value', axisLabel: { color: '#8899aa' } },
    series: [{ type: 'line', data: (data||[]).map(d => d.count), smooth:true, itemStyle:{color:'#2AA876'}, areaStyle:{color:'rgba(42,168,118,0.1)'} }]
  })
  charts.push(c)
}

onUnmounted(() => charts.forEach(c => c.dispose()))
</script>

<style scoped>
.nepv-dashboard { max-width:1400px; margin:0 auto; padding:32px; }
.hero-banner { text-align:center; margin-bottom:40px; }
.hero-banner h1 { font-size:28px; color:#fff; margin:0 0 8px; }
.hero-banner p { font-size:15px; color:#8899aa; margin:0; }

.kpi-grid { display:grid; grid-template-columns:repeat(6,1fr); gap:16px; margin-bottom:32px; }
.kpi { background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.06); border-radius:12px; padding:20px; text-align:center; }
.kpi.warn { border-color:rgba(245,166,35,0.3); }
.kpi .val { font-size:32px; font-weight:700; color:#409EFF; }
.kpi.warn .val { color:#F5A623; }
.kpi .lbl { font-size:13px; color:#8899aa; margin-top:6px; }

.charts-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
.chart-card { background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.06); border-radius:12px; padding:24px; }
.chart-card.span-2 { grid-column:1/-1; }
.chart-card h3 { margin:0 0 16px; font-size:16px; color:#ccc; }
</style>
