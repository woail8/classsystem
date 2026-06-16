package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.ClassMember;
import com.campuslearning.server.model.NotificationMessage;
import com.campuslearning.server.repository.ClassMemberRepository;
import com.campuslearning.server.repository.NotificationMessageRepository;
import com.campuslearning.server.security.CurrentUser;
import com.campuslearning.server.websocket.NotificationWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端通知服务，负责通知存储与 WebSocket 推送。
 */
@Service
public class NotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final UserService userService;
    private final CourseService courseService;
    private final ClassMemberRepository classMemberRepository;
    private final NotificationMessageRepository notificationMessageRepository;

    public NotificationService(NotificationWebSocketHandler notificationWebSocketHandler,
                               UserService userService,
                               CourseService courseService,
                               ClassMemberRepository classMemberRepository,
                               NotificationMessageRepository notificationMessageRepository) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.userService = userService;
        this.courseService = courseService;
        this.classMemberRepository = classMemberRepository;
        this.notificationMessageRepository = notificationMessageRepository;
    }

    @Transactional
    public Map<String, Object> publish(Long classId, String title, String content) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureClassAdmin(classId, currentUser.getUserId());

        NotificationMessage notification = new NotificationMessage();
        notification.setClassId(classId);
        notification.setTeacherId(currentUser.getUserId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationMessageRepository.save(notification);

        String dataJson = "{\"id\":" + notification.getId()
                + ",\"classId\":" + classId
                + ",\"title\":\"" + escape(title)
                + "\",\"content\":\"" + escape(content) + "\"}";

        for (ClassMember member : classMemberRepository.findByClassId(classId)) {
            if (member.getClassId().equals(classId) && !member.getUserId().equals(currentUser.getUserId())) {
                pushToUser(member.getUserId(), "NEW_NOTIFICATION", "收到一条新的班级通知", dataJson);
            }
        }

        return toMap(notification);
    }

    public List<Map<String, Object>> listMine() {
        CurrentUser currentUser = userService.getCurrentUser();
        List<Long> classIds = new ArrayList<>();
        for (ClassMember member : classMemberRepository.findByUserId(currentUser.getUserId())) {
            if (member.getUserId().equals(currentUser.getUserId())) {
                classIds.add(member.getClassId());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        notificationMessageRepository.findByClassIdInOrderByCreatedAtDesc(classIds)
                .forEach(item -> result.add(toMap(item)));
        return result;
    }

    public void pushToUser(Long userId, String type, String message, String dataJson) {
        String json = "{\"type\":\"" + type + "\",\"message\":\"" + escape(message)
                + "\",\"data\":" + dataJson + "}";
        notificationWebSocketHandler.sendToUser(userId, json);
    }

    private Map<String, Object> toMap(NotificationMessage notification) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", notification.getId());
        result.put("classId", notification.getClassId());
        result.put("title", notification.getTitle());
        result.put("content", notification.getContent());
        result.put("createTime", notification.getCreatedAt().format(FORMATTER));
        result.put("read", Boolean.FALSE);
        return result;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}
