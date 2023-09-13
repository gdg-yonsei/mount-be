package com.gdsc.mount.member.service;

import com.gdsc.mount.member.domain.Member;
import com.gdsc.mount.member.dto.MemberResponse;
import com.gdsc.mount.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberResponse createMember(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        memberRepository.save(new Member(username));
        return MemberResponse.builder()
                .username(username)
                .build();
    }

    public MemberResponse findMemberById(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No member exists with given id."));
        return MemberResponse.of(member);
    }

    public void deleteMemberById(String id) {
        memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No member exists with given id."));
        memberRepository.deleteById(id);
    }
}
