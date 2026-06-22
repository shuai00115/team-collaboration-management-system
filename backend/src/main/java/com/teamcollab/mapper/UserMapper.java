package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 * <p>
 * 提供用户相关的数据库操作，包括按用户名/邮箱查询用户、分页搜索用户列表等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，未找到返回 null
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户实体，未找到返回 null
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 分页查询用户列表（支持关键字搜索和角色筛选）
     *
     * @param page    分页对象
     * @param keyword 搜索关键字（匹配用户名或邮箱）
     * @param role    角色筛选条件
     * @return 分页用户列表
     */
    Page<User> selectUserPage(Page<User> page, @Param("keyword") String keyword, @Param("role") String role);
}
