<template>
  <div>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="注册用户数" :value="stats.userCount"/></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="反馈总数" :value="stats.feedbackCount"/></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="待处理反馈" :value="stats.pendingCount"/></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><el-statistic title="AQI检测数" :value="stats.aqiCount"/></el-card></el-col>
    </el-row>
    <el-card shadow="never">
      <template #header><span>快捷操作</span></template>
      <el-button type="primary" @click="$router.push('/admin/users')">👥 用户管理</el-button>
      <el-button type="warning" @click="$router.push('/admin/feedbacks')">📋 反馈管理</el-button>
      <el-button type="success" @click="$router.push('/admin/statistics')">📊 数据统计</el-button>
      <el-button type="info" @click="$router.push('/admin/news')">📰 新闻管理</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/statistics'
import { getFeedbackPage } from '@/api/feedback'

const stats = ref({ userCount: 0, feedbackCount: 0, pendingCount: 0, aqiCount: 0 })

onMounted(async () => {
  try {
    const res = await getOverview()
    if (res.data) {
      stats.value.userCount = res.data.totalUsers || 0
      stats.value.aqiCount = res.data.totalDetections || 0
      stats.value.feedbackCount = res.data.totalFeedbacks || 0
      stats.value.pendingCount = res.data.pendingFeedbacks || 0
    }
  } catch (e) {
    // fallback
    try { const r = await getFeedbackPage(1, 1); stats.value.feedbackCount = r.total || 0 } catch (e) {}
    try { const r = await getFeedbackPage(1, 1, 'PENDING'); stats.value.pendingCount = r.total || 0 } catch (e) {}
  }
})
</script>
