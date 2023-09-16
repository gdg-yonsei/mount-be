package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FolderCreateRequest (
    FileFolderType fileFolderType,
    Long parentId,
    Long childId,
    String originalName,
    String storedName,
    String path,
    //Long size,
    LocalDateTime uploadTime,
    String userName
){

    public FileFolder toEntity() {
            return FileFolder.builder()
                    .fileFolderType(fileFolderType)
                    .parentId(parentId)
                    .childId(childId)
                    .originalName(originalName)
                    .storedName(storedName)
                    .path(path)
                    //.size(size)
                    .uploadTime(uploadTime)
                    .userName(userName)
                    .build();
        }
}
