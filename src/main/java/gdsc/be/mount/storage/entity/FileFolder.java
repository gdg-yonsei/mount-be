package gdsc.be.mount.storage.entity;

import gdsc.be.mount.storage.Enum.FileFolderType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private FileFolderType fileFolderType;

    private Long parentId; // 부모 폴더 id
    
    private Long childId; // 자식 폴더 id

    @NotBlank
    private String originalName; // 사용자가 업로드한 파일명

    @NotBlank
    private String storedName; // 서버 내부에서 관리할 파일명 (충돌 방지)

    @NotBlank
    private String path;

    private Long size;

    private String contentType;

    @NotNull
    private LocalDateTime uploadTime;

    @NotBlank
    private String userName; // 사용자
}
