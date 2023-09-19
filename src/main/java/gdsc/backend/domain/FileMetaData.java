package gdsc.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class FileMetaData {

    @Id @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    private String userId;
    private String fileName;     // 작성자가 업로드한 파일명
    private String saveFileName; // 서버에 저장되는 파일명
    private Long fileSize;
    private LocalDateTime uploadDate;
    private LocalDateTime deleteDate;
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    private Folder parentFolder;

    public FileMetaData() {
    }

    public FileMetaData(String userId, String fileName, String saveFileName, Long fileSize, LocalDateTime uploadDate, LocalDateTime deleteDate, String filePath, Folder parentFolder) {
        this.userId = userId;
        this.fileName = fileName;
        this.saveFileName = saveFileName;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.deleteDate = deleteDate;
        this.filePath = filePath;
        this.parentFolder = parentFolder;
    }

    public void deleteFile() {
        this.deleteDate = LocalDateTime.now();
    }
}
