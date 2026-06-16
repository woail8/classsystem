package com.campuslearning.server.dto;

import javax.validation.constraints.NotBlank;

/**
 * 服务端创建课程请求。
 */
public class CreateCourseRequest {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
