<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, updateUser, getAdminTeams, forceDeleteTeam, createSkill, deleteSkill } from '@/api/admin'
import request from '@/api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

// Tab
const activeTab = ref('users')

// 用户管理
const users = ref([])
const userTotal = ref(0)
const userPage = ref(1)

async function fetchUsers() {
  const res = await getAdminUsers({ pageNum: userPage.value, pageSize: 20 })
  users.value = res.data?.records || []
  userTotal.value = res.data?.total || 0
}

async function toggleUserStatus(user) {
  const newStatus = user.status === 'active' ? 'disabled' : 'active'
  await updateUser(user.userId, { status: newStatus })
  ElMessage.success(`用户已${newStatus === 'active' ? '启用' : '禁用'}`)
  fetchUsers()
}

// 团队管理
const teams = ref([])

async function fetchTeams() {
  const res = await getAdminTeams({ pageSize: 100 })
  teams.value = res.data?.records || []
}

async function handleForceDelete(teamId, name) {
  await ElMessageBox.confirm(`确定强制解散「${name}」吗？`, '警告', { type: 'error' })
  await forceDeleteTeam(teamId)
  ElMessage.success('团队已解散')
  fetchTeams()
}

// 技能库
const skills = ref([])
const newSkillName = ref('')
const newSkillCategory = ref('前端')
const categories = ['前端', '后端', '数据库', '移动端', '设计', 'DevOps', '其他']

async function fetchSkills() {
  const res = await request.get('/skills', { params: { pageSize: 500 } })
  skills.value = res.data?.records || []
}

async function handleCreateSkill() {
  if (!newSkillName.value.trim()) return ElMessage.warning('请输入技能名称')
  await createSkill({ skillName: newSkillName.value, category: newSkillCategory.value })
  ElMessage.success('技能已添加')
  newSkillName.value = ''
  fetchSkills()
}

async function handleDeleteSkill(skillId) {
  await ElMessageBox.confirm('确定删除该技能标签吗？', '确认', { type: 'warning' })
  await deleteSkill(skillId)
  ElMessage.success('技能已删除')
  fetchSkills()
}

onMounted(() => { fetchUsers(); fetchTeams(); fetchSkills() })
</script>

<template>
  <div class="admin-page">
    <h3>管理后台</h3>

    <el-tabs v-model="activeTab">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <el-card>
          <el-table :data="users" stripe size="small">
            <el-table-column prop="userId" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="130" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="role" label="角色" width="80">
              <template #default="{ row }">
                <el-tag :type="row.role==='admin'?'danger':'primary'" size="small">
                  {{ row.role==='admin'?'管理员':'用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status==='active'?'success':'danger'" size="small">
                  {{ row.status==='active'?'正常':'禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="170" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="toggleUserStatus(row)">
                  {{ row.status==='active'?'禁用':'启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="userTotal > 20"
            v-model:current-page="userPage" :page-size="20" :total="userTotal"
            layout="prev, pager, next" @current-change="fetchUsers" style="text-align:center;margin-top:12px"
          />
        </el-card>
      </el-tab-pane>

      <!-- 团队管理 -->
      <el-tab-pane label="团队管理" name="teams">
        <el-card>
          <el-table :data="teams" stripe size="small">
            <el-table-column prop="teamId" label="ID" width="70" />
            <el-table-column prop="name" label="团队名称" min-width="160" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status==='recruiting'?'success':'info'" size="small">
                  {{ row.status==='recruiting'?'招募中':'已关闭' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="人数" width="80">
              <template #default="{ row }">{{ row.currentMembers }}/{{ row.maxMembers }}</template>
            </el-table-column>
            <el-table-column prop="creatorName" label="创建者" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleForceDelete(row.teamId, row.name)">强制解散</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 技能库维护 -->
      <el-tab-pane label="技能库维护" name="skills">
        <el-card>
          <div style="display:flex;gap:8px;margin-bottom:16px">
            <el-input v-model="newSkillName" placeholder="新技能名称" style="width:200px" />
            <el-select v-model="newSkillCategory" style="width:120px">
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
            <el-button type="primary" @click="handleCreateSkill">添加技能</el-button>
          </div>
          <div>
            <el-tag
              v-for="sk in skills" :key="sk.skillId"
              closable size="large" style="margin:0 8px 8px 0"
              @close="handleDeleteSkill(sk.skillId)"
            >
              {{ sk.skillName }}
              <span style="color:#999;font-size:12px">·{{ sk.category }}</span>
            </el-tag>
          </div>
          <el-empty v-if="!skills.length" description="暂无技能标签" />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }
h3 { margin-bottom: 16px; }
</style>
