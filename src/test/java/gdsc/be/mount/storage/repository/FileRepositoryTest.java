package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.entity.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Repository 테스트 (JPA CRUD)")
public class FileRepositoryTest {
    @Autowired
    private FileRepository fileRepository;

    @Test
    @DisplayName("File Repository save 테스트")
    public void testSaveFile() {
        // Given
        File file = createFileEntity();

        // When
        File savedFile = fileRepository.save(file);

        // Then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("File Repository findById 테스트")
    public void testFindFileById() {
        // Given
        File file = createFileEntity();
        File savedFile = fileRepository.save(file);

        // When
        File foundFile = fileRepository.findById(savedFile.getId()).orElse(null);

        // Then
        assertThat(foundFile).isNotNull();
        assertThat(foundFile.getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("File Repository deleteById 테스트")
    public void testDeleteFileById() {
        // Given
        File file = createFileEntity();
        File savedFile = fileRepository.save(file);

        // When
        fileRepository.deleteById(savedFile.getId());

        // Then
        File foundFile = fileRepository.findById(savedFile.getId()).orElse(null);
        assertThat(foundFile).isNull();
    }

    public File createFileEntity(){
        return File.builder()
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
