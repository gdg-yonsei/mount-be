package gdsc.backend.service;

import gdsc.backend.exception.UnauthorizedAccessException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Path;


public interface StorageService {

    void initUploadPath(Path root);

    void store(MultipartFile file, String userId, Long parentFolderId);

    Resource download(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException;

    void deleteOne(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException;
}
