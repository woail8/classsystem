package com.campuslearning.server.security;

import com.campuslearning.server.model.UserRole;

/**
 * 服务端当前登录用户上下文对象。
 */
public class CurrentUser {

    private final Long userId;
    private final String username;
    private final String realName;
    private final UserRole role;

    public CurrentUser(Long userId, String username, String realName, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public UserRole getRole() {
        return role;
    }
}
