<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotifications, markRead, markAllRead } from '@/api/notification'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const notifications = ref([])
const loading = ref(false)
const total = ref(0)
const filters = reactive({
  isRead: undefined,
  pageNum: 1,
  pageSize: 15
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getNotifications(filters)
    notifications.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchList)

async function handleRead(notif) {
  if (!notif.isRead) {
    await markRead(notif.notificationId)
    notif.isRead = 1
    userStore.unreadCount = Math.max(0, userStore.unreadCount - 1)
  }
  // 跳转到关联实体
  const { relatedType, relatedId } = notif
  if (relatedType === 'team') router.push(`/teams/${relatedId}`)
  else if (relatedType === 'task') router.push(`/projects/${relatedId}`)
}

async function handleMarkAll() {
  await markAllRead()
  ElMessage.success('已全部标记为已读')
  userStore.unreadCount = 0
  fetchList()
}

const typeLabels = {
  join_approved: '入队通过', join_rejected: '入队拒绝', new_join_request: '新申请',
  task_assigned: '任务分配', task_updated: '任务变更', due_reminder: '截止提醒',
  stage_overdue: '阶段超期', member_removed: '成员移除'
}
</script>

<template>
  <div class="notifications-page">
    <div class="page-header">
      <h3>消息中心</h3>
      <el-button @click="handleMarkAll" :disabled="!userStore.unreadCount">
        全部标记已读（{{ userStore.unreadCount }} 条未读）
      </el-button>
    </div>

    <el-card>
      <el-tabs v-model="filters.isRead" @tab-change="(v) => { filters.isRead = v; filters.pageNum = 1; fetchList() }">
        <el-tab-pane :label="`全部`" :name="undefined" />
        <el-tab-pane :label="`未读`" :name="0" />
        <el-tab-pane :label="`已读`" :name="1" />
      </el-tabs>

      <div v-loading="loading">
        <div v-if="notifications.length">
          <div
            v-for="n in notifications" :key="n.notificationId"
            class="notif-item" :class="{ unread: !n.isRead }"
            @click="handleRead(n)"
          >
            <div class="notif-dot" v-if="!n.isRead"></div>
            <div class="notif-content">
              <div class="notif-header">
                <strong>{{ n.title }}</strong>
                <el-tag size="small" type="info">{{ typeLabels[n.type] || n.type }}</el-tag>
              </div>
              <p class="notif-body">{{ n.content }}</p>
              <span class="notif-time">{{ n.createdAt }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无通知" />
      </div>

      <el-pagination
        v-if="total > filters.pageSize"
        v-model:current-page="filters.pageNum"
        :page-size="filters.pageSize" :total="total"
        layout="prev, pager, next" @current-change="fetchList"
        style="text-align:center;margin-top:16px"
      />
    </el-card>
  </div>
</template>

<style scoped>
.notifications-page { max-width: 800px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.notif-item {
  display: flex; align-items: flex-start; gap: 12px; padding: 14px 0;
  border-bottom: 1px solid #f0f0f0; cursor: pointer;
}
.notif-item.unread { background: #f0f7ff; margin: 0 -20px; padding: 14px 20px; }
.notif-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #f56c6c;
  flex-shrink: 0; margin-top: 6px;
}
.notif-content { flex: 1; }
.notif-header { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
.notif-body { color: #666; font-size: 13px; margin-bottom: 4px; }
.notif-time { color: #999; font-size: 12px; }
</style>
