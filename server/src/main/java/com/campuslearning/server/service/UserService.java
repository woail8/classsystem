package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.User;
import com.campuslearning.server.model.UserRole;
import com.campuslearning.server.repository.UserRepository;
import com.campuslearning.server.security.CurrentUser;
import com.campuslearning.server.security.UserContext;
import org.springframework.stereotype.Service;

/**
 * 服务端用户服务，负责获取当前用户与角色校验。
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    public CurrentUser getCurrentUser() {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }
        return currentUser;
    }

    public CurrentUser requireTeacher() {
        CurrentUser currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new BusinessException(403, "仅教师可执行该操作");
        }
        return currentUser;
    }

    public CurrentUser requireStudent() {
        CurrentUser currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.STUDENT) {
            throw new BusinessException(403, "仅学生可执行该操作");
        }
        return currentUser;
    }
}
