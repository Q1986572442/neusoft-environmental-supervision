<template>
  <div class="records-page">
    <h2>📊 我的历史检测记录</h2>
    <el-table :data="records" v-loading="loading" stripe>
      <el-table-column prop="id" label="编号" width="70"/>
      <el-table-column prop="feedbackId" label="关联案件" width="90"/>
      <el-table-column prop="so2Aqi" label="SO₂" width="80"/>
      <el-table-column prop="coAqi" label="CO" width="80"/>
      <el-table-column prop="pm25Aqi" label="PM2.5" width="80"/>
      <el-table-column prop="finalAqi" label="最终AQI" width="100">
        <template #default="{row}">
          <el-tag :type="row.finalAqi <= 50 ? 'success' : row.finalAqi <= 150 ? 'warning' : 'danger'">{{ row.finalAqi }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip/>
      <el-table-column prop="createTime" label="检测时间" width="160">
        <template #default="{row}">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyAqiRecords } from '@/api/aqi'

const records = ref([])
const loading = ref(false)

function formatTime(t) { if (!t) return ''; return t.replace('T', ' ').substring(0, 16) }

onMounted(async () => {
  loading.value = true
  const uid = localStorage.getItem('userId')
  try { const r = await getMyAqiRecords(Number(uid)); records.value = r.data || [] } catch(e) {}
  finally { loading.value = false }
})
</script>

<style scoped>
.records-page { max-width:1000px; margin:0 auto; }
.records-page h2 { margin:0 0 20px; font-size:20px; }
</style>
