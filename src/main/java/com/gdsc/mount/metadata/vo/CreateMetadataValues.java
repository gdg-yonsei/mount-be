package com.gdsc.mount.metadata.vo;

import com.gdsc.mount.metadata.dto.MetadataCreateRequest;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class CreateMetadataValues {
    private String fileCode;
    private String name;
    private String path;
    private String username;
    private MultipartFile file;

    public CreateMetadataValues(MetadataCreateRequest request, MultipartFile file, String fileCode) {
        this.fileCode = fileCode;
        this.name = file.getOriginalFilename();
        this.path = request.getPath();
        this.username = request.getUsername();
        this.file = file;
    }
}
