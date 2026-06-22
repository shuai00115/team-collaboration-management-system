<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeams, createTeam } from '@/api/team'
import request from '@/api/index'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
const getSkillsList = (params) => request.get('/skills', { params })

const router = useRouter()

// 是否已登录
const hasToken = !!(localStorage.getItem('accessToken'))

const teams = ref([])
const loading = ref(false)
const total = ref(0)
const filters = reactive({
  keyword: '',
  skillId: null,
  pageNum: 1,
  pageSize: 12
})
const skills = ref([])

function resetFilters() {
  Object.assign(filters, { keyword: '', skillId: null, pageNum: 1, pageSize: 12 })
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
  try {
    const res = await getSkillsList({ pageSize: 200 })
    skills.value = res.data?.records || []
  } catch { /* ignore */ }
}

onMounted(() => { fetchTeams(); fetchSkills() })

function goDetail(teamId) { router.push(`/teams/${teamId}`) }
function handleSearch() { filters.pageNum = 1; fetchTeams() }

// ========== 创建团队 ==========
const createVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  name: '',
  description: '',
  maxMembers: 10,
  requiredSkillIds: []
})

function openCreate() {
  if (!hasToken && localStorage.getItem('demoMode') !== 'true') {
    ElMessage.warning('请先登录后再创建团队')
    router.push('/login')
    return
  }
  createForm.name = ''
  createForm.description = ''
  createForm.maxMembers = 10
  createForm.requiredSkillIds = []
  createVisible.value = true
}

async function handleCreate() {
  if (!createForm.name.trim()) return ElMessage.warning('请输入团队名称')
  if (createForm.name.trim().length < 2 || createForm.name.trim().length > 50) {
    return ElMessage.warning('团队名称需2-50字')
  }
  createLoading.value = true
  try {
    await createTeam({
      name: createForm.name.trim(),
      description: createForm.description.trim() || undefined,
      maxMembers: createForm.maxMembers,
      requiredSkillIds: createForm.requiredSkillIds
    })
    ElMessage.success('团队创建成功')
    createVisible.value = false
    fetchTeams()
  } catch { /* error handled by interceptor */ }
  finally { createLoading.value = false }
}
</script>

<template>
  <div class="teams-page">
    <div class="page-header">
      <h3>团队招募墙</h3>
      <div style="display:flex;gap:10px">
        <el-button type="primary" :icon="Plus" @click="openCreate" v-if="hasToken">创建团队</el-button>
        <el-button @click="router.push('/dashboard')" v-if="hasToken">返回工作台</el-button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card style="margin-bottom:16px">
      <el-row :gutter="12" align="middle">
        <el-col :span="8">
          <el-input v-model="filters.keyword" placeholder="搜索团队名称" clearable @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="8">
          <el-select v-model="filters.skillId" placeholder="按技能筛选" clearable @change="handleSearch" style="width:100%">
            <el-option v-for="s in skills" :key="s.skillId" :label="`${s.skillName} (${s.category})`" :value="s.skillId" />
          </el-select>
        </el-col>
        <el-col :span="8">
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
            <el-tag
              v-for="sk in (team.requiredSkills || [])" :key="sk.skillId"
              size="small" type="info" effect="plain"
              style="margin-right:4px;margin-bottom:4px"
            >{{ sk.skillName }}</el-tag>
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

    <!-- 创建团队弹窗 -->
    <el-dialog v-model="createVisible" title="创建团队" width="520px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="团队名称" required>
          <el-input v-model="createForm.name" placeholder="2-50字" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="团队简介">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="描述团队目标与方向（可选）" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="计划人数">
          <el-input-number v-model="createForm.maxMembers" :min="2" :max="100" />
        </el-form-item>
        <el-form-item label="所需技能">
          <el-select v-model="createForm.requiredSkillIds" multiple filterable placeholder="选择团队需要的技能标签（可选）" style="width:100%">
            <el-option
              v-for="sk in skills" :key="sk.skillId"
              :label="`${sk.skillName} (${sk.category})`"
              :value="sk.skillId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="createLoading">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.teams-page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.team-card { cursor: pointer; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.team-name { font-weight: 600; font-size: 16px; }
.team-desc { color: #666; font-size: 13px; margin-bottom: 10px; min-height: 20px; max-height: 40px; overflow: hidden; }
.card-tags { min-height: 24px; margin-bottom: 8px; }
.card-footer { display: flex; justify-content: space-between; color: #999; font-size: 12px; }
</style>
