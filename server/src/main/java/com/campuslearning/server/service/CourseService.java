package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.Assignment;
import com.campuslearning.server.model.ClassInfo;
import com.campuslearning.server.model.ClassMember;
import com.campuslearning.server.model.Course;
import com.campuslearning.server.model.ResourceFile;
import com.campuslearning.server.model.Submission;
import com.campuslearning.server.model.User;
import com.campuslearning.server.repository.AssignmentRepository;
import com.campuslearning.server.repository.ClassInfoRepository;
import com.campuslearning.server.repository.ClassMemberRepository;
import com.campuslearning.server.repository.CourseRepository;
import com.campuslearning.server.repository.ExamQuestionRepository;
import com.campuslearning.server.repository.ExamRecordRepository;
import com.campuslearning.server.repository.ExamRepository;
import com.campuslearning.server.repository.NotificationMessageRepository;
import com.campuslearning.server.repository.ResourceFileRepository;
import com.campuslearning.server.repository.SignInRecordRepository;
import com.campuslearning.server.repository.SignInTaskRepository;
import com.campuslearning.server.repository.StudentAnswerRepository;
import com.campuslearning.server.repository.SubmissionRepository;
import com.campuslearning.server.security.CurrentUser;
import com.campuslearning.server.util.InviteCodeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端课程与班级服务。
 */
@Service
public class CourseService {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final CourseRepository courseRepository;
    private final ClassInfoRepository classInfoRepository;
    private final ClassMemberRepository classMemberRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final SignInTaskRepository signInTaskRepository;
    private final SignInRecordRepository signInRecordRepository;
    private final ResourceFileRepository resourceFileRepository;
    private final NotificationMessageRepository notificationMessageRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public CourseService(UserService userService,
                         FileStorageService fileStorageService,
                         CourseRepository courseRepository,
                         ClassInfoRepository classInfoRepository,
                         ClassMemberRepository classMemberRepository,
                         AssignmentRepository assignmentRepository,
                         SubmissionRepository submissionRepository,
                         SignInTaskRepository signInTaskRepository,
                         SignInRecordRepository signInRecordRepository,
                         ResourceFileRepository resourceFileRepository,
                         NotificationMessageRepository notificationMessageRepository,
                         ExamRepository examRepository,
                         ExamQuestionRepository examQuestionRepository,
                         ExamRecordRepository examRecordRepository,
                         StudentAnswerRepository studentAnswerRepository) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.courseRepository = courseRepository;
        this.classInfoRepository = classInfoRepository;
        this.classMemberRepository = classMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.signInTaskRepository = signInTaskRepository;
        this.signInRecordRepository = signInRecordRepository;
        this.resourceFileRepository = resourceFileRepository;
        this.notificationMessageRepository = notificationMessageRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examRecordRepository = examRecordRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Transactional
    public Map<String, Object> createCourse(String name) {
        CurrentUser currentUser = userService.getCurrentUser();

        Course course = new Course();
        course.setName(name);
        course.setTeacherId(currentUser.getUserId());
        course.setInviteCode(generateUniqueCourseInviteCode());
        course = courseRepository.save(course);

        ClassInfo defaultClass = createClassInternal(course.getId(), name + "默认班级", currentUser.getUserId());
        course.setDefaultClassId(defaultClass.getId());
        courseRepository.save(course);
        return classInfoToMap(defaultClass, course);
    }

    @Transactional
    public Map<String, Object> createClass(Long courseId, String className) {
        CurrentUser currentUser = userService.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(404, "课程不存在"));
        if (!course.getTeacherId().equals(currentUser.getUserId())) {
            throw new BusinessException(403, "仅课程管理员可创建班级");
        }

        ClassInfo classInfo = createClassInternal(courseId, className, currentUser.getUserId());
        return classInfoToMap(classInfo, course);
    }

    public Map<String, Object> regenerateInviteCode(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        ClassInfo classInfo = getClassInfo(classId);
        if (!classInfo.getTeacherId().equals(currentUser.getUserId())) {
            throw new BusinessException(403, "仅班级管理员可重置邀请码");
        }
        classInfo.setInviteCode(generateUniqueClassInviteCode());

        Map<String, Object> result = new HashMap<>();
        result.put("classId", classInfo.getId());
        result.put("inviteCode", classInfo.getInviteCode());
        return result;
    }

    @Transactional
    public Map<String, Object> joinClass(String inviteCode) {
        CurrentUser currentUser = userService.getCurrentUser();

        ClassInfo classInfo = classInfoRepository.findByInviteCodeIgnoreCase(inviteCode)
                .orElseThrow(() -> new BusinessException(404, "邀请码无效"));

        if (classMemberRepository.existsByClassIdAndUserId(classInfo.getId(), currentUser.getUserId())) {
            throw new BusinessException(400, "你已加入该班级");
        }

        ClassMember member = new ClassMember();
        member.setClassId(classInfo.getId());
        member.setUserId(currentUser.getUserId());
        member.setRole(currentUser.getRole());
        member.setJoinedAt(LocalDateTime.now());
        classMemberRepository.save(member);

        Course course = courseRepository.findById(classInfo.getCourseId()).orElse(null);
        return classInfoToMap(classInfo, course);
    }

    @Transactional
    public Map<String, Object> joinCourse(String inviteCode) {
        CurrentUser currentUser = userService.getCurrentUser();

        Course course = courseRepository.findByInviteCodeIgnoreCase(inviteCode)
                .orElseThrow(() -> new BusinessException(404, "课程邀请码无效"));

        if (course.getDefaultClassId() == null) {
            throw new BusinessException(400, "当前课程尚未创建默认班级");
        }

        ClassInfo classInfo = getClassInfo(course.getDefaultClassId());
        if (!classMemberRepository.existsByClassIdAndUserId(classInfo.getId(), currentUser.getUserId())) {
            ClassMember member = new ClassMember();
            member.setClassId(classInfo.getId());
            member.setUserId(currentUser.getUserId());
            member.setRole(currentUser.getRole());
            member.setJoinedAt(LocalDateTime.now());
            classMemberRepository.save(member);
        }

        return classInfoToMap(classInfo, course);
    }

    public List<Map<String, Object>> getMyCourses() {
        CurrentUser currentUser = userService.getCurrentUser();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClassMember member : classMemberRepository.findByUserId(currentUser.getUserId())) {
            ClassInfo classInfo = classInfoRepository.findById(member.getClassId()).orElse(null);
            Course course = classInfo == null ? null : courseRepository.findById(classInfo.getCourseId()).orElse(null);
            if (classInfo == null || course == null) {
                continue;
            }
            result.add(classInfoToMap(classInfo, course));
        }
        result.sort(Comparator.comparing(item -> String.valueOf(item.get("courseName"))));
        return result;
    }

    public Map<String, Object> getClassDetail(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        ClassInfo classInfo = getClassInfo(classId);
        ensureMember(classId, currentUser.getUserId());
        Course course = courseRepository.findById(classInfo.getCourseId()).orElse(null);
        return classInfoToMap(classInfo, course);
    }

    @Transactional
    public void dismissClass(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        ClassInfo classInfo = getClassInfo(classId);
        ensureClassAdmin(classId, currentUser.getUserId());

        long classCount = classInfoRepository.countByCourseId(classInfo.getCourseId());
        if (classCount <= 1) {
            throw new BusinessException(400, "课程至少保留一个班级，如需移除整个课程请直接解散课程");
        }

        Course course = courseRepository.findById(classInfo.getCourseId()).orElse(null);
        if (course != null && classId.equals(course.getDefaultClassId())) {
            Long nextDefaultClassId = classInfoRepository.findByCourseId(classInfo.getCourseId()).stream()
                    .filter(item -> !item.getId().equals(classId))
                    .map(ClassInfo::getId)
                    .findFirst()
                    .orElse(null);
            course.setDefaultClassId(nextDefaultClassId);
            courseRepository.save(course);
        }

        removeClassData(classId);
    }

    @Transactional
    public void dismissCourse(Long courseId) {
        CurrentUser currentUser = userService.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(404, "课程不存在"));
        if (!course.getTeacherId().equals(currentUser.getUserId())) {
            throw new BusinessException(403, "仅课程管理员可解散课程");
        }

        for (ClassInfo classInfo : classInfoRepository.findByCourseId(courseId)) {
            Long classId = classInfo.getId();
            removeClassData(classId);
        }
        courseRepository.delete(course);
    }

    @Transactional
    public void leaveCourse(Long courseId) {
        CurrentUser currentUser = userService.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(404, "课程不存在"));
        if (course.getTeacherId().equals(currentUser.getUserId())) {
            throw new BusinessException(400, "课程管理员不能直接退出课程，请先解散课程");
        }

        List<Long> classIds = new ArrayList<>();
        for (ClassInfo classInfo : classInfoRepository.findByCourseId(courseId)) {
            if (classInfo.getCourseId().equals(courseId)) {
                classIds.add(classInfo.getId());
            }
        }

        boolean removed = false;
        for (Long classId : classIds) {
            if (classMemberRepository.findByClassIdAndUserId(classId, currentUser.getUserId()).isPresent()) {
                classMemberRepository.deleteByClassIdAndUserId(classId, currentUser.getUserId());
                removed = true;
            }
        }
        if (!removed) {
            throw new BusinessException(400, "你尚未加入该课程");
        }
    }

    public ClassInfo getClassInfo(Long classId) {
        return classInfoRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(404, "班级不存在"));
    }

    public void ensureMember(Long classId, Long userId) {
        if (!classMemberRepository.existsByClassIdAndUserId(classId, userId)) {
            throw new BusinessException(403, "当前用户不在该班级中");
        }
    }

    public void ensureClassAdmin(Long classId, Long userId) {
        ClassInfo classInfo = getClassInfo(classId);
        if (!classInfo.getTeacherId().equals(userId)) {
            throw new BusinessException(403, "仅班级管理员可执行该操作");
        }
    }

    private void removeClassData(Long classId) {
        ClassInfo classInfo = getClassInfo(classId);
        Long courseId = classInfo.getCourseId();
        List<Long> assignmentIds = new ArrayList<>();
        List<Long> signInTaskIds = new ArrayList<>();
        List<Long> examIds = new ArrayList<>();
        List<Assignment> assignments = assignmentRepository.findByClassId(classId);
        List<ResourceFile> resourceFiles = resourceFileRepository.findByClassIdOrderByUploadTimeDesc(classId);
        List<Submission> submissions = new ArrayList<>();

        assignments.forEach(item -> assignmentIds.add(item.getId()));
        signInTaskRepository.findByClassId(classId).forEach(item -> signInTaskIds.add(item.getId()));
        examRepository.findByClassIdOrderByPublishedAtDesc(classId).forEach(item -> examIds.add(item.getId()));
        for (Assignment assignment : assignments) {
            submissions.addAll(submissionRepository.findByAssignmentId(assignment.getId()));
        }

        for (Assignment assignment : assignments) {
            fileStorageService.delete("assignments", assignment.getAttachmentName());
        }
        for (Submission submission : submissions) {
            fileStorageService.delete("submissions", submission.getFileName());
        }
        for (ResourceFile resourceFile : resourceFiles) {
            fileStorageService.delete("resources", resourceFile.getFileName());
        }

        if (!assignmentIds.isEmpty()) {
            submissionRepository.deleteByAssignmentIdIn(assignmentIds);
        }
        if (!signInTaskIds.isEmpty()) {
            signInRecordRepository.deleteByTaskIdIn(signInTaskIds);
        }
        if (!examIds.isEmpty()) {
            List<Long> recordIds = examRecordRepository.findAll().stream()
                    .filter(item -> examIds.contains(item.getExamId()))
                    .map(item -> item.getId())
                    .collect(java.util.stream.Collectors.toList());
            if (!recordIds.isEmpty()) {
                studentAnswerRepository.deleteByRecordIdIn(recordIds);
            }
            examRecordRepository.deleteByExamIdIn(examIds);
            examQuestionRepository.deleteByExamIdIn(examIds);
        }
        signInRecordRepository.deleteByClassId(classId);
        assignmentRepository.deleteByClassId(classId);
        signInTaskRepository.deleteByClassId(classId);
        examRepository.deleteByClassId(classId);
        resourceFileRepository.deleteByClassId(classId);
        notificationMessageRepository.deleteByClassId(classId);
        classMemberRepository.deleteByClassId(classId);
        classInfoRepository.deleteById(classId);

        String classDirectory = buildClassStorageDirectory(courseId, classId);
        fileStorageService.deleteDirectory("assignments", classDirectory);
        fileStorageService.deleteDirectory("submissions", classDirectory);
        fileStorageService.deleteDirectory("resources", classDirectory);
    }

    private Map<String, Object> classInfoToMap(ClassInfo classInfo, Course course) {
        User teacher = userService.getById(classInfo.getTeacherId());
        Map<String, Object> result = new HashMap<>();
        result.put("classId", classInfo.getId());
        result.put("className", classInfo.getClassName());
        result.put("inviteCode", classInfo.getInviteCode());
        result.put("courseId", course == null ? null : course.getId());
        result.put("courseName", course == null ? null : course.getName());
        result.put("courseInviteCode", course == null ? null : course.getInviteCode());
        result.put("teacherId", teacher.getId());
        result.put("teacherName", teacher.getRealName());
        result.put("creatorId", teacher.getId());
        result.put("creatorName", teacher.getRealName());
        result.put("semester", "2026 春");
        return result;
    }

    private ClassInfo createClassInternal(Long courseId, String className, Long creatorId) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setCourseId(courseId);
        classInfo.setClassName(className);
        classInfo.setTeacherId(creatorId);
        classInfo.setInviteCode(generateUniqueClassInviteCode());
        classInfo = classInfoRepository.save(classInfo);

        ClassMember member = new ClassMember();
        member.setClassId(classInfo.getId());
        member.setUserId(creatorId);
        member.setRole(userService.getById(creatorId).getRole());
        member.setJoinedAt(LocalDateTime.now());
        classMemberRepository.save(member);
        return classInfo;
    }

    private String generateUniqueCourseInviteCode() {
        String inviteCode;
        boolean exists;
        do {
            inviteCode = InviteCodeUtil.generate();
            exists = courseRepository.findByInviteCodeIgnoreCase(inviteCode).isPresent();
        } while (exists);
        return inviteCode;
    }

    private String generateUniqueClassInviteCode() {
        String inviteCode;
        boolean exists;
        do {
            inviteCode = InviteCodeUtil.generate();
            exists = classInfoRepository.findByInviteCodeIgnoreCase(inviteCode).isPresent();
        } while (exists);
        return inviteCode;
    }

    private String buildClassStorageDirectory(Long courseId, Long classId) {
        return "course-" + courseId + "/class-" + classId;
    }
}
