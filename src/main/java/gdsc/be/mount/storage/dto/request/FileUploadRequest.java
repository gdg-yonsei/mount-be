package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileUploadRequest(
        FileFolderType fileFolderType,
        Long parentId,
        //Long childId, // File 은 childId 가 없음
        String originalName,
        String storedName,
        String path,
        Long size,
        String contentType,
        LocalDateTime uploadTime,
        String userName
) {
    public FileFolder toEntity(){
        return FileFolder.builder()
                .fileFolderType(fileFolderType)
                .parentId(parentId)
                //.childId(childId)
                .originalName(originalName)
                .storedName(storedName)
                .path(path)
                .size(size)
                .contentType(contentType)
                .uploadTime(uploadTime)
                .userName(userName)
                .build();
    }
}