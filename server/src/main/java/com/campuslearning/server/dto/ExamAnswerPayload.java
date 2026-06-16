package com.campuslearning.server.dto;

import javax.validation.constraints.NotNull;

public class ExamAnswerPayload {

    @NotNull(message = "题目 ID 不能为空")
    private Long questionId;

    private String answer;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
