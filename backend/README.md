# 团队项目协作管理系统

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.3.3 |
| JDK | Java | 21 |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis-Plus | 3.5.7 |
| 鉴权 | Spring Security + JWT (jjwt) | 0.12.6 |
| 工具库 | Lombok, Hutool | 5.8.29 |
| 文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| WebSocket | Spring WebSocket | - |

## 快速启动

### 1. 环境要求
- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### 2. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS team_collab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE team_collab;
SOURCE src/main/resources/db/init.sql;
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    username: root
    password: your_password
```

### 4. 启动
```bash
mvn spring-boot:run
# 或直接在 IDEA 运行 TeamCollabApplication
```

### 5. 访问
- API 文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/api/v1/health

## 项目结构

```
teamcollab-server/
├── pom.xml
├── README.md
└── src/main/java/com/teamcollab/
    ├── TeamCollabApplication.java          # 启动类
    ├── config/                              # 配置 (CORS/MyBatis/Jackson/WebSocket/Knife4j)
    ├── security/                            # 安全 (JWT生成/过滤器/SpringSecurity配置)
    ├── common/                              # 通用组件
    │   ├── enums/                           #   枚举 (10种)
    │   ├── exception/                       #   异常 (业务异常+全局处理)
    │   ├── response/                        #   统一返回 (Result+PageResult)
    │   ├── util/                            #   工具类 (BCrypt/分页/排序安全)
    │   ├── constant/                        #   常量
    │   ├── annotation/                      #   注解 (@RequireLeader/@RequireAdmin)
    │   └── valid/                           #   校验分组
    ├── entity/                              # 实体 (13个)
    ├── dto/                                 # 数据传输对象 (50+)
    │   ├── auth/ user/ skill/ team/ application/
    │   ├── project/ stage/ tasklist/ task/
    │   ├── stats/ notification/ admin/
    ├── mapper/                              # 数据访问层 (13个接口+12个XML)
    ├── service/                             # 业务逻辑层 (12接口+12实现)
    ├── controller/                          # 控制器 (12个, 58个API端点)
    ├── websocket/                           # 实时通知
    └── scheduler/                           # 定时任务
```

## API 概览 (58个端点)

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /api/v1/auth/register | 用户注册 |
| | POST /api/v1/auth/login | 用户登录 |
| 用户 | GET /api/v1/users/me | 个人信息 |
| | PUT /api/v1/users/me | 更新资料 |
| | PUT /api/v1/users/me/password | 修改密码 |
| | GET /api/v1/users/me/teams | 我的团队 |
| | GET /api/v1/users/me/tasks | 我的任务 |
| | GET /api/v1/users/me/applications | 申请记录 |
| | POST/DELETE /api/v1/users/me/skills | 添加/删除技能 |
| 技能库 | GET /api/v1/skills | 技能列表 |
| | GET /api/v1/skills/categories | 分类列表 |
| 团队 | GET/POST /api/v1/teams | 招募墙/创建 |
| | GET/PUT/DELETE /api/v1/teams/{id} | 详情/编辑/解散 |
| | GET /api/v1/teams/{id}/members | 成员列表 |
| | DELETE /api/v1/teams/{id}/members/{uid} | 移除成员 |
| 入队申请 | GET/POST /api/v1/teams/{id}/applications | 申请 |
| | PUT /api/v1/teams/{id}/applications/{rid} | 审核 |
| 项目 | GET/POST /api/v1/teams/{id}/projects | 列表/创建 |
| | GET/PUT/DELETE /api/v1/projects/{id} | 详情/编辑/删除 |
| 阶段 | GET/POST /api/v1/projects/{id}/stages | 列表/创建 |
| | PUT/DELETE /api/v1/stages/{id} | 编辑/删除 |
| | PUT /api/v1/stages/{id}/status | 更新状态 |
| | GET /api/v1/stage-templates | 模板列表 |
| | POST /api/v1/projects/{id}/stages/template | 套用模板 |
| 任务列表 | GET/POST /api/v1/projects/{id}/lists | 看板列 |
| | PUT/DELETE /api/v1/task-lists/{id} | 编辑/删除 |
| | PUT /api/v1/task-lists/reorder | 重排 |
| 任务 | GET/POST /api/v1/task-lists/{id}/tasks | 任务列表/创建 |
| | GET/PUT/DELETE /api/v1/tasks/{id} | 详情/编辑/删除 |
| | PUT /api/v1/tasks/{id}/move | 移动 |
| | PUT /api/v1/tasks/reorder | 重排 |
| 统计 | GET /api/v1/teams/{id}/stats | 团队统计 |
| | GET /api/v1/teams/{id}/members/{uid}/stats | 个人贡献 |
| 通知 | GET /api/v1/notifications | 通知列表 |
| | GET /api/v1/notifications/unread-count | 未读数 |
| | PUT /api/v1/notifications/{id}/read | 标记已读 |
| | PUT /api/v1/notifications/read-all | 全部已读 |
| 管理员 | GET /api/v1/admin/users | 用户列表 |
| | PUT /api/v1/admin/users/{id} | 禁用/启用 |
| | GET/DELETE /api/v1/admin/teams | 团队管理 |
| | POST/DELETE /api/v1/admin/skills | 技能管理 |
