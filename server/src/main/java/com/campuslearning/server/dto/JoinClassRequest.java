package com.campuslearning.server.dto;

import javax.validation.constraints.NotBlank;

/**
 * 服务端学生通过邀请码加入班级请求。
 */
public class JoinClassRequest {

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
