package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface FileService {
    public FileUploadResponse uploadFile(MultipartFile file, String userName) throws IOException;
    public Long deleteFile(Long fileId) throws IOException;
    public FileDownloadResponse downloadFile(Long fileId, String userName) throws MalformedURLException;
}
