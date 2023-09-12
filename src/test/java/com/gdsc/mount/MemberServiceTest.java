package com.gdsc.mount.service;

import com.gdsc.mount.member.domain.Member;
import com.gdsc.mount.member.repository.MemberRepository;
import com.gdsc.mount.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureDataMongo
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    public void init() {
        memberRepository.save(new Member("becooq81"));
    }
    // 회원 등록
    @Test
    @DisplayName("회원 가입")
    public void register_member() {
        String username = "user";
        memberService.createMember(username);
        Optional<Member> result = memberRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(result.get().getUsername(), username);
    }

    // 회원 삭제
    @Test
    @DisplayName("회원 삭제")
    public void delete_member() {
        String username = "becooq81";
        Optional<Member> result = memberRepository.findByUsername(username);
        assertTrue(result.isPresent());
        memberService.deleteMemberById(result.get().get_id());
        Optional<Member> afterResult = memberRepository.findByUsername(username);
        assertFalse(afterResult.isPresent());
    }

}
