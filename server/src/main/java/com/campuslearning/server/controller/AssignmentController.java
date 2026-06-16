package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.service.AssignmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 服务端作业控制器。
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> publish(@RequestParam Long classId,
                                                    @RequestParam String title,
                                                    @RequestParam(required = false) String content,
                                                    @RequestParam String deadline,
                                                    @RequestParam(required = false) MultipartFile file) {
        return ApiResponse.success(assignmentService.publish(classId, title, content, deadline, file));
    }

    @GetMapping("/{classId}")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long classId) {
        return ApiResponse.success(assignmentService.listByClass(classId));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.success(assignmentService.detail(id));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long id,
                                                   @RequestParam MultipartFile file,
                                                   @RequestParam(required = false) String remark) {
        return ApiResponse.success(assignmentService.submit(id, file, remark));
    }
}
