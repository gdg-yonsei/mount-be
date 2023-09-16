package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.entity.FileFolder;

public record FileUploadResponse(String originalFileName, String storeFileName) {
    public static FileUploadResponse fromEntity(FileFolder fileFolder){
        return new FileUploadResponse(fileFolder.getOriginalName(), fileFolder.getStoredName());
    }
}
