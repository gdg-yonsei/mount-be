package com.gdsc.mount;

import com.gdsc.mount.member.domain.Member;
import com.gdsc.mount.member.repository.MemberRepository;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import com.gdsc.mount.metadata.service.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private MetadataRepository metadataRepository;

    @Qualifier("webApplicationContext")
    @Autowired
    ResourceLoader loader;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        String username = "becooq81";
        memberRepository.save(new Member(username));

    }

    // 파일 업로드
    @Test
    @DisplayName("파일 업로드")
    public void upload_file() throws Exception {
        Resource res = loader.getResource("uploads/3조_프로젝트_1.pdf");
        MockMultipartFile sampleFile = new MockMultipartFile(
                "file", // The part name should match the server's expected parameter name
                "filename",
                "text/plain",
                "This is the file content".getBytes()
        );
        RequestBuilder req = MockMvcRequestBuilders
                .multipart("/api/file/upload")
                .file(sampleFile)
                .param("username", "becooq81");
        ResultActions actions = mockMvc.perform(req);
        actions.andExpect(status().isCreated());
    }
}
