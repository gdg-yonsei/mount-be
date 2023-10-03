package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.entity.FileFolder;

public record FolderCreateResponse(String originalFileName, String storeFileName) {
    public static FolderCreateResponse fromEntity(FileFolder fileFolder){
        return new FolderCreateResponse(fileFolder.getOriginalName(), fileFolder.getStoredName());
    }
}
