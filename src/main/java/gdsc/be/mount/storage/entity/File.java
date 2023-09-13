package gdsc.be.mount.storage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName; // 사용자가 업로드한 파일명
    private String storeFileName; // 서버 내부에서 관리할 파일명 (충돌 방지)

    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
    private String userName; // 사용자

}
