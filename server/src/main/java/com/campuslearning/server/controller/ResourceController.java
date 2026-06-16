package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.service.ResourceService;
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
 * 服务端资料库控制器。
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> upload(@RequestParam Long classId,
                                                   @RequestParam String title,
                                                   @RequestParam MultipartFile file) {
        return ApiResponse.success(resourceService.upload(classId, title, file));
    }

    @GetMapping("/{classId}")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long classId) {
        return ApiResponse.success(resourceService.listByClass(classId));
    }
}
