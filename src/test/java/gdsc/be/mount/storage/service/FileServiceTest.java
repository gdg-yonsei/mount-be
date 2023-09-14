package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.entity.File;
import gdsc.be.mount.storage.repository.FileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service 테스트")
public class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;


    // ================ 이후 테스트 고도화 작업 필요
    @Test
    @DisplayName("File upload 테스트")
    public void testUploadFile() {
        // Given
        String userName = "testUser";
        String originalFileName = "test.txt";
        String contentType = "text/plain";
        byte[] content = "This is a test file content".getBytes();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", originalFileName, contentType, content);
        File file = createFileEntity();

        when(fileRepository.save(any())).thenReturn(file);

        // 파일 시스템 작업 모킹 필요
        // Mock Files.transfer method to simulate file transfer
        // doNothing().when(file).transferTo(any(java.io.File.class));

        // When
        FileUploadResponse response = fileService.uploadFile(multipartFile, userName);

        // Then
        assertNotNull(response);
        assertEquals(originalFileName, response.originalFileName());

        verify(fileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("File delete 테스트")
    public void testDeleteFile() {
        // 파일 시스템 작업 모킹 필요
        // Mock Files.delete method to simulate file deletion
    }

    @Test
    public void testDownloadFile() {
        // Given
        Long fileId = 1L;
        String userName = "testUser";

        File file = createFileEntity();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // When
        FileDownloadResponse response = fileService.downloadFile(fileId, userName);

        // Then
        assertNotNull(response);
        assertNotNull(response.urlResource());
        assertNotNull(response.contentDisposition());

        assertTrue(response.urlResource() instanceof UrlResource);
        assertTrue(response.contentDisposition().contains(file.getOriginalFileName()));
    }

    public File createFileEntity(){
        return File.builder()
            .id(1L)
            .originalFileName("test.txt")
            .storeFileName("RANDOMRANDOM")
            .filePath("/file/path")
            .fileSize(300L)
            .fileType("text/plain")
            .uploadTime(LocalDateTime.of(2023, 9, 3, 2, 1))
            .userName("testUser")
            .build();
    }
}
