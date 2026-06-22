# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

团队项目协作管理系统 (Team Project Collaboration Management System) — a full-stack team project management platform covering team formation, multi-stage project planning, Kanban task tracking, and progress statistics. This is a university database course design project.

## Repository Structure

```
├── README.md                  # Project README (architecture + DB design + functions)
├── 项目框架文档.md              # Feature specs + complete DB design (DDL, ER diagram, triggers, indexes)
├── RESTful-API接口文档.md       # 58 RESTful API endpoint specifications
├── init.sql                   # Executable DDL script (13 tables, MySQL 8.0)
├── frontend/                  # Vue 3 SPA (Vite + Element Plus + Pinia + Axios)
│   └── src/
│       ├── api/               # Axios wrapper + 7 API modules
│       ├── mock/              # Demo-mode data & interceptor (no backend needed)
│       ├── router/            # Vue Router with auth guard
│       ├── stores/            # Pinia store (user info + unread count)
│       ├── components/        # AppLayout (sidebar + header)
│       └── views/             # 9 page components
└── backend/                   # Spring Boot 3.3.3 REST API server (Java 21)
    └── src/main/java/com/teamcollab/
        ├── controller/        # 12 REST controllers (58 endpoints)
        ├── service/           # 12 service interfaces + 12 implementations
        ├── mapper/            # 13 MyBatis-Plus mapper interfaces (+ 13 XMLs)
        ├── entity/            # 13 entity classes
        ├── dto/               # 50+ request/response DTOs
        ├── security/          # JWT token provider + filter + Spring Security config
        ├── common/            # Shared enums, exceptions, response wrapper, utils
        ├── config/            # CORS, MyBatis-Plus, Jackson, Knife4j, WebSocket, Scheduler
        ├── websocket/         # Real-time notification push
        └── scheduler/         # Scheduled tasks (deadline reminders)
```

## Commands

### Frontend (port 5173)

```bash
cd frontend
npm install                # Install dependencies
npm run dev                # Dev server (proxies /api → localhost:8080)
npm run build              # Production build → dist/
```

### Backend (port 8080)

```bash
cd backend
mvn spring-boot:run        # Start server
mvn clean package          # Build JAR
```

### Database (MySQL 8.0, port 3307)

```bash
mysql -u root -p123456 -P 3307 -h 127.0.0.1 < init.sql          # Root-level init
mysql -u root -p123456 -P 3307 -h 127.0.0.1 < backend/init.sql   # Backend copy
```

Credentials: `root` / `123456` on `localhost:3307`, database `team_collab` (utf8mb4).

### API Docs

After starting the backend, browse: **http://localhost:8080/doc.html** (Knife4j / Swagger UI).

## Architecture

**Three-tier full-stack design:**

| Tier | Technology | Key Libraries |
|------|-----------|---------------|
| **Frontend** | Vue 3 (Composition API) | Element Plus 2.x, Vue Router 4, Pinia 2, Axios, Vite 6 |
| **Backend** | Spring Boot 3.3.3 + Java 21 | Spring Security, MyBatis-Plus 3.5.7, jjwt 0.12.6, Knife4j 4.5, Hutool 5.8 |
| **Database** | MySQL 8.0 InnoDB | database `team_collab`, 13 tables |

**Backend layered pattern:**
```
Controller (@RestController) → Service interface → ServiceImpl → Mapper (MyBatis-Plus) → XML/Annotation SQL
                                        ↕
                               Entity / DTO / Enum
```

**Unified response format:** All controllers return `Result<T>` — `{ code: int, msg: string, data: T }`.  
**Pagination:** Wrapped in `PageResult<T>` — `{ pageNum, pageSize, total, totalPages, records }`.  
**Login response:** Returns `{ accessToken, tokenType: "Bearer", expiresIn, userInfo }`.

## Key Design Decisions

### Backend

- **Authentication:** Spring Security filter chain. JWT stored client-side, sent as `Authorization: Bearer <token>`. Token contains `userId`, `username`, `role`. Expiration: 604800s (7 days).
- **Public endpoints:** `/api/v1/auth/**`, `/api/v1/skills/**`, `GET /api/v1/teams`, `GET /api/v1/teams/{id}`, `/api/v1/health`, `/ws/**`.
- **Current user injection:** Custom `@CurrentUser` annotation resolved by `CurrentUserResolver` extracts `userId` from JWT and injects directly into controller method parameters.
- **Role enforcement:** `@RequireLeader` and `@RequireAdmin` annotations on service methods, checked in implementation layer.
- **Exception handling:** `GlobalExceptionHandler` catches `BusinessException`, `ConflictException`, `ForbiddenException`, `ResourceNotFoundException`, `UnauthorizedException` and maps them to proper HTTP status codes + `Result` responses.
- **MyBatis-Plus:** Entity → table mapping via `@TableName`. `application.yml` has `map-underscore-to-camel-case: true`. Custom SQL in `src/main/resources/mapper/*.xml`.
- **Pagination interceptor:** MyBatis-Plus `PaginationInnerInterceptor` with MySQL dialect, max 500 rows per page.
- **Scheduled tasks:** `NotificationScheduler` runs periodic checks for task/stage due-date reminders and old notification cleanup.
- **WebSocket:** `NotificationWebSocketHandler` + `WebSocketSessionManager` push real-time notifications to connected clients at `/ws`.

### Frontend

- **Demo mode:** `localStorage.demoMode = 'true'` + fake token → all Axios requests intercepted in `src/api/index.js` → mock data returned from `src/mock/index.js`. No backend required to browse the UI.
- **Auth routing:** Routes with `meta.auth: true` require login. Routes with `meta.public: true` (recruitment wall, team detail) are open to anyone. Routes with `meta.noAuth: true` (login, register) redirect to dashboard if already logged in.
- **API modules:** `src/api/*.js` mirror backend resource paths. Each function is a thin wrapper around the Axios instance.
- **Vite `@` alias:** Resolves to `src/` directory.

### Shared conventions

- **IDs:** `BIGINT UNSIGNED AUTO_INCREMENT` in MySQL → `Long` in Java/JavaScript.
- **Naming:** DB uses `snake_case`; Java uses `camelCase`; MyBatis-Plus auto-converts. Frontend URL paths use `kebab-case`: `/teams/{team-id}/applications/{request-id}`.
- **Timestamps:** `created_at` (set once), `updated_at` (auto-update via `ON UPDATE CURRENT_TIMESTAMP`).
- **Enums:** Stored as MySQL `ENUM` or `VARCHAR`; mapped to Java enums in `com.teamcollab.common.enums` (10 enum classes).
- **Route lazy-loading:** All page components loaded via `() => import(...)`.

## Database Design (13 tables)

| # | Table | Key Points |
|---|-------|-----------|
| 1 | `users` | username UNIQUE, email UNIQUE, role ENUM(admin/member), status ENUM(active/disabled) |
| 2 | `skills` | Global skill tag pool, category for grouping |
| 3 | `user_skills` | PK (user_id, skill_id), level ENUM(beginner/intermediate/advanced) |
| 4 | `teams` | FK → users(creator_id), max_members, status ENUM(recruiting/closed) |
| 5 | `team_required_skills` | PK (team_id, skill_id), for recruitment requirements |
| 6 | `team_members` | PK (team_id, user_id), role ENUM(leader/member) |
| 7 | `join_requests` | UNIQUE (user_id, team_id), status ENUM(pending/approved/rejected) |
| 8 | `projects` | FK → teams, status tracking |
| 9 | `stages` | FK → projects, date range + status flow |
| 10 | `stage_templates` | JSON field storing preset stage structures |
| 11 | `task_lists` | FK → projects, position for ordering, 3 defaults per project |
| 12 | `tasks` | FK → task_lists, stages, users; priority, position, due_date |
| 13 | `notifications` | FK → users; type, relatedType, relatedId, **projectId** (denormalized for frontend navigation) |

## Concurrency & Data Integrity

- **防超募 (over-recruitment prevention):** `ApplicationServiceImpl.approve()` wraps a `SELECT ... FOR UPDATE` on the `teams` row in a transaction, checks `COUNT(members) < max_members` before inserting.
- **Unique constraints:** `PK (user_id, skill_id)` on `user_skills`, `PK (team_id, user_id)` on `team_members`, `UNIQUE (user_id, team_id)` on `join_requests`, `UNIQUE` on `users.username` and `users.email`.
- **Triggers (5 total):** Auto-add creator as team leader, auto-generate 3 default task lists on project creation, auto-send notifications on application review and task assignment change, auto-notify on assignee change.

## Notification Navigation (projectId)

Notifications have a `projectId` field (added to entity, DTO, and DB). When creating task/stage notifications, callers MUST pass the project ID so the frontend can navigate directly to `/projects/{projectId}` instead of incorrectly using the task/stage ID. Callers:
- `TaskServiceImpl.createTask/updateTask`: resolve via `taskList.getProjectId()`
- `NotificationScheduler.checkTaskDueReminders`: resolve via `taskListMapper.selectById(task.getListId())`
- `NotificationScheduler.checkStageOverdue`: use `stage.getProjectId()`
- Team/application notifications: pass `null` (not project-related)
