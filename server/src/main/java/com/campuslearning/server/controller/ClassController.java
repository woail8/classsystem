package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.dto.CreateClassRequest;
import com.campuslearning.server.dto.JoinClassRequest;
import com.campuslearning.server.service.CourseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 服务端班级控制器。
 */
@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final CourseService courseService;

    public ClassController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createClass(@Validated @RequestBody CreateClassRequest request) {
        return ApiResponse.success(courseService.createClass(request.getCourseId(), request.getClassName()));
    }

    @PostMapping("/join")
    public ApiResponse<Map<String, Object>> joinClass(@Validated @RequestBody JoinClassRequest request) {
        return ApiResponse.success(courseService.joinClass(request.getInviteCode()));
    }

    @PostMapping("/{classId}/invite-code/regenerate")
    public ApiResponse<Map<String, Object>> regenerateInviteCode(@PathVariable Long classId) {
        return ApiResponse.success(courseService.regenerateInviteCode(classId));
    }

    @GetMapping("/{classId}")
    public ApiResponse<Map<String, Object>> getClassDetail(@PathVariable Long classId) {
        return ApiResponse.success(courseService.getClassDetail(classId));
    }

    @DeleteMapping("/{classId}")
    public ApiResponse<Void> dismissClass(@PathVariable Long classId) {
        courseService.dismissClass(classId);
        return ApiResponse.success(null);
    }
}
