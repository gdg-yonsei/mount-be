package com.gdsc.mount.member;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdsc.mount.member.dto.CreateMemberRequest;
import com.gdsc.mount.member.repository.MemberRepository;
import com.gdsc.mount.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
public class MemberControllerTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("회원 가입")
    void register_member() throws Exception {
        String username = "test";
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/member/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new CreateMemberRequest(username)))
        ).andExpect(status().is2xxSuccessful());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
