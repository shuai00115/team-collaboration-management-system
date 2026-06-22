-- =====================================================
-- 团队项目协作管理系统 —— 测试数据脚本
-- 所有用户密码: 123456
-- =====================================================

USE team_collab;

-- =====================================================
-- 1. 用户 (6人)
-- =====================================================
-- 更新已有 test01 的密码为统一密码
UPDATE users SET password_hash = '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', role = 'member' WHERE user_id = 1;

INSERT INTO users (user_id, username, password_hash, email, bio, role, status, created_at) VALUES
(2, 'zhangsan', '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', 'zhangsan@team.com', '全栈开发者，5年后端经验', 'member', 'active', '2026-01-15 10:30:00'),
(3, 'lisi',     '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', 'lisi@team.com',     '前端架构师，专注Vue生态', 'member', 'active', '2026-01-20 14:00:00'),
(4, 'wangwu',   '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', 'wangwu@team.com',   'Java后端开发，熟悉Spring Boot', 'member', 'active', '2026-02-01 09:00:00'),
(5, 'zhaoliu',  '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', 'zhaoliu@team.com',  '初级开发者，渴望学习成长', 'member', 'active', '2026-02-15 16:00:00'),
(6, 'admin',    '$2b$10$Q31If0juOfr.W/EG.DmGD.BTP6F.1JNw08JNV7vzVC6/ifDvR.Gmi', 'admin@team.com',    '系统管理员', 'admin', 'active', '2026-01-01 08:00:00');

-- =====================================================
-- 2. 技能标签 (12个)
-- =====================================================
INSERT INTO skills (skill_id, skill_name, category) VALUES
(1,  'Java',         '后端'),
(2,  'Python',       '后端'),
(3,  'Spring Boot',  '后端'),
(4,  'Vue',          '前端'),
(5,  'React',        '前端'),
(6,  'TypeScript',   '前端'),
(7,  'MySQL',        '数据库'),
(8,  'Redis',        '数据库'),
(9,  'Docker',       'DevOps'),
(10, 'UI Design',    '设计'),
(11, 'Flutter',      '移动端'),
(12, '项目管理',     '其他');

-- =====================================================
-- 3. 用户技能
-- =====================================================
INSERT INTO user_skills (user_id, skill_id, level) VALUES
(1, 1,  'advanced'),     -- test01: Java精通
(1, 7,  'intermediate'), -- test01: MySQL掌握
(1, 3,  'advanced'),     -- test01: SpringBoot精通
(2, 1,  'advanced'),     -- zhangsan
(2, 3,  'advanced'),
(2, 9,  'intermediate'),
(3, 4,  'advanced'),     -- lisi
(3, 5,  'intermediate'),
(3, 6,  'advanced'),
(4, 1,  'intermediate'), -- wangwu
(4, 3,  'intermediate'),
(4, 7,  'advanced'),
(5, 2,  'beginner'),     -- zhaoliu
(5, 4,  'beginner');

-- =====================================================
-- 4. 团队 (3个)
-- =====================================================
INSERT INTO teams (team_id, name, description, max_members, status, creator_id, created_at) VALUES
(1, '后端开发小组', '专注于 Spring Boot 微服务开发，构建高可用后端系统', 6, 'recruiting', 2, '2026-03-01 09:00:00'),
(2, '前端开发小组', 'Vue3 + TypeScript 组件库与中后台项目开发',        4, 'recruiting', 3, '2026-03-15 14:00:00'),
(3, '数据分析小分队', '使用 Python 与 SQL 进行数据挖掘与报表分析',      5, 'closed',     1, '2026-02-10 08:00:00');

-- =====================================================
-- 5. 团队所需技能
-- =====================================================
INSERT INTO team_required_skills (team_id, skill_id) VALUES
(1, 1), (1, 3), (1, 7),   -- 后端组: Java, SpringBoot, MySQL
(2, 4), (2, 6),             -- 前端组: Vue, TypeScript
(3, 2), (3, 7);             -- 数据组: Python, MySQL

-- =====================================================
-- 6. 团队成员 (手动插入，因为无触发器)
-- =====================================================
INSERT INTO team_members (team_id, user_id, role, joined_at) VALUES
-- 后端开发小组 (3人)
(1, 2, 'leader', '2026-03-01 09:00:00'),
(1, 4, 'member', '2026-03-05 10:00:00'),
(1, 1, 'member', '2026-03-10 14:00:00'),
-- 前端开发小组 (2人)
(2, 3, 'leader', '2026-03-15 14:00:00'),
(2, 1, 'member', '2026-03-20 10:00:00'),
-- 数据分析小分队 (4人, 满员)
(3, 1, 'leader', '2026-02-10 08:00:00'),
(3, 2, 'member', '2026-02-12 10:00:00'),
(3, 3, 'member', '2026-02-12 14:00:00'),
(3, 4, 'member', '2026-02-15 09:00:00');

-- =====================================================
-- 7. 入队申请 (2条)
-- =====================================================
INSERT INTO join_requests (request_id, team_id, user_id, message, status, created_at) VALUES
(1, 1, 5, '我有Python基础，正在学习Java，希望能加入后端组一起学习进步！', 'pending',  '2026-06-20 08:00:00'),
(2, 2, 5, '我对前端开发很感兴趣，自学过HTML/CSS/JS基础',              'rejected', '2026-06-18 10:00:00');

-- =====================================================
-- 8. 项目 (2个)
-- =====================================================
INSERT INTO projects (project_id, team_id, name, description, created_at) VALUES
(1, 3, '团队协作管理系统', '数据库课程设计项目——开发一个完整的团队协作管理平台，覆盖团队组建、多阶段规划、看板任务追踪和进度统计', '2026-03-01 09:00:00'),
(2, 1, 'API网关服务',     '为微服务架构搭建统一的API网关，实现路由转发、限流、鉴权等功能',                                '2026-04-15 10:00:00');

-- =====================================================
-- 9. 阶段 (项目1有5个阶段, 项目2有3个阶段)
-- =====================================================
INSERT INTO stages (stage_id, project_id, name, description, start_date, end_date, order_index, status) VALUES
-- 项目1: 团队协作管理系统 (瀑布模型)
(1, 1, '需求分析', '收集与分析项目需求，产出需求规格说明书',       '2026-03-01', '2026-03-15', 0, 'completed'),
(2, 1, '系统设计', '数据库设计与RESTful API接口设计',              '2026-03-16', '2026-04-15', 1, 'completed'),
(3, 1, '编码开发', '核心功能前后端实现',                           '2026-04-16', '2026-06-30', 2, 'in_progress'),
(4, 1, '测试验收', '功能测试、集成测试与用户验收',                  '2026-07-01', '2026-07-15', 3, 'not_started'),
(5, 1, '部署上线', '部署到生产环境并编写运维文档',                  '2026-07-16', '2026-07-31', 4, 'not_started'),
-- 项目2: API网关服务
(6, 2, '技术调研', '调研主流API网关方案',                          '2026-04-15', '2026-04-30', 0, 'completed'),
(7, 2, '核心开发', '网关核心功能开发',                              '2026-05-01', '2026-06-15', 1, 'in_progress'),
(8, 2, '性能测试', '压力测试与性能优化',                            '2026-06-16', '2026-07-01', 2, 'not_started');

-- =====================================================
-- 10. 阶段模板 (2个)
-- =====================================================
INSERT INTO stage_templates (template_id, template_name, stages_json) VALUES
(1, '标准瀑布模型', '[
  {"name":"需求分析","description":"收集与分析项目需求","orderIndex":0},
  {"name":"系统设计","description":"数据库设计与接口设计","orderIndex":1},
  {"name":"编码开发","description":"核心功能实现","orderIndex":2},
  {"name":"测试验收","description":"功能测试与验收通过","orderIndex":3},
  {"name":"部署上线","description":"部署到生产环境","orderIndex":4}
]'),
(2, '课程设计', '[
  {"name":"开题报告","description":"确定选题与方案","orderIndex":0},
  {"name":"中期检查","description":"检查中期进度","orderIndex":1},
  {"name":"成果开发","description":"完成主要开发工作","orderIndex":2},
  {"name":"答辩准备","description":"准备答辩材料与演示","orderIndex":3}
]');

-- =====================================================
-- 11. 任务列表 (看板列 — 项目1: 默认3列+1自定义, 项目2: 默认3列)
-- =====================================================
INSERT INTO task_lists (list_id, project_id, name, position, is_default) VALUES
-- 项目1
(1, 1, '待办',   0, 1),
(2, 1, '进行中', 1, 1),
(3, 1, '已完成', 2, 1),
(4, 1, '代码审查中', 3, 0),
-- 项目2
(5, 2, '待办',   0, 1),
(6, 2, '进行中', 1, 1),
(7, 2, '已完成', 2, 1);

-- =====================================================
-- 12. 任务 (17个)
-- =====================================================
INSERT INTO tasks (task_id, list_id, stage_id, title, description, priority, due_date, assignee_id, creator_id, position, created_at) VALUES
-- ===== 项目1 - 待办 (list_id=1) =====
(1,  1, 3, '实现用户注册接口',     '完成 POST /auth/register 的控制器、服务和数据校验',                        'high',   '2026-07-05 18:00:00', 1, 1, 0, '2026-06-15 10:00:00'),
(2,  1, 3, '集成JWT鉴权中间件',    '实现 Spring Security + JWT 过滤器，支持免登录路径白名单',                  'high',   '2026-07-08 18:00:00', 4, 1, 1, '2026-06-16 09:00:00'),
(3,  1, 3, '编写Service层单元测试', '为 UserService、TeamService 编写 JUnit 测试，目标覆盖率80%',                'medium', '2026-07-12 18:00:00', NULL, 1, 2, '2026-06-18 14:00:00'),

-- ===== 项目1 - 进行中 (list_id=2) =====
(4,  2, 3, '完成任务看板拖拽功能',   '使用原生 drag & drop 实现看板内任务拖拽排序，对接 PUT /tasks/reorder',      'high',   '2026-06-25 18:00:00', 3, 1, 0, '2026-05-10 10:00:00'),
(5,  2, 3, '入队申请审核流程',      '包含 SELECT...FOR UPDATE 行级锁防超募、事务管理、审核后通知触发',           'high',   '2026-06-28 18:00:00', 1, 1, 1, '2026-05-12 09:00:00'),
(6,  2, 3, '实现团队创建功能',      '包括团队信息录入、技能标签关联、默认角色设定，创建者自动成为队长',           'high',   '2026-06-20 18:00:00', 4, 1, 2, '2026-05-15 14:00:00'),
(7,  2, 3, '前端对接阶段进度展示',   '在项目详情页展示阶段卡片，含进度条、超期标记、状态下拉切换',                 'medium', '2026-07-02 18:00:00', 2, 1, 3, '2026-05-20 11:00:00'),
(8,  2, 3, '通知系统前后端联调',    'WebSocket实时推送 + HTTP轮询降级策略',                                      'medium', '2026-07-05 18:00:00', NULL, 1, 4, '2026-05-25 16:00:00'),

-- ===== 项目1 - 代码审查中 (list_id=4) =====
(9,  4, 3, '审查用户模块代码',      '检查SQL注入防护、参数校验完整性、异常处理覆盖',                              'medium', '2026-06-22 18:00:00', 2, 1, 0, '2026-06-01 10:00:00'),
(10, 4, 3, '审查API接口规范符合度',  '逐一核对58个接口的请求/响应格式是否严格符合 RESTful-API接口文档.md',        'low',    '2026-06-24 18:00:00', 3, 1, 1, '2026-06-05 14:00:00'),

-- ===== 项目1 - 已完成 (list_id=3) =====
(11, 3, 1, '项目需求访谈',          '与老师沟通确认核心功能范围',                                                 'high',   '2026-03-10 18:00:00', 1, 1, 0, '2026-03-01 09:00:00'),
(12, 3, 1, '编写需求规格说明书',    'SRS文档编写与团队内部评审',                                                  'high',   '2026-03-15 18:00:00', 2, 1, 1, '2026-03-05 10:00:00'),
(13, 3, 1, '确定技术选型',          'Spring Boot vs Express vs Flask 评估对比',                                   'medium', '2026-03-12 18:00:00', 1, 1, 2, '2026-03-03 14:00:00'),
(14, 3, 2, '数据库ER图设计',       '绘制完整实体关系图，标注所有外键、索引和约束',                                'high',   '2026-03-25 18:00:00', 1, 1, 3, '2026-03-16 09:00:00'),
(15, 3, 2, '编写DDL建表脚本',      '包含13张表的完整建表SQL，含索引、约束、触发器',                               'high',   '2026-03-30 18:00:00', 4, 1, 4, '2026-03-20 10:00:00'),
(16, 3, 2, 'API接口文档编写',      '58个接口的完整规范（请求/响应示例、错误码、参数说明）',                        'high',   '2026-04-10 18:00:00', 1, 1, 5, '2026-03-25 14:00:00'),
(17, 3, 2, '项目框架文档编写',     '功能清单 + E-R图 + 触发器 + 索引设计 + 并发控制方案',                         'medium', '2026-04-05 18:00:00', 3, 1, 6, '2026-03-28 11:00:00');

-- =====================================================
-- 13. 通知 (8条，接收人均为test01)
-- =====================================================
INSERT INTO notifications (notification_id, user_id, type, title, content, related_type, related_id, is_read, created_at) VALUES
(1, 1, 'task_assigned',  '您被分配了新的任务',        '任务 "实现用户注册接口" 已分配给您，截止日期 2026-07-05',								'task',   1,  0, '2026-06-15 10:05:00'),
(2, 1, 'task_assigned',  '您被分配了新的任务',        '任务 "入队申请审核流程" 已分配给您，截止日期 2026-06-28',								'task',   5,  0, '2026-05-12 09:05:00'),
(3, 1, 'due_reminder',   '任务截止提醒',              '任务 "完成任务看板拖拽功能" 即将于2026-06-25到期，请尽快处理',							'task',   4,  0, '2026-06-22 08:00:00'),
(4, 1, 'stage_overdue',  '阶段超期提醒',              '项目 "团队协作管理系统" 的阶段 "系统设计" 已超过截止日期，请检查进度',					'project', 1, 1, '2026-04-16 08:00:00'),
(5, 1, 'join_approved',  '入队申请已通过',            '您加入团队 "前端开发小组" 的申请已通过！',												'team',   2,  1, '2026-03-20 10:00:00'),
(6, 1, 'new_join_request','收到新的入队申请',          '用户 zhaoliu 申请加入团队 "后端开发小组"，请前往审核',									'join_request', 1, 1, '2026-06-20 08:05:00'),
(7, 1, 'task_assigned',  '您被分配了新的任务',        '任务 "编写DDL建表脚本" 已分配给您，截止日期 2026-03-30',								'task',   15, 1, '2026-03-20 10:05:00'),
(8, 1, 'member_removed', '您已被移出团队',            '您已被移出团队 "XXX兴趣小组"',														'team',   NULL, 1, '2026-04-01 12:00:00');

-- =====================================================
-- 验证
-- =====================================================
SELECT '=== 测试数据统计 ===' AS '';
SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
UNION ALL SELECT 'skills', COUNT(*) FROM skills
UNION ALL SELECT 'user_skills', COUNT(*) FROM user_skills
UNION ALL SELECT 'teams', COUNT(*) FROM teams
UNION ALL SELECT 'team_members', COUNT(*) FROM team_members
UNION ALL SELECT 'join_requests', COUNT(*) FROM join_requests
UNION ALL SELECT 'projects', COUNT(*) FROM projects
UNION ALL SELECT 'stages', COUNT(*) FROM stages
UNION ALL SELECT 'stage_templates', COUNT(*) FROM stage_templates
UNION ALL SELECT 'task_lists', COUNT(*) FROM task_lists
UNION ALL SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications;
