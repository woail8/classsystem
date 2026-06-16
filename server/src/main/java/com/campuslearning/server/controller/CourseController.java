package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.dto.CreateCourseRequest;
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

import java.util.List;
import java.util.Map;

/**
 * 服务端课程控制器。
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createCourse(@Validated @RequestBody CreateCourseRequest request) {
        return ApiResponse.success(courseService.createCourse(request.getName()));
    }

    @PostMapping("/join")
    public ApiResponse<Map<String, Object>> joinCourse(@Validated @RequestBody JoinClassRequest request) {
        return ApiResponse.success(courseService.joinCourse(request.getInviteCode()));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyCourses() {
        return ApiResponse.success(courseService.getMyCourses());
    }

    @PostMapping("/{courseId}/leave")
    public ApiResponse<Void> leaveCourse(@PathVariable Long courseId) {
        courseService.leaveCourse(courseId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse<Void> dismissCourse(@PathVariable Long courseId) {
        courseService.dismissCourse(courseId);
        return ApiResponse.success(null);
    }
}
