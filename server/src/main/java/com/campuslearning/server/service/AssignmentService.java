package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.Assignment;
import com.campuslearning.server.model.ClassInfo;
import com.campuslearning.server.model.Submission;
import com.campuslearning.server.repository.AssignmentRepository;
import com.campuslearning.server.repository.SubmissionRepository;
import com.campuslearning.server.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端作业业务服务。
 */
@Service
public class AssignmentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserService userService;
    private final CourseService courseService;
    private final FileStorageService fileStorageService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(UserService userService,
                             CourseService courseService,
                             FileStorageService fileStorageService,
                             AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.fileStorageService = fileStorageService;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public Map<String, Object> publish(Long classId, String title, String content, String deadlineText, MultipartFile file) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureClassAdmin(classId, currentUser.getUserId());
        ClassInfo classInfo = courseService.getClassInfo(classId);

        Assignment assignment = new Assignment();
        assignment.setClassId(classId);
        assignment.setTeacherId(currentUser.getUserId());
        assignment.setTitle(title);
        assignment.setContent(content);
        assignment.setDeadline(LocalDateTime.parse(deadlineText, FORMATTER));
        assignment.setCreatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.store(
                    "assignments",
                    buildClassStorageDirectory(classInfo.getCourseId(), classId),
                    file
            );
            assignment.setAttachmentName(storedFileInfo.getStoredName());
            assignment.setAttachmentOriginalName(storedFileInfo.getOriginalName());
        }

        assignment = assignmentRepository.save(assignment);
        return toMap(assignment, null);
    }

    public List<Map<String, Object>> listByClass(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureMember(classId, currentUser.getUserId());

        List<Map<String, Object>> result = new ArrayList<>();
        assignmentRepository.findByClassIdOrderByCreatedAtDesc(classId)
                .forEach(item -> result.add(toMap(item, findSubmission(item.getId(), currentUser.getUserId()))));
        return result;
    }

    public Map<String, Object> detail(Long assignmentId) {
        Assignment assignment = getAssignment(assignmentId);
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureMember(assignment.getClassId(), currentUser.getUserId());
        return toMap(assignment, findSubmission(assignmentId, currentUser.getUserId()));
    }

    @Transactional
    public Map<String, Object> submit(Long assignmentId, MultipartFile file, String remark) {
        CurrentUser currentUser = userService.getCurrentUser();
        Assignment assignment = getAssignment(assignmentId);
        courseService.ensureMember(assignment.getClassId(), currentUser.getUserId());
        ClassInfo classInfo = courseService.getClassInfo(assignment.getClassId());

        Submission submission = findSubmission(assignmentId, currentUser.getUserId());
        if (submission == null) {
            submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(currentUser.getUserId());
        }

        if (file != null && !file.isEmpty()) {
            if (submission.getFileName() != null) {
                fileStorageService.delete("submissions", submission.getFileName());
            }
            FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.store(
                    "submissions",
                    buildSubmissionStorageDirectory(classInfo.getCourseId(), assignment.getClassId(), assignmentId),
                    file
            );
            submission.setFileName(storedFileInfo.getStoredName());
            submission.setOriginalFileName(storedFileInfo.getOriginalName());
        }
        submission.setRemark(remark);
        submission.setSubmittedAt(LocalDateTime.now());
        submission = submissionRepository.save(submission);

        Map<String, Object> result = new HashMap<>();
        result.put("submissionId", submission.getId());
        result.put("message", "作业提交成功");
        result.put("submittedAt", submission.getSubmittedAt().format(FORMATTER));
        return result;
    }

    public Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(404, "作业不存在"));
    }

    public Submission findSubmission(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).orElse(null);
    }

    private Map<String, Object> toMap(Assignment assignment, Submission submission) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", assignment.getId());
        result.put("classId", assignment.getClassId());
        result.put("title", assignment.getTitle());
        result.put("content", assignment.getContent());
        result.put("deadline", assignment.getDeadline().format(FORMATTER));
        result.put("attachmentUrl", assignment.getAttachmentName() == null
                ? null
                : buildDownloadUrl("assignments", assignment.getAttachmentName()));
        result.put("attachmentOriginalName", assignment.getAttachmentOriginalName());
        result.put("submitStatus", submission == null ? "未提交" : "已提交");
        return result;
    }

    private String buildClassStorageDirectory(Long courseId, Long classId) {
        return "course-" + courseId + "/class-" + classId;
    }

    private String buildSubmissionStorageDirectory(Long courseId, Long classId, Long assignmentId) {
        return buildClassStorageDirectory(courseId, classId) + "/assignment-" + assignmentId;
    }

    private String buildDownloadUrl(String category, String storedPath) {
        try {
            return "/api/files/download?category=" + URLEncoder.encode(category, "UTF-8")
                    + "&path=" + URLEncoder.encode(storedPath, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new BusinessException(500, "文件地址生成失败");
        }
    }
}
