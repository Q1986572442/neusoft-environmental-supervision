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
        <button class="swiss-btn ghost-btn" @click="loadAllData" :disabled="loading">
          <el-icon><Refresh /></el-icon> 刷新数据
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

      <div class="panel-body scroll-area" v-loading="loading" element-loading-text="数据加载中...">

        <div class="kpi-grid">
          <div class="kpi-card" v-for="kpi in kpiList" :key="kpi.kpiKey">
            <div class="kpi-icon-box" :style="{ color: kpi.color, backgroundColor: kpi.color + '1A' }">
              <el-icon><component :is="kpi.icon" /></el-icon>
            </div>
            <div class="kpi-content">
              <div class="kpi-value">{{ formatKpiValue(kpi.kpiValue) }} <span class="unit">{{ kpi.kpiUnit }}</span></div>
              <div class="kpi-label">{{ kpi.kpiLabel }}</div>
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

          <!-- 省级污染异常空间热力图（GIS 地图升级） -->
          <div class="chart-bento-card col-span-2">
            <div class="breathe-glow glow-warning"></div>
            <div class="breathe-glow glow-secondary"></div>

            <div class="chart-header">
              <el-icon><MapLocation /></el-icon> 省级污染异常空间热力图
            </div>
            <div class="chart-canvas" ref="mapChartRef"></div>
          </div>

        </div>

      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, shallowRef, onMounted, onUnmounted, markRaw, nextTick } from 'vue'
import * as echarts from 'echarts'
import { debounce } from 'lodash-es'
import {
  Filter, Download, DataAnalysis, Location, Warning,
  CircleCheck, Odometer, TrendCharts, PieChart, Aim, Histogram,
  Refresh, MapLocation
} from '@element-plus/icons-vue'
import { getDashboardAll } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const lineChartRef = ref(null)
const pieChartRef = ref(null)
const radarChartRef = ref(null)
const mapChartRef = ref(null)

const chartInstances = shallowRef([])
const loading = ref(false)
const geoJsonLoaded = ref(false)

let chinaGeoJson = null

const iconMap = {
  pending_todo: Location,
  recent_visits: Warning,
  data_accuracy: CircleCheck,
  safe_days: Odometer
}

const colorMap = {
  pending_todo: '#409EFF',
  recent_visits: '#F5A623',
  data_accuracy: '#2AA876',
  safe_days: '#85C77A'
}

const kpiList = ref([])

const defaultKpis = [
  { kpiKey: 'pending_todo', kpiLabel: '待处理待办数量', kpiValue: 1024, kpiUnit: '个', icon: Location, color: '#409EFF' },
  { kpiKey: 'recent_visits', kpiLabel: '近三十日访问次数', kpiValue: 238, kpiUnit: '次', icon: Warning, color: '#F5A623' },
  { kpiKey: 'data_accuracy', kpiLabel: '污染源数据准确率', kpiValue: 94.2, kpiUnit: '%', icon: CircleCheck, color: '#2AA876' },
  { kpiKey: 'safe_days', kpiLabel: '连续安全运行天数', kpiValue: 286, kpiUnit: '天', icon: Odometer, color: '#85C77A' }
]

kpiList.value = defaultKpis.map(k => ({ ...k, icon: iconMap[k.kpiKey] || Location, color: colorMap[k.kpiKey] || '#409EFF' }))

const glassTooltip = {
  backgroundColor: 'rgba(255, 255, 255, 0.85)',
  borderColor: 'rgba(255, 255, 255, 0.9)',
  borderWidth: 1,
  padding: [12, 16],
  textStyle: { color: '#1C2421', fontSize: 13, fontWeight: 500, fontFamily: 'SF Pro Display' },
  extraCssText: 'backdrop-filter: blur(24px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-radius: 12px;'
}

function formatKpiValue(val) {
  if (val === null || val === undefined) return '-'
  const num = Number(val)
  if (isNaN(num)) return val
  if (Number.isInteger(num)) return num.toLocaleString()
  return num.toFixed(1)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

/**
 * 加载中国 GeoJSON 地图数据（从阿里云 DataV 地理 API）
 */
async function loadChinaGeoJson() {
  if (chinaGeoJson) return chinaGeoJson
  try {
    const res = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    chinaGeoJson = await res.json()
    echarts.registerMap('china', chinaGeoJson)
    geoJsonLoaded.value = true
    return chinaGeoJson
  } catch (e) {
    console.warn('中国地图 GeoJSON 加载失败，回退至柱状图:', e)
    return null
  }
}

/**
 * 省份名称 → 英文名映射（用于 GeoJSON 匹配）
 */
const provinceNameMap = {
  '北京市': '北京', '天津市': '天津', '河北省': '河北', '山西省': '山西',
  '辽宁省': '辽宁', '吉林省': '吉林', '黑龙江省': '黑龙江', '上海市': '上海',
  '江苏省': '江苏', '浙江省': '浙江', '安徽省': '安徽', '福建省': '福建',
  '江西省': '江西', '山东省': '山东', '河南省': '河南', '湖北省': '湖北',
  '湖南省': '湖南', '广东省': '广东', '广西壮族自治区': '广西', '海南省': '海南',
  '重庆市': '重庆', '四川省': '四川', '贵州省': '贵州', '云南省': '云南',
  '陕西省': '陕西', '甘肃省': '甘肃', '青海省': '青海',
  '宁夏回族自治区': '宁夏', '新疆维吾尔自治区': '新疆',
  '内蒙古自治区': '内蒙古', '西藏自治区': '西藏', '台湾省': '台湾'
}

function getShortName(fullName) {
  return provinceNameMap[fullName] || fullName
}

const initCharts = (data) => {
  // 销毁旧实例
  chartInstances.value.forEach(c => c?.dispose())
  chartInstances.value = []

  const lineChart = markRaw(echarts.init(lineChartRef.value))
  const pieChart = markRaw(echarts.init(pieChartRef.value))
  const radarChart = markRaw(echarts.init(radarChartRef.value))
  const mapChart = markRaw(echarts.init(mapChartRef.value))

  // --- 折线图：AQI 趋势 ---
  const aqiList = data?.aqiList || []
  const lineDates = aqiList.map(item => formatDate(item.recordDate))
  const lineValues = aqiList.map(item => item.aqiValue)

  lineChart.setOption({
    tooltip: { trigger: 'axis', ...glassTooltip },
    grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
    xAxis: {
      type: 'category', boundaryGap: false,
      data: lineDates.length ? lineDates : ['06-01', '06-02', '06-03', '06-04', '06-05', '06-06', '06-07'],
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
      data: lineValues.length ? lineValues : [42, 65, 58, 85, 120, 95, 62],
      animationEasing: 'elasticOut', animationDuration: 2400
    }]
  })

  // --- 饼图：污染等级分布 ---
  const levelList = data?.pollutionLevels || []
  const pieData = levelList.length
    ? levelList.map(item => ({
        value: item.levelValue,
        name: item.levelName,
        itemStyle: { color: item.levelColor || '#999' }
      }))
    : [
        { value: 45, name: '优', itemStyle: { color: '#2AA876' } },
        { value: 30, name: '良', itemStyle: { color: '#85C77A' } },
        { value: 15, name: '轻度', itemStyle: { color: '#F5A623' } },
        { value: 8, name: '中度', itemStyle: { color: '#E87A31' } },
        { value: 2, name: '重度', itemStyle: { color: '#D9534F' } }
      ]

  pieChart.setOption({
    tooltip: { trigger: 'item', ...glassTooltip },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, textStyle: { color: '#74807B', fontWeight: 500 } },
    series: [{
      type: 'pie', radius: ['30%', '70%'], center: ['50%', '45%'], roseType: 'area',
      itemStyle: { borderRadius: 8, borderColor: '#F4F6F5', borderWidth: 2, shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.05)' },
      label: { show: false },
      data: pieData,
      animationEasing: 'elasticOut', animationDuration: 2000
    }]
  })

  // --- 雷达图：健康指标 ---
  const indicatorList = data?.healthIndicators || []
  const radarIndicators = indicatorList.length
    ? indicatorList.map(item => ({ name: item.indicatorName, max: Number(item.maxValue) || 100 }))
    : [
        { name: 'SO₂', max: 100 }, { name: 'NO₂', max: 100 },
        { name: 'PM10', max: 150 }, { name: 'PM2.5', max: 150 },
        { name: 'O₃', max: 100 }, { name: 'CO', max: 10 }
      ]
  const radarValues = indicatorList.length
    ? indicatorList.map(item => Number(item.indicatorValue) || 0)
    : [42, 35, 80, 65, 45, 4]

  radarChart.setOption({
    tooltip: { ...glassTooltip },
    radar: {
      indicator: radarIndicators,
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
      data: [{ value: radarValues, name: '实时指标浓度' }],
      animationEasing: 'cubicOut', animationDuration: 2000
    }]
  })

  // --- 省级异常：中国地图热力图（GIS 升级）或柱状图回退 ---
  const provinceList = data?.provinceAnomalies || []
  const barNames = provinceList.length ? provinceList.map(item => item.provinceName) : ['河北', '山西', '山东', '河南', '辽宁', '陕西']
  const barValues = provinceList.length ? provinceList.map(item => item.anomalyCount) : [155, 199, 189, 170, 118, 157]

  if (geoJsonLoaded.value && chinaGeoJson) {
    // GIS 地图热力图模式
    const mapData = provinceList.length
      ? provinceList.map(item => ({
          name: getShortName(item.provinceName),
          value: item.anomalyCount
        }))
      : [
          { name: '河北', value: 155 }, { name: '山西', value: 199 },
          { name: '山东', value: 189 }, { name: '河南', value: 170 },
          { name: '辽宁', value: 118 }, { name: '陕西', value: 157 }
        ]

    mapChart.setOption({
      tooltip: {
        trigger: 'item',
        ...glassTooltip,
        formatter: (params) => {
          if (params.data) {
            return `<strong>${params.name}</strong><br/>异常预警频次: <strong>${params.data.value ?? params.value ?? '无数据'}</strong>`
          }
          return `${params.name}: 暂无数据`
        }
      },
      visualMap: {
        min: 0,
        max: Math.max(...barValues, 200),
        left: 'left',
        bottom: 10,
        text: ['高', '低'],
        inRange: {
          color: ['#E8F5E9', '#FFF9C4', '#FFE0B2', '#FFAB91', '#EF9A9A', '#D9534F']
        },
        calculable: true,
        textStyle: { color: '#74807B', fontSize: 11 }
      },
      series: [{
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.15,
        center: [104.5, 36],
        aspectScale: 0.8,
        layoutCenter: ['50%', '50%'],
        layoutSize: '130%',
        label: {
          show: true,
          color: '#5C6B7A',
          fontSize: 10,
          fontWeight: 500
        },
        emphasis: {
          label: {
            show: true,
            color: '#1C2421',
            fontSize: 13,
            fontWeight: 700
          },
          itemStyle: {
            areaColor: '#FFE082',
            shadowBlur: 20,
            shadowColor: 'rgba(0,0,0,0.2)'
          }
        },
        itemStyle: {
          borderColor: 'rgba(255,255,255,0.9)',
          borderWidth: 1.5,
          areaColor: '#F0F4F3'
        },
        data: mapData,
        animationEasing: 'cubicOut',
        animationDuration: 2000
      }]
    })
  } else {
    // 回退：柱状图模式
    mapChart.setOption({
      tooltip: { trigger: 'axis', ...glassTooltip, axisPointer: { type: 'shadow' } },
      grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
      xAxis: {
        type: 'category', data: barNames,
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
        data: barValues,
        animationEasing: 'elasticOut', animationDuration: 2000,
        animationDelay: (idx) => idx * 100
      }]
    })
  }

  chartInstances.value = [lineChart, pieChart, radarChart, mapChart]
}

// 防抖处理窗口 resize —— 避免拖拽时高频触发 ECharts resize 导致卡顿
const handleResize = debounce(() => {
  chartInstances.value.forEach(chart => {
    if (chart && !chart.isDisposed()) chart.resize()
  })
}, 200)

const loadAllData = async () => {
  loading.value = true
  try {
    // 并行加载 GeoJSON 和业务数据
    const [res] = await Promise.all([
      getDashboardAll(),
      loadChinaGeoJson()
    ])
    const data = res.data || {}

    if (data.kpis && data.kpis.length) {
      kpiList.value = data.kpis.map(k => ({
        ...k,
        icon: iconMap[k.kpiKey] || Location,
        color: colorMap[k.kpiKey] || '#409EFF'
      }))
    }

    await nextTick()
    initCharts(data)
  } catch (error) {
    console.error('加载大屏数据失败:', error)
    ElMessage.error('数据加载失败，使用默认数据展示')

    kpiList.value = defaultKpis.map(k => ({
      ...k,
      icon: iconMap[k.kpiKey] || Location,
      color: colorMap[k.kpiKey] || '#409EFF'
    }))

    await nextTick()
    initCharts(null)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 取消防抖，避免内存泄漏
  handleResize.cancel()
  chartInstances.value.forEach(chart => {
    if (chart && !chart.isDisposed()) chart.dispose()
  })
})
</script>

<style scoped>
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

.scroll-area { flex: 1; overflow-y: auto; scrollbar-width: none; -ms-overflow-style: none; padding: 24px 32px 32px; }
.scroll-area::-webkit-scrollbar { display: none; }

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
.swiss-btn.ghost-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
.swiss-btn.primary-btn { padding: 0 24px; height: 40px; background: #007AFF; color: #FFF; box-shadow: 0 2px 10px rgba(0, 122, 255, 0.3); }
.swiss-btn.primary-btn:hover { background: #0066D6; transform: translateY(-1px); box-shadow: 0 6px 16px rgba(0, 122, 255, 0.4); }

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

.chart-canvas { flex: 1; position: relative; z-index: 1; min-height: 0; width: 100%; }

@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-bento-grid { grid-template-columns: 1fr; }
  .col-span-2 { grid-column: span 1; }
  .action-console { padding: 0 24px; }
  .scroll-area { padding: 24px; }
}
</style>
