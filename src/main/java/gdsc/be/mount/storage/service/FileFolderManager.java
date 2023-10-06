package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.exception.FileFolderDeletionException;
import gdsc.be.mount.storage.exception.FileFolderNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileFolderManager {

    @Value("${upload.path}")
    public String uploadPath;

    public void savePhysicalFile(MultipartFile file, String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        file.transferTo(Files.createFile(path));
    }

    public void deletePhysicalFile(String storeFileName) {
        // 물리적 파일 삭제
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new FileFolderDeletionException();
        }
    }

    public UrlResource getResource(String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path filePath = Paths.get(uploadPath, storeFileName);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw new FileFolderNotFoundException();
        }

        return resource;
    }
}

