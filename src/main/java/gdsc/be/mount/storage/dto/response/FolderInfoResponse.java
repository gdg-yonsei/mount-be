package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FolderInfoResponse (
        FileFolderType fileFolderType,
        Long parentId,
        List<Long> childIds,
        String originalName,
        String storedName,
        String path,
        LocalDateTime uploadTime,
        String userName
) {
    public static FolderInfoResponse fromEntity(FileFolder fileFolder){
        return FolderInfoResponse.builder()
                .fileFolderType(fileFolder.getFileFolderType())
                .parentId(fileFolder.getParentId())
                .childIds(fileFolder.getChildIds())
                .originalName(fileFolder.getOriginalName())
                .storedName(fileFolder.getStoredName())
                .path(fileFolder.getPath())
                .uploadTime(fileFolder.getUploadTime())
                .userName(fileFolder.getUserName())
                .build();
    }
}
