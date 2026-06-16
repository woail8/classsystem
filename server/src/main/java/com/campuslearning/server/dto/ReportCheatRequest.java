package com.campuslearning.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReportCheatRequest {

    @NotNull(message = "考试 ID 不能为空")
    private Long examId;

    @NotBlank(message = "作弊行为不能为空")
    private String action;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
