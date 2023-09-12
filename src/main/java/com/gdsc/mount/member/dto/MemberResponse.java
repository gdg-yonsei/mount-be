package com.gdsc.mount.member.dto;

import com.gdsc.mount.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private String memberId;
    private String username;
    protected MemberResponse() {};

    public MemberResponse(String memberId, String username) {
        this.memberId = memberId;
        this.username = username;
    }
    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .memberId(member.get_id())
                .username(member.getUsername())
                .build();
    }
}
