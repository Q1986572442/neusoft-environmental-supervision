<template>
  <div class="profile-page" v-loading="loading">
    <!-- 头像卡片 -->
    <div class="profile-header">
      <div class="avatar-section">
        <avatar-upload v-model="avatar" @uploaded="saveAvatar" />
        <div class="user-title">
          <h2>{{ form.realName || '用户' }}</h2>
          <span class="role-badge" :class="'role-' + form.role">{{ roleLabel }}</span>
        </div>
      </div>
      <div class="header-stats">
        <div class="h-stat"><span class="val">{{ form.phone }}</span><span class="lbl">手机号</span></div>
        <div class="h-stat"><span class="val">{{ form.createTime ? form.createTime.substring(0,10) : '-' }}</span><span class="lbl">注册时间</span></div>
      </div>
    </div>

    <div class="profile-body">
      <!-- 基本信息 -->
      <div class="section-card">
        <h3>📝 基本信息</h3>
        <el-form :model="form" label-width="90px" size="default">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄">
                <el-input-number v-model="form.age" :min="16" :max="120" style="width:100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio-button :value="1">👨 男</el-radio-button>
                  <el-radio-button :value="0">👩 女</el-radio-button>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号">
                <el-input :model-value="form.phone" disabled />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="doSave">💾 保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码 -->
      <div class="section-card">
        <h3>🔒 修改密码</h3>
        <el-form :model="pwdForm" label-width="90px" size="default">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="原密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入原密码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="warning" :loading="changingPwd" @click="doChangePassword">🔄 修改密码（修改后需重新登录）</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUser, getUserById, changePassword } from '@/api/user'
import AvatarUpload from '@/components/AvatarUpload.vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const saving = ref(false)
const changingPwd = ref(false)
const loading = ref(true)
const avatar = ref('')
const userId = ref(null)

const form = ref({ phone: '', realName: '', age: 25, gender: 1, role: '', createTime: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '' })

const roleLabel = computed(() => {
  const map = { NEPS: '公众监督员', NEPG: '网格员', NEPM: '系统管理员', NEPV: '决策者' }
  return map[form.value.role] || '用户'
})

async function loadProfile() {
  const uid = localStorage.getItem('userId')
  if (!uid) return
  userId.value = uid
  try {
    const res = await getUserById(uid)
    const u = res.data
    form.value = { phone: u.phone || '', realName: u.realName || '', age: u.age || 25, gender: u.gender ?? 1, role: u.role || '', createTime: u.createTime || '' }
    avatar.value = u.avatar || ''
  } catch(e) {} finally { loading.value = false }
}

function saveAvatar(url) { avatar.value = url; doSave({ avatar: url }) }

async function doSave(extra) {
  saving.value = true
  try {
    const data = { realName: form.value.realName, age: form.value.age, gender: form.value.gender }
    if (extra?.avatar) data.avatar = extra.avatar
    if (avatar.value && !data.avatar) data.avatar = avatar.value
    await updateUser(userId.value, data)
    ElMessage.success('保存成功')
    await userStore.fetchUser()
  } catch(e) {} finally { saving.value = false }
}

async function doChangePassword() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.value.newPassword.length < 6) { ElMessage.warning('新密码至少6位'); return }
  changingPwd.value = true
  try {
    await changePassword({ userId: userId.value, oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.clear()
    router.push('/login')
  } catch(e) {} finally { changingPwd.value = false }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { max-width:800px; margin:0 auto; }
.profile-header { background:linear-gradient(135deg,#0c8c3f,#1a6b3a); padding:40px; border-radius:16px; color:#fff; display:flex; justify-content:space-between; align-items:center; margin-bottom:24px; }
.avatar-section { display:flex; align-items:center; gap:20px; }
.user-title h2 { margin:0 0 6px; font-size:22px; }
.role-badge { padding:4px 14px; border-radius:10px; font-size:12px; font-weight:600; background:rgba(255,255,255,0.2); }
.header-stats { display:flex; gap:40px; }
.h-stat { text-align:center; }
.h-stat .val { display:block; font-size:16px; font-weight:600; }
.h-stat .lbl { display:block; font-size:12px; opacity:0.7; margin-top:4px; }

.profile-body { display:flex; flex-direction:column; gap:20px; }
.section-card { background:#fff; padding:28px; border-radius:14px; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.section-card h3 { margin:0 0 20px; font-size:16px; color:#333; }
</style>
