package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.SignInRecord;
import com.campuslearning.server.model.SignInTask;
import com.campuslearning.server.model.SignInType;
import com.campuslearning.server.repository.ClassMemberRepository;
import com.campuslearning.server.repository.SignInRecordRepository;
import com.campuslearning.server.repository.SignInTaskRepository;
import com.campuslearning.server.security.CurrentUser;
import com.campuslearning.server.util.DistanceUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端签到业务服务。
 */
@Service
public class SignInService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserService userService;
    private final CourseService courseService;
    private final NotificationService notificationService;
    private final SignInTaskRepository signInTaskRepository;
    private final SignInRecordRepository signInRecordRepository;
    private final ClassMemberRepository classMemberRepository;

    public SignInService(UserService userService,
                         CourseService courseService,
                         NotificationService notificationService,
                         SignInTaskRepository signInTaskRepository,
                         SignInRecordRepository signInRecordRepository,
                         ClassMemberRepository classMemberRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.notificationService = notificationService;
        this.signInTaskRepository = signInTaskRepository;
        this.signInRecordRepository = signInRecordRepository;
        this.classMemberRepository = classMemberRepository;
    }

    @Transactional
    public Map<String, Object> createTask(Long classId,
                                          String title,
                                          String typeValue,
                                          Double locationLat,
                                          Double locationLng,
                                          Double radius,
                                          String deadlineText) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureClassAdmin(classId, currentUser.getUserId());

        SignInType type;
        try {
            type = SignInType.valueOf(typeValue.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "签到类型仅支持 normal 或 location");
        }

        if (type == SignInType.LOCATION) {
            if (locationLat == null || locationLng == null || radius == null) {
                throw new BusinessException(400, "位置签到必须提供经纬度和半径");
            }
        }

        SignInTask task = new SignInTask();
        task.setClassId(classId);
        task.setTitle(title);
        task.setType(type);
        task.setLocationLat(locationLat);
        task.setLocationLng(locationLng);
        task.setRadius(radius);
        task.setDeadline(LocalDateTime.parse(deadlineText, FORMATTER));
        task.setCreatedAt(LocalDateTime.now());
        task.setTeacherId(currentUser.getUserId());
        task = signInTaskRepository.save(task);
        final Long createdTaskId = task.getId();

        classMemberRepository.findByClassId(classId).stream()
                .filter(item -> item.getClassId().equals(classId) && !item.getUserId().equals(currentUser.getUserId()))
                .forEach(item -> notificationService.pushToUser(
                        item.getUserId(),
                        "NEW_SIGNIN_TASK",
                        "班级管理员发布了新的签到任务",
                        "{\"taskId\":" + createdTaskId + ",\"classId\":" + classId + "}"
                ));

        return taskToMap(task);
    }

    public Map<String, Object> getActiveTask(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureMember(classId, currentUser.getUserId());

        return signInTaskRepository.findFirstByClassIdAndDeadlineAfterOrderByCreatedAtDesc(classId, LocalDateTime.now())
                .map(this::taskToMap)
                .orElse(null);
    }

    @Transactional
    public Map<String, Object> submit(Long taskId, Double latitude, Double longitude) {
        CurrentUser currentUser = userService.getCurrentUser();
        SignInTask task = signInTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "签到任务不存在"));

        courseService.ensureMember(task.getClassId(), currentUser.getUserId());
        if (task.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "签到任务已截止");
        }

        if (signInRecordRepository.existsByTaskIdAndStudentId(taskId, currentUser.getUserId())) {
            throw new BusinessException(400, "请勿重复签到");
        }

        SignInRecord record = new SignInRecord();
        record.setTaskId(taskId);
        record.setClassId(task.getClassId());
        record.setStudentId(currentUser.getUserId());
        record.setSubmitLat(latitude);
        record.setSubmitLng(longitude);
        record.setSignedAt(LocalDateTime.now());

        if (task.getType() == SignInType.NORMAL) {
            record.setSuccess(Boolean.TRUE);
            record.setResultMessage("普通签到成功");
        } else {
            if (latitude == null || longitude == null) {
                throw new BusinessException(400, "位置签到必须提交经纬度");
            }
            double distance = DistanceUtil.calculateMeters(latitude, longitude, task.getLocationLat(), task.getLocationLng());
            record.setDistanceMeters(distance);
            boolean success = distance <= task.getRadius();
            record.setSuccess(success);
            record.setResultMessage(success ? "位置签到成功" : "未在签到范围内");
        }

        record = signInRecordRepository.save(record);
        notificationService.pushToUser(
                task.getTeacherId(),
                "SIGNIN_RESULT",
                "有成员完成签到",
                "{\"taskId\":" + task.getId() + ",\"studentId\":" + currentUser.getUserId()
                        + ",\"success\":" + record.getSuccess() + "}"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("success", record.getSuccess());
        result.put("message", record.getResultMessage());
        result.put("distanceMeters", record.getDistanceMeters());
        return result;
    }

    private Map<String, Object> taskToMap(SignInTask task) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("title", task.getTitle());
        result.put("type", task.getType().name().toLowerCase());
        result.put("locationLat", task.getLocationLat());
        result.put("locationLng", task.getLocationLng());
        result.put("radius", task.getRadius());
        result.put("deadline", task.getDeadline().format(FORMATTER));
        return result;
    }
}
