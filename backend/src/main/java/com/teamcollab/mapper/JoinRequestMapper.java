package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.JoinRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 加入申请 Mapper 接口
 * <p>
 * 提供团队加入申请相关的数据库操作，包括申请列表查询、行级锁查询、
 * 重复申请检查、用户申请历史查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface JoinRequestMapper extends BaseMapper<JoinRequest> {

    /**
     * 分页查询指定团队的加入申请列表（支持状态筛选）
     *
     * @param page   分页对象
     * @param teamId 团队 ID
     * @param status 申请状态（pending/approved/rejected）
     * @return 分页申请列表
     */
    Page<Map<String, Object>> selectByTeamId(Page<JoinRequest> page, @Param("teamId") Long teamId, @Param("status") String status);

    /**
     * 行级锁查询申请记录（用于并发控制，如审批申请前先锁定）
     *
     * @param requestId 申请 ID
     * @return 申请实体
     */
    JoinRequest selectForUpdate(@Param("requestId") Long requestId);

    /**
     * 检查用户是否已有对指定团队的待处理申请
     *
     * @param userId 用户 ID
     * @param teamId 团队 ID
     * @return 待处理申请数量
     */
    int countByUserAndTeam(@Param("userId") Long userId, @Param("teamId") Long teamId);

    /**
     * 分页查询用户的所有申请记录（支持状态筛选）
     *
     * @param page   分页对象
     * @param userId 用户 ID
     * @param status 申请状态
     * @return 分页申请列表
     */
    Page<Map<String, Object>> selectUserApplications(Page<JoinRequest> page, @Param("userId") Long userId, @Param("status") String status);
}
