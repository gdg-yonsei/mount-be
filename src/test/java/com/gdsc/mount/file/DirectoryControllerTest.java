package com.gdsc.mount.file;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.dto.DirectoryUpdateRequest;
import com.gdsc.mount.member.domain.Member;
import com.gdsc.mount.member.repository.MemberRepository;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import java.nio.charset.StandardCharsets;
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
import org.springframework.mock.web.MockMultipartFile;
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
    private MetadataRepository metadataRepository;

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
                .content(asJsonString(new DirectoryCreateRequest("/test/", "becooq81")))
        ).andExpect(status().isOk());
        Assertions.assertTrue(metadataRepository.existsByPathWithFile("/test/"));
    }

    @Test
    @DisplayName("디렉토리 이름 변경")
    public void rename_directory() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "file", "text/plain", "test file".getBytes(
                StandardCharsets.UTF_8));
        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/file/upload")
                        .file(multipartFile1)
                        .param("username", "becooq81")
                        .param("path", "/helloWorld/bye/")
        ).andExpect(status().is2xxSuccessful());
        Assertions.assertTrue(metadataRepository.existsByPathWithFile("/helloWorld/bye/"));

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/directory/rename")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new DirectoryUpdateRequest("/helloWorld/bye/", "newName", "becooq81")))
        ).andExpect(status().is2xxSuccessful());
        Assertions.assertFalse(metadataRepository.existsByPathWithFile("/helloWorld/bye/"));
        Assertions.assertTrue(metadataRepository.existsByPathWithFile("/helloWorld/newName/"));

    }

    @Test
    @DisplayName("디렉토리 내부 컨텐츠 조회")
    public void get_directory_contents() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "file", "text/plain", "test file".getBytes(
                StandardCharsets.UTF_8));
        for (int i = 0; i < 5; i ++) {
            mockMvc.perform(
                    MockMvcRequestBuilders.multipart("/api/file/upload")
                            .file(multipartFile1)
                            .param("username", "becooq81")
                            .param("path", "/test100/bye/")
            ).andExpect(status().is2xxSuccessful());
        }
        for (int i = 0; i < 5; i ++) {
            DirectoryCreateRequest request = new DirectoryCreateRequest("/test100/bye/" + i + "/", "becooq81");
            mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/directory/new")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(request))
            ).andExpect(status().isOk());
        }
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/directory/contents")
                        .param("path", "/test100/bye/")
                        .param("page", "0")
        ).andExpect(status().is2xxSuccessful())
                .andDo(mvcResult -> {
                    String content = mvcResult.getResponse().getContentAsString();
                    System.out.println(content);
                });
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
