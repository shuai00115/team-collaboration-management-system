-- =====================================================
-- 团队项目协作管理系统 —— 数据库建表脚本
-- 数据库: team_collab
-- MySQL 8.0
-- =====================================================

USE team_collab;

-- =====================================================
-- 1. 用户表 users
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    email         VARCHAR(100)  NOT NULL UNIQUE,
    avatar        VARCHAR(500)  DEFAULT NULL      COMMENT '头像URL',
    bio           VARCHAR(500)  DEFAULT NULL      COMMENT '个人简介',
    role          ENUM('admin','member') NOT NULL DEFAULT 'member',
    status        ENUM('active','disabled') NOT NULL DEFAULT 'active' COMMENT '账户状态',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 技能表 skills
-- =====================================================
CREATE TABLE IF NOT EXISTS skills (
    skill_id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    skill_name  VARCHAR(50)  NOT NULL UNIQUE,
    category    VARCHAR(50)  NOT NULL        COMMENT '技能分类: 前端/后端/数据库/...',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_skills_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统技能标签库';

-- =====================================================
-- 3. 用户技能关联表 user_skills
-- =====================================================
CREATE TABLE IF NOT EXISTS user_skills (
    user_id    BIGINT UNSIGNED NOT NULL,
    skill_id   BIGINT UNSIGNED NOT NULL,
    level      ENUM('beginner','intermediate','advanced') NOT NULL DEFAULT 'beginner'
               COMMENT '熟练度: beginner=初学, intermediate=掌握, advanced=精通',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, skill_id),
    FOREIGN KEY (user_id)  REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-技能关联表';

-- =====================================================
-- 4. 团队表 teams
-- =====================================================
CREATE TABLE IF NOT EXISTS teams (
    team_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT         DEFAULT NULL,
    max_members INT          NOT NULL DEFAULT 10     COMMENT '计划人数上限',
    status      ENUM('recruiting','closed') NOT NULL DEFAULT 'recruiting'
                COMMENT '招募状态: recruiting=招募中, closed=关闭招募',
    creator_id  BIGINT UNSIGNED NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_teams_status (status),
    INDEX idx_teams_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- =====================================================
-- 5. 团队所需技能表 team_required_skills
-- =====================================================
CREATE TABLE IF NOT EXISTS team_required_skills (
    team_id  BIGINT UNSIGNED NOT NULL,
    skill_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (team_id, skill_id),
    FOREIGN KEY (team_id)  REFERENCES teams(team_id)  ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队所需技能关联表';

-- =====================================================
-- 6. 团队成员表 team_members
-- =====================================================
CREATE TABLE IF NOT EXISTS team_members (
    team_id   BIGINT UNSIGNED NOT NULL,
    user_id   BIGINT UNSIGNED NOT NULL,
    role      ENUM('leader','member') NOT NULL DEFAULT 'member'
              COMMENT '队内角色: leader=队长, member=普通成员',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, user_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- =====================================================
-- 7. 入队申请表 join_requests
-- =====================================================
CREATE TABLE IF NOT EXISTS join_requests (
    request_id  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    team_id     BIGINT UNSIGNED NOT NULL,
    user_id     BIGINT UNSIGNED NOT NULL,
    message     VARCHAR(500) DEFAULT NULL          COMMENT '申请留言',
    status      ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending'
                COMMENT '审核状态: pending=待审核, approved=已通过, rejected=已拒绝',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME DEFAULT NULL              COMMENT '审核时间',
    UNIQUE KEY uk_request (user_id, team_id),
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_join_requests_team_status (team_id, status),
    INDEX idx_join_requests_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入队申请表';

-- =====================================================
-- 8. 项目表 projects
-- =====================================================
CREATE TABLE IF NOT EXISTS projects (
    project_id  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    team_id     BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT         DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    INDEX idx_projects_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- =====================================================
-- 9. 阶段表 stages
-- =====================================================
CREATE TABLE IF NOT EXISTS stages (
    stage_id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT         DEFAULT NULL,
    start_date  DATE         DEFAULT NULL,
    end_date    DATE         DEFAULT NULL,
    order_index INT          NOT NULL DEFAULT 0     COMMENT '阶段排序序号',
    status      ENUM('not_started','in_progress','completed') NOT NULL DEFAULT 'not_started'
                COMMENT '阶段状态',
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    INDEX idx_stages_project_order (project_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';

-- =====================================================
-- 10. 阶段模板表 stage_templates
-- =====================================================
CREATE TABLE IF NOT EXISTS stage_templates (
    template_id   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    stages_json   JSON         NOT NULL              COMMENT '阶段结构JSON',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阶段模板表';

-- =====================================================
-- 11. 任务列表表 task_lists
-- =====================================================
CREATE TABLE IF NOT EXISTS task_lists (
    list_id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT UNSIGNED NOT NULL,
    name       VARCHAR(100) NOT NULL,
    position   INT          NOT NULL DEFAULT 0      COMMENT '列表排序序号',
    is_default TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '是否默认列表 (不可删除)',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    INDEX idx_task_lists_project (project_id, position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务列表(看板列)表';

-- =====================================================
-- 12. 任务表 tasks
-- =====================================================
CREATE TABLE IF NOT EXISTS tasks (
    task_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    list_id     BIGINT UNSIGNED NOT NULL,
    stage_id    BIGINT UNSIGNED DEFAULT NULL        COMMENT '所属阶段 (可为空)',
    title       VARCHAR(200) NOT NULL,
    description TEXT         DEFAULT NULL,
    priority    ENUM('high','medium','low') NOT NULL DEFAULT 'medium'
                COMMENT '优先级: high=高, medium=中, low=低',
    due_date    DATETIME     DEFAULT NULL            COMMENT '截止日期',
    assignee_id BIGINT UNSIGNED DEFAULT NULL         COMMENT '执行人',
    creator_id  BIGINT UNSIGNED NOT NULL             COMMENT '创建人',
    position    INT          NOT NULL DEFAULT 0      COMMENT '在看板列表内排序',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (list_id)     REFERENCES task_lists(list_id) ON DELETE CASCADE,
    FOREIGN KEY (stage_id)    REFERENCES stages(stage_id)    ON DELETE SET NULL,
    FOREIGN KEY (assignee_id) REFERENCES users(user_id)      ON DELETE SET NULL,
    FOREIGN KEY (creator_id)  REFERENCES users(user_id)      ON DELETE CASCADE,
    INDEX idx_tasks_list_position (list_id, position),
    INDEX idx_tasks_assignee (assignee_id),
    INDEX idx_tasks_stage (stage_id),
    INDEX idx_tasks_due_date (due_date),
    INDEX idx_tasks_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务卡片表';

-- =====================================================
-- 13. 通知表 notifications
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    type            VARCHAR(50)  NOT NULL            COMMENT '通知类型: join_approved, join_rejected, new_join_request, task_assigned, task_updated, due_reminder, stage_overdue, member_removed',
    title           VARCHAR(200) NOT NULL,
    content         TEXT         DEFAULT NULL,
    related_type    VARCHAR(50)  DEFAULT NULL        COMMENT '关联实体类型: team, project, task, join_request',
    related_id      BIGINT UNSIGNED DEFAULT NULL     COMMENT '关联实体ID',
    is_read         TINYINT(1)   NOT NULL DEFAULT 0  COMMENT '是否已读: 0=未读, 1=已读',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notifications_user_read (user_id, is_read, created_at),
    INDEX idx_notifications_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';
