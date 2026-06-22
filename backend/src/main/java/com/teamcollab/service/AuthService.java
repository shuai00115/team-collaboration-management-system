package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.auth.LoginRequest;
import com.teamcollab.dto.auth.LoginResponse;
import com.teamcollab.dto.auth.RegisterRequest;
import com.teamcollab.entity.User;

import java.util.Map;

/**
 * 认证服务接口
 * <p>
 * 提供用户注册、登录以及当前用户身份获取等功能。
 * </p>
 *
 * @author teamcollab
 */
public interface AuthService extends IService<User> {

    /**
     * 注册新用户
     * <p>
     * 根据注册请求信息创建新用户账号，返回创建成功后的用户基本信息。
     * </p>
     *
     * @param request 注册请求，包含用户名、密码、邮箱等注册信息
     * @return 包含新创建用户基本信息的键值对（如用户ID、用户名等）
     */
    Map<String, Object> register(RegisterRequest request);

    /**
     * 用户登录
     * <p>
     * 使用用户名和密码进行登录认证，验证通过后返回JWT令牌及用户信息。
     * </p>
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应，包含JWT令牌和用户基本信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户的ID
     * <p>
     * 从Spring Security的安全上下文中提取当前已认证用户的唯一标识。
     * </p>
     *
     * @return 当前登录用户的ID
     */
    Long getCurrentUserId();

    /**
     * 获取当前登录用户的角色
     * <p>
     * 从Spring Security的安全上下文中提取当前已认证用户的角色名称。
     * </p>
     *
     * @return 当前登录用户的角色字符串（如 ROLE_USER、ROLE_ADMIN 等）
     */
    String getCurrentUserRole();
}
