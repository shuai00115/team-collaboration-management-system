// ========================================
// Demo 模式 —— API Mock 拦截器
// 拦截 Axios 请求并返回模拟数据
// ========================================
import { mockData } from './data'

// 模拟网络延迟
function delay(ms = 200) {
  return new Promise(r => setTimeout(r, ms))
}

// 包装分页响应
function paginate(list, pageNum = 1, pageSize = 10) {
  const total = list.length
  const start = (pageNum - 1) * pageSize
  const records = list.slice(start, start + pageSize)
  return {
    pageNum, pageSize, total, totalPages: Math.ceil(total / pageSize), records
  }
}

/**
 * 核心：根据 url + method 返回对应的 mock 数据
 * 返回 null 表示不拦截（走真实请求）
 */
let nextId = 100

export async function getMockResponse(config) {
  await delay(150)

  const url = config.url.replace(/\/api\/v1/, '')
  const method = config.method.toUpperCase()
  const body = config.data ? (typeof config.data === 'string' ? JSON.parse(config.data) : config.data) : {}
  const params = config.params || {}

  // ============ 认证 ============
  if (url === '/auth/login' && method === 'POST') {
    return { code: 200, msg: '登录成功（Demo）', data: {
      accessToken: 'demo_token_fake_jwt',
      tokenType: 'Bearer', expiresIn: 604800,
      userInfo: mockData.demoUser
    }}
  }
  if (url === '/auth/register' && method === 'POST') {
    return { code: 200, msg: '注册成功（Demo）', data: { userId: 99, username: body.username } }
  }

  // ============ 用户 ============
  if (url === '/users/me' && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.demoUser }
  }
  if (url === '/users/me' && method === 'PUT') {
    if (body.avatar !== undefined) mockData.demoUser.avatar = body.avatar
    if (body.bio !== undefined) mockData.demoUser.bio = body.bio
    return { code: 200, msg: '更新成功', data: null }
  }
  if (url === '/users/me/password' && method === 'PUT') {
    return { code: 200, msg: '密码修改成功', data: null }
  }
  if (url === '/users/me/teams' && method === 'GET') {
    return { code: 200, msg: '操作成功', data: paginate(mockData.myTeams, params.pageNum, params.pageSize) }
  }
  if (url === '/users/me/tasks' && method === 'GET') {
    let list = mockData.tasks.filter(t => t.assigneeId === 1)
    if (params.priority) list = list.filter(t => t.priority === params.priority)
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum, params.pageSize) }
  }
  if (url === '/users/me/applications' && method === 'GET') {
    let list = mockData.myApplications
    if (params.status) list = list.filter(a => a.status === params.status)
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum, params.pageSize) }
  }
  if (url === '/users/me/skills' && method === 'POST') {
    const sk = mockData.skills.find(s => s.skillId === body.skillId)
    if (sk && !mockData.demoUser.skills.find(s => s.skillId === sk.skillId)) {
      mockData.demoUser.skills.push({ ...sk, level: body.level || 'beginner' })
    }
    return { code: 200, msg: '技能添加成功', data: null }
  }
  const userSkillMatch = url.match(/^\/users\/me\/skills\/(\d+)$/)
  if (userSkillMatch && method === 'DELETE') {
    const sid = Number(userSkillMatch[1])
    mockData.demoUser.skills = mockData.demoUser.skills.filter(s => s.skillId !== sid)
    return { code: 200, msg: '技能删除成功', data: null }
  }

  // ============ 技能 ============
  if (url === '/skills' && method === 'GET') {
    let list = mockData.skills
    if (params.category) list = list.filter(s => s.category === params.category)
    if (params.keyword) list = list.filter(s => s.skillName.includes(params.keyword))
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum || 1, params.pageSize || 200) }
  }
  if (url === '/skills/categories' && method === 'GET') {
    const cats = [...new Set(mockData.skills.map(s => s.category))]
    return { code: 200, msg: '操作成功', data: cats }
  }

  // ============ 团队 ============
  if (url === '/teams' && method === 'GET') {
    let list = [...mockData.teams]
    if (params.status) list = list.filter(t => t.status === params.status)
    if (params.skillId) list = list.filter(t => t.requiredSkills?.some(s => s.skillId === Number(params.skillId)))
    if (params.keyword) list = list.filter(t => t.name.includes(params.keyword))
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum || 1, params.pageSize || 12) }
  }
  if (url === '/teams' && method === 'POST') {
    const team = {
      teamId: ++nextId, name: body.name, description: body.description || '',
      maxMembers: body.maxMembers || 10, status: 'recruiting', creatorId: 1, creatorName: 'demo_user',
      currentMembers: 1, requiredSkills: (body.requiredSkillIds || []).map(id => mockData.skills.find(s => s.skillId === id)).filter(Boolean),
      createdAt: new Date().toISOString().replace('T',' ').slice(0,19),
      members: [{ userId: 1, username: 'demo_user', role: 'leader', joinedAt: new Date().toISOString().replace('T',' ').slice(0,19) }]
    }
    mockData.teams.push(team)
    mockData.myTeams.push({ teamId: team.teamId, teamName: team.name, description: team.description, myRole: 'leader', status: 'recruiting', currentMembers: 1, maxMembers: team.maxMembers, joinedAt: team.createdAt })
    return { code: 200, msg: '团队创建成功', data: { teamId: team.teamId, name: team.name, status: 'recruiting', maxMembers: team.maxMembers } }
  }

  const teamDetailMatch = url.match(/^\/teams\/(\d+)$/)
  if (teamDetailMatch && method === 'GET') {
    const team = mockData.teams.find(t => t.teamId === Number(teamDetailMatch[1]))
    if (!team) return { code: 30001, msg: '团队不存在', data: null }
    return { code: 200, msg: '操作成功', data: { ...team } }
  }
  if (teamDetailMatch && method === 'PUT') {
    const team = mockData.teams.find(t => t.teamId === Number(teamDetailMatch[1]))
    if (team) Object.assign(team, body)
    return { code: 200, msg: '团队信息更新成功', data: null }
  }
  if (teamDetailMatch && method === 'DELETE') {
    const idx = mockData.teams.findIndex(t => t.teamId === Number(teamDetailMatch[1]))
    if (idx > -1) mockData.teams.splice(idx, 1)
    return { code: 200, msg: '团队已解散', data: null }
  }

  const teamMembersMatch = url.match(/^\/teams\/(\d+)\/members$/)
  if (teamMembersMatch && method === 'GET') {
    const team = mockData.teams.find(t => t.teamId === Number(teamMembersMatch[1]))
    return { code: 200, msg: '操作成功', data: team?.members || [] }
  }

  const removeMemberMatch = url.match(/^\/teams\/(\d+)\/members\/(\d+)$/)
  if (removeMemberMatch && method === 'DELETE') {
    const team = mockData.teams.find(t => t.teamId === Number(removeMemberMatch[1]))
    if (team) team.members = team.members.filter(m => m.userId !== Number(removeMemberMatch[2]))
    return { code: 200, msg: '成员已移除', data: null }
  }

  // ============ 入队申请 ============
  const appListMatch = url.match(/^\/teams\/(\d+)\/applications$/)
  if (appListMatch && method === 'GET') {
    let list = mockData.joinRequests.filter(r => r.teamId === Number(appListMatch[1]))
    if (params.status) list = list.filter(r => r.status === params.status)
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum, params.pageSize) }
  }
  if (appListMatch && method === 'POST') {
    return { code: 200, msg: '申请已提交，请等待队长审核', data: { requestId: ++nextId } }
  }

  const reviewMatch = url.match(/^\/teams\/(\d+)\/applications\/(\d+)$/)
  if (reviewMatch && method === 'PUT') {
    const req = mockData.joinRequests.find(r => r.requestId === Number(reviewMatch[2]))
    if (req) req.status = body.action === 'approve' ? 'approved' : 'rejected'
    return { code: 200, msg: body.action === 'approve' ? '申请已通过' : '申请已拒绝', data: null }
  }

  // ============ 项目 ============
  const teamProjectsMatch = url.match(/^\/teams\/(\d+)\/projects$/)
  if (teamProjectsMatch && method === 'GET') {
    const list = mockData.projects.filter(p => p.teamId === Number(teamProjectsMatch[1]))
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum, params.pageSize) }
  }
  if (teamProjectsMatch && method === 'POST') {
    const p = { projectId: ++nextId, teamId: Number(teamProjectsMatch[1]), name: body.name, description: body.description || '', taskStats: { total: 0, completed: 0, completionRate: 0 }, createdAt: new Date().toISOString().replace('T',' ').slice(0,19) }
    mockData.projects.push(p)
    return { code: 200, msg: '项目创建成功', data: { projectId: p.projectId, name: p.name } }
  }

  const projectDetailMatch = url.match(/^\/projects\/(\d+)$/)
  if (projectDetailMatch && method === 'GET') {
    const p = mockData.projects.find(x => x.projectId === Number(projectDetailMatch[1]))
    if (!p) return { code: 40001, msg: '项目不存在', data: null }
    return { code: 200, msg: '操作成功', data: { ...p, stages: mockData.stages } }
  }
  if (projectDetailMatch && method === 'PUT') {
    return { code: 200, msg: '项目信息更新成功', data: null }
  }
  if (projectDetailMatch && method === 'DELETE') {
    return { code: 200, msg: '项目已删除', data: null }
  }

  // ============ 阶段 ============
  const stagesMatch = url.match(/^\/projects\/(\d+)\/stages$/)
  if (stagesMatch && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.stages }
  }
  if (stagesMatch && method === 'POST') {
    const s = { stageId: ++nextId, projectId: Number(stagesMatch[1]), name: body.name, description: body.description || '', startDate: body.startDate, endDate: body.endDate, orderIndex: mockData.stages.length, status: 'not_started', taskStats: { total: 0, completed: 0, completionRate: 0 } }
    mockData.stages.push(s)
    return { code: 200, msg: '阶段创建成功', data: { stageId: s.stageId, name: s.name, orderIndex: s.orderIndex, status: s.status } }
  }
  if (url === '/stage-templates' && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.templates }
  }
  if (url.match(/^\/projects\/\d+\/stages\/template$/) && method === 'POST') {
    const tpl = mockData.templates.find(t => t.templateId === body.templateId)
    if (!tpl) return { code: 404, msg: '模板不存在', data: null }
    const created = tpl.stages.map((s, i) => ({ stageId: ++nextId, name: s.name, orderIndex: s.orderIndex }))
    return { code: 200, msg: `已按模板创建 ${created.length} 个阶段`, data: created }
  }

  const stageActionMatch = url.match(/^\/stages\/(\d+)$/)
  if (stageActionMatch && method === 'PUT') {
    const s = mockData.stages.find(x => x.stageId === Number(stageActionMatch[1]))
    if (s) Object.assign(s, body)
    return { code: 200, msg: '阶段信息更新成功', data: null }
  }
  if (stageActionMatch && method === 'DELETE') {
    const sid = Number(stageActionMatch[1])
    const idx = mockData.stages.findIndex(x => x.stageId === sid)
    if (idx > -1) mockData.stages.splice(idx, 1)
    return { code: 200, msg: '阶段已删除', data: null }
  }

  const stageStatusMatch = url.match(/^\/stages\/(\d+)\/status$/)
  if (stageStatusMatch && method === 'PUT') {
    const s = mockData.stages.find(x => x.stageId === Number(stageStatusMatch[1]))
    if (s) s.status = body.status
    return { code: 200, msg: '阶段状态已更新', data: null }
  }

  // ============ 任务列表 ============
  const listsMatch = url.match(/^\/projects\/(\d+)\/lists$/)
  if (listsMatch && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.taskLists.map(l => ({ ...l, taskCount: mockData.tasks.filter(t => t.listId === l.listId).length })) }
  }
  if (listsMatch && method === 'POST') {
    const l = { listId: ++nextId, projectId: Number(listsMatch[1]), name: body.name, position: mockData.taskLists.length, isDefault: false }
    mockData.taskLists.push(l)
    return { code: 200, msg: '任务列表创建成功', data: { listId: l.listId, name: l.name, position: l.position, isDefault: false } }
  }

  const listActionMatch = url.match(/^\/task-lists\/(\d+)$/)
  if (listActionMatch && method === 'PUT') {
    return { code: 200, msg: '列表信息更新成功', data: null }
  }
  if (listActionMatch && method === 'DELETE') {
    const lid = Number(listActionMatch[1])
    const l = mockData.taskLists.find(x => x.listId === lid)
    if (l?.isDefault) return { code: 40005, msg: '默认列表不可删除', data: null }
    return { code: 200, msg: '列表已删除', data: null }
  }
  if (url === '/task-lists/reorder' && method === 'PUT') {
    return { code: 200, msg: '列表顺序已更新', data: null }
  }

  // ============ 任务 ============
  const tasksInListMatch = url.match(/^\/task-lists\/(\d+)\/tasks$/)
  if (tasksInListMatch && method === 'GET') {
    let list = mockData.tasks.filter(t => t.listId === Number(tasksInListMatch[1]))
    if (params.priority) list = list.filter(t => t.priority === params.priority)
    if (params.stageId) list = list.filter(t => t.stageId === Number(params.stageId))
    if (params.keyword) list = list.filter(t => t.title.includes(params.keyword))
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum || 1, params.pageSize || 50) }
  }
  if (tasksInListMatch && method === 'POST') {
    const task = {
      taskId: ++nextId, listId: Number(tasksInListMatch[1]), title: body.title, description: body.description || '',
      priority: body.priority || 'medium', dueDate: body.dueDate || null, assigneeId: body.assigneeId || null,
      assigneeName: body.assigneeId ? 'demo_user' : null, creatorId: 1, creatorName: 'demo_user',
      stageId: body.stageId || null, position: mockData.tasks.length,
      projectName: '团队协作管理系统', teamName: '数据分析小分队', teamId: 3,
      listName: mockData.taskLists.find(l => l.listId === Number(tasksInListMatch[1]))?.name || '',
      createdAt: new Date().toISOString().replace('T',' ').slice(0,19),
      updatedAt: new Date().toISOString().replace('T',' ').slice(0,19),
    }
    mockData.tasks.push(task)
    return { code: 200, msg: '任务创建成功', data: { taskId: task.taskId, title: task.title, priority: task.priority, position: task.position, listName: task.listName } }
  }

  const taskDetailMatch = url.match(/^\/tasks\/(\d+)$/)
  if (taskDetailMatch && method === 'GET') {
    const task = mockData.tasks.find(t => t.taskId === Number(taskDetailMatch[1]))
    if (!task) return { code: 40006, msg: '任务不存在', data: null }
    return { code: 200, msg: '操作成功', data: { ...task, projectId: 1 } }
  }
  if (taskDetailMatch && method === 'PUT') {
    const task = mockData.tasks.find(t => t.taskId === Number(taskDetailMatch[1]))
    if (task) Object.assign(task, body)
    return { code: 200, msg: '任务更新成功', data: null }
  }
  if (taskDetailMatch && method === 'DELETE') {
    const idx = mockData.tasks.findIndex(t => t.taskId === Number(taskDetailMatch[1]))
    if (idx > -1) mockData.tasks.splice(idx, 1)
    return { code: 200, msg: '任务已删除', data: null }
  }

  const moveTaskMatch = url.match(/^\/tasks\/(\d+)\/move$/)
  if (moveTaskMatch && method === 'PUT') {
    const task = mockData.tasks.find(t => t.taskId === Number(moveTaskMatch[1]))
    if (task) { task.listId = body.targetListId; task.listName = mockData.taskLists.find(l => l.listId === body.targetListId)?.name || '' }
    return { code: 200, msg: '任务已移动', data: null }
  }

  if (url === '/tasks/reorder' && method === 'PUT') {
    return { code: 200, msg: '任务顺序已更新', data: null }
  }

  // ============ 统计 ============
  const teamStatsMatch = url.match(/^\/teams\/(\d+)\/stats$/)
  if (teamStatsMatch && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.teamStats }
  }
  const memberStatsMatch = url.match(/^\/teams\/(\d+)\/members\/(\d+)\/stats$/)
  if (memberStatsMatch && method === 'GET') {
    return { code: 200, msg: '操作成功', data: mockData.memberStats }
  }

  // ============ 通知 ============
  if (url === '/notifications' && method === 'GET') {
    let list = mockData.notifications
    if (params.isRead !== undefined && params.isRead !== null && params.isRead !== '') {
      list = list.filter(n => n.isRead === Number(params.isRead))
    }
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum || 1, params.pageSize || 15) }
  }
  if (url === '/notifications/unread-count' && method === 'GET') {
    const count = mockData.notifications.filter(n => !n.isRead).length
    return { code: 200, msg: '操作成功', data: { unreadCount: count } }
  }
  const notifReadMatch = url.match(/^\/notifications\/(\d+)\/read$/)
  if (notifReadMatch && method === 'PUT') {
    const n = mockData.notifications.find(x => x.notificationId === Number(notifReadMatch[1]))
    if (n) n.isRead = 1
    return { code: 200, msg: '已标记为已读', data: null }
  }
  if (url === '/notifications/read-all' && method === 'PUT') {
    mockData.notifications.forEach(n => { n.isRead = 1 })
    return { code: 200, msg: '已全部标记为已读', data: { updatedCount: mockData.notifications.length } }
  }

  // ============ 管理员 ============
  if (url === '/admin/users' && method === 'GET') {
    return { code: 200, msg: '操作成功', data: paginate(mockData.adminUsers, params.pageNum, params.pageSize || 20) }
  }
  const adminUserMatch = url.match(/^\/admin\/users\/(\d+)$/)
  if (adminUserMatch && method === 'PUT') {
    const u = mockData.adminUsers.find(x => x.userId === Number(adminUserMatch[1]))
    if (u) u.status = body.status
    return { code: 200, msg: '用户状态已更新', data: null }
  }
  if (url === '/admin/teams' && method === 'GET') {
    const list = mockData.teams.map(t => ({
      teamId: t.teamId, name: t.name, status: t.status,
      currentMembers: t.members.length, maxMembers: t.maxMembers,
      creatorName: t.creatorName, projectCount: 1, createdAt: t.createdAt
    }))
    return { code: 200, msg: '操作成功', data: paginate(list, params.pageNum, params.pageSize || 20) }
  }
  const adminTeamMatch = url.match(/^\/admin\/teams\/(\d+)$/)
  if (adminTeamMatch && method === 'DELETE') {
    return { code: 200, msg: '团队已被强制解散', data: null }
  }
  if (url === '/admin/skills' && method === 'POST') {
    const sk = { skillId: ++nextId, skillName: body.skillName, category: body.category, createdAt: new Date().toISOString().replace('T',' ').slice(0,19) }
    mockData.skills.push(sk)
    return { code: 200, msg: '技能标签添加成功', data: { skillId: sk.skillId, skillName: sk.skillName, category: sk.category } }
  }
  const adminSkillMatch = url.match(/^\/admin\/skills\/(\d+)$/)
  if (adminSkillMatch && method === 'DELETE') {
    return { code: 200, msg: '技能标签已删除', data: null }
  }

  // 未匹配的接口
  return null
}
