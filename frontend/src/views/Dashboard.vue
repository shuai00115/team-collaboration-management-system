<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyTeams, getMyTasks, getMyApplications } from '@/api/user'
import {
  UserFilled, Files, Clock, DataBoard
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const myTeams = ref([])
const myTasks = ref([])
const myApps = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [tRes, tkRes, aRes] = await Promise.all([
      getMyTeams({ pageSize: 100 }),
      getMyTasks({ pageSize: 100 }),
      getMyApplications({ pageSize: 100 })
    ])
    myTeams.value = tRes.data?.records || []
    myTasks.value = tkRes.data?.records || []
    myApps.value = aRes.data?.records || []
  } finally {
    loading.value = false
  }
})

const cards = computed(() => {
  const todoTasks = myTasks.value.filter(t => t.listName === '待办' || t.status === 'todo')
  const inProgressTasks = myTasks.value.filter(t => t.listName === '进行中' || t.status === 'in_progress')
  const pendingApps = myApps.value.filter(a => a.status === 'pending')
  return [
    { label: '我的团队', value: myTeams.value.length, icon: UserFilled, color: '#409eff' },
    { label: '待办任务', value: todoTasks.length, icon: Files, color: '#e6a23c' },
    { label: '进行中任务', value: inProgressTasks.length, icon: Clock, color: '#67c23a' },
    { label: '待审核申请', value: pendingApps.length, icon: DataBoard, color: '#f56c6c' }
  ]
})
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <h3 class="page-title">我的工作台</h3>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:12px">
            <el-icon :size="32" :color="c.color"><component :is="c.icon" /></el-icon>
            <div>
              <div style="font-size:24px;font-weight:700">{{ c.value }}</div>
              <div style="color:#999;font-size:13px">{{ c.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的团队 -->
    <el-card class="section-card">
      <template #header><span>我的团队（{{ myTeams.length }}）</span></template>
      <el-row :gutter="16" v-if="myTeams.length">
        <el-col :span="8" v-for="team in myTeams" :key="team.teamId" style="margin-bottom:12px">
          <el-card shadow="hover" class="team-card" @click="router.push(`/teams/${team.teamId}`)">
            <p class="team-name">{{ team.teamName }}</p>
            <el-tag :type="team.myRole === 'leader' ? 'danger' : 'primary'" size="small">
              {{ team.myRole === 'leader' ? '队长' : '成员' }}
            </el-tag>
            <span style="margin-left:8px;color:#999;font-size:13px">
              {{ team.currentMembers }}/{{ team.maxMembers }} 人
            </span>
            <el-tag v-if="team.status==='recruiting'" type="success" size="small" effect="plain" style="margin-left:6px">招募中</el-tag>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="还没有加入任何团队" />
    </el-card>

    <!-- 我的任务 -->
    <el-card class="section-card">
      <template #header><span>我的任务</span></template>
      <el-table :data="myTasks" stripe size="small" v-if="myTasks.length">
        <el-table-column prop="title" label="任务" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="row.priority==='high'?'danger':row.priority==='medium'?'warning':'success'" size="small">
              {{ { high: '高', medium: '中', low: '低' }[row.priority] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目" width="140" />
        <el-table-column prop="teamName" label="团队" width="130" />
        <el-table-column prop="dueDate" label="截止日期" width="110" />
      </el-table>
      <el-empty v-else description="暂无任务" />
    </el-card>

    <!-- 申请记录 -->
    <el-card class="section-card">
      <template #header><span>最近的入队申请</span></template>
      <el-table :data="myApps" stripe size="small" v-if="myApps.length">
        <el-table-column prop="teamName" label="团队" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status==='approved'?'success':row.status==='rejected'?'danger':'warning'" size="small">
              {{ { pending:'待审核', approved:'已通过', rejected:'已拒绝' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
      </el-table>
      <el-empty v-else description="暂无申请记录" />
    </el-card>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1000px; margin: 0 auto; }
.page-title { margin-bottom: 20px; font-size: 20px; }
.section-card { margin-bottom: 16px; }
.team-card { cursor: pointer; }
.team-name { font-weight: 600; margin-bottom: 8px; font-size: 15px; }
</style>
