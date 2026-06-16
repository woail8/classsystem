package com.campuslearning.server.dto;

import javax.validation.constraints.NotNull;

/**
 * 服务端提交签到请求。
 */
public class SubmitSignInRequest {

    @NotNull(message = "签到任务 ID 不能为空")
    private Long taskId;

    private Double latitude;
    private Double longitude;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
