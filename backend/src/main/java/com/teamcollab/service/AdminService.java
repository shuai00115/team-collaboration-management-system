package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.dto.admin.*;

import java.util.Map;

/**
 * 管理员服务接口
 * <p>
 * 提供系统管理员专属的管理功能，包括用户管理、团队管理和技能标签管理。
 * 所有方法均需要管理员权限，不继承IService。
 * </p>
 *
 * @author teamcollab
 */
public interface AdminService {

    /**
     * 获取用户列表（管理员专用）
     * <p>
     * 分页查询系统所有用户，支持按关键词和角色进行过滤。
     * 仅管理员有权调用。
     * </p>
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param keyword  关键词模糊搜索（匹配用户名），可为null表示不过滤
     * @param role     按角色过滤，可为null表示不过滤
     * @return 分页的管理员用户视图响应列表
     */
    Page<AdminUserResponse> getUserList(int pageNum, int pageSize, String keyword, String role);

    /**
     * 更新用户状态（管理员专用）
     * <p>
     * 管理员启用或禁用指定用户账号。
     * 仅管理员有权调用。
     * </p>
     *
     * @param userId  目标用户ID
     * @param request 更新用户状态请求，包含目标状态（active/disabled）
     */
    void updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /**
     * 获取所有团队列表（管理员专用）
     * <p>
     * 分页查询系统所有团队，支持按关键词和团队状态过滤。
     * 仅管理员有权调用。
     * </p>
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param keyword  关键词模糊搜索（匹配团队名称），可为null表示不过滤
     * @param status   按团队状态过滤，可为null表示不过滤
     * @return 分页的管理员团队视图响应列表
     */
    Page<AdminTeamResponse> getTeamList(int pageNum, int pageSize, String keyword, String status);

    /**
     * 强制解散团队（管理员专用）
     * <p>
     * 管理员强制解散指定团队，无需二次确认。
     * 仅管理员有权调用。
     * </p>
     *
     * @param teamId 要解散的团队ID
     */
    void forceDisbandTeam(Long teamId);

    /**
     * 添加技能标签（管理员专用）
     * <p>
     * 管理员新增系统技能标签，供用户在个人信息中选择使用。
     * 仅管理员有权调用。
     * </p>
     *
     * @param request 创建技能请求，包含技能名称和分类
     * @return 包含新创建技能基本信息的键值对（如技能ID、名称等）
     */
    Map<String, Object> addSkill(CreateSkillRequest request);

    /**
     * 删除技能标签（管理员专用）
     * <p>
     * 管理员删除指定的技能标签，已关联该技能的用户关系也会被清除。
     * 仅管理员有权调用。
     * </p>
     *
     * @param skillId 要删除的技能ID
     */
    void deleteSkill(Long skillId);

    /**
     * 校验当前用户是否为管理员
     * <p>
     * 检查当前登录用户是否具有管理员角色，如果不是则抛出权限异常。
     * 通常作为其他管理操作的权限前置检查。
     * </p>
     *
     * @throws com.teamcollab.exception.BusinessException 当当前用户不是管理员时抛出
     */
    void checkIsAdmin();
}
