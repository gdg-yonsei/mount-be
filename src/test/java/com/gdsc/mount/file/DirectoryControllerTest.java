package com.gdsc.mount.file;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.repository.DirectoryRepository;
import com.gdsc.mount.member.domain.Member;
import com.gdsc.mount.member.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
public class DirectoryControllerTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        String username = "becooq81";
        memberRepository.save(new Member(username));
    }

    @Test
    @DisplayName("디렉토리 생성")
    public void create_directory() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/directory/new")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new DirectoryCreateRequest("/test/", "username")))
        ).andExpect(status().isOk());
        Assertions.assertTrue(directoryRepository.existsByPathIncludingDirectory("/test/"));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
