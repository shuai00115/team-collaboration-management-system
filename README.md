# 团队项目协作管理系统

## 项目简介

一个面向小型开发团队、课程项目组、兴趣小组的 **项目管理平台**，覆盖 **团队组建 → 阶段规划 → 任务分配 → 进度追踪** 的完整协作流程。

---

## 一、技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Frontend)                       │
│  Vue 3 + Element Plus + Pinia + Vue Router + Axios      │
│                    Port: 5173                            │
└────────────────────────┬────────────────────────────────┘
                         │  HTTP RESTful API (JSON)
                         │  JWT Bearer Token 鉴权
                         │  /api/v1/*
┌────────────────────────┴────────────────────────────────┐
│                    后端 (Backend)                        │
│         Spring Boot / Express / Flask（任选）             │
│                    Port: 8080                            │
└────────────────────────┬────────────────────────────────┘
                         │  JDBC / ORM
                         │  Prepared Statements
┌────────────────────────┴────────────────────────────────┐
│                 数据库 (Database)                         │
│              MySQL 8.0  InnoDB  utf8mb4                  │
│       database: team_collab   Port: 3307                 │
│                    13 张业务表                             │
└─────────────────────────────────────────────────────────┘
```

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue** | 3.x | 响应式 UI 框架（Composition API + `<script setup>`） |
| **Element Plus** | 2.x | 企业级 UI 组件库（表格、表单、弹窗、标签等） |
| **Vue Router** | 4.x | SPA 路由管理（懒加载 + 导航守卫） |
| **Pinia** | 2.x | 状态管理（用户信息、未读通知数） |
| **Axios** | 1.x | HTTP 客户端（请求/响应拦截器、JWT 自动附加） |
| **Vite** | 6.x | 构建工具（Rolldown 打包、HMR 热更新） |

### 后端技术建议

| 技术 | 说明 |
|------|------|
| **Spring Boot** (Java) | 推荐框架，或 Express (Node.js)、Flask (Python) |
| **MySQL 8.0** | 关系型数据库，InnoDB 引擎 |
| **JWT** | 无状态鉴权，Token 有效期 7 天 |
| **MyBatis / JPA** | ORM 或半自动化数据访问层 |

---

## 二、项目目录结构

```
数据库课程设计/
├── README.md                      ← 本文件
├── 项目框架文档.md                  ← 详细功能说明 + 数据库 DDL
├── RESTful-API接口文档.md          ← 58 个接口的完整规范
├── init.sql                        ← 数据库建表脚本（13 张表）
│
└── frontend/                       ← 前端 Demo（Vue 3）
    ├── index.html
    ├── vite.config.js              # Vite 配置（@别名、API代理）
    ├── package.json
    └── src/
        ├── main.js                 # 入口：注册插件、挂载应用
        ├── App.vue                 # 根组件
        ├── style.css               # 全局样式重置
        │
        ├── api/                    # ========== API 服务层 ==========
        │   ├── index.js            # Axios 实例 + 拦截器
        │   ├── auth.js             # POST /auth/login, /auth/register
        │   ├── user.js             # 用户资料、技能、个人工作台
        │   ├── team.js             # 团队 CRUD、成员、入队申请
        │   ├── project.js          # 项目 CRUD、阶段、模板、统计
        │   ├── task.js             # 看板列表、任务 CRUD、移动排序
        │   ├── notification.js     # 通知列表、已读管理
        │   └── admin.js            # 管理员：用户/团队/技能
        │
        ├── router/
        │   └── index.js            # 路由表（9 条路由 + 鉴权守卫）
        │
        ├── stores/
        │   └── user.js             # Pinia Store（用户状态 + 未读数）
        │
        ├── components/
        │   └── AppLayout.vue       # 主布局（侧边栏 + 顶栏 + RouterView）
        │
        └── views/                  # ========== 页面组件 ==========
            ├── Login.vue           # 登录页
            ├── Register.vue        # 注册页
            ├── Dashboard.vue       # 个人工作台
            ├── Teams.vue           # 招募墙
            ├── TeamDetail.vue      # 团队详情（成员 + 项目 + 审核）
            ├── ProjectDetail.vue   # 项目看板（阶段 + 列表 + 任务卡片）
            ├── Notifications.vue   # 消息中心
            ├── Profile.vue         # 个人中心
            └── admin/
                └── Dashboard.vue   # 管理后台
```

---

## 三、数据库设计概要

**数据库名**：`team_collab`（utf8mb4）

### 13 张业务表

| 序号 | 表名 | 说明 | 关键约束 |
|------|------|------|----------|
| 1 | `users` | 用户表 | username UNIQUE, email UNIQUE |
| 2 | `skills` | 系统技能标签库 | skill_name UNIQUE |
| 3 | `user_skills` | 用户-技能关联 | PK (user_id, skill_id) 防重 |
| 4 | `teams` | 团队表 | FK → users(creator_id) |
| 5 | `team_required_skills` | 团队所需技能 | PK (team_id, skill_id) 防重 |
| 6 | `team_members` | 团队成员 | PK (team_id, user_id) 防重复入队 |
| 7 | `join_requests` | 入队申请 | UNIQUE (user_id, team_id) 防重复申请 |
| 8 | `projects` | 项目表 | FK → teams |
| 9 | `stages` | 项目阶段 | FK → projects |
| 10 | `stage_templates` | 阶段模板 | JSON 字段存储阶段结构 |
| 11 | `task_lists` | 任务列表（看板列） | FK → projects |
| 12 | `tasks` | 任务卡片 | FK → task_lists, stages, users |
| 13 | `notifications` | 通知消息 | FK → users |

> 详细 DDL 见 `init.sql`，ER 图与触发器设计见 `项目框架文档.md`。

---

## 四、核心功能模块

```
用户认证 ──→ 个人工作台 ──→ 招募墙 ──→ 团队详情
                                          │
                                    创建项目 / 定义阶段
                                          │
                                    看板视图（拖拽任务）
                                          │
                                    阶段追踪 / 进度统计
                                          │
                                    消息通知中心
```

### 模块总览

| 模块 | 页面 | 核心功能 |
|------|------|----------|
| **认证** | Login / Register | JWT 登录注册、表单校验、Token 存储 |
| **工作台** | Dashboard | 我的团队卡片、我的任务列表、申请记录 |
| **招募墙** | Teams | 团队搜索筛选、技能标签、分页浏览 |
| **团队管理** | TeamDetail | 成员列表、入队申请审核、项目列表、解散 |
| **项目看板** | ProjectDetail | 阶段进度条、自定义列表、任务 CRUD、移动 |
| **消息** | Notifications | 已读/未读筛选、单条/全部标记、关联跳转 |
| **个人** | Profile | 资料编辑、密码修改、技能标签管理 |
| **管理** | Admin | 用户禁用/启用、强制解散团队、技能库维护 |

---

## 五、主要函数说明

### 5.1 API 服务层（`src/api/`）

#### `api/index.js` — Axios 实例与拦截器

```js
request.interceptors.request.use(/* 自动附加 Authorization: Bearer <token> */)
request.interceptors.response.use(/* 统一 code 校验 + 401 跳转登录 */)
```

#### `api/auth.js` — 认证

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `login(data)` | POST | `/auth/login` | 登录，返回 accessToken + userInfo |
| `register(data)` | POST | `/auth/register` | 注册，可附带 skillIds |

#### `api/user.js` — 用户

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getMyInfo()` | GET | `/users/me` | 获取当前用户完整信息 |
| `updateProfile(data)` | PUT | `/users/me` | 更新头像、简介 |
| `changePassword(data)` | PUT | `/users/me/password` | 修改密码（需原密码验证） |
| `getMyTeams(params)` | GET | `/users/me/teams` | 我的团队列表（分页） |
| `getMyTasks(params)` | GET | `/users/me/tasks` | 我的任务（跨团队聚合） |
| `getMyApplications(params)` | GET | `/users/me/applications` | 我的入队申请记录 |
| `addSkill(data)` | POST | `/users/me/skills` | 添加个人技能 |
| `removeSkill(skillId)` | DELETE | `/users/me/skills/:id` | 删除个人技能 |

#### `api/team.js` — 团队

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getTeams(params)` | GET | `/teams` | 招募墙列表（支持筛选） |
| `createTeam(data)` | POST | `/teams` | 创建团队（含所需技能） |
| `getTeamDetail(teamId)` | GET | `/teams/:id` | 团队详情（基本信息+成员） |
| `updateTeam(teamId, data)` | PUT | `/teams/:id` | 编辑团队（仅队长） |
| `deleteTeam(teamId)` | DELETE | `/teams/:id` | 解散团队（仅队长） |
| `getTeamMembers(teamId)` | GET | `/teams/:id/members` | 成员列表 |
| `removeMember(teamId, userId)` | DELETE | `/teams/:id/members/:uid` | 移除成员（仅队长） |
| `applyToJoin(teamId, data)` | POST | `/teams/:id/applications` | 提交入队申请 |
| `getApplications(teamId, params)` | GET | `/teams/:id/applications` | 申请列表（仅队长） |
| `reviewApplication(teamId, reqId, data)` | PUT | `/teams/:id/applications/:rid` | 审核通过/拒绝 |

#### `api/project.js` — 项目与阶段

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getTeamProjects(teamId)` | GET | `/teams/:id/projects` | 团队项目列表 |
| `createProject(teamId, data)` | POST | `/teams/:id/projects` | 创建项目 |
| `getProjectDetail(projectId)` | GET | `/projects/:id` | 项目详情（含阶段+统计） |
| `updateProject(projectId)` | PUT | `/projects/:id` | 编辑项目 |
| `deleteProject(projectId)` | DELETE | `/projects/:id` | 删除项目 |
| `getStages(projectId)` | GET | `/projects/:id/stages` | 阶段列表（含进度） |
| `createStage(projectId, data)` | POST | `/projects/:id/stages` | 创建阶段 |
| `updateStage(stageId, data)` | PUT | `/stages/:id` | 编辑阶段 |
| `deleteStage(stageId)` | DELETE | `/stages/:id` | 删除阶段 |
| `updateStageStatus(stageId, data)` | PUT | `/stages/:id/status` | 更新阶段状态 |
| `getStageTemplates()` | GET | `/stage-templates` | 获取模板列表 |
| `createStagesFromTemplate(pid, data)` | POST | `/projects/:id/stages/template` | 批量创建阶段 |
| `getTeamStats(teamId)` | GET | `/teams/:id/stats` | 团队进度统计 |
| `getMemberStats(teamId, userId)` | GET | `/teams/:id/members/:uid/stats` | 个人贡献度统计 |

#### `api/task.js` — 任务与看板

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getProjectLists(projectId)` | GET | `/projects/:id/lists` | 获取看板列表 |
| `createTaskList(projectId, data)` | POST | `/projects/:id/lists` | 创建自定义列表 |
| `updateTaskList(listId, data)` | PUT | `/task-lists/:id` | 编辑列表名称 |
| `deleteTaskList(listId)` | DELETE | `/task-lists/:id` | 删除列表（默认不可删） |
| `reorderLists(data)` | PUT | `/task-lists/reorder` | 拖拽重排列表 |
| `getTasks(listId, params)` | GET | `/task-lists/:id/tasks` | 列表下任务（支持排序筛选） |
| `createTask(listId, data)` | POST | `/task-lists/:id/tasks` | 创建任务卡片 |
| `getTaskDetail(taskId)` | GET | `/tasks/:id` | 任务详情 |
| `updateTask(taskId, data)` | PUT | `/tasks/:id` | 编辑任务（可变更执行人） |
| `deleteTask(taskId)` | DELETE | `/tasks/:id` | 删除任务 |
| `moveTask(taskId, data)` | PUT | `/tasks/:id/move` | 移动任务到其他列表 |
| `reorderTasks(data)` | PUT | `/tasks/reorder` | 看板内拖拽重排 |

#### `api/notification.js` — 通知

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getNotifications(params)` | GET | `/notifications` | 通知列表（已读/未读筛选） |
| `getUnreadCount()` | GET | `/notifications/unread-count` | 未读数量 |
| `markRead(id)` | PUT | `/notifications/:id/read` | 标记单条已读 |
| `markAllRead(data)` | PUT | `/notifications/read-all` | 全部标记已读 |

#### `api/admin.js` — 管理员

| 函数 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `getAdminUsers(params)` | GET | `/admin/users` | 用户管理列表 |
| `updateUser(userId, data)` | PUT | `/admin/users/:id` | 禁用/启用用户 |
| `getAdminTeams(params)` | GET | `/admin/teams` | 所有团队列表 |
| `forceDeleteTeam(teamId)` | DELETE | `/admin/teams/:id` | 强制解散团队 |
| `createSkill(data)` | POST | `/admin/skills` | 添加技能标签 |
| `deleteSkill(skillId)` | DELETE | `/admin/skills/:id` | 删除技能标签 |

### 5.2 路由设计（`src/router/index.js`）

| 路径 | 页面 | 鉴权 | 说明 |
|------|------|------|------|
| `/login` | Login | 否 | 登录页，已登录自动跳转工作台 |
| `/register` | Register | 否 | 注册页 |
| `/dashboard` | Dashboard | 是 | 个人工作台（默认首页） |
| `/teams` | Teams | 是 | 招募墙 |
| `/teams/:teamId` | TeamDetail | 是 | 团队详情页 |
| `/projects/:projectId` | ProjectDetail | 是 | 项目看板页 |
| `/notifications` | Notifications | 是 | 消息中心 |
| `/profile` | Profile | 是 | 个人中心 |
| `/admin` | Admin | 是 | 管理后台（需 admin 角色） |

**路由守卫**：`beforeEach` 中检查 `localStorage.accessToken`，未登录仅允许访问 `/login` 和 `/register`。

### 5.3 状态管理（`src/stores/user.js`）

**Pinia Store `useUserStore`**：

| 状态/方法 | 类型 | 说明 |
|-----------|------|------|
| `userInfo` | `ref` | 当前用户完整信息 |
| `unreadCount` | `ref` | 未读通知数（顶栏红点） |
| `isLoggedIn` | `computed` | 是否已登录 |
| `isAdmin` | `computed` | 是否管理员 |
| `fetchUserInfo()` | `async` | 从服务端拉取用户信息 |
| `fetchUnreadCount()` | `async` | 拉取未读通知数 |
| `setToken(t)` | 同步 | 保存 Token 到 localStorage |
| `logout()` | 同步 | 清除登录状态 |

### 5.4 页面组件核心函数

#### `AppLayout.vue` — 主布局

```
Props: 无（自动拉取 userStore）
核心功能:
  - 侧边栏菜单（isCollapse 折叠切换）
  - 顶栏：面包屑 + 通知铃铛（Badge 红点）+ 用户头像下拉菜单
  - RouterView 子路由出口
  - 生命周期: onMounted 时 fetchUserInfo() + fetchUnreadCount()
```

#### `Login.vue` — 登录页

```
核心函数:
  handleLogin()        表单校验 → login() → setToken → 跳转 /dashboard
规则:
  username: required | password: required, min:6, max:20
```

#### `Register.vue` — 注册页

```
核心函数:
  handleRegister()     表单校验 → register() → 跳转 /login
规则:
  username: required, pattern(/^[a-zA-Z0-9_]{4,20}$/)
  password: required, min:6, max:20
  confirmPwd: 必须与 password 一致
  email: required, type:email
```

#### `Dashboard.vue` — 工作台

```
核心函数:
  fetchAll()           Promise.all 并行请求 getMyTeams + getMyTasks + getMyApplications
数据展示:
  - 团队卡片网格 → 点击跳转 /teams/:teamId
  - 任务表格（标题、优先级标签、项目、团队、截止日期）
  - 申请记录表格（团队、状态标签、申请时间）
```

#### `Teams.vue` — 招募墙

```
核心函数:
  fetchTeams()         查询招募中的团队（支持 keyword / skillId / 分页）
  fetchSkills()        加载技能下拉选项
  resetFilters()       Object.assign 重置筛选条件
  goDetail(teamId)     跳转团队详情
数据展示:
  - 搜索栏（关键词 + 技能下拉 + 筛选/重置按钮）
  - 团队卡片网格（名称、描述、技能标签、人数、创建者）
  - 分页组件
```

#### `TeamDetail.vue` — 团队详情

```
核心函数:
  fetchData()          并行请求 getTeamDetail + getTeamMembers + getTeamProjects
  handleApply()        提交入队申请（留言弹窗）
  handleRemove(userId) 移除成员（确认弹窗）
  handleReview(reqId, action)  审核入队申请（approve/reject）
  handleCreateProject()        创建项目（弹窗）
  handleDeleteProject(pjId)    删除项目（确认弹窗）
  handleDeleteTeam()           解散团队（确认弹窗 → 跳转招募墙）
计算属性:
  isLeader             当前用户是否为队长
  isMember             当前用户是否为成员
```

#### `ProjectDetail.vue` — 项目看板（最复杂页面）

```
核心函数:
  fetchAll()           并行请求 getProjectDetail + getStages + getProjectLists

  // 阶段管理
  handleCreateStage()       弹窗创建阶段
  handleStatusChange(sid, s) 更新阶段状态（not_started/in_progress/completed）
  showTemplates() / useTemplate(id)  获取模板并批量创建

  // 任务列表
  handleCreateList()       新建自定义看板列表
  handleDeleteList(id)     删除列表 + 提示

  // 任务卡片
  openCreateTask(listId)   打开创建任务弹窗
  openEditTask(task)       打开编辑任务弹窗
  handleSaveTask()         保存任务（create/update 分支）
  handleDeleteTask(taskId) 删除任务（确认弹窗）
  handleMoveTask(taskId, targetListId)  移动任务到其他列表

数据展示:
  - 阶段进度条（状态 + 完成率 + 超期标记）
  - 看板列（横向滚动）：每列含任务卡片列表 + 添加任务按钮
  - 任务卡片：标题、优先级标签、执行人、截止日期、移动/删除操作
  - 右侧「新建列表」入口
```

#### `Notifications.vue` — 消息中心

```
核心函数:
  fetchList()           获取通知列表（分页 + 已读/未读筛选）
  handleRead(notif)     标记已读 + 跳转关联实体（team/task）
  handleMarkAll()       全部标记已读 + 清除红点
数据展示:
  - Tab 切换：全部 / 未读 / 已读
  - 通知列表（未读项蓝色背景 + 红点）
  - 类型标签 (typeLabels 映射中文)
```

#### `Profile.vue` — 个人中心

```
核心函数:
  handleUpdateProfile()  保存头像URL + 个人简介
  handleChangePwd()      修改密码（原密码 + 新密码）
  handleAddSkill()       添加技能
  handleRemoveSkill(id)  删除技能
数据展示:
  - 基本资料表单（用户名/邮箱禁用、头像URL、简介）
  - 修改密码表单
  - 技能标签列表（可关闭删除）
  - 账户信息描述列表
```

#### `admin/Dashboard.vue` — 管理后台

```
核心函数:
  fetchUsers()            获取用户列表（分页）
  toggleUserStatus(user)  切换用户 active/disabled
  fetchTeams()            获取所有团队
  handleForceDelete(id)   强制解散（确认弹窗）
  fetchSkills()           获取技能列表
  handleCreateSkill()     添加技能标签
  handleDeleteSkill(id)   删除技能标签
Tab 页:
  - 用户管理：表格 + 禁用/启用按钮
  - 团队管理：表格 + 强制解散按钮
  - 技能库维护：输入框 + 分类下拉 + 可关闭标签
```

---

## 六、启动指南

### 1. 初始化数据库

```bash
mysql -u root -p123456 -P 3307 -h 127.0.0.1 < init.sql
```

### 2. 启动后端（以 Spring Boot 为例）

```bash
# 配置 application.yml 连接 team_collab 数据库
mvn spring-boot:run
# 服务运行在 http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 浏览器打开 http://localhost:5173
```

### 4. 构建部署

```bash
cd frontend
npm run build
# 输出到 frontend/dist/，可直接部署到 Nginx
```

---

## 七、核心设计要点

### 并发控制（防超募）

审核入队申请时使用 **行级锁**：

```
START TRANSACTION;
SELECT max_members FROM teams WHERE team_id = ? FOR UPDATE;  -- 锁定团队行
SELECT COUNT(*) FROM team_members WHERE team_id = ?;         -- 检查当前人数
-- 若未满员：INSERT INTO team_members; UPDATE join_requests;
COMMIT;
```

### 防重唯一约束

| 表 | 约束 | 防止场景 |
|----|------|----------|
| `user_skills` | PK (user_id, skill_id) | 重复添加技能 |
| `team_members` | PK (team_id, user_id) | 重复入队 |
| `join_requests` | UNIQUE (user_id, team_id) | 重复申请 |
| `users` | UNIQUE username, UNIQUE email | 注册冲突 |

### 触发器自动化

| 触发器 | 作用 |
|--------|------|
| `trg_teams_after_insert` | 创建团队后自动将创建者加入为队长 |
| `trg_projects_after_insert` | 创建项目后自动生成「待办/进行中/已完成」三个默认列表 |
| `trg_join_requests_after_update` | 审核结果变更后自动发送通知 |
| `trg_tasks_assignee_update` | 任务执行人变更后自动通知新执行人 |

---

## 八、相关文档

| 文档 | 说明 |
|------|------|
| `项目框架文档.md` | 功能清单 + E-R 图 + 13 张表完整 DDL + 触发器 + 索引设计 |
| `RESTful-API接口文档.md` | 58 个接口的请求/响应格式、参数说明、错误码 |
| `init.sql` | 可直接执行的建表脚本 |

---

> 📅 最后更新：2026-06-22
>
> 📝 版本：v1.0
