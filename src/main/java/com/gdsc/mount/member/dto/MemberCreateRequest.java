package com.gdsc.mount.member.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberCreateRequest {

    @NotBlank
    private String username;
    protected MemberCreateRequest() {};
    public MemberCreateRequest(String username) {
        this.username = username;
    }
}
