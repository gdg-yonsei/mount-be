package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.entity.File;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileUploadRequest {
    private String originalFileName;
    private String storeFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadTime;
    private String userName;

    public File toEntity(){
        return File.builder()
                .originalFileName(originalFileName)
                .storeFileName(storeFileName)
                .filePath(filePath)
                .fileSize(fileSize)
                .fileType(fileType)
                .uploadTime(uploadTime)
                .userName(userName)
                .build();
    }
}