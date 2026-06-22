# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

团队项目协作管理系统 (Team Project Collaboration Management System) — a full-stack team project management platform covering team formation, multi-stage project planning, Kanban task tracking, and progress statistics. This is a university database course design project.

## Repository Structure

```
├── 项目框架文档.md       # Feature specs + complete DB design (DDL, ER diagram, triggers, indexes)
├── RESTful-API接口文档.md # 58 RESTful API endpoint specifications
├── init.sql              # Executable DDL script (13 tables, MySQL 8.0)
├── README.md             # Architecture overview + function catalog
└── frontend/             # Vue 3 SPA demo (Vite + Element Plus + Pinia + Axios)
```

## Commands

```bash
# Frontend dev server (port 5173, proxies /api → localhost:8080)
cd frontend && npm run dev

# Frontend build
cd frontend && npm run build

# Database setup (MySQL 8.0, port 3307, user:root, password:123456)
mysql -u root -p123456 -P 3307 -h 127.0.0.1 < init.sql
```

There is no backend implementation in this repo — the frontend expects a REST API at `localhost:8080/api/v1`. When implementing the backend, follow the interface specs in `RESTful-API接口文档.md`.

## Architecture

**Three-tier design:**
- **Database**: MySQL 8.0 InnoDB, database `team_collab`, 13 tables with foreign keys, unique constraints, row-level locking for concurrency control
- **Backend** (planned, not implemented): RESTful API at `/api/v1/*`, JWT Bearer auth, unified response `{code, msg, data}`
- **Frontend**: Vue 3 Composition API (`<script setup>`), Element Plus UI, SPA with 9 routes

**Key design decisions:**
- All IDs use `BIGINT UNSIGNED AUTO_INCREMENT`
- JWT token stored in `localStorage.accessToken`, auto-attached by Axios request interceptor (`src/api/index.js`)
- Route guard in `src/router/index.js` redirects unauthenticated users to `/login`
- Pinia store (`src/stores/user.js`) manages `userInfo` and `unreadCount` globally
- Vite `@` alias resolves to `src/` directory
- API proxy configured in `vite.config.js`: `/api` → `http://localhost:8080`

**Database naming conventions:**
- Tables/columns: `snake_case`
- Primary keys: `{entity}_id` (e.g., `user_id`, `team_id`)
- Timestamps: `created_at`, `updated_at` (with `ON UPDATE CURRENT_TIMESTAMP`)
- ENUMs used for status fields: `team.status` (recruiting/closed), `stage.status` (not_started/in_progress/completed), `task.priority` (high/medium/low), `join_request.status` (pending/approved/rejected)

**Frontend conventions:**
- API modules in `src/api/` mirror backend resource paths (one file per resource group)
- All API functions return raw Axios response — the interceptor in `api/index.js` handles `code !== 200` as errors
- Vue components use Element Plus Chinese locale (`zh-cn`)
- Page components are lazy-loaded via `() => import(...)` in router
- URL paths use kebab-case with path params in kebab-case: `/teams/{team-id}/applications/{request-id}`

**Concurrency control (critical for backend implementation):**
- Joining a team uses `SELECT ... FOR UPDATE` row-level lock on the `teams` row to prevent exceeding `max_members`
- Four `UNIQUE` constraints prevent duplicates: `user_skills(user_id,skill_id)`, `team_members(team_id,user_id)`, `join_requests(user_id,team_id)`, `users.username`, `users.email`
- Five triggers auto-create records: creator becomes team leader, projects get default task lists, notifications fire on application review and task assignment
