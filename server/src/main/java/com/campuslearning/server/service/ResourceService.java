package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.model.ClassInfo;
import com.campuslearning.server.model.ResourceFile;
import com.campuslearning.server.repository.ResourceFileRepository;
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
 * 服务端资料库业务服务。
 */
@Service
public class ResourceService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserService userService;
    private final CourseService courseService;
    private final FileStorageService fileStorageService;
    private final ResourceFileRepository resourceFileRepository;

    public ResourceService(UserService userService,
                           CourseService courseService,
                           FileStorageService fileStorageService,
                           ResourceFileRepository resourceFileRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.fileStorageService = fileStorageService;
        this.resourceFileRepository = resourceFileRepository;
    }

    @Transactional
    public Map<String, Object> upload(Long classId, String title, MultipartFile file) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureClassAdmin(classId, currentUser.getUserId());
        ClassInfo classInfo = courseService.getClassInfo(classId);

        FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.store(
                "resources",
                buildClassStorageDirectory(classInfo.getCourseId(), classId),
                file
        );

        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setClassId(classId);
        resourceFile.setTeacherId(currentUser.getUserId());
        resourceFile.setTitle(title);
        resourceFile.setFileName(storedFileInfo.getStoredName());
        resourceFile.setOriginalFileName(storedFileInfo.getOriginalName());
        resourceFile.setUploadTime(LocalDateTime.now());
        resourceFile = resourceFileRepository.save(resourceFile);
        return toMap(resourceFile);
    }

    public List<Map<String, Object>> listByClass(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureMember(classId, currentUser.getUserId());

        List<Map<String, Object>> result = new ArrayList<>();
        resourceFileRepository.findByClassIdOrderByUploadTimeDesc(classId)
                .forEach(item -> result.add(toMap(item)));
        return result;
    }

    private Map<String, Object> toMap(ResourceFile resourceFile) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", resourceFile.getId());
        result.put("classId", resourceFile.getClassId());
        result.put("title", resourceFile.getTitle());
        result.put("fileName", resourceFile.getOriginalFileName());
        result.put("uploadTime", resourceFile.getUploadTime().format(FORMATTER));
        result.put("downloadUrl", buildDownloadUrl("resources", resourceFile.getFileName()));
        return result;
    }

    private String buildClassStorageDirectory(Long courseId, Long classId) {
        return "course-" + courseId + "/class-" + classId;
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
