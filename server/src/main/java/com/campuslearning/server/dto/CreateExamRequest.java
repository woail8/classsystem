package com.campuslearning.server.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateExamRequest {

    @NotBlank(message = "考试标题不能为空")
    private String title;

    @NotBlank(message = "题卷模板不能为空")
    private String presetKey;

    @NotNull(message = "考试时长不能为空")
    @Min(value = 5, message = "考试时长不能少于 5 分钟")
    private Integer duration;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPresetKey() {
        return presetKey;
    }

    public void setPresetKey(String presetKey) {
        this.presetKey = presetKey;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
