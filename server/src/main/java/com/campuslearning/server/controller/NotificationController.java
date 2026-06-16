package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.dto.PublishNotificationRequest;
import com.campuslearning.server.service.NotificationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 服务端通知控制器。
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> publish(@Validated @RequestBody PublishNotificationRequest request) {
        return ApiResponse.success(notificationService.publish(request.getClassId(), request.getTitle(), request.getContent()));
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listMine() {
        return ApiResponse.success(notificationService.listMine());
    }
}
