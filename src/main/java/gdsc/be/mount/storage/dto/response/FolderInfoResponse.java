package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.Enum.FileFolderType;

import java.time.LocalDateTime;

public record FolderInfoResponse (
        FileFolderType fileFolderType,
        Long parentId,
        Long childId,
        String originalName,
        String storedName,
        String path,
        LocalDateTime uploadTime,
        String userName
) {
    public static FolderInfoResponse fromEntity(FileFolderType fileFolderType, Long parentId, Long childId, String originalName, String storedName, String path, LocalDateTime uploadTime, String userName) {
        return new FolderInfoResponse(fileFolderType, parentId, childId, originalName, storedName, path, uploadTime, userName);
    }
}
