package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.File;
import gdsc.be.mount.storage.repository.FileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileServiceImpl fileService;


    // ================ 이후 테스트 고도화 작업 필요
    @Test
    @DisplayName("파일 upload 테스트")
    public void testUploadFile() throws IOException {
        // Given
        String userName = "testUser";
        String originalFileName = "test.txt";

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file", originalFileName, "text/plain", "Test data".getBytes());

        when(fileRepository.save(any())).thenReturn(createFileEntity());

        // When
        FileUploadResponse response = fileService.uploadFile(mockMultipartFile, userName);

        // Then
        verify(fileRepository, times(1)).save(any());

        assertNotNull(response);
        assertEquals(originalFileName, response.getOriginalFileName());
    }

    public File createFileEntity(){
        return File.builder()
                .originalFileName("test.txt")
                .storeFileName("Test data")
                .fileType("text/plain")
                .userName("testUser")
                .build();
    }
}
