package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadFile(MultipartFile file, String userName);
    Long deleteFile(Long fileId);
    FileDownloadResponse downloadFile(Long fileId, String userName);
}
