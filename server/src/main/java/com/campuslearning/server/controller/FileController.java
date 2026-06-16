package com.campuslearning.server.controller;

import com.campuslearning.server.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务端文件下载控制器。
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadByQuery(@RequestParam String category, @RequestParam("path") String filePath) {
        return buildDownloadResponse(fileStorageService.loadAsResource(category, filePath));
    }

    @GetMapping("/{category}/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String category, @PathVariable String fileName) {
        return buildDownloadResponse(fileStorageService.loadAsResource(category, fileName));
    }

    private ResponseEntity<Resource> buildDownloadResponse(Resource resource) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
