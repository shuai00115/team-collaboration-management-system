# 团队项目协作管理系统

> 📦 数据库课程设计项目 | Vue 3 + Spring Boot + MySQL 全栈实现

---

## 一、项目简介

**团队项目协作管理系统**是一个面向小型开发团队、课程项目组、兴趣小组的**项目管理平台**，贯穿 **团队组建 → 阶段规划 → 任务分配 → 进度追踪** 的完整协作流程。

**核心业务链路：**

```
用户注册 ──→ 创建/加入团队 ──→ 创建项目 ──→ 定义阶段 ──→ 看板任务协作 ──→ 进度统计与通知
```

**角色体系：**

| 角色 | 核心权限 |
|------|----------|
| **系统管理员** | 用户管理、技能库维护、全局团队管控 |
| **队长** | 团队编辑、入队审核、移除成员、项目与阶段管理 |
| **成员** | 查看团队/项目、执行任务、拖拽任务卡片 |
| **游客** | 注册、浏览招募墙 |

---

## 二、技术架构

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
│       Spring Boot 3.3.3 + Java 21 + MyBatis-Plus         │
│                    Port: 8080                            │
└────────────────────────┬────────────────────────────────┘
                         │  JDBC / ORM
                         │  Prepared Statements + 行级锁
┌────────────────────────┴────────────────────────────────┐
│                 数据库 (Database)                         │
│              MySQL 8.0  InnoDB  utf8mb4                  │
│       database: team_collab   Port: 3307                 │
│                    13 张业务表                             │
└─────────────────────────────────────────────────────────┘
```

### 前端技术栈

| 技术 | 用途 |
|------|------|
| **Vue 3** (Composition API) | 响应式 UI 框架 |
| **Element Plus** | 企业级 UI 组件库（表格、表单、弹窗、标签等） |
| **Vue Router 4** | SPA 路由（懒加载 + 导航守卫） |
| **Pinia 2** | 状态管理（用户信息、未读通知数） |
| **Axios** | HTTP 客户端（拦截器、JWT 自动附加） |
| **Vite 6** | 构建工具（Rolldown 打包、HMR 热更新） |

### 后端技术栈

| 技术 | 用途 |
|------|------|
| **Spring Boot 3.3.3** + **Java 21** | 应用框架 |
| **MyBatis-Plus 3.5.7** | ORM（Entity 映射 + 自定义 XML SQL） |
| **Spring Security + JWT** | 无状态鉴权，Token 有效期 7 天 |
| **Knife4j 4.5** | API 文档自动生成（OpenAPI 3） |
| **Spring WebSocket** | 实时通知推送 |
| **Hutool 5.8** | 通用工具库 |
| **Lombok** | 样板代码简化 |

---

## 三、数据库设计思路（核心）

> 这是本项目作为**数据库课程设计**的核心内容，涵盖了从需求分析、ER 建模、规范化到物理实现的完整数据库设计流程。

### 3.1 设计方法

采用**自顶向下 + 自底向上**结合的 ER 图设计方法，经过**概念设计 → 逻辑设计 → 物理设计**三个阶段。

### 3.2 实体识别与 ER 模型

分析系统需求，识别出以下**核心实体**及其关系：

```
          users ──N:M── skills
            │
            │1:N
            │
         teams ──1:N── team_members ──N:1── users
            │
            │1:N
            │
        projects ──1:N── stages
            │
            │1:N
            │
       task_lists ──1:N── tasks ──N:1── users (assignee)
                         │
                         │N:1
                         │
                      stages
```

**13 个实体的 ER 关系概览：**

| 关系类型 | 实体 | 关联方式 |
|----------|------|----------|
| **N:M** | users ↔ skills | 通过 `user_skills` 中间表，含 `level` 熟练度属性 |
| **1:N** | users → teams | 用户创建多个团队 |
| **N:M** | teams ↔ users | 通过 `team_members` 中间表，含 `role` 角色属性 |
| **1:N** | teams → projects | 一个团队可有多个项目 |
| **1:N** | projects → stages | 一个项目包含多个阶段 |
| **1:N** | projects → task_lists | 一个项目有多个看板列（默认3个） |
| **1:N** | task_lists → tasks | 一个列表含多个任务卡片 |
| **N:1** | tasks → users (assignee) | 任务指派给执行者 |

### 3.3 范式分析与规范化

所有表满足 **第三范式（3NF）**：

- **1NF**：所有字段原子化，无重复组（如技能标签用 `user_skills` 关联表而非 JSON 列）
- **2NF**：所有非主键属性完全函数依赖于主键（如 `user_skills.level` 依赖于 `(user_id, skill_id)` 而非单独一个）
- **3NF**：消除传递依赖（如 `tasks` 中不存 `project_id`，通过 `task_lists` → `project_id` 获取；通知表中新增的 `project_id` 属于冗余加速字段，用于前端免去多表 JOIN 跳转，是以读性能换写的可控反范式）

### 3.4 13 张表设计详解

```
数据库名: team_collab (utf8mb4)
引擎: InnoDB (支持行级锁与事务)
```

#### 核心业务表

| 序号 | 表名 | 核心字段 | 设计要点 |
|------|------|----------|----------|
| 1 | **users** | `user_id`, `username`, `password_hash`, `email`, `role`, `status` | 角色用 ENUM 约束；状态字段支持软禁用 |
| 2 | **skills** | `skill_id`, `skill_name`, `category` | 全局技能标签池，管理员维护 |
| 3 | **user_skills** | `user_id`, `skill_id`, `level` | N:M 中间表，PK 防重复；level 含初学/掌握/精通 |
| 4 | **teams** | `team_id`, `name`, `creator_id`, `max_members`, `status` | 招募态/关闭态自动切换；满员关停 |
| 5 | **team_required_skills** | `team_id`, `skill_id` | PK 防重复，用于招募需求展示 |
| 6 | **team_members** | `team_id`, `user_id`, `role`, `joined_at` | PK 防重复入队；role 区分队长/成员 |
| 7 | **join_requests** | `request_id`, `team_id`, `user_id`, `status` | UNIQUE(user_id, team_id) 防重复申请 |
| 8 | **projects** | `project_id`, `team_id`, `name`, `status` | FK → teams，级联删除 |
| 9 | **stages** | `stage_id`, `project_id`, `name`, `start_date`, `end_date`, `status` | 支持状态流转：未开始→进行中→已完成 |
| 10 | **stage_templates** | `template_id`, `name`, `stages_data` | JSON 字段存储预定义阶段模板 |
| 11 | **task_lists** | `list_id`, `project_id`, `name`, `position` | 默认3个（待办/进行中/已完成），支持自定义列 |
| 12 | **tasks** | `task_id`, `list_id`, `stage_id`, `title`, `priority`, `assignee_id`, `position` | FK 多重关联；position 支持拖拽排序 |
| 13 | **notifications** | `notification_id`, `user_id`, `type`, `related_type`, `related_id`, `project_id` | 关联实体通用设计；project_id 冗余加速前端跳转 |

#### 表关系总图

```
users ──N:M── skills (via user_skills)
users ──1:N── teams (creator)
users ──N:M── teams (via team_members)
teams ──N:M── skills (via team_required_skills)
users ──1:N── join_requests ──N:1── teams
teams ──1:N── projects
projects ──1:N── stages
projects ──1:N── task_lists
task_lists ──1:N── tasks
stages ──1:N── tasks
users ──1:N── tasks (assignee)
users ──1:N── notifications
stage_templates (独立模板表)
```

### 3.5 索引设计策略

| 索引类型 | 示例 | 设计原则 |
|----------|------|----------|
| **主键索引** | 所有表的自增 ID | 每表必设，保证行唯一 |
| **唯一索引** | `users.username`, `users.email`, `join_requests(user_id, team_id)` | 业务唯一性约束 |
| **外键索引** | `tasks.list_id`, `stages.project_id` | 加速 JOIN 查询 |
| **复合索引** | `notifications(user_id, is_read, created_at)` | 覆盖高频查询条件 |
| **排序索引** | `tasks(list_id, position)`, `tasks(assignee_id)` | 看板视图与我的任务 |

### 3.6 触发器自动化（5 个）

| 触发器 | 触发时机 | 做什么 |
|--------|----------|--------|
| `trg_teams_after_insert` | teams 插入后 | 自动将创建者加入为**队长** |
| `trg_projects_after_insert` | projects 插入后 | 自动创建**待办/进行中/已完成**三个默认列表 |
| `trg_join_requests_after_update` | join_requests 更新后 | 审核通过时将申请人加入 `team_members` |
| `trg_join_requests_notification` | join_requests 更新后 | 审核结果变更时自动发送通知 |
| `trg_tasks_assignee_update` | tasks 更新后 | 执行人变更后通知新执行人 |

### 3.7 并发控制与事务

**防超募（核心并发场景）**：

```sql
START TRANSACTION;
SELECT max_members FROM teams WHERE team_id = ? FOR UPDATE;  -- 行级锁
SELECT COUNT(*) FROM team_members WHERE team_id = ?;
-- 判满员：current < max_members → INSERT INTO team_members
-- 判满员：满员 → 自动 UPDATE teams SET status = 'closed'
COMMIT;
```

后端实现位于 `ApplicationServiceImpl.approve()`，使用 `@Transactional` + `SELECT ... FOR UPDATE` 确保多人同时申请时不会超额录取。

### 3.8 数据完整性约束

| 约束类型 | 表 | 作用 |
|----------|------|------|
| `PRIMARY KEY (user_id, skill_id)` | `user_skills` | 防止重复添加技能 |
| `PRIMARY KEY (team_id, user_id)` | `team_members` | 防止重复入队 |
| `UNIQUE (user_id, team_id)` | `join_requests` | 防止重复申请 |
| `FOREIGN KEY ... ON DELETE CASCADE` | 子表外键 | 删除父记录时级联清理 |
| `ENUM` 类型 | `users.role`, `users.status`, `tasks.priority` | 约束取值集合 |

---

## 四、核心功能

### 功能总览

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

### 模块一览

| 模块 | 页面 | 能力 |
|------|------|------|
| **认证** | Login / Register | JWT 登录注册、表单校验、Token 管理 |
| **工作台** | Dashboard | 我的团队卡片、我的任务列表、申请记录 |
| **招募墙** | Teams | 团队搜索、技能筛选、分页浏览 |
| **团队管理** | TeamDetail | 成员列表、入队审核（通过/拒绝）、项目 CRUD |
| **项目看板** | ProjectDetail | 阶段进度条、自定义看板列、任务 CRUD、拖拽移动 |
| **消息中心** | Notifications | 已读/未读切换、单条/批量标记、点击跳转关联实体 |
| **个人中心** | Profile | 资料编辑、密码修改、技能标签管理 |
| **管理后台** | Admin | 用户禁用/启用、强制解散团队、技能库维护 |

### 关键设计

- **Demo 模式**：`localStorage.demoMode = 'true'` + 任意 token → 前端 Mock 拦截器返回内置数据，无需后端即可浏览
- **实时通知**：WebSocket 推送 + 定时任务（截止提醒、阶段超期检测、旧通知清理）
- **58 个 RESTful API**：完整覆盖认证→用户→团队→项目→任务→通知→管理全链路

---

## 五、主要函数说明

### 5.1 后端核心服务

#### `NotificationServiceImpl` — 通知服务

```java
// 发送通知（核心方法）
void sendNotification(userId, type, title, content, relatedType, relatedId, projectId)
// 向团队队长发送通知
void sendToTeamLeader(teamId, type, title, content, relatedType, relatedId, projectId)
// 向团队全体成员发送通知
void sendToTeamMembers(teamId, type, title, content, relatedType, relatedId, excludeUserId, projectId)
```

#### `ApplicationServiceImpl.approve()` — 审核入队（含行级锁防超募）

```java
@Transactional
void approve(requestId, reviewerId):
    1. 校验申请存在 + 操作者是队长
    2. SELECT ... FROM teams WHERE team_id = ? FOR UPDATE  // 行级锁
    3. 检查当前人数 < max_members
    4. INSERT INTO team_members
    5. 满员则自动 UPDATE teams SET status = 'closed'
    6. 发送通过通知 + 更新申请状态
```

#### `TaskServiceImpl` — 任务服务

```java
createTask(listId, userId, data):
    1. 通过 listId → projectId → teamId 链校验权限
    2. 计算 position（最大+1）
    3. 插入任务 + 发送分配通知（含 projectId 用于跳转）

updateTask(taskId, userId, data):
    1. 记录原执行人
    2. 更新字段（只更新非 null 字段）
    3. 执行人变更 → 发送通知（解析 listId → projectId 用于跳转）
```

#### `NotificationScheduler` — 定时任务

```java
@Scheduled(cron = "0 0 * * * ?")
checkTaskDueReminders():        // 每小时：24h 内到期的任务 → 提醒执行人

@Scheduled(cron = "0 0 2 * * ?")
checkStageOverdue():            // 每日2:00：过期的未完成阶段 → 提醒团队队长

@Scheduled(cron = "0 0 3 * * ?")
cleanOldNotifications():        // 每日3:00：清除 90 天前已读通知
```

### 5.2 前端页面核心函数

#### `ProjectDetail.vue` — 项目看板（最复杂页面）

```
fetchAll()              并行请求 getProjectDetail + getStages + getProjectLists + getTasks
handleCreateStage()     弹窗创建阶段
useTemplate(id)         套用阶段模板批量创建
handleCreateList()      新建自定义看板列
openCreateTask(listId)  打开创建任务弹窗
handleSaveTask()        保存任务（create/update 分支）
handleMoveTask(id, list) 移动任务到其他列表
handleDeleteTask(id)    删除任务（确认弹窗）
```

#### `TeamDetail.vue` — 团队详情

```
fetchData()             并行请求 teamDetail + members + projects
handleApply()           提交入队申请（留言弹窗）
handleReview(reqId, action)  审核操作（approve/reject，含权限校验）
handleRemove(userId)    移除成员（确认弹窗，仅队长）
handleCreateProject()   创建项目弹窗
handleDeleteTeam()      解散团队（仅队长）
```

#### `Notifications.vue` — 消息中心

```
fetchList()             分页获取（支持全部/未读/已读 Tab）
handleRead(notif)       标记已读 + 根据 relatedType/projectId 跳转关联页面
handleMarkAll()         全部标记已读 + 清零红点
```

#### `Dashboard.vue` — 个人工作台

```
fetchAll()              Promise.all：myTeams + myTasks + myApplications
// 数据展示：团队卡片网格、任务表格、申请记录
```

#### `AppLayout.vue` — 主布局

```
侧边栏：菜单项（isCollapse 折叠切换）、RouterView 子路由出口
顶栏：面包屑 + 通知铃铛（Badge 红点）+ 用户头像下拉菜单
onMounted：fetchUserInfo() + fetchUnreadCount()
```

### 5.3 API 请求模块（7 个）

| 模块 | 文件路径 | 说明 |
|------|----------|------|
| 认证 | `api/auth.js` | `login()`, `register()` |
| 用户 | `api/user.js` | `getMyInfo()`, `updateProfile()`, `getMyTeams()`, `getMyTasks()`, `addSkill()` 等 8 个 |
| 团队 | `api/team.js` | `getTeams()`, `createTeam()`, `approveApply()`, `removeMember()` 等 10 个 |
| 项目 | `api/project.js` | `createProject()`, `getStages()`, `getStageTemplates()`, `getTeamStats()` 等 13 个 |
| 任务 | `api/task.js` | `createTask()`, `moveTask()`, `reorderTasks()`, `getProjectLists()` 等 11 个 |
| 通知 | `api/notification.js` | `getNotifications()`, `markRead()`, `markAllRead()` 等 4 个 |
| 管理 | `api/admin.js` | `getAdminUsers()`, `forceDeleteTeam()`, `createSkill()` 等 6 个 |

---

## 六、快速启动

### 1. 初始化数据库

```bash
mysql -u root -p123456 -P 3307 -h 127.0.0.1 < init.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# → http://localhost:8080
# API 文档: http://localhost:8080/doc.html
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

### 4. Demo 模式（无需后端）

打开浏览器控制台执行：
```js
localStorage.demoMode = 'true'
localStorage.accessToken = 'demo'
```
刷新页面即可用内置数据浏览完整 UI。

---

## 七、项目目录结构

```
数据库课程设计/
├── README.md                     ← 本文件
├── CLAUDE.md                     ← Claude Code 工作指引
├── 项目框架文档.md                 ← 功能规格 + 完整 DB 设计（ER 图、DDL、触发器、索引）
├── RESTful-API接口文档.md         ← 58 个 RESTful API 规范
├── init.sql                      ← 可执行 DDL 脚本（13 张表）
│
├── frontend/                     ← Vue 3 前端
│   ├── src/
│   │   ├── api/                  ← 7 个 API 模块
│   │   ├── mock/                 ← Demo 模式数据与拦截器
│   │   ├── router/               ← 路由表 + 鉴权守卫
│   │   ├── stores/               ← Pinia Store
│   │   ├── components/           ← AppLayout（侧边栏 + 顶栏）
│   │   └── views/                ← 9 个页面组件
│   └── vite.config.js
│
└── backend/                      ← Spring Boot 后端
    └── src/main/java/com/teamcollab/
        ├── controller/           ← 12 个 REST Controller
        ├── service/              ← 12 个 Service 接口 + 实现
        ├── mapper/               ← 13 个 Mapper + XML
        ├── entity/               ← 13 个实体
        ├── dto/                  ← 50+ 请求/响应 DTO
        ├── security/             ← JWT + Spring Security
        ├── common/               ← 枚举、异常、响应包装、工具类
        ├── config/               ← 配置（CORS、MyBatis、Knife4j、WebSocket）
        ├── websocket/            ← 实时通知推送
        └── scheduler/            ← 定时任务
```

---

> 📅 最后更新：2026-06-22
> 📝 版本：v1.1
