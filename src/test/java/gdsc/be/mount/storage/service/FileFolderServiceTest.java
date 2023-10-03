package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service 테스트")
public class FileFolderServiceTest {

    @Mock
    private FileFolderRepository fileFolderRepository;

    @InjectMocks
    private FileFolderService fileFolderService;

    // ================ 이후 테스트 고도화 작업 필요
    @Test
    @DisplayName("FileFolder upload 테스트")
    public void testUploadFile() {
        // Given
        String userName = "testUser";
        String originalFileName = "test.txt";
        String contentType = "text/plain";
        byte[] content = "This is a test fileFolder content".getBytes();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "fileFolder", originalFileName, contentType, content);
        FileFolder fileFolder = createFileEntity();
        FileUploadRequest fileUploadRequest = FileUploadRequest.builder()
                .parentId(null)
                .userName(userName)
                .build();

        when(fileFolderRepository.save(any())).thenReturn(fileFolder);

        // 파일 시스템 작업 모킹 필요
        // Mock Files.transfer method to simulate fileFolder transfer
        // doNothing().when(fileFolder).transferTo(any(java.io.FileFolder.class));

        // When
        FileUploadResponse response = fileFolderService.uploadFile(multipartFile, fileUploadRequest);

        // Then
        assertNotNull(response);
        assertEquals(originalFileName, response.originalFileName());

        verify(fileFolderRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("FileFolder delete 테스트")
    public void testDeleteFile() {
        // 파일 시스템 작업 모킹 필요
        // Mock Files.delete method to simulate file deletion
    }

    @Test
    @DisplayName("FileFolder download 테스트")
    public void testDownloadFile() throws IOException {
        // Given
        Long fileId = 1L;
        String userName = "testUser";

        FileFolder fileFolder = createFileEntity();

        // When
        when(fileFolderRepository.findById(fileId)).thenReturn(Optional.of(fileFolder));

        when(fileFolderService.getResource(fileFolder.getPath()))
                .thenReturn(new UrlResource("file://" + fileFolder.getPath()));

        // When
        FileDownloadResponse response = fileFolderService.downloadFile(fileId, userName);

        // Then
        assertNotNull(response);
        assertNotNull(response.urlResource());
        assertNotNull(response.contentDisposition());

        assertTrue(response.urlResource() instanceof UrlResource);
        assertTrue(response.contentDisposition().contains(fileFolder.getOriginalName()));
    }

    public FileFolder createFileEntity(){
        return FileFolder.builder()
            .fileFolderType(FileFolderType.FILE)
            .parentId(null)
            .childIds(null)
            .id(1L)
            .originalName("test.txt")
            .storedName("RANDOMRANDOM")
            .path("/file/path")
            .size(300L)
            .uploadTime(LocalDateTime.of(2023, 9, 3, 2, 1))
            .userName("testUser")
            .build();
    }
}
