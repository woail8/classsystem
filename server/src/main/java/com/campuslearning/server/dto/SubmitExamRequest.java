package com.campuslearning.server.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SubmitExamRequest {

    @Valid
    @NotNull(message = "答案列表不能为空")
    private List<ExamAnswerPayload> answers;

    public List<ExamAnswerPayload> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswerPayload> answers) {
        this.answers = answers;
    }
}
