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

function goToTeam(teamId) {
  router.push(`/teams/${teamId}`)
}
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <div class="page-header">
      <div>
        <h3 class="page-title">我的工作台</h3>
        <p class="page-subtitle">欢迎回来，{{ userStore.userInfo?.username || '用户' }}</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${c.color}` }">
          <div class="stat-card-inner">
            <div class="stat-icon" :style="{ background: c.color }">
              <el-icon :size="24" color="#fff"><component :is="c.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ c.value }}</div>
              <div class="stat-label">{{ c.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的团队 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的团队</span>
          <el-tag size="small" round>{{ myTeams.length }} 个</el-tag>
        </div>
      </template>
      <el-row :gutter="16" v-if="myTeams.length">
        <el-col :span="8" v-for="team in myTeams" :key="team.teamId" style="margin-bottom:12px">
          <el-card shadow="hover" class="team-card" @click="goToTeam(team.teamId)">
            <div class="team-card-top">
              <p class="team-name">{{ team.teamName }}</p>
              <el-tag v-if="team.status==='recruiting'" type="success" size="small" effect="dark" round>招募中</el-tag>
            </div>
            <p class="team-desc">{{ team.description }}</p>
            <div class="team-card-bottom">
              <el-tag :type="team.myRole === 'leader' ? 'danger' : 'primary'" size="small" effect="plain" round>
                {{ team.myRole === 'leader' ? '👑 队长' : '👤 成员' }}
              </el-tag>
              <span class="team-member-count">{{ team.currentMembers }}/{{ team.maxMembers }} 人</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="还没有加入任何团队" />
    </el-card>

    <!-- 我的任务 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的任务</span>
          <el-tag size="small" round>{{ myTasks.length }} 项</el-tag>
        </div>
      </template>
      <el-table :data="myTasks" stripe size="small" v-if="myTasks.length" :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: 600 }">
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
      <template #header>
        <div class="card-header">
          <span class="card-title">最近的入队申请</span>
          <el-tag size="small" round>{{ myApps.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="myApps" stripe size="small" v-if="myApps.length" :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: 600 }">
        <el-table-column prop="teamName" label="团队" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status==='approved'?'success':row.status==='rejected'?'danger':'warning'" size="small" effect="dark">
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
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { margin: 0 0 4px 0; font-size: 24px; font-weight: 700; color: #303133; }
.page-subtitle { margin: 0; color: #909399; font-size: 14px; }

/* 统计卡片 */
.stat-card { border-radius: 10px; transition: transform 0.2s, box-shadow 0.2s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.1) !important; }
.stat-card-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.stat-value { font-size: 28px; font-weight: 700; line-height: 1.2; color: #303133; }
.stat-label { color: #909399; font-size: 13px; margin-top: 2px; }

/* 区块卡片 */
.section-card {
  margin-bottom: 20px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: box-shadow 0.3s;
}
.section-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 16px; font-weight: 600; color: #303133; }

.team-card {
  cursor: pointer; border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.team-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.1) !important; }
.team-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.team-name { font-weight: 700; font-size: 15px; margin: 0; color: #303133; }
.team-desc { color: #909399; font-size: 12px; margin: 0 0 10px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.team-card-bottom { display: flex; align-items: center; gap: 8px; }
.team-member-count { color: #909399; font-size: 12px; }
</style>
