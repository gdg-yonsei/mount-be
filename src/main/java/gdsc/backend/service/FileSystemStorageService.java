package gdsc.backend.service;


import gdsc.backend.domain.FileMetaData;
import gdsc.backend.exception.StorageException;
import gdsc.backend.exception.UnauthorizedAccessException;
import gdsc.backend.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Override
    public void initUploadPath(Path root) {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new StorageException("Failed to initialize storage", e);
        }

    }

    @Override
    @Transactional
    public void store(MultipartFile file, String userId) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }

            String originalFileName = file.getOriginalFilename();
            String saveFileName = getSaveFileName(userId, originalFileName);

            // 1. Save the file
            saveOriginalFile(file, saveFileName);
            // 2. Save the file meta data
            saveFileMetaData(file, userId, originalFileName, saveFileName);

        } catch (Exception e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }


    @Override
    public Resource download(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException{
        FileMetaData fileMetaData = fileMetadataRepository.findByFileId(fileId);

        // [Error] StorageException : 해당 파일 메타데이터가 없을 경우
        if (fileMetaData == null) {
            throw new StorageException("File meta data not found");
        }
        // [Error] UnauthorizedAccessException : 해당 파일의 주인이 아닐 경우
        if (!fileMetaData.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to download this file");
        }
        // [Success] : 해당 파일이 존재하고, 주인이 맞을 경우
        String saveFileName = fileMetaData.getSaveFileName();
        try {
            Path file = Paths.get(uploadPath + "/" + saveFileName);
            Resource resource = new UrlResource(file.toUri());
            return resource;
        } catch (Exception e) {
            // [Error] FileNotFoundException : 해당 파일이 없을 경우
            throw new FileNotFoundException("File not found");
        }
    }

    @Override
    @Transactional
    public void deleteOne(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException{
        FileMetaData fileMetaData = fileMetadataRepository.findByFileId(fileId);

        // [Error] StorageException : 해당 파일 메타데이터가 없을 경우
        if (fileMetaData == null) {
            throw new StorageException("File meta data not found");
        }
        // [Error] UnauthorizedAccessException : 해당 파일의 주인이 아닐 경우
        if (!fileMetaData.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this file");
        }
        // [Success]: 해당 파일이 존재하고, 주인이 맞을 경우
        String saveFileName = fileMetaData.getSaveFileName();
        try {
            Path file = Paths.get(uploadPath + "/" + saveFileName);
            Files.delete(file);

            fileMetaData.deleteFile();
            fileMetadataRepository.save(fileMetaData);
        } catch (Exception e) {
            // [Error] FileNotFoundException : 해당 파일이 없을 경우
            throw new FileNotFoundException("File not found");
        }

    }

    /**
     *
     * Methods for storing files
     *
     */

    private String getSaveFileName(String userId, String originalFileName) {
        // Generate a unique UUID
        String uuid = UUID.randomUUID().toString();

        // Get the file extension
        String fileExtension = "";
        int lastIndex = originalFileName.lastIndexOf(".");
        if (lastIndex != -1) {
            fileExtension = originalFileName.substring(lastIndex);
        } else {
            throw new IllegalArgumentException("File name does not contain a valid file extension");
        }

        // Return the save file name
        return String.format("%s_%s%s", userId, uuid, fileExtension);
    }

    private void saveFileMetaData(MultipartFile file, String userId, String originalFileName, String saveFileName) {
        FileMetaData fileMetaData = new FileMetaData(userId, originalFileName, saveFileName, file.getSize(), LocalDateTime.now(), null);
        fileMetadataRepository.save(fileMetaData);
    }

    private void saveOriginalFile(MultipartFile file, String saveFileName) throws IOException {
        Path root = Paths.get(uploadPath);
        if (!Files.exists(root)) {
            initUploadPath(root);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, root.resolve(saveFileName));
        }
    }
}
