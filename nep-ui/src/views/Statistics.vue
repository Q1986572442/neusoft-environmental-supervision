<template>
  <div class="swiss-dashboard">
    
    <section class="action-console glass-panel fixed-section">
      <div class="console-left">
        <div class="title-group">
          <h2 class="page-title">全景数据分析中枢</h2>
          <span class="page-subtitle">环境质量空间计算与生物态感知矩阵</span>
        </div>
      </div>
      
      <div class="console-right">
        <button class="swiss-btn ghost-btn">
          <el-icon><Filter /></el-icon> 深度筛选
        </button>
        <button class="swiss-btn primary-btn">
          <el-icon><Download /></el-icon> 导出空间分析报告
        </button>
      </div>
    </section>

    <section class="stretch-section scrollable-card glass-panel">
      
      <div class="panel-header">
        <h3 class="panel-title"><el-icon><DataAnalysis /></el-icon> 宏观态势总览</h3>
      </div>

      <div class="panel-body scroll-area">
        
        <div class="kpi-grid">
          <div class="kpi-card" v-for="kpi in kpis" :key="kpi.label">
            <div class="kpi-icon-box" :style="{ color: kpi.color, backgroundColor: kpi.color + '1A' }">
              <el-icon><component :is="kpi.icon" /></el-icon>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ kpi.value }} <span class="unit">{{ kpi.unit }}</span></div>
              <div class="kpi-label">{{ kpi.label }}</div>
            </div>
          </div>
        </div>

        <div class="chart-bento-grid">
          
          <div class="chart-bento-card col-span-2">
            <div class="breathe-glow glow-primary"></div>
            <div class="breathe-glow glow-secondary"></div>
            
            <div class="chart-header">
              <el-icon><TrendCharts /></el-icon> 全网环境指数态势 (AQI)
            </div>
            <div class="chart-canvas" ref="lineChartRef"></div>
          </div>

          <div class="chart-bento-card">
            <div class="breathe-glow glow-warning"></div>
            <div class="breathe-glow glow-danger"></div>

            <div class="chart-header">
              <el-icon><PieChart /></el-icon> 污染评估聚类分布
            </div>
            <div class="chart-canvas" ref="pieChartRef"></div>
          </div>

          <div class="chart-bento-card">
            <div class="breathe-glow glow-secondary"></div>
            <div class="breathe-glow glow-primary"></div>

            <div class="chart-header">
              <el-icon><Aim /></el-icon> 微观指标多维剖析
            </div>
            <div class="chart-canvas" ref="radarChartRef"></div>
          </div>

          <div class="chart-bento-card col-span-2">
            <div class="breathe-glow glow-warning"></div>
            <div class="breathe-glow glow-secondary"></div>

            <div class="chart-header">
              <el-icon><Histogram /></el-icon> 省级网格异常频次跃迁
            </div>
            <div class="chart-canvas" ref="barChartRef"></div>
          </div>

        </div>

      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, shallowRef, onMounted, onUnmounted, markRaw } from 'vue'
import * as echarts from 'echarts'
import { 
  Filter, Download, DataAnalysis, Location, Warning, 
  CircleCheck, Odometer, TrendCharts, PieChart, Aim, Histogram
} from '@element-plus/icons-vue'

// DOM Refs
const lineChartRef = ref(null)
const pieChartRef = ref(null)
const radarChartRef = ref(null)
const barChartRef = ref(null)

// 存储 ECharts 实例，使用 shallowRef 避免 Vue 响应式代理带来的极度卡顿
const chartInstances = shallowRef([])

// 顶部 KPI 数据
const kpis = [
  { label: '环境监控网格节点', value: '1,024', unit: '个', icon: Location, color: '#409EFF' },
  { label: '近三十日异常预警', value: '238', unit: '次', icon: Warning, color: '#F5A623' },
  { label: '污染源追踪闭环率', value: '94.2', unit: '%', icon: CircleCheck, color: '#2AA876' },
  { label: '年度优良天数累计', value: '286', unit: '天', icon: Odometer, color: '#85C77A' }
]

// 极致的 Apple 玻璃化 Tooltip 设定
const glassTooltip = {
  backgroundColor: 'rgba(255, 255, 255, 0.85)',
  borderColor: 'rgba(255, 255, 255, 0.9)',
  borderWidth: 1,
  padding: [12, 16],
  textStyle: { color: '#1C2421', fontSize: 13, fontWeight: 500, fontFamily: 'SF Pro Display' },
  extraCssText: 'backdrop-filter: blur(24px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-radius: 12px;'
}

const initCharts = () => {
  const lineChart = markRaw(echarts.init(lineChartRef.value))
  const pieChart = markRaw(echarts.init(pieChartRef.value))
  const radarChart = markRaw(echarts.init(radarChartRef.value))
  const barChart = markRaw(echarts.init(barChartRef.value))

  // 1. 流动折线图 (AQI 态势)
  lineChart.setOption({
    tooltip: { trigger: 'axis', ...glassTooltip },
    grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
    xAxis: {
      type: 'category', boundaryGap: false,
      data: ['06-01', '06-02', '06-03', '06-04', '06-05', '06-06', '06-07'],
      axisLine: { show: false }, axisTick: { show: false },
      axisLabel: { color: '#74807B', margin: 16 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(28, 36, 33, 0.05)', type: 'dashed' } },
      axisLabel: { color: '#74807B' }
    },
    series: [{
      name: '系统合成 AQI', type: 'line', smooth: 0.5, symbol: 'circle', symbolSize: 8,
      itemStyle: { color: '#2AA876', borderColor: '#FFF', borderWidth: 2 },
      lineStyle: { width: 4, shadowBlur: 14, shadowColor: 'rgba(42, 168, 118, 0.4)', shadowOffsetY: 6 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(42, 168, 118, 0.35)' },
          { offset: 1, color: 'rgba(42, 168, 118, 0.0)' }
        ])
      },
      data: [42, 65, 58, 85, 120, 95, 62],
      animationEasing: 'elasticOut', animationDuration: 2400
    }]
  })

  // 2. 南丁格尔玫瑰饼图 (污染聚类)
  pieChart.setOption({
    tooltip: { trigger: 'item', ...glassTooltip },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { color: '#74807B', fontWeight: 500 } },
    series: [{
      type: 'pie', radius: ['30%', '70%'], center: ['50%', '45%'], roseType: 'area',
      itemStyle: { borderRadius: 8, borderColor: '#F4F6F5', borderWidth: 2, shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.05)' },
      label: { show: false },
      data: [
        { value: 45, name: '优', itemStyle: { color: '#2AA876' } },
        { value: 30, name: '良', itemStyle: { color: '#85C77A' } },
        { value: 15, name: '轻度', itemStyle: { color: '#F5A623' } },
        { value: 8, name: '中度', itemStyle: { color: '#E87A31' } },
        { value: 2, name: '重度', itemStyle: { color: '#D9534F' } }
      ],
      animationEasing: 'elasticOut', animationDuration: 2000
    }]
  })

  // 3. 多维物理雷达图 (指标剖析)
  radarChart.setOption({
    tooltip: { ...glassTooltip },
    radar: {
      indicator: [
        { name: 'SO₂', max: 100 }, { name: 'NO₂', max: 100 },
        { name: 'PM10', max: 150 }, { name: 'PM2.5', max: 150 },
        { name: 'O₃', max: 100 }, { name: 'CO', max: 10 }
      ],
      radius: '65%', center: ['50%', '50%'], splitNumber: 4,
      axisName: { color: '#1C2421', fontWeight: 600, fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(28, 36, 33, 0.06)' } },
      splitArea: { show: false },
      axisLine: { lineStyle: { color: 'rgba(28, 36, 33, 0.06)' } }
    },
    series: [{
      type: 'radar', symbol: 'none',
      lineStyle: { width: 3, color: '#409EFF', shadowBlur: 12, shadowColor: 'rgba(64, 158, 255, 0.4)', shadowOffsetY: 4 },
      areaStyle: { color: 'rgba(64, 158, 255, 0.15)' },
      data: [{ value: [42, 35, 80, 65, 45, 4], name: '实时指标浓度' }],
      animationEasing: 'cubicOut', animationDuration: 2000
    }]
  })

  // 4. 圆角流体柱状图 (省级频次)
  barChart.setOption({
    tooltip: { trigger: 'axis', ...glassTooltip, axisPointer: { type: 'shadow' } },
    grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
    xAxis: {
      type: 'category', data: ['河北', '山西', '山东', '河南', '辽宁', '陕西'],
      axisLine: { show: false }, axisTick: { show: false },
      axisLabel: { color: '#74807B', margin: 16 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(28, 36, 33, 0.04)', type: 'dashed' } },
      axisLabel: { color: '#74807B' }
    },
    series: [{
      name: '异常预警频次', type: 'bar', barWidth: 24,
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#F5A623' },
          { offset: 1, color: 'rgba(245, 166, 35, 0.1)' }
        ])
      },
      data: [155, 199, 189, 170, 118, 157],
      animationEasing: 'elasticOut', animationDuration: 2000,
      animationDelay: (idx) => idx * 100
    }]
  })

  chartInstances.value = [lineChart, pieChart, radarChart, barChart]
}

const handleResize = () => {
  chartInstances.value.forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.value.forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style scoped>
/* =========================================
   核心空间调度 (继承系统主框架规范)
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

/* 全局唯一滑动区 */
.scroll-area { flex: 1; overflow-y: auto; scrollbar-width: none; -ms-overflow-style: none; padding: 24px 32px 32px; }
.scroll-area::-webkit-scrollbar { display: none; }

/* =========================================
   1. 悬浮控制台 (严苛的高低差抹平)
   ========================================= */
.action-console {
  height: 120px; padding: 0 48px; display: flex; justify-content: space-between; align-items: center; box-sizing: border-box;
}

.console-left { display: flex; align-items: center; gap: 20px; }
.title-group { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 500; margin: 0; letter-spacing: 0.5px; color: #1C2421; }
.page-subtitle { font-size: 14px; font-weight: 500; color: #74807B; }

.console-right { display: flex; align-items: center; gap: 16px; }

.swiss-btn {
  border: none; outline: none; border-radius: 10px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-size: 14px; font-weight: 600; transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.swiss-btn.ghost-btn { padding: 0 20px; height: 40px; background: rgba(255, 255, 255, 0.6); color: #1C2421; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.swiss-btn.ghost-btn:hover { background: #FFF; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.swiss-btn.primary-btn { padding: 0 24px; height: 40px; background: #007AFF; color: #FFF; box-shadow: 0 2px 10px rgba(0, 122, 255, 0.3); }
.swiss-btn.primary-btn:hover { background: #0066D6; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(0, 122, 255, 0.4); }

/* =========================================
   2. KPI 数据网格
   ========================================= */
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; margin-bottom: 24px; }
.kpi-card {
  background: rgba(255, 255, 255, 0.4); border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px; padding: 24px; display: flex; align-items: center; gap: 16px;
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1); box-shadow: 0 4px 16px -4px rgba(0,0,0,0.02);
}
.kpi-card:hover { transform: translateY(-2px); background: #FFF; box-shadow: 0 8px 24px -8px rgba(0,0,0,0.06); }

.kpi-icon-box { width: 48px; height: 48px; border-radius: 14px; display: flex; justify-content: center; align-items: center; font-size: 24px; }
.kpi-content { display: flex; flex-direction: column; gap: 4px; }
.kpi-value { font-size: 28px; font-weight: 700; font-family: "SF Pro Display", sans-serif; color: #1C2421; line-height: 1;}
.kpi-value .unit { font-size: 14px; font-weight: 600; color: #86868B; margin-left: 2px;}
.kpi-label { font-size: 13px; font-weight: 600; color: #74807B; }

/* =========================================
   3. ECharts 图表便当盒 (生物呼吸材质引擎)
   ========================================= */
.chart-bento-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); grid-auto-rows: 360px; gap: 24px;
}
.col-span-2 { grid-column: span 2; }

.chart-bento-card {
  position: relative; overflow: hidden;
  background: rgba(255, 255, 255, 0.45); border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px; padding: 24px; box-shadow: 0 4px 16px -4px rgba(0,0,0,0.02);
  display: flex; flex-direction: column;
  transition: all 0.5s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.chart-bento-card:hover { transform: translateY(-2px) scale(1.005); box-shadow: 0 12px 32px -8px rgba(0,0,0,0.06); background: #FFF; }

/* 生物态液态光晕 */
.breathe-glow {
  position: absolute; border-radius: 50%; filter: blur(50px); opacity: 0.15;
  z-index: 0; pointer-events: none;
  animation: liquid-morph 12s infinite alternate cubic-bezier(0.45, 0.05, 0.55, 0.95);
}
.glow-primary { background: #2AA876; width: 60%; height: 60%; top: -10%; left: -10%; }
.glow-secondary { background: #409EFF; width: 50%; height: 50%; bottom: -10%; right: -10%; animation-delay: -4s; }
.glow-warning { background: #F5A623; width: 55%; height: 55%; top: 10%; right: -10%; }
.glow-danger { background: #D9534F; width: 60%; height: 60%; bottom: -10%; left: -10%; animation-delay: -6s; }

@keyframes liquid-morph {
  0% { border-radius: 40% 60% 70% 30% / 40% 40% 60% 50%; transform: scale(1) translate(0, 0); }
  50% { border-radius: 70% 30% 50% 50% / 30% 30% 70% 70%; transform: scale(1.1) translate(5%, 5%); }
  100% { border-radius: 40% 60% 70% 30% / 40% 40% 60% 50%; transform: scale(0.9) translate(-5%, -5%); }
}

.chart-header {
  position: relative; z-index: 1; display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: #1C2421; margin-bottom: 8px;
}
.chart-header .el-icon { color: #86868B; font-size: 16px; }

/* 保证 ECharts 渲染不塌陷 */
.chart-canvas { flex: 1; position: relative; z-index: 1; min-height: 0; width: 100%; }

@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-bento-grid { grid-template-columns: 1fr; }
  .col-span-2 { grid-column: span 1; }
  .action-console { padding: 0 24px; }
  .scroll-area { padding: 24px; }
}
</style>