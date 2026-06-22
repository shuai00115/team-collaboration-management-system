<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getProjectDetail, getStages, createStage, updateStageStatus, getStageTemplates, createStagesFromTemplate } from '@/api/project'
import {
  getProjectLists, getTasks, createTask, updateTask, deleteTask, moveTask,
  createTaskList, deleteTaskList
} from '@/api/task'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const projectId = computed(() => Number(route.params.projectId))

const project = ref(null)
const stages = ref([])
const lists = ref([])
const tasksMap = ref({})    // { listId: [task, ...] }
const members = ref([])     // 团队成员（用于执行人选择）
const loading = ref(true)

// 新增阶段
const stageFormVisible = ref(false)
const stageForm = ref({ name: '', description: '', startDate: '', endDate: '' })

// 模板
const templatesVisible = ref(false)
const templates = ref([])

// 任务弹窗
const taskDialogVisible = ref(false)
const taskFormMode = ref('create') // 'create' | 'edit'
const currentTask = ref(null)
const currentListId = ref(null)
const taskForm = ref({
  title: '', description: '', priority: 'medium',
  dueDate: '', assigneeId: null, stageId: null
})

// 任务列表
const newListName = ref('')
const showNewListInput = ref(false)

async function fetchAll() {
  const [pRes, sRes, lRes] = await Promise.all([
    getProjectDetail(projectId.value),
    getStages(projectId.value),
    getProjectLists(projectId.value)
  ])
  project.value = pRes.data
  stages.value = sRes.data || []
  lists.value = lRes.data || []

  // 加载团队成员（用于执行人选择）
  if (project.value?.teamId) {
    try {
      const { getTeamMembers } = await import('@/api/team')
      const mRes = await getTeamMembers(project.value.teamId)
      members.value = mRes.data || []
    } catch { members.value = [] }
  }

  // 加载每个列表的任务
  const map = {}
  await Promise.all(lists.value.map(async (l) => {
    const tRes = await getTasks(l.listId, { pageSize: 200 })
    map[l.listId] = tRes.data?.records || []
  }))
  tasksMap.value = map
}

onMounted(() => { fetchAll() })

// ---- 阶段操作 ----
async function handleCreateStage() {
  await createStage(projectId.value, stageForm.value)
  ElMessage.success('阶段创建成功')
  stageFormVisible.value = false
  stageForm.value = { name: '', description: '', startDate: '', endDate: '' }
  fetchAll()
}

async function handleStatusChange(stageId, status) {
  await updateStageStatus(stageId, { status })
  ElMessage.success('阶段状态已更新')
  fetchAll()
}

async function showTemplates() {
  const res = await getStageTemplates()
  templates.value = res.data || []
  templatesVisible.value = true
}

async function useTemplate(templateId) {
  await createStagesFromTemplate(projectId.value, { templateId })
  ElMessage.success('模板阶段已创建')
  templatesVisible.value = false
  fetchAll()
}

// ---- 任务列表 ----
async function handleCreateList() {
  if (!newListName.value.trim()) return
  await createTaskList(projectId.value, { name: newListName.value })
  ElMessage.success('列表已创建')
  newListName.value = ''
  showNewListInput.value = false
  fetchAll()
}

async function handleDeleteList(listId) {
  await ElMessageBox.confirm('删除列表将同时删除其中的任务，确定继续？', '警告', { type: 'warning' })
  await deleteTaskList(listId)
  ElMessage.success('列表已删除')
  fetchAll()
}

// ---- 任务操作 ----
function openCreateTask(listId) {
  taskFormMode.value = 'create'
  currentListId.value = listId
  taskForm.value = { title: '', description: '', priority: 'medium', dueDate: '', assigneeId: null, stageId: null }
  taskDialogVisible.value = true
}

function openEditTask(task) {
  taskFormMode.value = 'edit'
  currentTask.value = task
  taskForm.value = {
    title: task.title, description: task.description || '',
    priority: task.priority, dueDate: task.dueDate || '',
    assigneeId: task.assigneeId, stageId: task.stageId
  }
  taskDialogVisible.value = true
}

async function handleSaveTask() {
  if (!taskForm.value.title.trim()) return ElMessage.warning('请输入任务标题')
  if (taskFormMode.value === 'create') {
    await createTask(currentListId.value, taskForm.value)
    ElMessage.success('任务创建成功')
  } else {
    await updateTask(currentTask.value.taskId, taskForm.value)
    ElMessage.success('任务更新成功')
  }
  taskDialogVisible.value = false
  fetchAll()
}

async function handleDeleteTask(taskId) {
  await ElMessageBox.confirm('确定删除该任务？', '确认', { type: 'warning' })
  await deleteTask(taskId)
  ElMessage.success('任务已删除')
  fetchAll()
}

async function handleMoveTask(taskId, targetListId) {
  await moveTask(taskId, { targetListId })
  ElMessage.success('任务已移动')
  fetchAll()
}

function canOperate() {
  // 简化：team members 都有操作权限
  return true
}
</script>

<template>
  <div class="project-page" v-loading="loading">
    <div class="page-header" v-if="project">
      <div>
        <h3>{{ project.name }}</h3>
        <p style="color:#999;font-size:13px">{{ project.description }}</p>
      </div>
      <div style="display:flex;gap:10px">
        <el-button type="primary" @click="stageFormVisible = true">添加阶段</el-button>
        <el-button @click="showTemplates">使用模板</el-button>
        <span style="line-height:32px;color:#67c23a;font-weight:600">
          完成率 {{ project.taskStats?.completionRate || 0 }}%
        </span>
      </div>
    </div>

    <!-- 阶段进度条 -->
    <el-card class="section-card" v-if="stages.length">
      <template #header><span>阶段进度</span></template>
      <div class="stages-row">
        <div v-for="stage in stages" :key="stage.stageId" class="stage-item"
             :class="{ 'is-overdue': stage.isOverdue, 'is-active': stage.status === 'in_progress' }">
          <div class="stage-name">{{ stage.name }}</div>
          <div class="stage-date">{{ stage.startDate || '?' }} ~ {{ stage.endDate || '?' }}</div>
          <el-progress :percentage="stage.taskStats?.completionRate || 0" :stroke-width="6"
            :color="stage.isOverdue ? '#f56c6c' : '#409eff'" />
          <el-select :model-value="stage.status" size="small" @change="(v) => handleStatusChange(stage.stageId, v)" style="margin-top:6px;width:100%">
            <el-option label="未开始" value="not_started" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </div>
      </div>
    </el-card>

    <!-- 看板 -->
    <div class="kanban">
      <div v-for="list in lists" :key="list.listId" class="kanban-column">
        <div class="column-header">
          <strong>{{ list.name }}</strong>
          <span class="task-count">{{ (tasksMap[list.listId] || []).length }}</span>
          <el-button v-if="!list.isDefault" type="danger" link size="small" @click="handleDeleteList(list.listId)" style="margin-left:auto">×</el-button>
        </div>
        <div class="column-body">
          <div v-for="task in (tasksMap[list.listId] || [])" :key="task.taskId" class="task-card" @click="openEditTask(task)">
            <p class="task-title">{{ task.title }}</p>
            <div class="task-meta">
              <el-tag :type="task.priority==='high'?'danger':task.priority==='medium'?'warning':'success'" size="small">
                {{ { high:'高', medium:'中', low:'低' }[task.priority] }}
              </el-tag>
              <span v-if="task.assigneeName" style="font-size:12px;color:#999">{{ task.assigneeName }}</span>
              <span v-if="task.dueDate" style="font-size:12px;color:#999">{{ task.dueDate?.slice(0,10) }}</span>
            </div>
            <div class="task-actions">
              <el-dropdown trigger="click" @command="(targetListId) => handleMoveTask(task.taskId, targetListId)">
                <el-button link size="small">移动</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="l in lists.filter(x=>x.listId!==list.listId)" :key="l.listId" :command="l.listId">
                      → {{ l.name }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button type="danger" link size="small" @click.stop="handleDeleteTask(task.taskId)">删除</el-button>
            </div>
          </div>
          <el-button class="add-task-btn" @click="openCreateTask(list.listId)">+ 添加任务</el-button>
        </div>
      </div>

      <!-- 新建列表 -->
      <div class="kanban-column add-column">
        <div v-if="!showNewListInput" class="add-list-btn" @click="showNewListInput = true">+ 新建列表</div>
        <div v-else class="new-list-input">
          <el-input v-model="newListName" placeholder="列表名称" size="small" @keyup.enter="handleCreateList" />
          <div style="margin-top:8px;display:flex;gap:6px">
            <el-button type="primary" size="small" @click="handleCreateList">添加</el-button>
            <el-button size="small" @click="showNewListInput = false; newListName = ''">取消</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建阶段弹窗 -->
    <el-dialog v-model="stageFormVisible" title="创建阶段" width="420px">
      <el-form>
        <el-form-item label="名称" required><el-input v-model="stageForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="stageForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="stageForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="stageForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stageFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateStage">创建</el-button>
      </template>
    </el-dialog>

    <!-- 模板选择弹窗 -->
    <el-dialog v-model="templatesVisible" title="选择阶段模板" width="500px">
      <el-card v-for="t in templates" :key="t.templateId" shadow="hover" class="template-card" @click="useTemplate(t.templateId)">
        <strong>{{ t.templateName }}</strong>
        <p style="color:#999;font-size:13px;margin-top:4px">
          {{ (t.stages||[]).map(s => s.name).join(' → ') }}
        </p>
      </el-card>
      <template #footer><el-button @click="templatesVisible = false">取消</el-button></template>
    </el-dialog>

    <!-- 任务编辑弹窗 -->
    <el-dialog v-model="taskDialogVisible" :title="taskFormMode==='create'?'创建任务':'编辑任务'" width="500px">
      <el-form>
        <el-form-item label="标题" required><el-input v-model="taskForm.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="taskForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="taskForm.priority">
            <el-radio label="high">高</el-radio>
            <el-radio label="medium">中</el-radio>
            <el-radio label="low">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="执行人">
          <el-select v-model="taskForm.assigneeId" placeholder="可选，不选即为待认领" clearable style="width:100%">
            <el-option v-for="m in members" :key="m.userId" :label="m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="taskForm.dueDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="所属阶段">
          <el-select v-model="taskForm.stageId" placeholder="可选" clearable style="width:100%">
            <el-option v-for="s in stages" :key="s.stageId" :label="s.name" :value="s.stageId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.project-page { max-width: 100%; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.section-card { margin-bottom: 16px; }
.stages-row { display: flex; gap: 16px; flex-wrap: wrap; }
.stage-item {
  flex: 1; min-width: 180px; padding: 12px; border: 1px solid #e4e7ed; border-radius: 8px;
}
.stage-item.is-active { border-color: #409eff; background: #ecf5ff; }
.stage-item.is-overdue { border-color: #f56c6c; background: #fef0f0; }
.stage-name { font-weight: 600; margin-bottom: 4px; }
.stage-date { font-size: 12px; color: #999; margin-bottom: 6px; }

/* 看板 */
.kanban {
  display: flex; gap: 12px; align-items: flex-start; overflow-x: auto; padding-bottom: 20px;
}
.kanban-column {
  min-width: 280px; max-width: 320px; flex-shrink: 0;
  background: #f0f2f5; border-radius: 8px; padding: 12px;
}
.column-header {
  display: flex; align-items: center; gap: 6px; margin-bottom: 10px;
}
.task-count { background: #ccc; color: #fff; border-radius: 10px; padding: 0 6px; font-size: 12px; }
.column-body { min-height: 100px; }
.task-card {
  background: #fff; border-radius: 6px; padding: 10px; margin-bottom: 8px;
  cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,.1);
}
.task-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,.15); }
.task-title { font-weight: 500; margin-bottom: 6px; }
.task-meta { display: flex; gap: 6px; align-items: center; margin-bottom: 4px; }
.task-actions { display: flex; gap: 4px; margin-top: 6px; border-top: 1px solid #eee; padding-top: 6px; }
.add-task-btn { width: 100%; border: 1px dashed #ccc; background: transparent; }
.add-column {
  background: transparent;
  display: flex; align-items: center; justify-content: center;
  min-height: 60px;
}
.add-list-btn {
  padding: 10px 20px; border: 1px dashed #999; border-radius: 6px;
  color: #999; cursor: pointer; white-space: nowrap;
}
.template-card { cursor: pointer; margin-bottom: 8px; }
</style>
