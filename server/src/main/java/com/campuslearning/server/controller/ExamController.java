package com.campuslearning.server.controller;

import com.campuslearning.server.common.ApiResponse;
import com.campuslearning.server.dto.CreateExamRequest;
import com.campuslearning.server.dto.ReportCheatRequest;
import com.campuslearning.server.dto.SubmitExamRequest;
import com.campuslearning.server.service.ExamService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/presets")
    public ApiResponse<List<Map<String, Object>>> listPresets() {
        return ApiResponse.success(examService.listPresets());
    }

    @PostMapping("/classes/{classId}")
    public ApiResponse<Map<String, Object>> createExam(@PathVariable Long classId,
                                                       @Validated @RequestBody CreateExamRequest request) {
        return ApiResponse.success(examService.createExam(
                classId,
                request.getTitle(),
                request.getPresetKey(),
                request.getDuration(),
                request.getStartTime(),
                request.getEndTime()
        ));
    }

    @GetMapping("/{classId}/active")
    public ApiResponse<List<Map<String, Object>>> listByClass(@PathVariable Long classId) {
        return ApiResponse.success(examService.listExamsByClass(classId));
    }

    @PostMapping("/{examId}/start")
    public ApiResponse<Map<String, Object>> start(@PathVariable Long examId) {
        return ApiResponse.success(examService.startExam(examId));
    }

    @PostMapping("/{examId}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long examId,
                                                   @Validated @RequestBody SubmitExamRequest request) {
        return ApiResponse.success(examService.submitExam(examId, request.getAnswers()));
    }

    @PostMapping("/cheat")
    public ApiResponse<Map<String, Object>> reportCheat(@Validated @RequestBody ReportCheatRequest request) {
        return ApiResponse.success(examService.reportCheat(request.getExamId(), request.getAction()));
    }

    @GetMapping("/{examId}/records")
    public ApiResponse<List<Map<String, Object>>> listRecords(@PathVariable Long examId) {
        return ApiResponse.success(examService.listExamRecords(examId));
    }
}
