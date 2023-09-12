package gdsc.be.mount.storage.dto.response;

import gdsc.be.mount.storage.entity.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponse {
    private String originalFileName;
    private String storeFileName;

    public static FileUploadResponse fromEntity(File fileEntity){
        return FileUploadResponse.builder()
            .originalFileName(fileEntity.getOriginalFileName())
            .storeFileName(fileEntity.getStoreFileName())
            .build();
    }
}
