# 团队项目协作管理系统 —— RESTful API 接口文档

---

## 文档版本

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0 | 2026-06-22 | — | 初始版本，覆盖全部业务模块 |

---

## 目录

- [一、通用约定](#一通用约定)
  - [1.1 基础信息](#11-基础信息)
  - [1.2 统一返回格式](#12-统一返回格式)
  - [1.3 全局错误码](#13-全局错误码)
  - [1.4 请求头规范](#14-请求头规范)
  - [1.5 分页规范](#15-分页规范)
  - [1.6 枚举值说明](#16-枚举值说明)
- [二、认证模块](#二认证模块)
- [三、用户模块](#三用户模块)
- [四、技能库模块](#四技能库模块)
- [五、团队模块](#五团队模块)
- [六、入队申请模块](#六入队申请模块)
- [七、项目模块](#七项目模块)
- [八、阶段模块](#八阶段模块)
- [九、任务列表模块](#九任务列表模块)
- [十、任务模块](#十任务模块)
- [十一、统计模块](#十一统计模块)
- [十二、通知模块](#十二通知模块)
- [十三、管理员模块](#十三管理员模块)

---

## 一、通用约定

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| **Base URL** | `http://localhost:8080/api/v1` |
| **字符编码** | UTF-8 |
| **请求格式** | `application/json` (Content-Type) |
| **响应格式** | `application/json` |
| **鉴权方式** | JWT Bearer Token（除注册/登录外，所有接口需携带） |

### 1.2 统一返回格式

所有接口统一返回以下 JSON 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

**分页查询** 返回的 `data` 结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,
    "totalPages": 10,
    "records": []
  }
}
```

### 1.3 全局错误码

| 错误码 | 含义 | 说明 |
|--------|------|------|
| `200` | 成功 | 请求正常处理 |
| `400` | 请求参数错误 | 缺少必填字段、格式错误、校验失败 |
| `401` | 未登录/Token 过期 | Token 无效或已过期，需重新登录 |
| `403` | 无权限 | 当前用户没有该操作的权限（如非队长尝试审核） |
| `404` | 资源不存在 | 查询的实体不存在 |
| `409` | 资源冲突 | 重复申请、重复入队、用户名已存在等 |
| `500` | 服务器内部错误 | 服务端异常 |

**业务级错误码（`code` 字段值）**：

| code | 说明 |
|------|------|
| `200` | 操作成功 |
| `20001` | 用户名已存在 |
| `20002` | 邮箱已被注册 |
| `20003` | 原密码错误 |
| `20004` | 用户已被禁用 |
| `30001` | 团队不存在 |
| `30002` | 团队已满员 |
| `30003` | 非团队成员，无权操作 |
| `30004` | 非队长，无权操作 |
| `30005` | 已经申请过该团队 |
| `30006` | 已是该团队成员 |
| `30007` | 不能移除队长 |
| `40001` | 项目不存在 |
| `40002` | 阶段不存在 |
| `40003` | 阶段下仍有未完成任务，无法删除 |
| `40004` | 任务列表不存在 |
| `40005` | 默认列表不可删除 |
| `40006` | 任务不存在 |
| `50001` | 技能标签不存在 |
| `50002` | 技能标签已存在 |

### 1.4 请求头规范

**需鉴权的接口** 均需携带以下请求头：

| Header 名 | 值 | 必填 | 说明 |
|------------|----|------|------|
| `Authorization` | `Bearer <jwt_token>` | 是 | JWT 鉴权 Token |
| `Content-Type` | `application/json` | 是 | 请求体格式 |

### 1.5 分页规范

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pageNum` | Integer | `1` | 当前页码，从 1 开始 |
| `pageSize` | Integer | `10` | 每页条数，最大不超过 100 |

### 1.6 枚举值说明

**团队状态 `status`**：

| 值 | 含义 |
|----|------|
| `recruiting` | 招募中 |
| `closed` | 已关闭招募 |

**阶段状态 `status`**：

| 值 | 含义 |
|----|------|
| `not_started` | 未开始 |
| `in_progress` | 进行中 |
| `completed` | 已完成 |

**任务优先级 `priority`**：

| 值 | 含义 |
|----|------|
| `high` | 高 |
| `medium` | 中 |
| `low` | 低 |

**申请状态 `status`**：

| 值 | 含义 |
|----|------|
| `pending` | 待审核 |
| `approved` | 已通过 |
| `rejected` | 已拒绝 |

**队内角色 `role`**：

| 值 | 含义 |
|----|------|
| `leader` | 队长 |
| `member` | 普通成员 |

**技能熟练度 `level`**：

| 值 | 含义 |
|----|------|
| `beginner` | 初学 |
| `intermediate` | 掌握 |
| `advanced` | 精通 |

---

## 二、认证模块

### 2.1 用户注册

| 属性 | 内容 |
|------|------|
| **接口名称** | 用户注册 |
| **请求方式** | `POST` |
| **接口路径** | `/auth/register` |
| **是否鉴权** | 否 |

**Body 请求参数**：

```json
{
  "username": "zhangsan",
  "password": "123456",
  "email": "zhangsan@example.com",
  "skillIds": [1, 3, 5]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | String | 是 | 用户名，4-20 位字母、数字或下划线，全局唯一 |
| `password` | String | 是 | 密码，6-20 位 |
| `email` | String | 是 | 邮箱地址，全局唯一 |
| `skillIds` | Long[] | 否 | 初始技能标签 ID 列表（可选） |

**返回示例**：

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": 1,
    "username": "zhangsan"
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `20001` | 用户名已存在 |
| `20002` | 邮箱已被注册 |
| `400` | 用户名/密码/邮箱格式不合法 |

---

### 2.2 用户登录

| 属性 | 内容 |
|------|------|
| **接口名称** | 用户登录 |
| **请求方式** | `POST` |
| **接口路径** | `/auth/login` |
| **是否鉴权** | 否 |

**Body 请求参数**：

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userInfo": {
      "userId": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "avatar": "https://cdn.example.com/avatars/default.png",
      "bio": null,
      "role": "member"
    }
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `401` | 用户名或密码错误 |
| `20004` | 用户已被禁用 |

---

## 三、用户模块

### 3.1 获取当前用户信息

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取当前用户信息 |
| **请求方式** | `GET` |
| **接口路径** | `/users/me` |
| **是否鉴权** | 是 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "avatar": "https://cdn.example.com/avatars/default.png",
    "bio": "热爱编程，擅长后端开发",
    "role": "member",
    "skills": [
      { "skillId": 1, "skillName": "Java", "category": "后端", "level": "advanced" },
      { "skillId": 3, "skillName": "MySQL", "category": "数据库", "level": "intermediate" }
    ],
    "createdAt": "2026-01-15 10:30:00"
  }
}
```

---

### 3.2 更新个人资料

| 属性 | 内容 |
|------|------|
| **接口名称** | 更新个人资料 |
| **请求方式** | `PUT` |
| **接口路径** | `/users/me` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "avatar": "https://cdn.example.com/avatars/zhangsan.jpg",
  "bio": "热爱编程，擅长后端开发"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `avatar` | String | 否 | 头像 URL |
| `bio` | String | 否 | 个人简介，最长 500 字 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 3.3 修改密码

| 属性 | 内容 |
|------|------|
| **接口名称** | 修改密码 |
| **请求方式** | `PUT` |
| **接口路径** | `/users/me/password` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `oldPassword` | String | 是 | 原密码 |
| `newPassword` | String | 是 | 新密码，6-20 位 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "密码修改成功",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `20003` | 原密码错误 |

---

### 3.4 获取我的团队列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取我的团队列表 |
| **请求方式** | `GET` |
| **接口路径** | `/users/me/teams` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 3,
    "totalPages": 1,
    "records": [
      {
        "teamId": 1,
        "teamName": "后端开发小组",
        "description": "专注于 Spring Boot 微服务开发",
        "myRole": "leader",
        "status": "recruiting",
        "currentMembers": 5,
        "maxMembers": 10,
        "joinedAt": "2026-01-20 14:00:00"
      },
      {
        "teamId": 2,
        "teamName": "数据分析小分队",
        "description": "使用 Python 进行数据挖掘与分析",
        "myRole": "member",
        "status": "closed",
        "currentMembers": 8,
        "maxMembers": 8,
        "joinedAt": "2026-02-10 09:30:00"
      }
    ]
  }
}
```

---

### 3.5 获取我的任务

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取我的任务（跨团队聚合） |
| **请求方式** | `GET` |
| **接口路径** | `/users/me/tasks` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `priority` | String | 否 | — | 按优先级筛选：`high` / `medium` / `low` |
| `listId` | Long | 否 | — | 按任务列表筛选 |
| `teamId` | Long | 否 | — | 按所属团队筛选 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 12,
    "totalPages": 2,
    "records": [
      {
        "taskId": 5,
        "title": "设计用户权限表结构",
        "priority": "high",
        "status": "in_progress",
        "dueDate": "2026-06-25 18:00:00",
        "projectName": "团队协作系统",
        "teamName": "后端开发小组",
        "teamId": 1,
        "listName": "进行中",
        "stageName": "系统设计",
        "createdAt": "2026-06-15 10:00:00"
      }
    ]
  }
}
```

---

### 3.6 获取我的入队申请记录

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取我的入队申请记录 |
| **请求方式** | `GET` |
| **接口路径** | `/users/me/applications` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `status` | String | 否 | — | 申请状态：`pending` / `approved` / `rejected` |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 2,
    "totalPages": 1,
    "records": [
      {
        "requestId": 10,
        "teamId": 3,
        "teamName": "前端开发小组",
        "message": "我有 2 年 React 开发经验，希望加入贡献",
        "status": "pending",
        "createdAt": "2026-06-20 08:00:00",
        "reviewedAt": null
      },
      {
        "requestId": 5,
        "teamId": 1,
        "teamName": "后端开发小组",
        "message": "熟练掌握 Java 和 Spring Boot",
        "status": "approved",
        "createdAt": "2026-01-19 14:30:00",
        "reviewedAt": "2026-01-20 10:00:00"
      }
    ]
  }
}
```

---

### 3.7 添加个人技能

| 属性 | 内容 |
|------|------|
| **接口名称** | 添加个人技能 |
| **请求方式** | `POST` |
| **接口路径** | `/users/me/skills` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "skillId": 5,
  "level": "intermediate"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `skillId` | Long | 是 | 技能 ID |
| `level` | String | 是 | 熟练度：`beginner` / `intermediate` / `advanced` |

**返回示例**：

```json
{
  "code": 200,
  "msg": "技能添加成功",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `409` | 该技能已添加，不可重复 |
| `50001` | 技能标签不存在 |

---

### 3.8 删除个人技能

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除个人技能 |
| **请求方式** | `DELETE` |
| **接口路径** | `/users/me/skills/{skill-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `skill-id` | Long | 是 | 技能 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "技能删除成功",
  "data": null
}
```

---

## 四、技能库模块

### 4.1 获取技能列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取技能列表 |
| **请求方式** | `GET` |
| **接口路径** | `/skills` |
| **是否鉴权** | 否 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `category` | String | 否 | — | 按分类筛选，如 `前端`、`后端` |
| `keyword` | String | 否 | — | 按技能名称模糊搜索 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 50 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 50,
    "total": 5,
    "totalPages": 1,
    "records": [
      { "skillId": 1, "skillName": "Java", "category": "后端" },
      { "skillId": 2, "skillName": "Python", "category": "后端" },
      { "skillId": 3, "skillName": "Go", "category": "后端" },
      { "skillId": 4, "skillName": "Node.js", "category": "后端" },
      { "skillId": 5, "skillName": "Spring Boot", "category": "后端" }
    ]
  }
}
```

---

### 4.2 获取技能分类列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取技能分类列表 |
| **请求方式** | `GET` |
| **接口路径** | `/skills/categories` |
| **是否鉴权** | 否 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    "前端",
    "后端",
    "数据库",
    "移动端",
    "设计",
    "DevOps",
    "其他"
  ]
}
```

---

## 五、团队模块

### 5.1 获取招募墙团队列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取招募墙团队列表 |
| **请求方式** | `GET` |
| **接口路径** | `/teams` |
| **是否鉴权** | 否 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `status` | String | 否 | `recruiting` | 团队状态，招募墙默认传 `recruiting` |
| `skillId` | Long | 否 | — | 按所需技能 ID 筛选 |
| `keyword` | String | 否 | — | 按团队名称模糊搜索 |
| `minMembers` | Integer | 否 | — | 最低成员数 |
| `maxMembers` | Integer | 否 | — | 最高成员数 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 12 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 12,
    "total": 8,
    "totalPages": 1,
    "records": [
      {
        "teamId": 1,
        "name": "后端开发小组",
        "description": "专注于 Spring Boot 微服务开发",
        "status": "recruiting",
        "currentMembers": 5,
        "maxMembers": 10,
        "creatorName": "zhangsan",
        "requiredSkills": [
          { "skillId": 1, "skillName": "Java", "category": "后端" },
          { "skillId": 3, "skillName": "MySQL", "category": "数据库" }
        ],
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

---

### 5.2 创建团队

| 属性 | 内容 |
|------|------|
| **接口名称** | 创建团队 |
| **请求方式** | `POST` |
| **接口路径** | `/teams` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "name": "后端开发小组",
  "description": "专注于 Spring Boot 微服务开发",
  "maxMembers": 10,
  "requiredSkillIds": [1, 3, 5]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 团队名称，2-50 字 |
| `description` | String | 否 | 团队简介，最长 500 字 |
| `maxMembers` | Integer | 否 | 计划人数上限，默认 10，范围 2-100 |
| `requiredSkillIds` | Long[] | 否 | 所需技能 ID 列表 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "团队创建成功",
  "data": {
    "teamId": 1,
    "name": "后端开发小组",
    "status": "recruiting",
    "maxMembers": 10
  }
}
```

> **说明**：创建成功后，创建者自动成为队长，触发器将其加入 `team_members` 表。

---

### 5.3 获取团队详情

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取团队详情 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}` |
| **是否鉴权** | 否 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "teamId": 1,
    "name": "后端开发小组",
    "description": "专注于 Spring Boot 微服务开发",
    "status": "recruiting",
    "currentMembers": 5,
    "maxMembers": 10,
    "creatorId": 1,
    "creatorName": "zhangsan",
    "requiredSkills": [
      { "skillId": 1, "skillName": "Java", "category": "后端" },
      { "skillId": 3, "skillName": "MySQL", "category": "数据库" }
    ],
    "members": [
      {
        "userId": 1,
        "username": "zhangsan",
        "avatar": "https://cdn.example.com/avatars/default.png",
        "role": "leader",
        "joinedAt": "2026-01-15 10:30:00"
      },
      {
        "userId": 2,
        "username": "lisi",
        "avatar": "https://cdn.example.com/avatars/lisi.jpg",
        "role": "member",
        "joinedAt": "2026-01-20 14:00:00"
      }
    ],
    "createdAt": "2026-01-15 10:30:00",
    "updatedAt": "2026-06-20 08:00:00"
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |

---

### 5.4 编辑团队信息

| 属性 | 内容 |
|------|------|
| **接口名称** | 编辑团队信息 |
| **请求方式** | `PUT` |
| **接口路径** | `/teams/{team-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**Body 请求参数**：

```json
{
  "name": "后端开发小组（升级版）",
  "description": "专注于 Spring Boot 微服务与分布式系统",
  "maxMembers": 15,
  "status": "closed",
  "requiredSkillIds": [1, 3, 5, 7]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 否 | 团队名称 |
| `description` | String | 否 | 团队简介 |
| `maxMembers` | Integer | 否 | 计划人数上限，不能小于当前成员数 |
| `status` | String | 否 | 招募状态：`recruiting` / `closed` |
| `requiredSkillIds` | Long[] | 否 | 所需技能 ID 列表（全量替换） |

**返回示例**：

```json
{
  "code": 200,
  "msg": "团队信息更新成功",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30004` | 非队长，无权操作 |

---

### 5.5 解散团队

| 属性 | 内容 |
|------|------|
| **接口名称** | 解散团队 |
| **请求方式** | `DELETE` |
| **接口路径** | `/teams/{team-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `confirm` | String | 否 | — | 二次确认，需传入团队名称原文 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "团队已解散",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30004` | 非队长，无权操作 |
| `400` | confirm 校验不通过 |

---

### 5.6 获取团队成员列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取团队成员列表 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}/members` |
| **是否鉴权** | 否 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "userId": 1,
      "username": "zhangsan",
      "avatar": "https://cdn.example.com/avatars/default.png",
      "role": "leader",
      "joinedAt": "2026-01-15 10:30:00"
    },
    {
      "userId": 2,
      "username": "lisi",
      "avatar": "https://cdn.example.com/avatars/lisi.jpg",
      "role": "member",
      "joinedAt": "2026-01-20 14:00:00"
    }
  ]
}
```

---

### 5.7 移除成员

| 属性 | 内容 |
|------|------|
| **接口名称** | 移除团队成员 |
| **请求方式** | `DELETE` |
| **接口路径** | `/teams/{team-id}/members/{user-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |
| `user-id` | Long | 是 | 被移除用户 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "成员已移除",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30004` | 非队长，无权操作 |
| `30007` | 不能移除队长 |
| `404` | 该用户不是团队成员 |

---

## 六、入队申请模块

### 6.1 提交入队申请

| 属性 | 内容 |
|------|------|
| **接口名称** | 提交入队申请 |
| **请求方式** | `POST` |
| **接口路径** | `/teams/{team-id}/applications` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 目标团队 ID |

**Body 请求参数**：

```json
{
  "message": "我有 2 年后端开发经验，熟悉 Spring Boot 和 MySQL，希望能为团队贡献力量"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | String | 否 | 申请留言，最长 500 字 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "申请已提交，请等待队长审核",
  "data": {
    "requestId": 10
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30002` | 团队已满员 |
| `30005` | 已经申请过该团队 |
| `30006` | 已是该团队成员 |
| `30004` | 团队已关闭招募（status=closed） |

---

### 6.2 获取入队申请列表（队长视角）

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取入队申请列表 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}/applications` |
| **是否鉴权** | 是 |

**权限**：仅队长可查看

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `status` | String | 否 | — | 筛选状态：`pending` / `approved` / `rejected` |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 3,
    "totalPages": 1,
    "records": [
      {
        "requestId": 10,
        "userId": 5,
        "username": "wangwu",
        "avatar": "https://cdn.example.com/avatars/wangwu.jpg",
        "userSkills": [
          { "skillId": 1, "skillName": "Java", "level": "advanced" },
          { "skillId": 3, "skillName": "MySQL", "level": "intermediate" }
        ],
        "message": "我有 2 年后端开发经验",
        "status": "pending",
        "createdAt": "2026-06-20 08:00:00"
      }
    ]
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30004` | 非队长，无权查看 |

---

### 6.3 审核入队申请

| 属性 | 内容 |
|------|------|
| **接口名称** | 审核入队申请（通过/拒绝） |
| **请求方式** | `PUT` |
| **接口路径** | `/teams/{team-id}/applications/{request-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |
| `request-id` | Long | 是 | 申请记录 ID |

**Body 请求参数**：

```json
{
  "action": "approve"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `action` | String | 是 | 审核操作：`approve`（通过）/ `reject`（拒绝） |

**返回示例（通过）**：

```json
{
  "code": 200,
  "msg": "申请已通过，申请人已加入团队",
  "data": null
}
```

**返回示例（拒绝）**：

```json
{
  "code": 200,
  "msg": "申请已拒绝",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30002` | 团队已满员（通过时校验） |
| `30004` | 非队长，无权操作 |
| `404` | 申请记录不存在 |
| `409` | 该申请已处理（非 pending 状态） |

> **并发安全说明**：审核通过时使用 `SELECT ... FOR UPDATE` 行级锁锁定团队记录，防止并发超募。

---

## 七、项目模块

### 7.1 获取团队项目列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取团队项目列表 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}/projects` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 2,
    "totalPages": 1,
    "records": [
      {
        "projectId": 1,
        "teamId": 1,
        "name": "团队协作管理系统",
        "description": "数据库课程设计项目",
        "taskStats": {
          "total": 15,
          "completed": 8,
          "completionRate": 53.3
        },
        "createdAt": "2026-03-01 09:00:00",
        "updatedAt": "2026-06-20 16:00:00"
      }
    ]
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `30001` | 团队不存在 |
| `30003` | 非团队成员，无权查看 |

---

### 7.2 创建项目

| 属性 | 内容 |
|------|------|
| **接口名称** | 创建项目 |
| **请求方式** | `POST` |
| **接口路径** | `/teams/{team-id}/projects` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**Body 请求参数**：

```json
{
  "name": "团队协作管理系统",
  "description": "数据库课程设计项目，开发一个团队协作管理平台"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 项目名称，2-100 字 |
| `description` | String | 否 | 项目描述，最长 500 字 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "项目创建成功",
  "data": {
    "projectId": 1,
    "name": "团队协作管理系统"
  }
}
```

> **说明**：创建项目后，触发器自动生成三个默认任务列表：待办、进行中、已完成。

---

### 7.3 获取项目详情

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取项目详情（含阶段进度、任务统计） |
| **请求方式** | `GET` |
| **接口路径** | `/projects/{project-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "projectId": 1,
    "teamId": 1,
    "teamName": "后端开发小组",
    "name": "团队协作管理系统",
    "description": "数据库课程设计项目",
    "taskStats": {
      "total": 15,
      "completed": 8,
      "inProgress": 4,
      "todo": 3,
      "completionRate": 53.3
    },
    "stages": [
      {
        "stageId": 1,
        "name": "需求分析",
        "status": "completed",
        "startDate": "2026-03-01",
        "endDate": "2026-03-15",
        "orderIndex": 0,
        "completionRate": 100.0
      },
      {
        "stageId": 2,
        "name": "系统设计",
        "status": "in_progress",
        "startDate": "2026-03-16",
        "endDate": "2026-04-15",
        "orderIndex": 1,
        "completionRate": 60.0
      }
    ],
    "createdAt": "2026-03-01 09:00:00",
    "updatedAt": "2026-06-20 16:00:00"
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `40001` | 项目不存在 |
| `30003` | 非团队成员，无权查看 |

---

### 7.4 编辑项目

| 属性 | 内容 |
|------|------|
| **接口名称** | 编辑项目信息 |
| **请求方式** | `PUT` |
| **接口路径** | `/projects/{project-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**Body 请求参数**：

```json
{
  "name": "团队协作管理系统 v2.0",
  "description": "升级版数据库课程设计项目"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 否 | 项目名称 |
| `description` | String | 否 | 项目描述 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "项目信息更新成功",
  "data": null
}
```

---

### 7.5 删除项目

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除项目 |
| **请求方式** | `DELETE` |
| **接口路径** | `/projects/{project-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "项目已删除",
  "data": null
}
```

> **说明**：删除项目将级联删除其下的所有阶段、任务列表和任务。

---

## 八、阶段模块

### 8.1 获取项目阶段列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取项目阶段列表 |
| **请求方式** | `GET` |
| **接口路径** | `/projects/{project-id}/stages` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "stageId": 1,
      "projectId": 1,
      "name": "需求分析",
      "description": "收集与分析项目需求",
      "startDate": "2026-03-01",
      "endDate": "2026-03-15",
      "orderIndex": 0,
      "status": "completed",
      "taskStats": {
        "total": 5,
        "completed": 5,
        "completionRate": 100.0
      },
      "isOverdue": false
    },
    {
      "stageId": 2,
      "projectId": 1,
      "name": "系统设计",
      "description": "完成数据库设计和接口设计",
      "startDate": "2026-03-16",
      "endDate": "2026-04-15",
      "orderIndex": 1,
      "status": "in_progress",
      "taskStats": {
        "total": 5,
        "completed": 3,
        "completionRate": 60.0
      },
      "isOverdue": true
    }
  ]
}
```

> `isOverdue` 判断逻辑：当前日期 > endDate 且 status ≠ `completed` 时为 `true`。

---

### 8.2 创建阶段

| 属性 | 内容 |
|------|------|
| **接口名称** | 创建阶段 |
| **请求方式** | `POST` |
| **接口路径** | `/projects/{project-id}/stages` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**Body 请求参数**：

```json
{
  "name": "测试验收",
  "description": "完成功能测试和用户验收",
  "startDate": "2026-05-01",
  "endDate": "2026-05-20"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 阶段名称，2-50 字 |
| `description` | String | 否 | 阶段描述 |
| `startDate` | String | 否 | 开始日期，格式 `YYYY-MM-DD` |
| `endDate` | String | 否 | 截止日期，格式 `YYYY-MM-DD` |

**返回示例**：

```json
{
  "code": 200,
  "msg": "阶段创建成功",
  "data": {
    "stageId": 3,
    "name": "测试验收",
    "orderIndex": 2,
    "status": "not_started"
  }
}
```

> **说明**：`orderIndex` 自动取当前项目下最大 `orderIndex + 1`。

---

### 8.3 编辑阶段

| 属性 | 内容 |
|------|------|
| **接口名称** | 编辑阶段信息 |
| **请求方式** | `PUT` |
| **接口路径** | `/stages/{stage-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `stage-id` | Long | 是 | 阶段 ID |

**Body 请求参数**：

```json
{
  "name": "测试验收（修订）",
  "description": "完成集成测试和用户验收测试",
  "startDate": "2026-05-02",
  "endDate": "2026-05-25"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 否 | 阶段名称 |
| `description` | String | 否 | 描述 |
| `startDate` | String | 否 | 开始日期 |
| `endDate` | String | 否 | 截止日期 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "阶段信息更新成功",
  "data": null
}
```

---

### 8.4 删除阶段

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除阶段 |
| **请求方式** | `DELETE` |
| **接口路径** | `/stages/{stage-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `stage-id` | Long | 是 | 阶段 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "阶段已删除",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `40002` | 阶段不存在 |
| `40003` | 阶段下仍有未完成任务，无法删除 |

> **说明**：删除阶段时，该阶段下的任务 `stage_id` 将被设为 NULL（`ON DELETE SET NULL`）。

---

### 8.5 更新阶段状态

| 属性 | 内容 |
|------|------|
| **接口名称** | 更新阶段状态 |
| **请求方式** | `PUT` |
| **接口路径** | `/stages/{stage-id}/status` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `stage-id` | Long | 是 | 阶段 ID |

**Body 请求参数**：

```json
{
  "status": "in_progress"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | String | 是 | 阶段状态：`not_started` / `in_progress` / `completed` |

**返回示例**：

```json
{
  "code": 200,
  "msg": "阶段状态已更新",
  "data": null
}
```

---

### 8.6 获取阶段模板列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取阶段模板列表 |
| **请求方式** | `GET` |
| **接口路径** | `/stage-templates` |
| **是否鉴权** | 是 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "templateId": 1,
      "templateName": "标准瀑布模型",
      "stageCount": 5,
      "stages": [
        { "name": "需求分析", "description": "收集与分析需求", "orderIndex": 0 },
        { "name": "系统设计", "description": "架构与接口设计", "orderIndex": 1 },
        { "name": "编码开发", "description": "核心功能实现", "orderIndex": 2 },
        { "name": "测试验收", "description": "测试与验收通过", "orderIndex": 3 },
        { "name": "部署上线", "description": "部署到生产环境", "orderIndex": 4 }
      ]
    },
    {
      "templateId": 2,
      "templateName": "课程设计",
      "stageCount": 4,
      "stages": [
        { "name": "开题报告", "description": "确定选题与方案", "orderIndex": 0 },
        { "name": "中期检查", "description": "检查中期进度", "orderIndex": 1 },
        { "name": "成果开发", "description": "完成主要开发工作", "orderIndex": 2 },
        { "name": "答辩准备", "description": "准备答辩材料", "orderIndex": 3 }
      ]
    }
  ]
}
```

---

### 8.7 使用模板批量创建阶段

| 属性 | 内容 |
|------|------|
| **接口名称** | 使用模板批量创建阶段 |
| **请求方式** | `POST` |
| **接口路径** | `/projects/{project-id}/stages/template` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**Body 请求参数**：

```json
{
  "templateId": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `templateId` | Long | 是 | 模板 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "已按模板创建 5 个阶段",
  "data": [
    { "stageId": 4, "name": "需求分析", "orderIndex": 0 },
    { "stageId": 5, "name": "系统设计", "orderIndex": 1 },
    { "stageId": 6, "name": "编码开发", "orderIndex": 2 },
    { "stageId": 7, "name": "测试验收", "orderIndex": 3 },
    { "stageId": 8, "name": "部署上线", "orderIndex": 4 }
  ]
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `404` | 模板不存在 |

---

## 九、任务列表模块

### 9.1 获取看板列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取项目下所有任务列表（看板列） |
| **请求方式** | `GET` |
| **接口路径** | `/projects/{project-id}/lists` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "listId": 1,
      "name": "待办",
      "position": 0,
      "isDefault": true,
      "taskCount": 3
    },
    {
      "listId": 2,
      "name": "进行中",
      "position": 1,
      "isDefault": true,
      "taskCount": 4
    },
    {
      "listId": 3,
      "name": "已完成",
      "position": 2,
      "isDefault": true,
      "taskCount": 8
    },
    {
      "listId": 4,
      "name": "代码审查中",
      "position": 3,
      "isDefault": false,
      "taskCount": 2
    }
  ]
}
```

---

### 9.2 创建自定义任务列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 创建自定义任务列表 |
| **请求方式** | `POST` |
| **接口路径** | `/projects/{project-id}/lists` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project-id` | Long | 是 | 项目 ID |

**Body 请求参数**：

```json
{
  "name": "代码审查中"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 列表名称，2-50 字 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务列表创建成功",
  "data": {
    "listId": 4,
    "name": "代码审查中",
    "position": 3,
    "isDefault": false
  }
}
```

> `position` 自动取当前项目下最大 position + 1。

---

### 9.3 编辑任务列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 编辑任务列表信息 |
| **请求方式** | `PUT` |
| **接口路径** | `/task-lists/{list-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `list-id` | Long | 是 | 列表 ID |

**Body 请求参数**：

```json
{
  "name": "等待测试"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 新名称 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "列表信息更新成功",
  "data": null
}
```

---

### 9.4 删除任务列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除任务列表 |
| **请求方式** | `DELETE` |
| **接口路径** | `/task-lists/{list-id}` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `list-id` | Long | 是 | 列表 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "列表已删除",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `40004` | 任务列表不存在 |
| `40005` | 默认列表不可删除 |

---

### 9.5 拖拽重排任务列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 拖拽重排任务列表 |
| **请求方式** | `PUT` |
| **接口路径** | `/task-lists/reorder` |
| **是否鉴权** | 是 |

**权限**：仅队长可操作

**Body 请求参数**：

```json
{
  "orders": [
    { "listId": 1, "position": 0 },
    { "listId": 4, "position": 1 },
    { "listId": 2, "position": 2 },
    { "listId": 3, "position": 3 }
  ]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orders` | Object[] | 是 | 排序后的列表顺序 |
| `orders[].listId` | Long | 是 | 列表 ID |
| `orders[].position` | Integer | 是 | 新的排序位置 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "列表顺序已更新",
  "data": null
}
```

---

## 十、任务模块

### 10.1 获取任务列表下的任务

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取任务列表下的任务 |
| **请求方式** | `GET` |
| **接口路径** | `/task-lists/{list-id}/tasks` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `list-id` | Long | 是 | 列表 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `sortBy` | String | 否 | `position` | 排序方式：`position`（默认）/ `priority` / `due_date` / `created_at` |
| `priority` | String | 否 | — | 按优先级筛选 |
| `stageId` | Long | 否 | — | 按阶段筛选 |
| `assigneeId` | Long | 否 | — | 按执行人筛选 |
| `keyword` | String | 否 | — | 按标题模糊搜索 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 20 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 4,
    "totalPages": 1,
    "records": [
      {
        "taskId": 1,
        "title": "设计用户权限表结构",
        "description": "根据RBAC模型设计用户-角色-权限三张表",
        "priority": "high",
        "listId": 2,
        "listName": "进行中",
        "stageId": 2,
        "stageName": "系统设计",
        "assigneeId": 5,
        "assigneeName": "wangwu",
        "assigneeAvatar": "https://cdn.example.com/avatars/wangwu.jpg",
        "creatorId": 1,
        "creatorName": "zhangsan",
        "dueDate": "2026-06-25 18:00:00",
        "position": 0,
        "createdAt": "2026-06-15 10:00:00",
        "updatedAt": "2026-06-18 14:30:00"
      }
    ]
  }
}
```

---

### 10.2 创建任务

| 属性 | 内容 |
|------|------|
| **接口名称** | 创建任务 |
| **请求方式** | `POST` |
| **接口路径** | `/task-lists/{list-id}/tasks` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `list-id` | Long | 是 | 目标列表 ID |

**Body 请求参数**：

```json
{
  "title": "设计用户权限表结构",
  "description": "根据RBAC模型设计用户-角色-权限三张表",
  "priority": "high",
  "dueDate": "2026-06-25 18:00:00",
  "assigneeId": 5,
  "stageId": 2
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | String | 是 | 任务标题，2-100 字 |
| `description` | String | 否 | 任务描述 |
| `priority` | String | 否 | 优先级：`high` / `medium` / `low`，默认 `medium` |
| `dueDate` | String | 否 | 截止日期，格式 `YYYY-MM-DD HH:mm:ss` |
| `assigneeId` | Long | 否 | 执行人用户 ID，必须是团队成员 |
| `stageId` | Long | 否 | 所属阶段 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务创建成功",
  "data": {
    "taskId": 1,
    "title": "设计用户权限表结构",
    "priority": "high",
    "position": 0,
    "listName": "待办"
  }
}
```

> **说明**：`position` 自动取当前列表下最大 position + 1。若指定了 `assigneeId`，系统自动发送任务分配通知。

---

### 10.3 获取任务详情

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取任务详情 |
| **请求方式** | `GET` |
| **接口路径** | `/tasks/{task-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `task-id` | Long | 是 | 任务 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "taskId": 1,
    "title": "设计用户权限表结构",
    "description": "根据RBAC模型设计用户-角色-权限三张表，建立合理的索引和约束",
    "priority": "high",
    "listId": 2,
    "listName": "进行中",
    "stageId": 2,
    "stageName": "系统设计",
    "projectId": 1,
    "projectName": "团队协作管理系统",
    "teamId": 1,
    "teamName": "后端开发小组",
    "assigneeId": 5,
    "assigneeName": "wangwu",
    "assigneeAvatar": "https://cdn.example.com/avatars/wangwu.jpg",
    "creatorId": 1,
    "creatorName": "zhangsan",
    "dueDate": "2026-06-25 18:00:00",
    "position": 0,
    "createdAt": "2026-06-15 10:00:00",
    "updatedAt": "2026-06-18 14:30:00"
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `40006` | 任务不存在 |

---

### 10.4 编辑任务

| 属性 | 内容 |
|------|------|
| **接口名称** | 编辑任务 |
| **请求方式** | `PUT` |
| **接口路径** | `/tasks/{task-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `task-id` | Long | 是 | 任务 ID |

**Body 请求参数**：

```json
{
  "title": "设计用户权限表结构（含数据字典）",
  "description": "补充字段说明文档",
  "priority": "high",
  "dueDate": "2026-06-28 18:00:00",
  "assigneeId": 5,
  "stageId": 2
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | String | 否 | 任务标题 |
| `description` | String | 否 | 任务描述 |
| `priority` | String | 否 | 优先级 |
| `dueDate` | String | 否 | 截止日期 |
| `assigneeId` | Long | 否 | 执行人（传 null 可取消指派） |
| `stageId` | Long | 否 | 所属阶段（传 null 可取消关联） |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务更新成功",
  "data": null
}
```

> **说明**：若执行人发生变更，系统自动向新/旧执行人发送通知。

---

### 10.5 删除任务

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除任务 |
| **请求方式** | `DELETE` |
| **接口路径** | `/tasks/{task-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `task-id` | Long | 是 | 任务 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务已删除",
  "data": null
}
```

---

### 10.6 移动任务到其他列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 移动任务到其他列表 |
| **请求方式** | `PUT` |
| **接口路径** | `/tasks/{task-id}/move` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `task-id` | Long | 是 | 任务 ID |

**Body 请求参数**：

```json
{
  "targetListId": 3,
  "targetPosition": 0
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `targetListId` | Long | 是 | 目标列表 ID |
| `targetPosition` | Integer | 否 | 在目标列表中的位置，默认追加到末尾 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务已移动",
  "data": null
}
```

---

### 10.7 看板内任务拖拽重排

| 属性 | 内容 |
|------|------|
| **接口名称** | 看板内任务拖拽重排 |
| **请求方式** | `PUT` |
| **接口路径** | `/tasks/reorder` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "listId": 2,
  "orders": [
    { "taskId": 3, "position": 0 },
    { "taskId": 1, "position": 1 },
    { "taskId": 5, "position": 2 }
  ]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `listId` | Long | 是 | 操作的列表 ID |
| `orders` | Object[] | 是 | 排序后的任务顺序 |
| `orders[].taskId` | Long | 是 | 任务 ID |
| `orders[].position` | Integer | 是 | 新的排序位置 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "任务顺序已更新",
  "data": null
}
```

---

## 十一、统计模块

### 11.1 团队进度统计

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取团队进度统计 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}/stats` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `projectId` | Long | 否 | — | 按项目筛选（默认统计所有项目） |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "teamId": 1,
    "teamName": "后端开发小组",
    "overview": {
      "totalTasks": 25,
      "completedTasks": 15,
      "inProgressTasks": 6,
      "todoTasks": 4,
      "completionRate": 60.0
    },
    "priorityBreakdown": {
      "high": { "total": 8, "completed": 5 },
      "medium": { "total": 12, "completed": 8 },
      "low": { "total": 5, "completed": 2 }
    },
    "memberStats": [
      {
        "userId": 5,
        "username": "wangwu",
        "avatar": "https://cdn.example.com/avatars/wangwu.jpg",
        "assignedTasks": 8,
        "completedTasks": 5,
        "overdueTasks": 1,
        "completionRate": 62.5,
        "contribution": 33.3
      }
    ]
  }
}
```

---

### 11.2 个人贡献度统计

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取个人贡献度统计 |
| **请求方式** | `GET` |
| **接口路径** | `/teams/{team-id}/members/{user-id}/stats` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |
| `user-id` | Long | 是 | 成员用户 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 5,
    "username": "wangwu",
    "teamId": 1,
    "teamName": "后端开发小组",
    "assignedTasks": 8,
    "completedTasks": 5,
    "inProgressTasks": 2,
    "todoTasks": 1,
    "overdueTasks": 1,
    "completionRate": 62.5,
    "contribution": 33.3,
    "tasksByPriority": {
      "high": { "assigned": 3, "completed": 2 },
      "medium": { "assigned": 4, "completed": 3 },
      "low": { "assigned": 1, "completed": 0 }
    }
  }
}
```

> **贡献度** = 个人完成任务数 / 团队所有完成任务数 × 100%

---

## 十二、通知模块

### 12.1 获取通知列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取通知列表 |
| **请求方式** | `GET` |
| **接口路径** | `/notifications` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `isRead` | Integer | 否 | — | `0` = 未读，`1` = 已读。不传则返回全部 |
| `type` | String | 否 | — | 通知类型筛选 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 15 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 15,
    "total": 20,
    "totalPages": 2,
    "records": [
      {
        "notificationId": 25,
        "type": "task_assigned",
        "title": "您被分配了新的任务",
        "content": "任务 \"设计用户权限表结构\" 已分配给您。",
        "relatedType": "task",
        "relatedId": 1,
        "isRead": 0,
        "createdAt": "2026-06-20 10:30:00"
      },
      {
        "notificationId": 24,
        "type": "join_approved",
        "title": "入队申请已通过",
        "content": "您加入团队 \"后端开发小组\" 的申请已通过！",
        "relatedType": "team",
        "relatedId": 1,
        "isRead": 1,
        "createdAt": "2026-06-18 09:00:00"
      }
    ]
  }
}
```

**通知类型 `type` 枚举**：

| type 值 | 说明 |
|---------|------|
| `join_approved` | 入队申请已通过 |
| `join_rejected` | 入队申请已拒绝 |
| `new_join_request` | 收到新的入队申请（仅队长） |
| `task_assigned` | 被分配新任务 |
| `task_updated` | 任务信息变更 |
| `due_reminder` | 任务截止日期提醒 |
| `stage_overdue` | 阶段超期提醒 |
| `member_removed` | 被移出团队 |

---

### 12.2 获取未读通知数

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取未读通知数 |
| **请求方式** | `GET` |
| **接口路径** | `/notifications/unread-count` |
| **是否鉴权** | 是 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "unreadCount": 5
  }
}
```

---

### 12.3 标记单条通知已读

| 属性 | 内容 |
|------|------|
| **接口名称** | 标记单条通知已读 |
| **请求方式** | `PUT` |
| **接口路径** | `/notifications/{notification-id}/read` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `notification-id` | Long | 是 | 通知 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "已标记为已读",
  "data": null
}
```

---

### 12.4 全部标记已读

| 属性 | 内容 |
|------|------|
| **接口名称** | 全部标记已读 |
| **请求方式** | `PUT` |
| **接口路径** | `/notifications/read-all` |
| **是否鉴权** | 是 |

**Body 请求参数**（可选）：

```json
{
  "type": "task_assigned"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | String | 否 | 仅标记指定类型的通知为已读，不传则全部标记 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "已全部标记为已读",
  "data": {
    "updatedCount": 5
  }
}
```

---

## 十三、管理员模块

> **权限说明**：以下所有接口均需管理员角色（`role = 'admin'`）方可调用，普通用户请求返回 `403`。

### 13.1 获取用户列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取用户列表（管理员） |
| **请求方式** | `GET` |
| **接口路径** | `/admin/users` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | String | 否 | — | 按用户名/邮箱模糊搜索 |
| `role` | String | 否 | — | 按角色筛选：`admin` / `member` |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 20 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 56,
    "totalPages": 3,
    "records": [
      {
        "userId": 1,
        "username": "zhangsan",
        "email": "zhangsan@example.com",
        "avatar": "https://cdn.example.com/avatars/default.png",
        "role": "member",
        "status": "active",
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

> **说明**：`status` 字段用于标记用户状态（`active` = 正常，`disabled` = 已禁用）。若 `users` 表中无此字段，可在建表时单独增加或通过 `role` 区分。建议在 `users` 表增加 `status ENUM('active','disabled') DEFAULT 'active'`。

---

### 13.2 禁用/启���用户

| 属性 | 内容 |
|------|------|
| **接口名称** | 禁用/启用用户 |
| **请求方式** | `PUT` |
| **接口路径** | `/admin/users/{user-id}` |
| **是否鉴权** | 是 |

**权限**：管理员

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user-id` | Long | 是 | 目标用户 ID |

**Body 请求参数**：

```json
{
  "status": "disabled"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | String | 是 | 用户状态：`active`（启用）/ `disabled`（禁用） |

**返回示例**：

```json
{
  "code": 200,
  "msg": "用户状态已更新",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `403` | 非管理员，无权操作 |
| `404` | 用户不存在 |

---

### 13.3 获取团队管理列表

| 属性 | 内容 |
|------|------|
| **接口名称** | 获取所有团队列表（管理员） |
| **请求方式** | `GET` |
| **接口路径** | `/admin/teams` |
| **是否鉴权** | 是 |

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | String | 否 | — | 按团队名称模糊搜索 |
| `status` | String | 否 | — | 按状态筛选 |
| `pageNum` | Integer | 否 | 1 | 当前页码 |
| `pageSize` | Integer | 否 | 20 | 每页条数 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 12,
    "totalPages": 1,
    "records": [
      {
        "teamId": 1,
        "name": "后端开发小组",
        "status": "recruiting",
        "currentMembers": 5,
        "maxMembers": 10,
        "creatorName": "zhangsan",
        "projectCount": 2,
        "createdAt": "2026-01-15 10:30:00"
      }
    ]
  }
}
```

---

### 13.4 强制解散团队

| 属性 | 内容 |
|------|------|
| **接口名称** | 强制解散团队（管理员） |
| **请求方式** | `DELETE` |
| **接口路径** | `/admin/teams/{team-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `team-id` | Long | 是 | 团队 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "团队已被强制解散",
  "data": null
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `404` | 团队不存在 |

---

### 13.5 添加技能标签

| 属性 | 内容 |
|------|------|
| **接口名称** | 添加技能标签（管理员） |
| **请求方式** | `POST` |
| **接口路径** | `/admin/skills` |
| **是否鉴权** | 是 |

**Body 请求参数**：

```json
{
  "skillName": "Rust",
  "category": "后端"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `skillName` | String | 是 | 技能名称，全局唯一 |
| `category` | String | 是 | 所属分类 |

**返回示例**：

```json
{
  "code": 200,
  "msg": "技能标签添加成功",
  "data": {
    "skillId": 25,
    "skillName": "Rust",
    "category": "后端"
  }
}
```

**错误码说明**：

| code | 说明 |
|------|------|
| `50002` | 技能标签已存在 |

---

### 13.6 删除技能标签

| 属性 | 内容 |
|------|------|
| **接口名称** | 删除技能标签（管理员） |
| **请求方式** | `DELETE` |
| **接口路径** | `/admin/skills/{skill-id}` |
| **是否鉴权** | 是 |

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `skill-id` | Long | 是 | 技能 ID |

**返回示例**：

```json
{
  "code": 200,
  "msg": "技能标签已删除",
  "data": null
}
```

---

## 附录 A：接口汇总表

| 序号 | 模块 | 接口名称 | 方法 | 路径 | 鉴权 |
|------|------|----------|------|------|------|
| 1 | 认证 | 用户注册 | POST | `/auth/register` | 否 |
| 2 | 认证 | 用户登录 | POST | `/auth/login` | 否 |
| 3 | 用户 | 获取当前用户信息 | GET | `/users/me` | 是 |
| 4 | 用户 | 更新个人资料 | PUT | `/users/me` | 是 |
| 5 | 用户 | 修改密码 | PUT | `/users/me/password` | 是 |
| 6 | 用户 | 获取我的团队列表 | GET | `/users/me/teams` | 是 |
| 7 | 用户 | 获取我的任务 | GET | `/users/me/tasks` | 是 |
| 8 | 用户 | 获取我的入队申请记录 | GET | `/users/me/applications` | 是 |
| 9 | 用户 | 添加个人技能 | POST | `/users/me/skills` | 是 |
| 10 | 用户 | 删除个人技能 | DELETE | `/users/me/skills/{skill-id}` | 是 |
| 11 | 技能库 | 获取技能列表 | GET | `/skills` | 否 |
| 12 | 技能库 | 获取技能分类列表 | GET | `/skills/categories` | 否 |
| 13 | 团队 | 获取招募墙团队列表 | GET | `/teams` | 否 |
| 14 | 团队 | 创建团队 | POST | `/teams` | 是 |
| 15 | 团队 | 获取团队详情 | GET | `/teams/{team-id}` | 否 |
| 16 | 团队 | 编辑团队信息 | PUT | `/teams/{team-id}` | 是 |
| 17 | 团队 | 解散团队 | DELETE | `/teams/{team-id}` | 是 |
| 18 | 团队 | 获取团队成员列表 | GET | `/teams/{team-id}/members` | 否 |
| 19 | 团队 | 移除成员 | DELETE | `/teams/{team-id}/members/{user-id}` | 是 |
| 20 | 入队申请 | 提交入队申请 | POST | `/teams/{team-id}/applications` | 是 |
| 21 | 入队申请 | 获取申请列表 | GET | `/teams/{team-id}/applications` | 是 |
| 22 | 入队申请 | 审核申请 | PUT | `/teams/{team-id}/applications/{request-id}` | 是 |
| 23 | 项目 | 获取团队项目列表 | GET | `/teams/{team-id}/projects` | 是 |
| 24 | 项目 | 创建项目 | POST | `/teams/{team-id}/projects` | 是 |
| 25 | 项目 | 获取项目详情 | GET | `/projects/{project-id}` | 是 |
| 26 | 项目 | 编辑项目 | PUT | `/projects/{project-id}` | 是 |
| 27 | 项目 | 删除项目 | DELETE | `/projects/{project-id}` | 是 |
| 28 | 阶段 | 获取阶段列表 | GET | `/projects/{project-id}/stages` | 是 |
| 29 | 阶段 | 创建阶段 | POST | `/projects/{project-id}/stages` | 是 |
| 30 | 阶段 | 编辑阶段 | PUT | `/stages/{stage-id}` | 是 |
| 31 | 阶段 | 删除阶段 | DELETE | `/stages/{stage-id}` | 是 |
| 32 | 阶段 | 更新阶段状态 | PUT | `/stages/{stage-id}/status` | 是 |
| 33 | 阶段 | 获取阶段模板列表 | GET | `/stage-templates` | 是 |
| 34 | 阶段 | 使用模板批量创建阶段 | POST | `/projects/{project-id}/stages/template` | 是 |
| 35 | 任务列表 | 获取看板列表 | GET | `/projects/{project-id}/lists` | 是 |
| 36 | 任务列表 | 创建自定义列表 | POST | `/projects/{project-id}/lists` | 是 |
| 37 | 任务列表 | 编辑列表 | PUT | `/task-lists/{list-id}` | 是 |
| 38 | 任务列表 | 删除列表 | DELETE | `/task-lists/{list-id}` | 是 |
| 39 | 任务列表 | 拖拽重排列表 | PUT | `/task-lists/reorder` | 是 |
| 40 | 任务 | 获取列表下的任务 | GET | `/task-lists/{list-id}/tasks` | 是 |
| 41 | 任务 | 创建任务 | POST | `/task-lists/{list-id}/tasks` | 是 |
| 42 | 任务 | 获取任务详情 | GET | `/tasks/{task-id}` | 是 |
| 43 | 任务 | 编辑任务 | PUT | `/tasks/{task-id}` | 是 |
| 44 | 任务 | 删除任务 | DELETE | `/tasks/{task-id}` | 是 |
| 45 | 任务 | 移动任务 | PUT | `/tasks/{task-id}/move` | 是 |
| 46 | 任务 | 任务拖拽重排 | PUT | `/tasks/reorder` | 是 |
| 47 | 统计 | 团队进度统计 | GET | `/teams/{team-id}/stats` | 是 |
| 48 | 统计 | 个人贡献度统计 | GET | `/teams/{team-id}/members/{user-id}/stats` | 是 |
| 49 | 通知 | 获取通知列表 | GET | `/notifications` | 是 |
| 50 | 通知 | 获取未读通知数 | GET | `/notifications/unread-count` | 是 |
| 51 | 通知 | 标记单条已读 | PUT | `/notifications/{notification-id}/read` | 是 |
| 52 | 通知 | 全部标记已读 | PUT | `/notifications/read-all` | 是 |
| 53 | 管理员 | 获取用户列表 | GET | `/admin/users` | 是 |
| 54 | 管理员 | 禁用/启用用户 | PUT | `/admin/users/{user-id}` | 是 |
| 55 | 管理员 | 获取所有团队 | GET | `/admin/teams` | 是 |
| 56 | 管理员 | 强制解散团队 | DELETE | `/admin/teams/{team-id}` | 是 |
| 57 | 管理员 | 添加技能标签 | POST | `/admin/skills` | 是 |
| 58 | 管理员 | 删除技能标签 | DELETE | `/admin/skills/{skill-id}` | 是 |

---

> 📅 最后更新：2026-06-22
>
> 📝 版本：v1.0
