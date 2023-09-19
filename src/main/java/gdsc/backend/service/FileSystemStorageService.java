package gdsc.backend.service;


import gdsc.backend.domain.FileMetaData;
import gdsc.backend.domain.Folder;
import gdsc.backend.exception.StorageException;
import gdsc.backend.exception.UnauthorizedAccessException;
import gdsc.backend.repository.FileMetadataRepository;
import gdsc.backend.repository.FolderRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FolderRepository folderRepository;

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
    public void store(MultipartFile file, String userId, Long parentFolderId) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }

            String originalFileName = file.getOriginalFilename();
            String saveFileName = getSaveFileName(userId, originalFileName);

            // parentFolder 설정 : null(root) or Folder
            Folder parentFolder = null;
            if (parentFolderId != null) {
                // [Error] StorageException : 해당 폴더가 없을 경우
                parentFolder = folderRepository.findById(parentFolderId)
                        .orElseThrow(() -> new StorageException("Folder not found"));
            }

            // 1. Save the file
            saveOriginalFile(file, saveFileName);
            // 2. Save the file meta data
            saveFileMetaData(file, userId, originalFileName, saveFileName, parentFolder);

        } catch (Exception e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }


    @Override
    public Resource download(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException{
        Optional<FileMetaData> fileMetaDataOptional = fileMetadataRepository.findById(fileId);

        // [Error] StorageException : 해당 파일 메타데이터가 없을 경우
        FileMetaData fileMetaData = fileMetaDataOptional.orElseThrow(() -> new StorageException("File meta data not found"));

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
        Optional<FileMetaData> fileMetaDataOptional = fileMetadataRepository.findById(fileId);

        // [Error] StorageException : 해당 파일 메타데이터가 없을 경우
        FileMetaData fileMetaData = fileMetaDataOptional.orElseThrow(() -> new StorageException("File meta data not found"));

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

    // 재귀적으로 부모 폴더의 경로를 가져와 파일 경로 생성
    private String getFilePath(Folder folder, String fileName) {
        if (folder == null) {
            // 최상위 폴더에 도달했을 때 종료
            return fileName;
        }
        // 부모 폴더의 경로를 가져옴
        String parentFolderPath = getFilePath(folder.getParent(), folder.getName());
        return parentFolderPath + "/" + fileName;
    }

    private void saveFileMetaData(MultipartFile file, String userId, String originalFileName, String saveFileName, Folder parentFolder) {
        String filePath = getFilePath(parentFolder, saveFileName);
        FileMetaData fileMetaData = new FileMetaData(userId, originalFileName, saveFileName, file.getSize(), LocalDateTime.now(), null, filePath, parentFolder);
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
