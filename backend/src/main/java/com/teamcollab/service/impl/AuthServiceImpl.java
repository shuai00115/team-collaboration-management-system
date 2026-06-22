package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.common.util.BcryptUtils;
import com.teamcollab.dto.auth.LoginRequest;
import com.teamcollab.dto.auth.LoginResponse;
import com.teamcollab.dto.auth.RegisterRequest;
import com.teamcollab.entity.User;
import com.teamcollab.entity.UserSkill;
import com.teamcollab.mapper.UserMapper;
import com.teamcollab.mapper.UserSkillMapper;
import com.teamcollab.security.JwtTokenProvider;
import com.teamcollab.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现类
 * <p>
 * 提供用户注册、登录、获取当前登录用户信息等认证相关功能的完整实现。
 * 注册时对明文密码进行 BCrypt 不可逆加密后存储，确保密码安全。
 * 登录时校验密码后生成 JWT 令牌，后续请求通过令牌验证身份。
 * </p>
 *
 * <p><b>安全设计：</b></p>
 * <ul>
 *     <li>密码使用 BCrypt 算法加密，每次加密结果不同（内置随机盐值）</li>
 *     <li>JWT 令牌包含用户ID、用户名、角色等核心信息</li>
 *     <li>令牌过期后客户端需重新登录获取新令牌</li>
 *     <li>注册时默认角色为 member，默认状态为 active</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    /** 用户 Mapper，提供按用户名/邮箱查询等数据库操作 */
    private final UserMapper userMapper;

    /** 用户技能关联 Mapper，用于注册时批量插入技能标签 */
    private final UserSkillMapper userSkillMapper;

    /** JWT 令牌提供器，负责令牌的生成、解析与验证 */
    private final JwtTokenProvider jwtTokenProvider;

    // ==================== 用户注册 ====================

    /**
     * 新用户注册
     * <p>
     * 完整注册流程：
     * <ol>
     *     <li>校验用户名唯一性 —— 重名则抛出 USERNAME_EXISTS 异常</li>
     *     <li>校验邮箱唯一性 —— 重复则抛出 EMAIL_EXISTS 异常</li>
     *     <li>对明文密码执行 BCrypt 加密</li>
     *     <li>构建 User 实体（默认角色 member，默认状态 active）并插入数据库</li>
     *     <li>如果请求中包含技能 ID 列表，批量插入 user_skills 关联记录</li>
     * </ol>
     * </p>
     *
     * @param request 注册请求 DTO，包含 username（必填）、password（必填）、email（必填）、skillIds（可选）
     * @return 包含 userId 和 username 的键值对 Map
     * @throws BusinessException 当用户名或邮箱已被占用时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest request) {
        log.info("开始注册新用户: username={}, email={}", request.getUsername(), request.getEmail());

        // 1. 校验用户名是否已被占用
        User existingByUsername = userMapper.selectByUsername(request.getUsername());
        if (existingByUsername != null) {
            log.warn("注册失败 - 用户名已存在: {}", request.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 2. 校验邮箱是否已被注册
        User existingByEmail = userMapper.selectByEmail(request.getEmail());
        if (existingByEmail != null) {
            log.warn("注册失败 - 邮箱已被注册: {}", request.getEmail());
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 3. 对明文密码进行 BCrypt 加密（自动生成随机盐值，每次结果不同）
        String encodedPassword = BcryptUtils.encode(request.getPassword());

        // 4. 构建 User 实体对象并插入数据库
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(encodedPassword);
        user.setEmail(request.getEmail());
        user.setRole("member");        // 新用户默认角色为普通成员
        user.setStatus("active");      // 新用户默认状态为激活
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        log.info("用户基本信息已插入数据库: userId={}", user.getUserId());

        // 5. 如果提供了技能 ID 列表，批量插入 user_skills 关联记录
        List<Long> skillIds = request.getSkillIds();
        if (skillIds != null && !skillIds.isEmpty()) {
            for (Long skillId : skillIds) {
                UserSkill userSkill = new UserSkill();
                userSkill.setUserId(user.getUserId());
                userSkill.setSkillId(skillId);
                userSkill.setLevel("beginner");   // 注册时默认技能等级为初级
                userSkill.setCreatedAt(LocalDateTime.now());
                userSkillMapper.insert(userSkill);
            }
            log.info("已为用户批量添加 {} 个技能标签: userId={}", skillIds.size(), user.getUserId());
        }

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());

        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return result;
    }

    // ==================== 用户登录 ====================

    /**
     * 用户登录认证
     * <p>
     * 完整登录流程：
     * <ol>
     *     <li>根据用户名查询用户记录</li>
     *     <li>检查账号状态，被禁用的账号无法登录</li>
     *     <li>BCrypt 比对输入的明文密码与数据库中的密文</li>
     *     <li>生成包含用户信息的 JWT 令牌</li>
     *     <li>组装 LoginResponse 返回令牌及用户概要信息</li>
     * </ol>
     * </p>
     *
     * @param request 登录请求 DTO，包含 username 和 password
     * @return LoginResponse 包含 accessToken、tokenType、expiresIn 及 userInfo
     * @throws BusinessException 用户名不存在、密码错误或账号被禁用时抛出
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户尝试登录: username={}", request.getUsername());

        // 1. 根据用户名查找用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败 - 用户不存在: {}", request.getUsername());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }

        // 2. 检查账号是否已被管理员禁用
        if ("disabled".equals(user.getStatus())) {
            log.warn("登录失败 - 用户已被禁用: {}", request.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 3. 使用 BCrypt 验证密码（防时序攻击的常量时间比对）
        if (!BcryptUtils.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("登录失败 - 密码错误: {}", request.getUsername());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码错误");
        }

        // 4. 生成 JWT 访问令牌
        String token = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );

        // 5. 构建返回的用户简要信息（内嵌类）
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .role(user.getRole())
                .build();

        // 6. 组装完整的登录响应
        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(7200L)   // 令牌有效期 7200 秒（2小时）
                .userInfo(userInfo)
                .build();

        log.info("用户登录成功: userId={}, username={}, role={}", user.getUserId(), user.getUsername(), user.getRole());
        return response;
    }

    // ==================== 当前用户信息获取 ====================

    /**
     * 获取当前登录用户的 ID
     * <p>
     * 从 Spring Security 的安全上下文（SecurityContextHolder）中提取当前认证信息，
     * 将认证主体（Principal）解析为 Long 类型的用户 ID。
     * 认证主体在 JwtAuthenticationFilter 中被设置为用户 ID。
     * </p>
     *
     * @return 当前登录用户的 Long 类型 ID
     * @throws BusinessException 当用户未登录或认证信息无法解析时
     */
    @Override
    public Long getCurrentUserId() {
        // 从 SecurityContextHolder 获取当前认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 未登录或认证信息无效
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("获取当前用户ID失败：未登录或认证已过期");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法获取当前用户信息");
        }

        // 尝试解析 Principal 为 Long 类型的用户 ID
        try {
            if (principal instanceof Long) {
                return (Long) principal;
            }
            // 兼容 principal 为字符串类型的情况（如 JWT subject 存储为字符串）
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            log.error("解析当前用户ID失败，principal={}", principal, e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法获取当前用户信息");
        }
    }

    /**
     * 获取当前登录用户的角色
     * <p>
     * 从 Spring Security 的认证信息中提取用户的权限列表（GrantedAuthority），
     * 返回第一个权限名称。权限通常以 "ROLE_" 为前缀，这里去掉前缀返回纯角色名。
     * 如果用户无任何权限，默认返回 "member"。
     * </p>
     *
     * @return 当前用户的角色名称，如 "admin" 或 "member"
     * @throws BusinessException 当用户未登录时
     */
    @Override
    public String getCurrentUserRole() {
        // 从 SecurityContextHolder 获取当前认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 未登录
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("获取当前用户角色失败：未登录");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 无权限信息时默认返回 member
        if (authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            log.warn("当前用户无权限信息，默认返回 member 角色");
            return "member";
        }

        // 获取第一个权限名称，去掉 "ROLE_" 前缀并转为小写
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", "").toLowerCase())
                .orElse("member");
    }
}
