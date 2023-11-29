package com.gdsc.mount.metadata.vo;

import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class CreateMetadataValues {
    private String fileCode;
    private String name;
    private String path;
    private boolean atRoot;
    private String username;
    private MultipartFile file;

    public CreateMetadataValues(CreateMetadataRequest request, MultipartFile file, String fileCode) {
        this.fileCode = fileCode;
        this.name = file.getOriginalFilename();
        this.path = request.getPath() + "/" + name;
        this.atRoot = request.isAtRoot();
        this.username = request.getUsername();
        this.file = file;
    }
}
