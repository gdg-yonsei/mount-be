package gdsc.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class FileMetaData {

    @Id @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    private String uuid;
    private String userId;
    private String fileName;     // 작성자가 업로드한 파일명
    private String saveFileName; // 서버에 저장되는 파일명
    private Long fileSize;
    private LocalDateTime uploadDate;
    private LocalDateTime deleteDate;
}
