package gdsc.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface StorageService {

    void init();

    void store(MultipartFile file, String userId);

    Resource download(String uuid, String userId);

    void deleteOne(String uuid, String userId);
}
