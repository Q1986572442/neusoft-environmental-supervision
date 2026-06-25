<template>
  <div class="map-view-container">
    <!-- 顶部控制栏 -->
    <div class="map-controls" v-if="currentLevel === 'city'">
      <el-button size="small" @click="backToChina" :icon="ArrowLeft">
        返回全国视图
      </el-button>
      <span class="current-label">{{ currentProvinceName }} — 市级AQI分布</span>
    </div>

    <!-- 地图容器 -->
    <div ref="chartRef" class="chart-box" v-loading="loading"></div>

    <!-- 图例 -->
    <div class="map-legend">
      <span class="legend-label">AQI 等级</span>
      <div class="legend-items">
        <div v-for="item in legendData" :key="item.label" class="legend-item">
          <span class="legend-color" :style="{ background: item.color }"></span>
          <span class="legend-text">{{ item.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getMapAqi } from '@/api/statistics'
import { ArrowLeft } from '@element-plus/icons-vue'

const chartRef = ref(null)
let chart = null
const loading = ref(false)

// 当前视图级别：china | city
const currentLevel = ref('china')
const currentProvinceName = ref('')
const currentProvinceCode = ref('')

// 数据
const cityData = ref([])
const provinceData = ref([])

// 缓存已加载的GeoJSON
const geoCache = {}

// AQI 颜色映射
const aqiColors = [
  { min: 0, max: 50, color: '#2AA876', label: '优 (0-50)' },
  { min: 51, max: 100, color: '#85C77A', label: '良 (51-100)' },
  { min: 101, max: 150, color: '#F5D76E', label: '轻度 (101-150)' },
  { min: 151, max: 200, color: '#F5A623', label: '中度 (151-200)' },
  { min: 201, max: 300, color: '#E87A31', label: '重度 (201-300)' },
  { min: 301, max: 999, color: '#D9534F', label: '严重 (300+)' }
]

const legendData = aqiColors

function getAqiColor(value) {
  if (!value && value !== 0) return '#ccc'
  for (const item of aqiColors) {
    if (value <= item.max) return item.color
  }
  return '#7B2D8E'
}

// ============ GeoJSON 加载 ============

const CHINA_GEO_URL = 'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json'

async function loadGeoJSON(url) {
  if (geoCache[url]) return geoCache[url]
  const res = await fetch(url)
  if (!res.ok) throw new Error(`加载地图数据失败: ${url}`)
  const json = await res.json()
  geoCache[url] = json
  return json
}

// 省份adcode映射（基于DataV的编码规则）
// 省级code = 省份id对应的编码，需要从data中匹配
function findProvinceCode(provinceName) {
  // 常见省份名称→adcode映射
  const nameMap = {
    '北京': 110000, '北京市': 110000,
    '天津': 120000, '天津市': 120000,
    '河北': 130000, '河北省': 130000,
    '山西': 140000, '山西省': 140000,
    '内蒙古': 150000, '内蒙古自治区': 150000,
    '辽宁': 210000, '辽宁省': 210000,
    '吉林': 220000, '吉林省': 220000,
    '黑龙江': 230000, '黑龙江省': 230000,
    '上海': 310000, '上海市': 310000,
    '江苏': 320000, '江苏省': 320000,
    '浙江': 330000, '浙江省': 330000,
    '安徽': 340000, '安徽省': 340000,
    '福建': 350000, '福建省': 350000,
    '江西': 360000, '江西省': 360000,
    '山东': 370000, '山东省': 370000,
    '河南': 410000, '河南省': 410000,
    '湖北': 420000, '湖北省': 420000,
    '湖南': 430000, '湖南省': 430000,
    '广东': 440000, '广东省': 440000,
    '广西': 450000, '广西壮族自治区': 450000,
    '海南': 460000, '海南省': 460000,
    '重庆': 500000, '重庆市': 500000,
    '四川': 510000, '四川省': 510000,
    '贵州': 520000, '贵州省': 520000,
    '云南': 530000, '云南省': 530000,
    '西藏': 540000, '西藏自治区': 540000,
    '陕西': 610000, '陕西省': 610000,
    '甘肃': 620000, '甘肃省': 620000,
    '青海': 630000, '青海省': 630000,
    '宁夏': 640000, '宁夏回族自治区': 640000,
    '新疆': 650000, '新疆维吾尔自治区': 650000,
    '台湾': 710000, '台湾省': 710000,
    '香港': 810000, '香港特别行政区': 810000,
    '澳门': 820000, '澳门特别行政区': 820000,
  }
  return nameMap[provinceName] || null
}

// ============ 数据加载 ============

async function fetchData() {
  loading.value = true
  try {
    const res = await getMapAqi()
    cityData.value = res.data.cities || []
    provinceData.value = res.data.provinces || []
  } catch (e) {
    console.error('加载地图数据失败:', e)
    cityData.value = []
    provinceData.value = []
  } finally {
    loading.value = false
  }
}

// ============ 渲染 ============

async function renderChinaMap() {
  if (!chart) return

  loading.value = true
  try {
    // 加载并注册中国地图
    const chinaGeo = await loadGeoJSON(CHINA_GEO_URL)
    echarts.registerMap('china', chinaGeo)

    // 构建省份→AQI映射
    const provinceAqiMap = {}
    provinceData.value.forEach(p => {
      // 尝试多种名称匹配
      provinceAqiMap[p.provinceName] = p.avgAqi
      // 也添加简称（去掉"省"、"市"等后缀）
      const short = p.provinceName.replace(/省|市|自治区|维吾尔自治区|壮族自治区|回族自治区/g, '')
      provinceAqiMap[short] = p.avgAqi
    })

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          if (!params.data) return params.name
          const name = params.name
          const aqi = provinceAqiMap[name]
          const provData = provinceData.value.find(p =>
            p.provinceName === name || p.provinceName.startsWith(name) || name.startsWith(p.provinceName.replace(/省|市|自治区|维吾尔自治区|壮族自治区|回族自治区/g, ''))
          )
          if (aqi !== undefined) {
            return `<strong>${name}</strong><br/>平均AQI: <b style="color:${getAqiColor(aqi)}">${aqi}</b><br/>检测次数: ${provData?.totalDetections || 0}`
          }
          return `<strong>${name}</strong><br/>暂无数据`
        }
      },
      visualMap: {
        min: 0,
        max: 300,
        left: 'left',
        bottom: 20,
        text: ['高', '低'],
        inRange: {
          color: ['#2AA876', '#85C77A', '#F5D76E', '#F5A623', '#E87A31', '#D9534F']
        },
        calculable: true
      },
      series: [{
        name: '中国AQI分布',
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.2,
        center: [105, 36],
        emphasis: {
          label: { show: true, fontSize: 14 },
          itemStyle: { areaColor: '#ffb980' }
        },
        label: { show: false },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1.5
        },
        data: Object.entries(provinceAqiMap).map(([name, value]) => ({ name, value }))
      }]
    }

    chart.setOption(option, true)

    // 点击省份→下钻
    chart.off('click')
    chart.on('click', async (params) => {
      if (params.componentType === 'series' && currentLevel.value === 'china') {
        const name = params.name
        const code = findProvinceCode(name)
        if (code) {
          await drillToProvince(name, code)
        }
      }
    })

    currentLevel.value = 'china'
    currentProvinceName.value = ''
  } catch (e) {
    console.error('渲染中国地图失败:', e)
  } finally {
    loading.value = false
  }
}

async function drillToProvince(name, code) {
  if (!chart) return
  loading.value = true
  try {
    const url = `https://geo.datav.aliyun.com/areas_v3/bound/${code}_full.json`
    const provGeo = await loadGeoJSON(url)
    const mapName = `province_${code}`
    echarts.registerMap(mapName, provGeo)

    // 构建该省的城市AQI数据
    const cityAqiMap = {}
    cityData.value.forEach(c => {
      // 匹配该省份下的城市
      if (c.provinceName && (c.provinceName === name ||
          c.provinceName.startsWith(name.replace(/省|市|自治区|维吾尔自治区|壮族自治区|回族自治区/g, '')) ||
          name.startsWith(c.provinceName.replace(/省|市|自治区|维吾尔自治区|壮族自治区|回族自治区/g, '')))) {
        cityAqiMap[c.cityName] = { avgAqi: c.avgAqi, count: c.detectionCount }
      }
    })

    const dataPoints = Object.entries(cityAqiMap).map(([cityName, info]) => ({
      name: cityName,
      value: info.avgAqi,
      count: info.count
    }))

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          if (!params.data || !params.data.value) return params.name
          return `<strong>${params.name}</strong><br/>平均AQI: <b style="color:${getAqiColor(params.data.value)}">${params.data.value}</b><br/>检测次数: ${params.data.count || 0}`
        }
      },
      visualMap: {
        min: 0,
        max: 300,
        left: 'left',
        bottom: 20,
        text: ['高', '低'],
        inRange: {
          color: ['#2AA876', '#85C77A', '#F5D76E', '#F5A623', '#E87A31', '#D9534F']
        },
        calculable: true
      },
      series: [{
        name: `${name} 城市AQI`,
        type: 'map',
        map: mapName,
        roam: true,
        zoom: 1.2,
        emphasis: {
          label: { show: true, fontSize: 12 },
          itemStyle: { areaColor: '#ffb980' }
        },
        label: { show: true, fontSize: 10, color: '#333' },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        data: dataPoints
      }]
    }

    chart.setOption(option, true)
    currentLevel.value = 'city'
    currentProvinceName.value = name
    currentProvinceCode.value = code
  } catch (e) {
    console.error('下钻到省份失败:', e)
  } finally {
    loading.value = false
  }
}

async function backToChina() {
  await renderChinaMap()
}

// ============ 生命周期 ============

onMounted(async () => {
  await fetchData()
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    await renderChinaMap()
    window.addEventListener('resize', () => chart?.resize())
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', () => chart?.resize())
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.map-view-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 480px;
  border-radius: 16px;
  overflow: hidden;
  background: #f8fafc;
  border: 1px solid #e8edf2;
}

.map-controls {
  position: absolute;
  top: 12px;
  left: 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.chart-box {
  width: 100%;
  height: 100%;
  min-height: 480px;
}

.map-legend {
  position: absolute;
  right: 16px;
  bottom: 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  padding: 10px 14px;
  border: 1px solid #e8edf2;
  z-index: 10;
}

.legend-label {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  display: block;
  margin-bottom: 6px;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 14px;
  height: 10px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-text {
  font-size: 11px;
  color: #888;
}
</style>
