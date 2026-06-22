package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.application.ApplicationResponse;
import com.teamcollab.entity.JoinRequest;

import java.util.Map;

/**
 * 入队申请服务接口
 * <p>
 * 提供用户加入团队的申请提交、队长审核申请以及申请状态查询等功能。
 * </p>
 *
 * @author teamcollab
 */
public interface ApplicationService extends IService<JoinRequest> {

    /**
     * 提交入队申请
     * <p>
     * 用户向指定团队提交加入申请。申请人必须尚未是该团队成员，
     * 且不能存在待处理的申请记录。
     * </p>
     *
     * @param teamId  目标团队ID
     * @param userId  申请人用户ID
     * @param message 申请留言/自我介绍
     * @return 包含申请结果信息的键值对（如申请ID、状态等）
     */
    Map<String, Object> apply(Long teamId, Long userId, String message);

    /**
     * 获取团队的申请列表（分页）
     * <p>
     * 查询指定团队收到的所有入队申请，仅队长有权查看。
     * 支持按申请状态进行过滤。
     * </p>
     *
     * @param teamId   团队ID
     * @param userId   当前操作用户ID（需为队长）
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param status   按申请状态过滤（如 pending/approved/rejected），可为null表示不过滤
     * @return 分页的申请响应列表
     */
    Page<ApplicationResponse> getApplications(Long teamId, Long userId, int pageNum, int pageSize, String status);

    /**
     * 审核入队申请（批准/拒绝）
     * <p>
     * 队长对指定入队申请进行审核，操作包括批准或拒绝。
     * 使用数据库行级锁（SELECT FOR UPDATE）确保并发安全。
     * </p>
     *
     * @param teamId    团队ID
     * @param requestId 申请记录ID
     * @param userId    当前操作用户ID（需为队长）
     * @param action    审核动作（approve 批准 / reject 拒绝）
     */
    void reviewApplication(Long teamId, Long requestId, Long userId, String action);

    /**
     * 检查用户是否有待处理的入队申请
     * <p>
     * 判断指定用户是否已经向指定团队提交过且仍处于待审核状态的申请。
     * </p>
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return true表示存在待处理的申请，false表示不存在
     */
    boolean hasPendingApplication(Long teamId, Long userId);
}
