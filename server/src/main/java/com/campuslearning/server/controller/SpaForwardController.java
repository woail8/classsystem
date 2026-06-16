package com.campuslearning.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务端前端路由转发控制器。
 * 让 Spring Boot 在同一个端口下托管前端 SPA 页面，用户机只需访问服务机局域网地址即可。
 */
@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/login",
            "/courses",
            "/notifications",
            "/profile",
            "/classes/{classId}",
            "/classes/{classId}/signin",
            "/classes/{classId}/assignments",
            "/classes/{classId}/exams",
            "/classes/{classId}/resources",
            "/assignments/{assignmentId}/submit",
            "/exams/{examId}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
