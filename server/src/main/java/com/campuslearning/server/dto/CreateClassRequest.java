package com.campuslearning.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 服务端创建班级请求。
 */
public class CreateClassRequest {

    @NotNull(message = "课程 ID 不能为空")
    private Long courseId;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
