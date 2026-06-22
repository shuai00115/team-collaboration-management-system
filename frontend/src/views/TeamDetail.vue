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

// 是否已登录
const isLoggedIn = computed(() => !!(localStorage.getItem('accessToken')))
// 是否队长/成员（有成员数据后才能判断）
const myRole = ref('')
const isLeader = computed(() => myRole.value === 'leader')
const isMember = computed(() => myRole.value === 'leader' || myRole.value === 'member')

const applyMsg = ref('')
const applyVisible = ref(false)
const createProjectVisible = ref(false)
const newProjectName = ref('')
const newProjectDesc = ref('')

function computeMyRole() {
  if (!userStore.userInfo) return ''
  const me = members.value.find(m => m.userId === userStore.userInfo.userId)
  return me ? me.role : ''
}

async function fetchData() {
  loading.value = true
  try {
    // 第一步：获取团队基本信息（公开接口，任何人可调用）
    const tRes = await getTeamDetail(teamId.value)
    team.value = tRes.data

    // 第二步：获取成员 & 项目（需要登录，失败则静默忽略）
    const results = await Promise.allSettled([
      getTeamMembers(teamId.value),
      getTeamProjects(teamId.value)
    ])
    if (results[0].status === 'fulfilled') {
      members.value = results[0].value.data || []
    } else {
      members.value = []
    }
    if (results[1].status === 'fulfilled') {
      projects.value = results[1].value.data?.records || []
    } else {
      projects.value = []
    }

    // 第三步：判断当前用户身份
    myRole.value = computeMyRole()

    // 第四步：队长才拉取待审核申请
    if (isLeader.value) {
      try {
        const aRes = await getApplications(teamId.value, { status: 'pending' })
        applications.value = aRes.data?.records || []
      } catch { applications.value = [] }
    }
  } catch {
    // 团队详情加载失败
    team.value = null
    ElMessage.error('团队不存在或已被解散')
  } finally { loading.value = false }
}

onMounted(fetchData)

async function handleApply() {
  // 未登录先跳转
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再申请加入团队')
    router.push('/login')
    return
  }
  try {
    await applyToJoin(teamId.value, { message: applyMsg.value })
    ElMessage.success('申请已提交，请等待队长审核')
    applyVisible.value = false
    applyMsg.value = ''
  } catch { /* error handled by interceptor */ }
}

async function handleRemove(userId) {
  try {
    await ElMessageBox.confirm('确定要移除该成员吗？', '确认', { type: 'warning' })
    await removeMember(teamId.value, userId)
    ElMessage.success('已移除')
    fetchData()
  } catch { /* cancelled or error */ }
}

async function handleReview(requestId, action) {
  try {
    await reviewApplication(teamId.value, requestId, { action })
    ElMessage.success(action === 'approve' ? '已通过' : '已拒绝')
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleCreateProject() {
  if (!newProjectName.value.trim()) return ElMessage.warning('请输入项目名称')
  try {
    await createProject(teamId.value, { name: newProjectName.value, description: newProjectDesc.value })
    ElMessage.success('项目创建成功')
    createProjectVisible.value = false
    newProjectName.value = ''
    newProjectDesc.value = ''
    fetchData()
  } catch { /* error handled by interceptor */ }
}

async function handleDeleteProject(projectId) {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？', '确认', { type: 'warning' })
    await deleteProject(projectId)
    ElMessage.success('已删除')
    fetchData()
  } catch { /* cancelled or error */ }
}

async function handleDeleteTeam() {
  try {
    await ElMessageBox.confirm('确定要解散该团队吗？此操作不可逆！', '确认解散', { type: 'error', confirmButtonText: '确认解散' })
    await deleteTeam(teamId.value)
    ElMessage.success('团队已解散')
    router.push('/teams')
  } catch { /* cancelled or error */ }
}

// 判断是否因为未登录而看不到成员信息
const showLoginHint = computed(() => !isLoggedIn.value && members.value.length === 0 && !loading.value)
</script>

<template>
  <div class="team-detail" v-loading="loading">
    <div v-if="team">
      <!-- 基本信息（所有人可见） -->
      <el-card class="section-card">
        <div class="team-header">
          <div>
            <h3>{{ team.name }}</h3>
            <el-tag :type="team.status === 'recruiting' ? 'success' : 'info'">
              {{ team.status === 'recruiting' ? '招募中' : '已关闭' }}
            </el-tag>
            <span style="margin-left:12px;color:#999">
              成员 {{ team.currentMembers ?? members.length }} / {{ team.maxMembers }}
            </span>
          </div>
          <div>
            <!-- 未登录 → 提示登录 -->
            <el-button v-if="!isLoggedIn && team.status === 'recruiting'" type="primary" @click="router.push('/login')">
              登录后申请加入
            </el-button>
            <!-- 已登录但非成员 → 申请加入 -->
            <el-button v-if="isLoggedIn && !isMember && team.status === 'recruiting'" type="primary" @click="applyVisible = true">
              申请加入
            </el-button>
            <!-- 队长 → 解散 -->
            <el-button v-if="isLeader" type="danger" @click="handleDeleteTeam">解散团队</el-button>
          </div>
        </div>
        <p style="color:#666;margin-top:12px">{{ team.description || '暂无简介' }}</p>
        <div style="margin-top:8px">
          <el-tag
            v-for="sk in (team.requiredSkills || [])" :key="sk.skillId"
            effect="plain" type="info" style="margin-right:6px"
          >{{ sk.skillName }}</el-tag>
        </div>
      </el-card>

      <el-row :gutter="16">
        <!-- 成员列表 -->
        <el-col :span="12">
          <el-card class="section-card">
            <template #header><span>团队成员（{{ members.length || team.currentMembers || 0 }}）</span></template>
            <!-- 未登录提示 -->
            <div v-if="showLoginHint" style="text-align:center;padding:30px 0;color:#999">
              <p>登录后可查看完整成员列表</p>
              <el-button type="primary" size="small" @click="router.push('/login')" style="margin-top:8px">
                去登录
              </el-button>
            </div>
            <!-- 成员列表 -->
            <div v-else-if="members.length">
              <div v-for="m in members" :key="m.userId" class="member-item">
                <span class="member-avatar">{{ m.username?.charAt(0)?.toUpperCase() }}</span>
                <span class="member-name">{{ m.username }}</span>
                <el-tag :type="m.role === 'leader' ? 'danger' : 'primary'" size="small">
                  {{ m.role === 'leader' ? '队长' : '成员' }}
                </el-tag>
                <el-button v-if="isLeader && m.role !== 'leader'" type="danger" link size="small" @click="handleRemove(m.userId)">移除</el-button>
              </div>
            </div>
            <!-- 加载完成但无数据（可能是非成员无法查看） -->
            <el-empty v-else-if="!loading && isLoggedIn" description="暂无成员数据" />
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
            <!-- 未登录提示 -->
            <div v-if="showLoginHint" style="text-align:center;padding:30px 0;color:#999">
              <p>登录后可查看项目信息</p>
            </div>
            <!-- 项目列表 -->
            <div v-else-if="projects.length">
              <div v-for="p in projects" :key="p.projectId" class="project-item">
                <div>
                  <span class="project-name" @click="router.push(`/projects/${p.projectId}`)">{{ p.name }}</span>
                  <p style="color:#999;font-size:12px">{{ p.description || '' }}</p>
                </div>
                <span style="font-size:13px;color:#67c23a">
                  {{ p.taskStats?.completionRate || 0 }}%
                </span>
                <el-button v-if="isLeader" type="danger" link size="small" @click="handleDeleteProject(p.projectId)">删除</el-button>
              </div>
            </div>
            <el-empty v-else-if="isLoggedIn" description="暂无项目" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 入队申请审核（仅队长可见） -->
      <el-card v-if="isLeader && applications.length" class="section-card">
        <template #header><span>待审核申请（{{ applications.length }}）</span></template>
        <el-table :data="applications" stripe size="small">
          <el-table-column prop="username" label="申请人" width="120" />
          <el-table-column prop="message" label="留言" min-width="200" />
          <el-table-column label="技能" width="220">
            <template #default="{ row }">
              <el-tag v-for="sk in (row.userSkills || [])" :key="sk.skillId" size="small" style="margin-right:4px">
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
        <el-input
          v-model="applyMsg" type="textarea" :rows="3"
          placeholder="介绍一下自己，增加通过率..."
          maxlength="500" show-word-limit
        />
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
.section-card {
  margin-bottom: 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.team-header h3 {
  margin: 0 0 10px 0;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.2s;
  margin-bottom: 4px;
}
.member-item:hover { background: #f5f7fa; }
.member-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700;
  box-shadow: 0 2px 8px rgba(64,158,255,0.25);
  flex-shrink: 0;
}
.member-name { flex: 1; font-weight: 500; color: #303133; }

.project-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 8px;
  transition: background 0.2s;
  margin-bottom: 4px;
}
.project-item:hover { background: #f5f7fa; }
.project-name {
  font-weight: 600; color: #409eff; cursor: pointer;
  transition: color 0.2s;
}
.project-name:hover { color: #337ecc; }
</style>
