package gdsc.backend.service;


import gdsc.backend.domain.FileMetaData;
import gdsc.backend.exception.StorageException;
import gdsc.backend.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Override
    public void init() {

    }

    @Override
    @Transactional
    public void store(MultipartFile file, String userId) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }

            String originalFileName = file.getOriginalFilename();

            // Generate a unique UUID
            String uuid = UUID.randomUUID().toString();

            // Get the file extension
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));

            // Generate the file name
            String saveFileName = userId + "_" + uuid + fileExtension;

            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                init();
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, root.resolve(saveFileName));
            }

            FileMetaData fileMetaData = new FileMetaData(userId, originalFileName, saveFileName, file.getSize(), LocalDateTime.now(), null);
            fileMetadataRepository.save(fileMetaData);
        } catch (Exception e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Resource download(String uuid, String userId) {
        FileMetaData fileMetaData = fileMetadataRepository.findByUuidAndUserId(uuid, userId);
        if (fileMetaData == null) {
            throw new StorageException("You are not authorized to download this file");
        }
        String saveFileName = fileMetaData.getSaveFileName();
        try {
            Path file = Paths.get(uploadPath + "/" + saveFileName);
            Resource resource = new UrlResource(file.toUri());
            return resource;
        } catch (Exception e) {
            throw new StorageException("Failed to download file " + saveFileName, e);
        }
    }

    @Override
    @Transactional
    public void deleteOne(String uuid, String userId) {
        try {
            FileMetaData fileMetaData = fileMetadataRepository.findByUuidAndUserId(uuid, userId);
            if (fileMetaData == null) {
                throw new StorageException("You are not authorized to delete this file");
            }
            String saveFileName = fileMetaData.getSaveFileName();
            Path file = Paths.get(uploadPath + "/" + saveFileName);
            Files.deleteIfExists(file);

            fileMetaData.deleteFile();
            fileMetadataRepository.save(fileMetaData);
        } catch (Exception e) {
            throw new StorageException("Failed to delete file " + uuid, e);
        }

    }
}
