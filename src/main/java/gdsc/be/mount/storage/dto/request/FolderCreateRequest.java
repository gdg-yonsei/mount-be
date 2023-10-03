package gdsc.be.mount.storage.dto.request;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Builder
public record FolderCreateRequest (
    Long parentId,
    String userName
){

    public FileFolder toEntity(String folderName, String folderDir, Long parentId, String userName) {
            return FileFolder.builder()
                    .fileFolderType(FileFolderType.FOLDER)
                    .parentId(parentId)
                    .childIds(new ArrayList<>())
                    .originalName(folderName) // 추후 수정 필요
                    .storedName(folderName)
                    .path(folderDir)
                    .uploadTime(LocalDateTime.now())
                    .userName(userName)
                    .build();
        }
}
