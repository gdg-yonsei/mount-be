package com.gdsc.mount.file.service;

import com.gdsc.mount.metadata.domain.Metadata;
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

    private static final Path root = Paths.get("Files-Upload");

    private final MetadataService metadataService;

    public String uploadFile(MultipartFile file) throws Exception {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        String fileCode = RandomStringUtils.randomAlphanumeric(10) + "-" + file.getOriginalFilename();

        Path filePath = root.resolve(file.getOriginalFilename());

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileCode;
    }

    public Resource downloadFile(DownloadFileRequest request) throws IOException {
        String path = root+"/"+request.getPath();
        Metadata metadata = metadataService.findByPathIfOwner(request.getUsername(), path);
        Path foundFile = Paths.get(path);

        return new UrlResource(foundFile.toUri());
    }

    public String deleteFile(String path) throws IOException, NoSuchFileException {
        Path filePath = root.resolve(path);
        boolean success = Files.deleteIfExists(filePath);
        if (!success) {
            throw new NoSuchFileException("No such file with given path.");
        }
        return filePath.toString();
    }
}
