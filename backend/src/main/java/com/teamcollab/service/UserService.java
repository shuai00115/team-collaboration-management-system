package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.user.*;
import com.teamcollab.entity.User;

/**
 * 用户服务接口
 * <p>
 * 提供当前用户个人信息管理、团队查询、任务查询、申请记录查询以及技能管理等功能。
 * </p>
 *
 * @author teamcollab
 */
public interface UserService extends IService<User> {

    /**
     * 获取当前用户的完整个人信息
     * <p>
     * 查询当前登录用户的个人资料，包含技能标签等详细信息。
     * </p>
     *
     * @return 用户完整信息响应，包含基本资料和技能列表
     */
    UserInfoResponse getCurrentUserInfo();

    /**
     * 更新当前用户个人资料
     * <p>
     * 修改指定用户的头像、个人简介等基本资料信息。
     * </p>
     *
     * @param userId  目标用户ID
     * @param request 更新资料请求，包含要修改的头像URL、个人简介等字段
     */
    void updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改用户密码
     * <p>
     * 修改指定用户的登录密码，修改前需要验证旧密码的正确性。
     * </p>
     *
     * @param userId  目标用户ID
     * @param request 修改密码请求，包含旧密码和新密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 获取当前用户所属团队列表（分页）
     * <p>
     * 查询指定用户所加入的所有团队，返回分页结果。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @return 分页的我的团队响应列表
     */
    Page<MyTeamResponse> getMyTeams(Long userId, int pageNum, int pageSize);

    /**
     * 获取当前用户的任务列表（分页）
     * <p>
     * 查询指定用户在所有团队中被分配或创建的任务，支持按优先级、任务列表、团队等多条件过滤。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param priority 按优先级过滤，可为null表示不过滤
     * @param listId   按任务列表ID过滤，可为null表示不过滤
     * @param teamId   按团队ID过滤，可为null表示不过滤
     * @return 分页的我的任务响应列表
     */
    Page<MyTaskResponse> getMyTasks(Long userId, int pageNum, int pageSize, String priority, Long listId, Long teamId);

    /**
     * 获取当前用户的入队申请历史记录（分页）
     * <p>
     * 查询指定用户提交的所有加入团队申请记录，支持按申请状态过滤。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param status   按申请状态过滤（如 pending/approved/rejected），可为null表示不过滤
     * @return 分页的我的申请响应列表
     */
    Page<MyApplicationResponse> getMyApplications(Long userId, int pageNum, int pageSize, String status);

    /**
     * 为当前用户添加技能标签
     * <p>
     * 将指定技能关联到当前用户，并设置该技能的掌握程度。
     * </p>
     *
     * @param userId  用户ID
     * @param skillId 技能ID
     * @param level   技能掌握程度（如 入门/熟练/精通）
     */
    void addSkill(Long userId, Long skillId, String level);

    /**
     * 删除当前用户的技能标签
     * <p>
     * 解除指定技能与当前用户的关联关系。
     * </p>
     *
     * @param userId  用户ID
     * @param skillId 要删除的技能ID
     */
    void deleteSkill(Long userId, Long skillId);
}
