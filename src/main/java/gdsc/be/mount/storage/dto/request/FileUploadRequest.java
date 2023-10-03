package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileUploadRequest(
        Long parentId, // File 은 childId 가 없음
        @NotBlank
        String userName
) {
    public FileFolder toEntity(String originalFileName, String storeFileName, String logicalFilePath, long fileSize, String fileType){
        return FileFolder.builder()
                .fileFolderType(FileFolderType.FILE)
                .parentId(parentId)
                .originalName(originalFileName)
                .storedName(storeFileName)
                .path(logicalFilePath)
                .size(fileSize)
                .contentType(fileType)
                .uploadTime(LocalDateTime.now())
                .userName(userName)
                .build();
    }
}