package com.teamcollab.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teamcollab.entity.User;
import com.teamcollab.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务实现
 * 实现 Spring Security 的 UserDetailsService 接口，
 * 从数据库中加载用户信息用于认证
 *
 * @author TeamCollab
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     * 该方法由 Spring Security 在登录认证时自动调用
     *
     * @param username 登录用户名
     * @return UserDetails 对象，包含用户名、密码、权限、账户状态等
     * @throws UsernameNotFoundException 如果用户不存在则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用 LambdaQueryWrapper 构建查询条件，按用户名查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在时抛出异常，Spring Security 会处理为认证失败
        if (user == null) {
            log.warn("登录失败：用户 {} 不存在", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 构建用户权限列表，角色需要加上 "ROLE_" 前缀
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        // 判断账户是否被禁用：status="active"表示正常，"disabled"表示禁用
        boolean enabled = "active".equals(user.getStatus());

        log.debug("查询到用户：{}，角色：{}，账户状态：{}", username, user.getRole(), enabled ? "正常" : "禁用");

        // 返回 Spring Security 的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // 用户名
                user.getPasswordHash(), // 密码（数据库中已BCrypt加密）
                enabled, // enabled - 账户状态（active启用/disabled禁用）
                true, // accountNonExpired - 账户是否未过期
                true, // credentialsNonExpired - 凭证是否未过期
                true, // accountNonLocked - 账户是否未锁定
                Collections.singletonList(authority) // authorities - 权限列表
        );
    }
}
