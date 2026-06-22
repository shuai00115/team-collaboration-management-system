<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeams } from '@/api/team'
import request from '@/api/index'
const getSkillsList = (params) => request.get('/skills', { params })

const router = useRouter()
const teams = ref([])
const loading = ref(false)
const total = ref(0)
const filters = reactive({
  keyword: '',
  skillId: null,
  minMembers: null,
  maxMembers: null,
  pageNum: 1,
  pageSize: 12
})
const skills = ref([])

function resetFilters() {
  Object.assign(filters, {
    keyword: '', skillId: null, minMembers: null,
    maxMembers: null, pageNum: 1, pageSize: 12
  })
}

async function fetchTeams() {
  loading.value = true
  try {
    const res = await getTeams({ status: 'recruiting', ...filters })
    teams.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function fetchSkills() {
  const res = await getSkillsList({ pageSize: 200 })
  skills.value = res.data?.records || []
}

onMounted(() => { fetchTeams(); fetchSkills() })

function goDetail(teamId) { router.push(`/teams/${teamId}`) }
function handleSearch() { filters.pageNum = 1; fetchTeams() }
</script>

<template>
  <div class="teams-page">
    <div class="page-header">
      <h3>团队招募墙</h3>
      <el-button type="primary" @click="router.push('/dashboard')">返回工作台</el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card style="margin-bottom:16px">
      <el-row :gutter="12" align="middle">
        <el-col :span="6">
          <el-input v-model="filters.keyword" placeholder="搜索团队名称" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.skillId" placeholder="按技能筛选" clearable @change="handleSearch" style="width:100%">
            <el-option v-for="s in skills" :key="s.skillId" :label="s.skillName" :value="s.skillId" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">筛选</el-button>
          <el-button @click="resetFilters(); fetchTeams()">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 团队卡片 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="team in teams" :key="team.teamId" style="margin-bottom:16px">
        <el-card shadow="hover" class="team-card" @click="goDetail(team.teamId)">
          <div class="card-top">
            <span class="team-name">{{ team.name }}</span>
            <el-tag type="success" size="small">招募中</el-tag>
          </div>
          <p class="team-desc">{{ team.description || '暂无简介' }}</p>
          <div class="card-tags">
            <el-tag v-for="sk in (team.requiredSkills||[])" :key="sk.skillId" size="small" type="info" effect="plain" style="margin-right:4px;margin-bottom:4px">
              {{ sk.skillName }}
            </el-tag>
          </div>
          <div class="card-footer">
            <span>{{ team.currentMembers }} / {{ team.maxMembers }} 人</span>
            <span>队长：{{ team.creatorName }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !teams.length" description="暂无招募中的团队" />

    <el-pagination
      v-if="total > filters.pageSize"
      v-model:current-page="filters.pageNum"
      :page-size="filters.pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="fetchTeams"
      style="text-align:center;margin-top:16px"
    />
  </div>
</template>

<style scoped>
.teams-page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.team-card { cursor: pointer; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.team-name { font-weight: 600; font-size: 16px; }
.team-desc { color: #666; font-size: 13px; margin-bottom: 10px; height: 36px; overflow: hidden; }
.card-tags { min-height: 24px; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; color: #999; font-size: 12px; }
</style>
