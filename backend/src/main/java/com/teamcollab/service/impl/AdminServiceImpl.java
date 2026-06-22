package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.common.exception.ForbiddenException;
import com.teamcollab.dto.admin.*;
import com.teamcollab.entity.Skill;
import com.teamcollab.entity.User;
import com.teamcollab.mapper.SkillMapper;
import com.teamcollab.mapper.TeamMapper;
import com.teamcollab.mapper.UserMapper;
import com.teamcollab.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 * <p>
 * 提供系统管理员专属的管理功能，包括用户管理、团队管理和技能标签管理。
 * 所有方法在执行前均需通过 checkIsAdmin() 进行管理员权限校验。
 * 这是一个纯业务管理服务，不继承 MyBatis-Plus 的 ServiceImpl。
 * </p>
 *
 * <p><b>管理员权限：</b></p>
 * 管理员（role = "admin"）拥有以下特殊权限：
 * <ul>
 *     <li>查看所有用户列表</li>
 *     <li>启用/禁用用户账号</li>
 *     <li>查看所有团队列表</li>
 *     <li>强制解散任何团队（无需队长确认）</li>
 *     <li>管理系统技能标签库（添加/删除技能）</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    /** 用户 Mapper */
    private final UserMapper userMapper;

    /** 团队 Mapper */
    private final TeamMapper teamMapper;

    /** 技能 Mapper */
    private final SkillMapper skillMapper;

    // ==================== 用户管理 ====================

    /**
     * 获取用户列表（管理员专用，分页）
     * <p>
     * 调用 userMapper.selectUserPage 分页查询所有用户，
     * 支持按关键词（匹配用户名或邮箱）和角色进行过滤。
     * </p>
     *
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param keyword  关键词模糊搜索，可为 null
     * @param role     按角色过滤，可为 null
     * @return 分页的管理员用户视图响应列表
     * @throws ForbiddenException 当当前用户不是管理员时
     */
    @Override
    public Page<AdminUserResponse> getUserList(int pageNum, int pageSize, String keyword, String role) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 分页查询用户
        Page<User> queryPage = new Page<>(pageNum, pageSize);
        Page<User> resultPage = userMapper.selectUserPage(queryPage, keyword, role);

        // 3. 转换为响应 DTO
        List<AdminUserResponse> records = resultPage.getRecords().stream().map(user -> {
            AdminUserResponse response = new AdminUserResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setAvatar(user.getAvatar());
            response.setRole(user.getRole());
            response.setStatus(user.getStatus());
            response.setCreatedAt(user.getCreatedAt());
            return response;
        }).collect(Collectors.toList());

        // 4. 构建分页响应
        Page<AdminUserResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("管理员查询用户列表: page={}/{}, keyword={}, role={}, 总数={}",
                pageNum, pageSize, keyword, role, resultPage.getTotal());
        return responsePage;
    }

    /**
     * 更新用户状态（管理员专用）
     * <p>
     * 管理员启用或禁用指定用户账号。状态仅允许 "active" 或 "disabled"。
     * 被禁用的用户将无法登录系统。
     * </p>
     *
     * @param userId  目标用户 ID
     * @param request 更新请求，包含目标状态（active/disabled）
     * @throws ForbiddenException 当当前用户不是管理员时
     * @throws BusinessException  当目标用户不存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 校验用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 3. 更新用户状态
        User update = new User();
        update.setUserId(userId);
        update.setStatus(request.getStatus());
        userMapper.updateById(update);

        log.info("管理员更新用户状态: userId={}, oldStatus={}, newStatus={}",
                userId, user.getStatus(), request.getStatus());
    }

    // ==================== 团队管理 ====================

    /**
     * 获取所有团队列表（管理员专用，分页）
     * <p>
     * 调用 teamMapper.selectAdminTeamPage 分页查询所有团队，
     * 支持按关键词（匹配团队名称）和状态进行过滤。
     * 返回的管理员视图包含项目数量等管理字段。
     * </p>
     *
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param keyword  关键词模糊搜索，可为 null
     * @param status   按团队状态过滤，可为 null
     * @return 分页的管理员团队视图响应列表
     * @throws ForbiddenException 当当前用户不是管理员时
     */
    @Override
    public Page<AdminTeamResponse> getTeamList(int pageNum, int pageSize, String keyword, String status) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 分页查询团队
        Page<com.teamcollab.entity.Team> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = teamMapper.selectAdminTeamPage(queryPage, keyword, status);

        // 3. 转换为响应 DTO
        List<AdminTeamResponse> records = resultPage.getRecords().stream().map(row -> {
            AdminTeamResponse response = new AdminTeamResponse();
            response.setTeamId(MapUtils.getLong(row, "team_id"));
            response.setName((String) row.get("name"));
            response.setStatus((String) row.get("status"));
            response.setCurrentMembers(row.get("current_members") != null
                    ? ((Number) row.get("current_members")).intValue() : 0);
            response.setMaxMembers(((Number) row.get("max_members")).intValue());
            response.setCreatorName((String) row.get("creator_name"));
            response.setProjectCount(row.get("project_count") != null
                    ? ((Number) row.get("project_count")).intValue() : 0);
            if (row.get("created_at") != null) {
                response.setCreatedAt((java.time.LocalDateTime) row.get("created_at"));
            }
            return response;
        }).collect(Collectors.toList());

        // 4. 构建分页响应
        Page<AdminTeamResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("管理员查询团队列表: page={}/{}, keyword={}, status={}, 总数={}",
                pageNum, pageSize, keyword, status, resultPage.getTotal());
        return responsePage;
    }

    /**
     * 强制解散团队（管理员专用，无需队长确认）
     * <p>
     * 管理员可强制解散任何团队，不需要团队名称确认。
     * 解散操作会级联删除团队成员、所需技能、项目、任务等关联数据。
     * </p>
     *
     * @param teamId 要解散的团队 ID
     * @throws ForbiddenException 当当前用户不是管理员时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceDisbandTeam(Long teamId) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 校验团队存在
        com.teamcollab.entity.Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }

        // 3. 强制解散（级联删除）
        teamMapper.deleteById(teamId);
        log.info("管理员强制解散团队: teamId={}, name={}", teamId, team.getName());
    }

    // ==================== 技能管理 ====================

    /**
     * 添加技能标签（管理员专用）
     * <p>
     * 管理员向系统技能库中添加新的技能选项，供用户在个人信息中选择。
     * 添加前会校验技能名称的唯一性，防止重复添加同名技能。
     * </p>
     *
     * @param request 创建技能请求，包含技能名称和分类
     * @return 包含 skillId、skillName、category 的键值对
     * @throws ForbiddenException 当当前用户不是管理员时
     * @throws BusinessException  当技能名称已存在时（ErrorCode.SKILL_EXISTS）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addSkill(CreateSkillRequest request) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 校验技能名称唯一性
        List<Skill> existingSkills = skillMapper.selectList(
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getSkillName, request.getSkillName()));
        if (!existingSkills.isEmpty()) {
            log.warn("添加技能被拒绝 - 技能名称已存在: {}", request.getSkillName());
            throw new BusinessException(ErrorCode.SKILL_EXISTS);
        }

        // 3. 插入技能记录
        Skill skill = new Skill();
        skill.setSkillName(request.getSkillName());
        skill.setCategory(request.getCategory());
        skill.setCreatedAt(java.time.LocalDateTime.now());
        skillMapper.insert(skill);

        log.info("管理员添加技能: skillId={}, skillName={}, category={}",
                skill.getSkillId(), skill.getSkillName(), skill.getCategory());

        Map<String, Object> result = new HashMap<>();
        result.put("skillId", skill.getSkillId());
        result.put("skillName", skill.getSkillName());
        result.put("category", skill.getCategory());
        return result;
    }

    /**
     * 删除技能标签（管理员专用）
     * <p>
     * 管理员删除指定的技能标签。删除前会校验技能是否存在。
     * 已关联该技能的用户关系也会被级联清除
     * （依赖于数据库外键 ON DELETE CASCADE 或业务层的清理逻辑）。
     * </p>
     *
     * @param skillId 要删除的技能 ID
     * @throws ForbiddenException 当当前用户不是管理员时
     * @throws BusinessException  当技能不存在时（ErrorCode.SKILL_NOT_FOUND）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long skillId) {
        // 1. 管理员权限校验
        checkIsAdmin();

        // 2. 校验技能存在
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.SKILL_NOT_FOUND);
        }

        // 3. 删除技能
        skillMapper.deleteById(skillId);
        log.info("管理员删除技能: skillId={}, skillName={}", skillId, skill.getSkillName());
    }

    // ==================== 权限校验 ====================

    /**
     * 校验当前用户是否为管理员
     * <p>
     * 从 Spring Security 的 SecurityContextHolder 中获取当前认证信息，
     * 检查用户是否拥有 "ROLE_admin" 权限（由 JwtAuthenticationFilter 在认证时设置）。
     * 如果当前用户不是管理员，抛出 ForbiddenException。
     * </p>
     * <p>
     * 此方法应作为所有管理操作的前置权限检查。
     * </p>
     *
     * @throws ForbiddenException 当当前用户不是管理员时
     */
    @Override
    public void checkIsAdmin() {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 未登录或权限信息缺失
        if (authentication == null || authentication.getAuthorities() == null) {
            log.warn("管理员权限校验失败：未登录或权限信息缺失");
            throw new ForbiddenException("非管理员，无权操作");
        }

        // 检查是否拥有管理员权限（ROLE_admin）
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_admin"));

        if (!isAdmin) {
            log.warn("管理员权限校验失败：用户非管理员角色");
            throw new ForbiddenException("非管理员，无权操作");
        }

        log.debug("管理员权限校验通过");
    }
}
