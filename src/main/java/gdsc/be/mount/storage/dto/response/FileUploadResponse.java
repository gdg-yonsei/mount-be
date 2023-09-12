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

    public static FileUploadResponse fromEntity(File file){
        return FileUploadResponse.builder()
            .originalFileName(file.getOriginalFileName())
            .storeFileName(file.getStoreFileName())
            .build();
    }
}
