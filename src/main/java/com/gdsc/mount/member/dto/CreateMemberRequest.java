package com.gdsc.mount.member.dto;

import lombok.Getter;

@Getter
public class CreateMemberRequest {
    private String username;
    protected CreateMemberRequest() {};
    public CreateMemberRequest(String username) {
        this.username = username;
    }
}
