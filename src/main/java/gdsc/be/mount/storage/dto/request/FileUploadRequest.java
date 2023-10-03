package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileUploadRequest(
        Long parentId, // File 은 childId 가 없음
        String userName
) {
    public FileFolder toEntity(String originalFileName, String storeFileName, String filePath, long fileSize, String fileType){
        return FileFolder.builder()
                .fileFolderType(FileFolderType.FILE)
                .parentId(parentId)
                .originalName(originalFileName)
                .storedName(storeFileName)
                .path(filePath)
                .size(fileSize)
                .contentType(fileType)
                .uploadTime(LocalDateTime.now())
                .userName(userName)
                .build();
    }
}