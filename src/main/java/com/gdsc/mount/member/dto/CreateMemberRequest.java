package com.gdsc.mount.member.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateMemberRequest {

    @NotBlank
    private String username;
    protected CreateMemberRequest() {};
    public CreateMemberRequest(String username) {
        this.username = username;
    }
}
