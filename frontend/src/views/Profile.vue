<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword, addSkill, removeSkill } from '@/api/user'
import request from '@/api/index'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const profileForm = ref({
  avatar: '',
  bio: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: ''
})

const skills = ref([])
const allSkills = ref([])

onMounted(async () => {
  if (userStore.userInfo) {
    profileForm.value.avatar = userStore.userInfo.avatar || ''
    profileForm.value.bio = userStore.userInfo.bio || ''
    skills.value = userStore.userInfo.skills || []
  }
  const res = await request.get('/skills', { params: { pageSize: 200 } })
  allSkills.value = res.data?.records || []
})

async function handleUpdateProfile() {
  await updateProfile(profileForm.value)
  ElMessage.success('资料已更新')
  userStore.fetchUserInfo()
}

async function handleChangePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    return ElMessage.warning('请填写完整')
  }
  await changePassword(pwdForm.value)
  ElMessage.success('密码已修改')
  pwdForm.value = { oldPassword: '', newPassword: '' }
}

async function handleAddSkill() {
  // 简单起见，用第一个未添加的技能
  const existingIds = skills.value.map(s => s.skillId)
  const available = allSkills.value.filter(s => !existingIds.includes(s.skillId))
  if (!available.length) return ElMessage.warning('所有技能已添加')
  await addSkill({ skillId: available[0].skillId, level: 'beginner' })
  ElMessage.success('技能已添加')
  userStore.fetchUserInfo()
}

async function handleRemoveSkill(skillId) {
  await removeSkill(skillId)
  ElMessage.success('技能已移除')
  userStore.fetchUserInfo()
}
</script>

<template>
  <div class="profile-page">
    <h3>个人中心</h3>

    <el-row :gutter="16">
      <!-- 基本资料 -->
      <el-col :span="14">
        <el-card class="section-card">
          <template #header><span>基本资料</span></template>
          <el-form label-width="80px">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.userInfo?.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input :model-value="userStore.userInfo?.email" disabled />
            </el-form-item>
            <el-form-item label="头像URL">
              <el-input v-model="profileForm.avatar" placeholder="输入头像图片地址" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="section-card">
          <template #header><span>修改密码</span></template>
          <el-form label-width="80px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 技能管理 -->
      <el-col :span="10">
        <el-card class="section-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>我的技能</span>
              <el-button type="primary" size="small" @click="handleAddSkill">添加技能</el-button>
            </div>
          </template>
          <div v-if="skills.length">
            <el-tag
              v-for="sk in skills" :key="sk.skillId"
              closable size="large" style="margin:0 8px 8px 0"
              @close="handleRemoveSkill(sk.skillId)"
            >
              {{ sk.skillName }}
              <span style="color:#999;font-size:12px">·{{ { beginner:'初学', intermediate:'掌握', advanced:'精通' }[sk.level] }}</span>
            </el-tag>
          </div>
          <el-empty v-else description="暂无技能" />
        </el-card>

        <!-- 账户信息 -->
        <el-card class="section-card">
          <template #header><span>账户信息</span></template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="用户ID">{{ userStore.userInfo?.userId }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userStore.isAdmin?'danger':'primary'" size="small">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ userStore.userInfo?.createdAt }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.profile-page { max-width: 1000px; margin: 0 auto; }
h3 { margin-bottom: 16px; }
.section-card { margin-bottom: 16px; }
</style>
