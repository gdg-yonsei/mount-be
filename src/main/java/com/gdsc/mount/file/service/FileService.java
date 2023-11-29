package com.gdsc.mount.file.service;

import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.service.MetadataService;
import com.gdsc.mount.metadata.vo.CreateMetadataValues;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {
    private final MetadataService metadataService;

    public String uploadFile(MultipartFile file) throws Exception {
        Path uploadPath = Paths.get("Files-Upload");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileCode = RandomStringUtils.randomAlphanumeric(10) + "-" + file.getName();

        Path filePath = uploadPath.resolve(file.getName());

        System.out.println("FILE SERVICE: " + filePath.toString());

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileCode;
    }

    public Resource downloadFile(DownloadFileRequest request) throws IOException {
        Metadata metadata = metadataService.findByPathIfOwner(request.getUsername(), request.getPath());
        Path foundFile = Paths.get(request.getPath());

        return new UrlResource(foundFile.toUri());
    }
}
