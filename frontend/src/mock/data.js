// ========================================
// Demo 模式 —— 完整模拟数据
// 无需后端即可浏览所有界面
// ========================================

const now = new Date()
function ago(days, hours = 0) {
  const d = new Date(now)
  d.setDate(d.getDate() - days)
  d.setHours(d.getHours() - hours)
  return d.toISOString().replace('T', ' ').slice(0, 19)
}
function future(days) {
  const d = new Date(now)
  d.setDate(d.getDate() + days)
  return d.toISOString().replace('T', ' ').slice(0, 19)
}
function date(daysAgo) {
  const d = new Date(now)
  d.setDate(d.getDate() - daysAgo)
  return d.toISOString().slice(0, 10)
}

// ---- 技能 ----
export const skills = [
  { skillId: 1, skillName: 'Java', category: '后端' },
  { skillId: 2, skillName: 'Python', category: '后端' },
  { skillId: 3, skillName: 'Spring Boot', category: '后端' },
  { skillId: 4, skillName: 'MySQL', category: '数据库' },
  { skillId: 5, skillName: 'Redis', category: '数据库' },
  { skillId: 6, skillName: 'Vue', category: '前端' },
  { skillId: 7, skillName: 'React', category: '前端' },
  { skillId: 8, skillName: 'TypeScript', category: '前端' },
  { skillId: 9, skillName: 'Docker', category: 'DevOps' },
  { skillId: 10, skillName: 'UI Design', category: '设计' },
  { skillId: 11, skillName: 'Node.js', category: '后端' },
  { skillId: 12, skillName: 'Flutter', category: '移动端' },
]

// ---- 当前 Demo 用户 ----
export const demoUser = {
  userId: 1,
  username: 'demo_user',
  email: 'demo@example.com',
  avatar: '',
  bio: '这是一个演示账号，用于体验系统各项功能。我是后端开发工程师，擅长 Java 和 Spring Boot。',
  role: 'admin',
  status: 'active',
  skills: [
    { skillId: 1, skillName: 'Java', category: '后端', level: 'advanced' },
    { skillId: 3, skillName: 'Spring Boot', category: '后端', level: 'advanced' },
    { skillId: 4, skillName: 'MySQL', category: '数据库', level: 'intermediate' },
  ],
  createdAt: '2026-01-15 10:30:00',
}

// ---- 用户列表（管理员视图） ----
export const adminUsers = [
  { userId: 1, username: 'demo_user', email: 'demo@example.com', role: 'admin', status: 'active', createdAt: '2026-01-15 10:30:00' },
  { userId: 2, username: 'zhangsan', email: 'zs@example.com', role: 'member', status: 'active', createdAt: '2026-01-20 14:00:00' },
  { userId: 3, username: 'lisi', email: 'ls@example.com', role: 'member', status: 'active', createdAt: '2026-02-01 09:00:00' },
  { userId: 4, username: 'wangwu', email: 'ww@example.com', role: 'member', status: 'active', createdAt: '2026-02-10 11:00:00' },
  { userId: 5, username: 'zhaoliu', email: 'zl@example.com', role: 'member', status: 'disabled', createdAt: '2026-03-01 16:00:00' },
  { userId: 6, username: 'sunqi', email: 'sq@example.com', role: 'member', status: 'active', createdAt: '2026-03-15 08:30:00' },
]

// ---- 团队 ----
const teamsData = [
  {
    teamId: 1, name: '后端开发小组', description: '专注于 Spring Boot 微服务开发，构建高可用后端系统', maxMembers: 8, status: 'recruiting',
    creatorId: 2, creatorName: 'zhangsan', currentMembers: 5,
    requiredSkills: [{ skillId: 1, skillName: 'Java', category: '后端' }, { skillId: 3, skillName: 'Spring Boot', category: '后端' }, { skillId: 4, skillName: 'MySQL', category: '数据库' }],
    createdAt: '2026-03-01 09:00:00',
    members: [
      { userId: 2, username: 'zhangsan', role: 'leader', joinedAt: '2026-03-01 09:00:00' },
      { userId: 1, username: 'demo_user', role: 'member', joinedAt: '2026-03-10 14:00:00' },
      { userId: 3, username: 'lisi', role: 'member', joinedAt: '2026-03-05 10:00:00' },
      { userId: 4, username: 'wangwu', role: 'member', joinedAt: '2026-03-08 11:00:00' },
      { userId: 6, username: 'sunqi', role: 'member', joinedAt: '2026-03-12 09:00:00' },
    ],
  },
  {
    teamId: 2, name: '前端开发小组', description: 'Vue3 + TypeScript 组件库开发', maxMembers: 6, status: 'recruiting',
    creatorId: 3, creatorName: 'lisi', currentMembers: 3,
    requiredSkills: [{ skillId: 6, skillName: 'Vue', category: '前端' }, { skillId: 8, skillName: 'TypeScript', category: '前端' }],
    createdAt: '2026-03-15 14:00:00',
    members: [
      { userId: 3, username: 'lisi', role: 'leader', joinedAt: '2026-03-15 14:00:00' },
      { userId: 1, username: 'demo_user', role: 'member', joinedAt: '2026-03-20 10:00:00' },
      { userId: 4, username: 'wangwu', role: 'member', joinedAt: '2026-03-25 16:00:00' },
    ],
  },
  {
    teamId: 3, name: '数据分析小分队', description: '使用 Python 与 SQL 进行数据挖掘与报表分析', maxMembers: 5, status: 'closed',
    creatorId: 1, creatorName: 'demo_user', currentMembers: 5,
    requiredSkills: [{ skillId: 2, skillName: 'Python', category: '后端' }, { skillId: 4, skillName: 'MySQL', category: '数据库' }],
    createdAt: '2026-02-10 08:00:00',
    members: [
      { userId: 1, username: 'demo_user', role: 'leader', joinedAt: '2026-02-10 08:00:00' },
      { userId: 2, username: 'zhangsan', role: 'member', joinedAt: '2026-02-12 10:00:00' },
      { userId: 3, username: 'lisi', role: 'member', joinedAt: '2026-02-12 14:00:00' },
      { userId: 4, username: 'wangwu', role: 'member', joinedAt: '2026-02-15 09:00:00' },
      { userId: 6, username: 'sunqi', role: 'member', joinedAt: '2026-02-18 11:00:00' },
    ],
  },
  {
    teamId: 4, name: '移动端 Flutter 组', description: '跨平台移动应用开发', maxMembers: 4, status: 'recruiting',
    creatorId: 6, creatorName: 'sunqi', currentMembers: 1,
    requiredSkills: [{ skillId: 12, skillName: 'Flutter', category: '移动端' }],
    createdAt: '2026-05-01 10:00:00',
    members: [{ userId: 6, username: 'sunqi', role: 'leader', joinedAt: '2026-05-01 10:00:00' }],
  },
]

// 补充 team 列表给招募墙用
teamsData.forEach(t => {
  t.currentMembers = t.members.length
})

// ---- 阶段模板 ----
const templateStages = [
  { name: '需求分析', description: '收集与分析项目需求，产出需求文档', orderIndex: 0 },
  { name: '系统设计', description: '数据库设计与接口设计', orderIndex: 1 },
  { name: '编码开发', description: '核心功能实现', orderIndex: 2 },
  { name: '测试验收', description: '功能测试与验收通过', orderIndex: 3 },
  { name: '部署上线', description: '部署到生产环境', orderIndex: 4 },
]

// ---- 项目与阶段 ----
const stagesData = [
  { stageId: 1, projectId: 1, name: '需求分析', description: '收集与分析需求', startDate: date(110), endDate: date(95), orderIndex: 0, status: 'completed', taskStats: { total: 4, completed: 4, completionRate: 100 } },
  { stageId: 2, projectId: 1, name: '系统设计', description: '数据库设计与接口设计', startDate: date(94), endDate: date(70), orderIndex: 1, status: 'completed', taskStats: { total: 5, completed: 5, completionRate: 100 } },
  { stageId: 3, projectId: 1, name: '编码开发', description: '核心功能实现', startDate: date(69), endDate: future(20), orderIndex: 2, status: 'in_progress', taskStats: { total: 8, completed: 4, completionRate: 50 } },
  { stageId: 4, projectId: 1, name: '测试验收', description: '功能测试与验收', startDate: future(21), endDate: future(40), orderIndex: 3, status: 'not_started', taskStats: { total: 0, completed: 0, completionRate: 0 } },
  { stageId: 5, projectId: 1, name: '部署上线', description: '部署到生产环境', startDate: future(41), endDate: future(50), orderIndex: 4, status: 'not_started', taskStats: { total: 0, completed: 0, completionRate: 0 } },
]

const projectsData = [
  {
    projectId: 1, teamId: 3, teamName: '数据分析小分队',
    name: '团队协作管理系统', description: '数据库课程设计项目，开发一个完整的团队协作管理平台',
    taskStats: { total: 17, completed: 9, inProgress: 5, todo: 3, completionRate: 53 },
    createdAt: '2026-03-01 09:00:00',
  },
]

// 预先计算各个阶段的 isOverdue
stagesData.forEach(s => {
  const end = new Date(s.endDate)
  s.isOverdue = end < now && s.status !== 'completed'
})

// ---- 任务列表（看板列） ----
const taskLists = [
  { listId: 1, projectId: 1, name: '待办', position: 0, isDefault: true },
  { listId: 2, projectId: 1, name: '进行中', position: 1, isDefault: true },
  { listId: 3, projectId: 1, name: '已完成', position: 2, isDefault: true },
  { listId: 4, projectId: 1, name: '代码审查中', position: 3, isDefault: false },
]

// ---- 任务卡片 ----
const tasks = [
  // 待办
  { taskId: 1, listId: 1, stageId: 3, title: '实现用户注册接口', description: '完成 /auth/register 的控制器、服务和数据校验', priority: 'high', dueDate: future(5), assigneeId: 1, assigneeName: 'demo_user', assigneeAvatar: '', creatorId: 1, creatorName: 'demo_user', position: 0, stageName: '编码开发', listName: '待办', projectName: '团队协作管理系统', teamName: '数据分析小分队', teamId: 3, createdAt: ago(10), updatedAt: ago(2) },
  { taskId: 2, listId: 1, stageId: 3, title: '集成 JWT 鉴权中间件', description: '实现 Spring Security + JWT 过滤器', priority: 'high', dueDate: future(8), assigneeId: 3, assigneeName: 'lisi', creatorId: 1, creatorName: 'demo_user', position: 1, stageName: '编码开发', createdAt: ago(8), updatedAt: ago(1) },
  { taskId: 3, listId: 1, stageId: 3, title: '编写单元测试', description: '为 Service 层编写 JUnit 测试，覆盖率 > 80%', priority: 'medium', dueDate: future(15), assigneeId: null, assigneeName: null, creatorId: 1, creatorName: 'demo_user', position: 2, stageName: '编码开发', createdAt: ago(5), updatedAt: ago(5) },

  // 进行中
  { taskId: 4, listId: 2, stageId: 3, title: '设计数据库 ER 图', description: '绘制完整的实体关系图，标注所有外键和索引', priority: 'high', dueDate: ago(2), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 0, stageName: '编码开发', createdAt: ago(20), updatedAt: ago(3) },
  { taskId: 5, listId: 2, stageId: 3, title: '实现团队创建功能', description: '包括团队信息录入、技能标签关联、默认角色设定', priority: 'high', dueDate: future(3), assigneeId: 4, assigneeName: 'wangwu', creatorId: 1, creatorName: 'demo_user', position: 1, stageName: '编码开发', createdAt: ago(15), updatedAt: ago(4) },
  { taskId: 6, listId: 2, stageId: 3, title: '完成任务看板拖拽功能', description: '使用 vuedraggable 实现前端拖拽 + 后端 reorder 接口', priority: 'medium', dueDate: future(7), assigneeId: 2, assigneeName: 'zhangsan', creatorId: 1, creatorName: 'demo_user', position: 2, stageName: '编码开发', createdAt: ago(12), updatedAt: ago(1) },
  { taskId: 7, listId: 2, stageId: 3, title: '入队申请审核流程', description: '包含行级锁防超募、事务管理、通知触发', priority: 'high', dueDate: future(2), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 3, stageName: '编码开发', createdAt: ago(6), updatedAt: ago(0) },
  { taskId: 8, listId: 2, stageId: null, title: '修复登录页样式问题', description: '在移动端分辨率下登录卡片溢出', priority: 'low', dueDate: future(10), assigneeId: 6, assigneeName: 'sunqi', creatorId: 1, creatorName: 'demo_user', position: 4, stageName: null, createdAt: ago(1), updatedAt: ago(1) },

  // 代码审查中
  { taskId: 9, listId: 4, stageId: 3, title: '审查用户模块代码', description: '检查 SQL 注入防护、参数校验完整性', priority: 'medium', dueDate: future(4), assigneeId: 2, assigneeName: 'zhangsan', creatorId: 1, creatorName: 'demo_user', position: 0, stageName: '编码开发', createdAt: ago(7), updatedAt: ago(2) },
  { taskId: 10, listId: 4, stageId: 3, title: '审查 API 接口规范', description: '确保所有接口返回格式符合统一规范', priority: 'low', dueDate: future(6), assigneeId: 3, assigneeName: 'lisi', creatorId: 1, creatorName: 'demo_user', position: 1, stageName: '编码开发', createdAt: ago(4), updatedAt: ago(1) },

  // 已完成
  { taskId: 11, listId: 3, stageId: 1, title: '项目需求访谈', description: '与用户进行需求沟通，产出访谈纪要', priority: 'high', dueDate: ago(30), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 0, stageName: '需求分析', createdAt: ago(40), updatedAt: ago(30) },
  { taskId: 12, listId: 3, stageId: 1, title: '编写需求规格说明书', description: 'SRS 文档编写与评审', priority: 'high', dueDate: ago(25), assigneeId: 2, assigneeName: 'zhangsan', creatorId: 1, creatorName: 'demo_user', position: 1, stageName: '需求分析', createdAt: ago(35), updatedAt: ago(25) },
  { taskId: 13, listId: 3, stageId: 1, title: '确定技术选型', description: '评估 Spring Boot vs Express vs Flask', priority: 'medium', dueDate: ago(28), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 2, stageName: '需求分析', createdAt: ago(38), updatedAt: ago(28) },
  { taskId: 14, listId: 3, stageId: 1, title: '绘制业务流程图', description: '用户注册、团队创建、任务分配三大流程', priority: 'medium', dueDate: ago(20), assigneeId: 3, assigneeName: 'lisi', creatorId: 1, creatorName: 'demo_user', position: 3, stageName: '需求分析', createdAt: ago(30), updatedAt: ago(20) },
  { taskId: 15, listId: 3, stageId: 2, title: '数据库概念模型设计', description: '确定实体、属性和关系', priority: 'high', dueDate: ago(18), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 4, stageName: '系统设计', createdAt: ago(22), updatedAt: ago(18) },
  { taskId: 16, listId: 3, stageId: 2, title: '编写 DDL 建表脚本', description: '包含所有约束、索引、触发器', priority: 'high', dueDate: ago(15), assigneeId: 4, assigneeName: 'wangwu', creatorId: 1, creatorName: 'demo_user', position: 5, stageName: '系统设计', createdAt: ago(20), updatedAt: ago(15) },
  { taskId: 17, listId: 3, stageId: 2, title: 'API 接口文档编写', description: '58 个接口的完整规范文档', priority: 'high', dueDate: ago(10), assigneeId: 1, assigneeName: 'demo_user', creatorId: 1, creatorName: 'demo_user', position: 6, stageName: '系统设计', createdAt: ago(15), updatedAt: ago(10) },
]

// 给每个 task 补上 projectName / teamName 等，方便跨团队查询
tasks.forEach(t => {
  if (!t.projectName) t.projectName = '团队协作管理系统'
  if (!t.teamName) t.teamName = '数据分析小分队'
  if (!t.teamId) t.teamId = 3
  if (!t.listName) {
    const l = taskLists.find(x => x.listId === t.listId)
    t.listName = l ? l.name : ''
  }
})

// ---- 入队申请 ----
const joinRequests = [
  { requestId: 1, teamId: 1, userId: 5, username: 'zhaoliu', avatar: '', message: '我有 3 年 Java 后端开发经验，熟悉微服务架构，希望能加入贡献。', status: 'pending', userSkills: [{ skillId: 1, skillName: 'Java', level: 'advanced' }, { skillId: 3, skillName: 'Spring Boot', level: 'advanced' }], createdAt: ago(1, 2), reviewedAt: null },
  { requestId: 2, teamId: 1, userId: 6, username: 'sunqi', message: '我是全栈开发者，前后端都能做。', status: 'pending', userSkills: [{ skillId: 7, skillName: 'React', level: 'intermediate' }, { skillId: 11, skillName: 'Node.js', level: 'advanced' }], createdAt: ago(0, 5), reviewedAt: null },
]

// ---- 通知 ----
const notifications = [
  { notificationId: 1, userId: 1, type: 'task_assigned', title: '您被分配了新的任务', content: '任务 "实现用户注册接口" 已分配给您，截止日期 ' + future(5).slice(0, 10), relatedType: 'task', relatedId: 1, isRead: 0, createdAt: ago(10) },
  { notificationId: 2, userId: 1, type: 'task_assigned', title: '您被分配了新的任务', content: '任务 "入队申请审核流程" 已分配给您。', relatedType: 'task', relatedId: 7, isRead: 0, createdAt: ago(6) },
  { notificationId: 3, userId: 1, type: 'due_reminder', title: '任务即将到期', content: '任务 "设计数据库 ER 图" 将于明天截止。', relatedType: 'task', relatedId: 4, isRead: 0, createdAt: ago(1) },
  { notificationId: 4, userId: 1, type: 'stage_overdue', title: '阶段超期提醒', content: '阶段 "系统设计" 已超过截止日期，请尽快处理。', relatedType: 'project', relatedId: 1, isRead: 1, createdAt: ago(3) },
  { notificationId: 5, userId: 1, type: 'join_approved', title: '入队申请已通过', content: '您加入团队 "前端开发小组" 的申请已通过！', relatedType: 'team', relatedId: 2, isRead: 1, createdAt: ago(20) },
  { notificationId: 6, userId: 1, type: 'new_join_request', title: '收到新的入队申请', content: '用户 wangwu 申请加入团队 "数据分析小分队"', relatedType: 'join_request', relatedId: 1, isRead: 1, createdAt: ago(7) },
  { notificationId: 7, userId: 1, type: 'task_assigned', title: '您被分配了新的任务', content: '任务 "编写 DDL 建表脚本" 已分配给您。', relatedType: 'task', relatedId: 16, isRead: 1, createdAt: ago(20) },
  { notificationId: 8, userId: 1, type: 'task_updated', title: '任务信息已更新', content: '任务 "修复登录页样式问题" 优先级已从 中 改为 低。', relatedType: 'task', relatedId: 8, isRead: 0, createdAt: ago(0, 1) },
]

// ---- 个人工作台数据 ----
// myTeams（带 myRole）
const myTeams = [
  { teamId: 1, teamName: '后端开发小组', description: '专注于 Spring Boot 微服务开发', myRole: 'member', status: 'recruiting', currentMembers: 5, maxMembers: 8, joinedAt: '2026-03-10 14:00:00' },
  { teamId: 2, teamName: '前端开发小组', description: 'Vue3 + TypeScript 组件库开发', myRole: 'member', status: 'recruiting', currentMembers: 3, maxMembers: 6, joinedAt: '2026-03-20 10:00:00' },
  { teamId: 3, teamName: '数据分析小分队', description: '使用 Python 与 SQL 进行数据挖掘与报表分析', myRole: 'leader', status: 'closed', currentMembers: 5, maxMembers: 5, joinedAt: '2026-02-10 08:00:00' },
]

const myApplications = [
  { requestId: 5, teamId: 1, teamName: '后端开发小组', message: '熟练掌握 Java 和 Spring Boot', status: 'approved', createdAt: '2026-03-09 14:30:00', reviewedAt: '2026-03-10 14:00:00' },
  { requestId: 6, teamId: 2, teamName: '前端开发小组', message: '有 Vue3 + TS 项目经验', status: 'approved', createdAt: '2026-03-19 10:00:00', reviewedAt: '2026-03-20 10:00:00' },
]

// ---- 统计 ----
const teamStats = {
  teamId: 3, teamName: '数据分析小分队',
  overview: { totalTasks: 17, completedTasks: 9, inProgressTasks: 5, todoTasks: 3, completionRate: 53 },
  priorityBreakdown: {
    high: { total: 9, completed: 6 },
    medium: { total: 6, completed: 3 },
    low: { total: 2, completed: 0 },
  },
  memberStats: [
    { userId: 1, username: 'demo_user', assignedTasks: 8, completedTasks: 5, overdueTasks: 1, completionRate: 62.5, contribution: 55.6 },
    { userId: 2, username: 'zhangsan', assignedTasks: 4, completedTasks: 2, overdueTasks: 0, completionRate: 50, contribution: 22.2 },
    { userId: 3, username: 'lisi', assignedTasks: 3, completedTasks: 1, overdueTasks: 0, completionRate: 33.3, contribution: 11.1 },
    { userId: 4, username: 'wangwu', assignedTasks: 2, completedTasks: 1, overdueTasks: 1, completionRate: 50, contribution: 11.1 },
  ],
}

const memberStats = {
  userId: 1, username: 'demo_user', teamId: 3, teamName: '数据分析小分队',
  assignedTasks: 8, completedTasks: 5, inProgressTasks: 2, todoTasks: 1, overdueTasks: 1,
  completionRate: 62.5, contribution: 55.6,
  tasksByPriority: {
    high: { assigned: 5, completed: 4 },
    medium: { assigned: 2, completed: 1 },
    low: { assigned: 1, completed: 0 },
  },
}

// ---- 聚合导出 ----
export const mockData = {
  skills,
  demoUser,
  adminUsers,
  teams: teamsData,
  projects: projectsData,
  stages: stagesData,
  taskLists,
  tasks,
  joinRequests,
  notifications,
  myTeams,
  myApplications,
  teamStats,
  memberStats,
  templates: [
    { templateId: 1, templateName: '标准瀑布模型', stageCount: 5, stages: templateStages },
    { templateId: 2, templateName: '课程设计', stageCount: 4, stages: [
      { name: '开题报告', description: '确定选题与方案', orderIndex: 0 },
      { name: '中期检查', description: '检查中期进度', orderIndex: 1 },
      { name: '成果开发', description: '完成主要开发工作', orderIndex: 2 },
      { name: '答辩准备', description: '准备答辩材料', orderIndex: 3 },
    ]},
  ],
}
