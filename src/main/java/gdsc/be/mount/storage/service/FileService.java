package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    public FileUploadResponse uploadFile(MultipartFile file, String userName) throws IOException;
    public Long deleteFile(Long fileId) throws IOException;
}
