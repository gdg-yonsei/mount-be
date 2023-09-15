package gdsc.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


public interface StorageService {

    void init(Path root);

    void store(MultipartFile file, String userId);

    Resource download(Long fileId, String userId);

    void deleteOne(Long fileId, String userId);
}
