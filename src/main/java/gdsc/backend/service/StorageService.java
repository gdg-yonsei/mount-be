package gdsc.backend.service;

import gdsc.backend.exception.UnauthorizedAccessException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;


public interface StorageService {

    void init(Path root);

    void store(MultipartFile file, String userId);

    Resource download(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException;

    void deleteOne(Long fileId, String userId) throws FileNotFoundException, UnauthorizedAccessException;
}
