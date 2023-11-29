package com.gdsc.mount.member.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCreateRequest {
    @NotBlank
    @ValidName
    private String username;
}
