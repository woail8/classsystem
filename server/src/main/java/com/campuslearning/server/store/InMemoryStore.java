package com.campuslearning.server.store;

import com.campuslearning.server.model.ClassInfo;
import com.campuslearning.server.model.ClassMember;
import com.campuslearning.server.model.Course;
import com.campuslearning.server.model.Assignment;
import com.campuslearning.server.model.NotificationMessage;
import com.campuslearning.server.model.ResourceFile;
import com.campuslearning.server.model.SignInRecord;
import com.campuslearning.server.model.SignInTask;
import com.campuslearning.server.model.Submission;
import com.campuslearning.server.model.User;
import com.campuslearning.server.model.UserRole;
import com.campuslearning.server.util.InviteCodeUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务端内存存储。
 * 当前阶段先让接口链路跑通，后续可替换为 MySQL / Redis 持久化实现。
 */
@Component
public class InMemoryStore {

    private final AtomicLong userIdGenerator = new AtomicLong(1);
    private final AtomicLong courseIdGenerator = new AtomicLong(1);
    private final AtomicLong classIdGenerator = new AtomicLong(1);
    private final AtomicLong memberIdGenerator = new AtomicLong(1);
    private final AtomicLong signInTaskIdGenerator = new AtomicLong(1);
    private final AtomicLong signInRecordIdGenerator = new AtomicLong(1);
    private final AtomicLong assignmentIdGenerator = new AtomicLong(1);
    private final AtomicLong submissionIdGenerator = new AtomicLong(1);
    private final AtomicLong resourceIdGenerator = new AtomicLong(1);
    private final AtomicLong notificationIdGenerator = new AtomicLong(1);

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final Map<Long, User> userMap = new ConcurrentHashMap<>();
    private final Map<Long, Course> courseMap = new ConcurrentHashMap<>();
    private final Map<Long, ClassInfo> classMap = new ConcurrentHashMap<>();
    private final Map<Long, ClassMember> classMemberMap = new ConcurrentHashMap<>();
    private final Map<Long, SignInTask> signInTaskMap = new ConcurrentHashMap<>();
    private final Map<Long, SignInRecord> signInRecordMap = new ConcurrentHashMap<>();
    private final Map<Long, Assignment> assignmentMap = new ConcurrentHashMap<>();
    private final Map<Long, Submission> submissionMap = new ConcurrentHashMap<>();
    private final Map<Long, ResourceFile> resourceFileMap = new ConcurrentHashMap<>();
    private final Map<Long, NotificationMessage> notificationMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        User teacher = new User();
        teacher.setId(nextUserId());
        teacher.setUsername("teacher01");
        teacher.setRealName("张老师");
        teacher.setPasswordHash(passwordEncoder.encode("123456"));
        teacher.setRole(UserRole.TEACHER);
        userMap.put(teacher.getId(), teacher);

        User student = new User();
        student.setId(nextUserId());
        student.setUsername("student01");
        student.setRealName("李同学");
        student.setPasswordHash(passwordEncoder.encode("123456"));
        student.setRole(UserRole.STUDENT);
        userMap.put(student.getId(), student);

        User testUser = new User();
        testUser.setId(nextUserId());
        testUser.setUsername("test");
        testUser.setRealName("测试用户");
        testUser.setPasswordHash(passwordEncoder.encode("1234"));
        testUser.setRole(UserRole.STUDENT);
        userMap.put(testUser.getId(), testUser);
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public Map<Long, User> getUserMap() {
        return userMap;
    }

    public Map<Long, Course> getCourseMap() {
        return courseMap;
    }

    public Map<Long, ClassInfo> getClassMap() {
        return classMap;
    }

    public Map<Long, ClassMember> getClassMemberMap() {
        return classMemberMap;
    }

    public Map<Long, SignInTask> getSignInTaskMap() {
        return signInTaskMap;
    }

    public Map<Long, SignInRecord> getSignInRecordMap() {
        return signInRecordMap;
    }

    public Map<Long, Assignment> getAssignmentMap() {
        return assignmentMap;
    }

    public Map<Long, Submission> getSubmissionMap() {
        return submissionMap;
    }

    public Map<Long, ResourceFile> getResourceFileMap() {
        return resourceFileMap;
    }

    public Map<Long, NotificationMessage> getNotificationMap() {
        return notificationMap;
    }

    public Long nextUserId() {
        return userIdGenerator.getAndIncrement();
    }

    public Long nextCourseId() {
        return courseIdGenerator.getAndIncrement();
    }

    public Long nextClassId() {
        return classIdGenerator.getAndIncrement();
    }

    public Long nextMemberId() {
        return memberIdGenerator.getAndIncrement();
    }

    public Long nextSignInTaskId() {
        return signInTaskIdGenerator.getAndIncrement();
    }

    public Long nextSignInRecordId() {
        return signInRecordIdGenerator.getAndIncrement();
    }

    public Long nextAssignmentId() {
        return assignmentIdGenerator.getAndIncrement();
    }

    public Long nextSubmissionId() {
        return submissionIdGenerator.getAndIncrement();
    }

    public Long nextResourceId() {
        return resourceIdGenerator.getAndIncrement();
    }

    public Long nextNotificationId() {
        return notificationIdGenerator.getAndIncrement();
    }
}
