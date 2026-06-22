<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTeamDetail, getTeamMembers, applyToJoin, removeMember, getApplications, reviewApplication, deleteTeam } from '@/api/team'
import { getTeamProjects, createProject, deleteProject } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const teamId = computed(() => Number(route.params.teamId))
const team = ref(null)
const members = ref([])
const projects = ref([])
const applications = ref([])
const loading = ref(true)
const applyMsg = ref('')
const applyVisible = ref(false)
const createProjectVisible = ref(false)
const newProjectName = ref('')
const newProjectDesc = ref('')

const isLeader = computed(() => {
  const me = members.value.find(m => m.userId === userStore.userInfo?.userId)
  return me?.role === 'leader'
})

const isMember = computed(() => {
  return members.value.some(m => m.userId === userStore.userInfo?.userId)
})

async function fetchData() {
  loading.value = true
  try {
    const [tRes, mRes, pRes] = await Promise.all([
      getTeamDetail(teamId.value),
      getTeamMembers(teamId.value),
      getTeamProjects(teamId.value)
    ])
    team.value = tRes.data
    members.value = mRes.data || []
    projects.value = pRes.data?.records || []
    if (isLeader.value) {
      const aRes = await getApplications(teamId.value, { status: 'pending' })
      applications.value = aRes.data?.records || []
    }
  } finally { loading.value = false }
}

onMounted(fetchData)

async function handleApply() {
  await applyToJoin(teamId.value, { message: applyMsg.value })
  ElMessage.success('申请已提交')
  applyVisible.value = false
  applyMsg.value = ''
}

async function handleRemove(userId) {
  await ElMessageBox.confirm('确定要移除该成员吗？', '确认', { type: 'warning' })
  await removeMember(teamId.value, userId)
  ElMessage.success('已移除')
  fetchData()
}

async function handleReview(requestId, action) {
  await reviewApplication(teamId.value, requestId, { action })
  ElMessage.success(action === 'approve' ? '已通过' : '已拒绝')
  fetchData()
}

async function handleCreateProject() {
  if (!newProjectName.value.trim()) return ElMessage.warning('请输入项目名称')
  await createProject(teamId.value, { name: newProjectName.value, description: newProjectDesc.value })
  ElMessage.success('项目创建成功')
  createProjectVisible.value = false
  newProjectName.value = ''
  newProjectDesc.value = ''
  fetchData()
}

async function handleDeleteProject(projectId) {
  await ElMessageBox.confirm('确定要删除该项目吗？', '确认', { type: 'warning' })
  await deleteProject(projectId)
  ElMessage.success('已删除')
  fetchData()
}

async function handleDeleteTeam() {
  await ElMessageBox.confirm('确定要解散该团队吗？此操作不可逆！', '确认解散', { type: 'error', confirmButtonText: '确认解散' })
  await deleteTeam(teamId.value)
  ElMessage.success('团队已解散')
  router.push('/teams')
}
</script>

<template>
  <div class="team-detail" v-loading="loading">
    <div v-if="team">
      <!-- 基本信息 -->
      <el-card class="section-card">
        <div class="team-header">
          <div>
            <h3>{{ team.name }}</h3>
            <el-tag :type="team.status==='recruiting'?'success':'info'">{{ team.status==='recruiting'?'招募中':'已关闭' }}</el-tag>
            <span style="margin-left:12px;color:#999">成员 {{ team.currentMembers }} / {{ team.maxMembers }}</span>
          </div>
          <div>
            <el-button v-if="!isMember && team.status==='recruiting'" type="primary" @click="applyVisible = true">申请加入</el-button>
            <el-button v-if="isLeader" type="danger" @click="handleDeleteTeam">解散团队</el-button>
          </div>
        </div>
        <p style="color:#666;margin-top:12px">{{ team.description || '暂无简介' }}</p>
        <div style="margin-top:8px">
          <el-tag v-for="sk in (team.requiredSkills||[])" :key="sk.skillId" effect="plain" type="info" style="margin-right:6px">
            {{ sk.skillName }}
          </el-tag>
        </div>
      </el-card>

      <el-row :gutter="16">
        <!-- 成员列表 -->
        <el-col :span="12">
          <el-card class="section-card">
            <template #header><span>团队成员（{{ members.length }}）</span></template>
            <div v-for="m in members" :key="m.userId" class="member-item">
              <span class="member-avatar">{{ m.username?.charAt(0)?.toUpperCase() }}</span>
              <span class="member-name">{{ m.username }}</span>
              <el-tag :type="m.role==='leader'?'danger':'primary'" size="small">{{ m.role==='leader'?'队长':'成员' }}</el-tag>
              <el-button v-if="isLeader && m.role!=='leader'" type="danger" link size="small" @click="handleRemove(m.userId)">移除</el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 项目列表 -->
        <el-col :span="12">
          <el-card class="section-card">
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span>项目（{{ projects.length }}）</span>
                <el-button v-if="isLeader" type="primary" size="small" @click="createProjectVisible = true">创建项目</el-button>
              </div>
            </template>
            <div v-if="projects.length">
              <div v-for="p in projects" :key="p.projectId" class="project-item">
                <div>
                  <span class="project-name" @click="router.push(`/projects/${p.projectId}`)">{{ p.name }}</span>
                  <p style="color:#999;font-size:12px">{{ p.description || '' }}</p>
                </div>
                <span style="font-size:13px;color:#67c23a">{{ p.taskStats?.completionRate || 0 }}%</span>
                <el-button v-if="isLeader" type="danger" link size="small" @click="handleDeleteProject(p.projectId)">删除</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无项目" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 入队申请审核 -->
      <el-card v-if="isLeader && applications.length" class="section-card">
        <template #header><span>待审核申请（{{ applications.length }}）</span></template>
        <el-table :data="applications" stripe size="small">
          <el-table-column prop="username" label="申请人" width="120" />
          <el-table-column prop="message" label="留言" min-width="200" />
          <el-table-column label="技能" width="200">
            <template #default="{ row }">
              <el-tag v-for="sk in (row.userSkills||[])" :key="sk.skillId" size="small" style="margin-right:4px">
                {{ sk.skillName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleReview(row.requestId, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row.requestId, 'reject')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 申请加入弹窗 -->
      <el-dialog v-model="applyVisible" title="申请加入团队" width="420px">
        <el-input v-model="applyMsg" type="textarea" :rows="3" placeholder="介绍一下自己，增加通过率..." maxlength="500" show-word-limit />
        <template #footer>
          <el-button @click="applyVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApply">提交申请</el-button>
        </template>
      </el-dialog>

      <!-- 创建项目弹窗 -->
      <el-dialog v-model="createProjectVisible" title="创建项目" width="420px">
        <el-form>
          <el-form-item label="项目名称" required>
            <el-input v-model="newProjectName" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="项目描述">
            <el-input v-model="newProjectDesc" type="textarea" :rows="2" placeholder="请输入项目描述" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="createProjectVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateProject">创建</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.team-detail { max-width: 1100px; margin: 0 auto; }
.section-card { margin-bottom: 16px; }
.team-header { display: flex; justify-content: space-between; align-items: center; }
.member-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.member-avatar {
  width: 32px; height: 32px; border-radius: 50%; background: #409eff;
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px;
}
.member-name { flex: 1; font-weight: 500; }
.project-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.project-name { font-weight: 500; color: #409eff; cursor: pointer; flex: 1; }
</style>
