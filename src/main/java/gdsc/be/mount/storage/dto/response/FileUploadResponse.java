package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.entity.File;

public record FileUploadResponse(String originalFileName, String storeFileName) {
    public static FileUploadResponse fromEntity(File file){
        return new FileUploadResponse(file.getOriginalFileName(), file.getStoreFileName());
    }
}
