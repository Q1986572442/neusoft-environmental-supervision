<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#1d1e2c">
      <div style="color:#fff;text-align:center;padding:20px 0;font-size:18px;font-weight:bold">
        ⚙️ NEP 管理员
      </div>
      <el-menu :default-active="activeMenu" router background-color="#1d1e2c" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/admin/dashboard"><el-icon><Odometer /></el-icon><span>管理首页</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/feedbacks"><el-icon><Document /></el-icon><span>反馈管理</span></el-menu-item>
        <el-menu-item index="/admin/news"><el-icon><Reading /></el-icon><span>新闻管理</span></el-menu-item>
        <el-menu-item index="/admin/statistics"><el-icon><PieChart /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/admin/notifications"><el-icon><Bell /></el-icon><span>系统通知</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between;padding:0 20px">
        <el-breadcrumb><el-breadcrumb-item>管理员端</el-breadcrumb-item><el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item></el-breadcrumb>
        <el-dropdown @command="handleCmd">
          <span style="cursor:pointer"><el-avatar :size="32" icon="UserFilled"/> {{ userStore.user?.realName || '管理员' }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background:#f0f2f5;padding:20px"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

function handleCmd(cmd) {
  if (cmd === 'logout') { userStore.logout(); router.push('/login') }
  else if (cmd === 'profile') router.push('/admin/dashboard')
}

onMounted(() => userStore.fetchUser())
</script>
