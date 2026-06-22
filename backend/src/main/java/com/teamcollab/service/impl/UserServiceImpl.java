package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.common.util.BcryptUtils;
import com.teamcollab.dto.user.*;
import com.teamcollab.entity.User;
import com.teamcollab.entity.UserSkill;
import com.teamcollab.mapper.*;
import com.teamcollab.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * <p>
 * 提供当前用户的个人信息管理、团队/任务/申请查询、技能管理等功能。
 * 所有操作均基于当前登录用户身份进行，确保数据隔离和安全性。
 * </p>
 *
 * <p><b>功能模块：</b></p>
 * <ul>
 *     <li>个人信息：获取详情、修改资料、修改密码</li>
 *     <li>我的团队：查看已加入的团队列表</li>
 *     <li>我的任务：查看分配给我的任务（支持多条件筛选）</li>
 *     <li>我的申请：查看入队申请历史记录</li>
 *     <li>技能管理：添加/删除个人技能标签</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 用户 Mapper，基础用户 CRUD 操作 */
    private final UserMapper userMapper;

    /** 用户技能关联 Mapper */
    private final UserSkillMapper userSkillMapper;

    /** 团队 Mapper，查询用户加入的团队 */
    private final TeamMapper teamMapper;

    /** 任务 Mapper，查询用户的任务列表 */
    private final TaskMapper taskMapper;

    /** 入队申请 Mapper，查询用户的申请记录 */
    private final JoinRequestMapper joinRequestMapper;

    /** 技能 Mapper，校验技能是否存在 */
    private final SkillMapper skillMapper;

    // ==================== 辅助方法 ====================

    /**
     * 获取当前登录用户的完整实体
     * <p>
     * 从 Spring Security 安全上下文中获取当前认证用户 ID，
     * 然后根据 ID 查询数据库返回 User 实体。
     * </p>
     *
     * @return 当前登录的 User 实体
     * @throws BusinessException 当用户未登录或用户不存在时
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = Long.parseLong(auth.getPrincipal().toString());
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    // ==================== 个人信息管理 ====================

    /**
     * 获取当前用户的完整信息
     * <p>
     * 查询用户的基本信息（用户名、邮箱、头像、简介、角色），
     * 以及用户的技能标签列表（含技能名称、分类、掌握等级），
     * 组装为完整的 UserInfoResponse 返回。
     * </p>
     *
     * @return 当前用户的详细信息响应，包含技能列表
     */
    @Override
    public UserInfoResponse getCurrentUserInfo() {
        // 1. 获取当前登录用户实体
        User user = getCurrentUser();

        // 2. 构建基本信息响应
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        // 3. 查询用户的技能列表（通过 UserSkillMapper 联表查询技能详情）
        List<Map<String, Object>> skillRows = userSkillMapper.selectUserSkills(user.getUserId());
        List<UserInfoResponse.UserSkillVO> skillVOList = skillRows.stream().map(row -> {
            UserInfoResponse.UserSkillVO vo = new UserInfoResponse.UserSkillVO();
            vo.setSkillId(MapUtils.getLong(row, "skill_id"));
            vo.setSkillName((String) row.get("skill_name"));
            vo.setCategory((String) row.get("category"));
            vo.setLevel((String) row.get("level"));
            return vo;
        }).collect(Collectors.toList());
        response.setSkills(skillVOList);

        log.info("获取用户信息成功: userId={}", user.getUserId());
        return response;
    }

    /**
     * 更新用户个人资料
     * <p>
     * 仅允许修改头像（avatar）和个人简介（bio）两个字段，
     * 用户名和邮箱等核心信息不允许通过此接口修改。
     * 使用 updateById 仅更新非 null 字段，因此可部分更新。
     * </p>
     *
     * @param userId  当前用户 ID
     * @param request 更新请求，包含 avatar 和 bio（均可为 null 表示不修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = new User();
        user.setUserId(userId);
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        this.updateById(user);
        log.info("用户资料更新成功: userId={}", userId);
    }

    /**
     * 修改登录密码
     * <p>
     * 业务流程：
     * <ol>
     *     <li>查询当前用户完整信息（含密码哈希值）</li>
     *     <li>使用 BCrypt 验证用户输入的旧密码是否正确</li>
     *     <li>旧密码验证通过后，对新密码进行 BCrypt 加密</li>
     *     <li>更新数据库中的密码哈希值</li>
     * </ol>
     * 注意：修改密码后不会使现有 JWT 令牌失效，如需立即生效可配合登出逻辑。
     * </p>
     *
     * @param userId  当前用户 ID
     * @param request 修改密码请求，包含 oldPassword 和 newPassword
     * @throws BusinessException 当旧密码验证失败时（ErrorCode.OLD_PASSWORD_ERROR）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 1. 查询用户完整信息（需要获取 passwordHash 字段）
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 2. 使用 BCrypt 验证旧密码是否正确
        if (!BcryptUtils.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("修改密码失败 - 旧密码错误: userId={}", userId);
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }

        // 3. 对新密码进行 BCrypt 加密并更新
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPasswordHash(BcryptUtils.encode(request.getNewPassword()));
        this.updateById(updateUser);

        log.info("密码修改成功: userId={}", userId);
    }

    // ==================== 我的团队 ====================

    /**
     * 获取当前用户加入的团队列表（分页）
     * <p>
     * 通过 TeamMapper.selectUserTeams 查询用户所在的所有团队，
     * 返回的每个团队包含我的角色、当前成员数、最大成员数等信息。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @return 分页的我的团队响应列表
     */
    @Override
    public Page<MyTeamResponse> getMyTeams(Long userId, int pageNum, int pageSize) {
        // 查询用户加入的所有团队
        List<Map<String, Object>> teamRows = teamMapper.selectUserTeams(userId);

        // 将 Map 结果转换为 MyTeamResponse DTO 列表
        List<MyTeamResponse> records = teamRows.stream().map(row -> {
            MyTeamResponse response = new MyTeamResponse();
            response.setTeamId(MapUtils.getLong(row, "team_id"));
            response.setTeamName((String) row.get("team_name"));
            response.setDescription((String) row.get("description"));
            response.setMyRole((String) row.get("role"));
            response.setStatus((String) row.get("status"));
            response.setCurrentMembers(((Number) row.get("current_members")).intValue());
            response.setMaxMembers(((Number) row.get("max_members")).intValue());
            if (row.get("joined_at") != null) {
                response.setJoinedAt((LocalDateTime) row.get("joined_at"));
            }
            return response;
        }).collect(Collectors.toList());

        // 构建分页结果
        Page<MyTeamResponse> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(records.size());
        resultPage.setRecords(records);

        log.info("查询用户团队成功: userId={}, 团队数={}", userId, records.size());
        return resultPage;
    }

    // ==================== 我的任务 ====================

    /**
     * 获取当前用户的任务列表（分页，支持多条件筛选）
     * <p>
     * 通过 TaskMapper.selectUserTasks 查询用户作为负责人或创建者的任务，
     * 支持按优先级（priority）、任务列表（listId）、团队（teamId）等条件过滤。
     * 返回的任务包含所属项目、团队、阶段等上下文信息。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param priority 优先级筛选（high/medium/low），可为 null
     * @param listId   任务列表 ID 筛选，可为 null
     * @param teamId   团队 ID 筛选，可为 null
     * @return 分页的我的任务响应列表
     */
    @Override
    public Page<MyTaskResponse> getMyTasks(Long userId, int pageNum, int pageSize,
                                            String priority, Long listId, Long teamId) {
        Page<com.teamcollab.entity.Task> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = taskMapper.selectUserTasks(
                queryPage, userId, priority, listId, teamId);

        // 转换为 DTO 列表
        List<MyTaskResponse> records = resultPage.getRecords().stream().map(row -> {
            MyTaskResponse response = new MyTaskResponse();
            response.setTaskId(MapUtils.getLong(row, "task_id"));
            response.setTitle((String) row.get("title"));
            response.setPriority((String) row.get("priority"));
            response.setListName((String) row.get("list_name"));
            response.setStageName((String) row.get("stage_name"));
            response.setProjectName((String) row.get("project_name"));
            response.setTeamName((String) row.get("team_name"));
            if (row.get("team_id") != null) {
                response.setTeamId(MapUtils.getLong(row, "team_id"));
            }
            if (row.get("due_date") != null) {
                response.setDueDate((LocalDateTime) row.get("due_date"));
            }
            if (row.get("created_at") != null) {
                response.setCreatedAt((LocalDateTime) row.get("created_at"));
            }
            return response;
        }).collect(Collectors.toList());

        // 构建分页结果
        Page<MyTaskResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询用户任务成功: userId={}, 过滤条件[priority={}, listId={}, teamId={}], 总数={}",
                userId, priority, listId, teamId, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 我的申请 ====================

    /**
     * 获取当前用户的入队申请记录（分页，支持按状态筛选）
     * <p>
     * 通过 JoinRequestMapper.selectUserApplications 查询用户的申请历史，
     * 可按申请状态（pending/approved/rejected）进行过滤。
     * 返回的每条申请包含目标团队名称、审核状态和时间。
     * </p>
     *
     * @param userId   当前用户 ID
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param status   申请状态筛选（pending/approved/rejected），可为 null 表示全部
     * @return 分页的我的申请响应列表
     */
    @Override
    public Page<MyApplicationResponse> getMyApplications(Long userId, int pageNum, int pageSize, String status) {
        Page<com.teamcollab.entity.JoinRequest> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = joinRequestMapper.selectUserApplications(
                queryPage, userId, status);

        // 转换为 DTO 列表
        List<MyApplicationResponse> records = resultPage.getRecords().stream().map(row -> {
            MyApplicationResponse response = new MyApplicationResponse();
            response.setRequestId(MapUtils.getLong(row, "request_id"));
            response.setTeamId(MapUtils.getLong(row, "team_id"));
            response.setTeamName((String) row.get("team_name"));
            response.setMessage((String) row.get("message"));
            response.setStatus((String) row.get("status"));
            if (row.get("created_at") != null) {
                response.setCreatedAt((LocalDateTime) row.get("created_at"));
            }
            if (row.get("reviewed_at") != null) {
                response.setReviewedAt((LocalDateTime) row.get("reviewed_at"));
            }
            return response;
        }).collect(Collectors.toList());

        // 构建分页结果
        Page<MyApplicationResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询用户申请成功: userId={}, status={}, 总数={}", userId, status, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 技能管理 ====================

    /**
     * 为用户添加技能标签
     * <p>
     * 业务流程：
     * <ol>
     *     <li>校验技能是否存在（通过 skillId 查询）</li>
     *     <li>校验用户是否已添加过该技能（防止重复添加）</li>
     *     <li>插入 user_skills 关联记录</li>
     * </ol>
     * 默认等级为 "beginner"（初级），可通过传入 level 参数指定等级。
     * </p>
     *
     * @param userId  当前用户 ID
     * @param skillId 要添加的技能 ID
     * @param level   技能掌握等级（beginner/intermediate/advanced）
     * @throws BusinessException 当技能不存在时（ErrorCode.SKILL_NOT_FOUND）
     * @throws BusinessException 当技能已添加时（ErrorCode.CONFLICT）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSkill(Long userId, Long skillId, String level) {
        // 1. 校验技能是否存在
        if (skillMapper.selectById(skillId) == null) {
            throw new BusinessException(ErrorCode.SKILL_NOT_FOUND);
        }

        // 2. 校验是否已添加过该技能（防止重复）
        if (userSkillMapper.countByUserAndSkill(userId, skillId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该技能已添加，不可重复添加");
        }

        // 3. 插入用户技能关联记录
        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        userSkill.setSkillId(skillId);
        userSkill.setLevel(level != null ? level : "beginner");  // 默认等级为初级
        userSkill.setCreatedAt(LocalDateTime.now());
        userSkillMapper.insert(userSkill);

        log.info("用户添加技能成功: userId={}, skillId={}, level={}", userId, skillId, level);
    }

    /**
     * 删除用户的技能标签
     * <p>
     * 从 user_skills 表中删除指定用户与技能之间的关联记录。
     * 不会删除技能库中的技能定义本身。
     * </p>
     *
     * @param userId  当前用户 ID
     * @param skillId 要删除的技能 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long userId, Long skillId) {
        userSkillMapper.deleteByUserAndSkill(userId, skillId);
        log.info("用户删除技能成功: userId={}, skillId={}", userId, skillId);
    }
}
