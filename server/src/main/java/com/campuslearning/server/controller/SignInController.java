package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.dto.CreateSignInTaskRequest;
import com.campuslearning.server.dto.SubmitSignInRequest;
import com.campuslearning.server.service.SignInService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 服务端签到控制器。
 */
@RestController
@RequestMapping("/api")
public class SignInController {

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    @PostMapping("/classes/{classId}/signin")
    public ApiResponse<Map<String, Object>> createTask(@PathVariable Long classId,
                                                       @Validated @RequestBody CreateSignInTaskRequest request) {
        return ApiResponse.success(signInService.createTask(
                classId,
                request.getTitle(),
                request.getType(),
                request.getLocationLat(),
                request.getLocationLng(),
                request.getRadius(),
                request.getDeadline()
        ));
    }

    @GetMapping("/classes/{classId}/active-signin")
    public ApiResponse<Map<String, Object>> getActiveTask(@PathVariable Long classId) {
        return ApiResponse.success(signInService.getActiveTask(classId));
    }

    @PostMapping("/signin/submit")
    public ApiResponse<Map<String, Object>> submit(@Validated @RequestBody SubmitSignInRequest request) {
        return ApiResponse.success(signInService.submit(request.getTaskId(), request.getLatitude(), request.getLongitude()));
    }
}
