package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
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
public class FileFolderRepositoryTest {
    @Autowired
    private FileFolderRepository fileFolderRepository;

    @Test
    @DisplayName("FileFolder Repository save 테스트")
    public void testSaveFile() {
        // Given
        FileFolder fileFolder = createFileEntity();

        // When
        FileFolder savedFileFolder = fileFolderRepository.save(fileFolder);

        // Then
        assertThat(savedFileFolder.getId()).isNotNull();
        assertThat(savedFileFolder.getOriginalName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("FileFolder Repository findById 테스트")
    public void testFindFileById() {
        // Given
        FileFolder fileFolder = createFileEntity();
        FileFolder savedFileFolder = fileFolderRepository.save(fileFolder);

        // When
        FileFolder foundFileFolder = fileFolderRepository.findById(savedFileFolder.getId()).orElse(null);

        // Then
        assertThat(foundFileFolder).isNotNull();
        assertThat(foundFileFolder.getOriginalName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("FileFolder Repository deleteById 테스트")
    public void testDeleteFileById() {
        // Given
        FileFolder fileFolder = createFileEntity();
        FileFolder savedFileFolder = fileFolderRepository.save(fileFolder);

        // When
        fileFolderRepository.deleteById(savedFileFolder.getId());

        // Then
        FileFolder foundFileFolder = fileFolderRepository.findById(savedFileFolder.getId()).orElse(null);
        assertThat(foundFileFolder).isNull();
    }

    public FileFolder createFileEntity(){
        return FileFolder.builder()
                .fileFolderType(FileFolderType.FILE)
                .originalName("test.txt")
                .storedName("RANDOMRANDOM")
                .path("/file/path")
                .size(300L)
                .uploadTime(LocalDateTime.of(2023, 9, 3, 2, 1))
                .userName("testUser")
                .build();
    }
}
