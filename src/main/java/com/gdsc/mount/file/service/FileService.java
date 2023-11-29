package com.gdsc.mount.file.service;

import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.service.MetadataService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

    private static final String ROOT = "Files-Upload";

    private final MetadataService metadataService;

    public String uploadFile(MultipartFile file, String path) throws Exception {
        Path uploadPath = Paths.get(ROOT+path);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileCode = RandomStringUtils.randomAlphanumeric(10) + "-" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileCode);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileCode;
    }

    public Resource downloadFile(DownloadFileRequest request) throws IOException {
        Metadata metadata = metadataService.findByPathIfOwner(request.getUsername(), request.getPath()+request.getFileName());
        Path foundFile = Paths.get(ROOT+request.getPath());

        return new UrlResource(foundFile.toUri());
    }

    public String deleteFile(DeleteFileRequest request) throws IOException, NoSuchFileException {
        Path filePath = Paths.get(ROOT + request.getPath());
        Metadata metadata = metadataService.findByPathIfOwner(request.getUsername(), request.getPath() + request.getFileName());
        Path file = filePath.resolve(metadata.get_id());
        boolean success = Files.deleteIfExists(file);
        if (!success) {
            throw new NoSuchFileException("No such file with given path in file storage.");
        }
        return filePath.toString();
    }
}
