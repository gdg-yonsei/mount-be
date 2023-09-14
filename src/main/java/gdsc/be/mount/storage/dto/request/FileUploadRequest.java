package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.entity.File;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileUploadRequest(
        String originalFileName,
        String storeFileName,
        String filePath,
        Long fileSize,
        String fileType,
        LocalDateTime uploadTime,
        String userName
) {
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