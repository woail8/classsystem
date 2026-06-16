package com.campuslearning.server.security;

import com.campuslearning.server.common.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务端鉴权拦截器。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未提供有效的登录凭证");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parse(token);
        CurrentUser currentUser = new CurrentUser(
                Long.valueOf(claims.getSubject()),
                String.valueOf(claims.get("username")),
                String.valueOf(claims.get("realName")),
                Enum.valueOf(com.campuslearning.server.model.UserRole.class, String.valueOf(claims.get("role")))
        );
        UserContext.set(currentUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
