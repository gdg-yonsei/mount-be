package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.File;
import gdsc.be.mount.storage.exception.FileNotFoundException;
import gdsc.be.mount.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileServiceImpl implements FileService{

    private final FileRepository fileRepository;

    @Value("${upload.path}")
    private String fileDir;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String userName) throws IOException {
        if(file.isEmpty()){
            return null;
        }

        String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
        String storeFileName = createStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명

        // 파일 시스템에 파일 저장
        String filePath = getFullPath(storeFileName);
        file.transferTo(new java.io.File(filePath));

        // DB 에 파일 메타데이터 저장
        FileUploadRequest fileUploadRequest =
            FileUploadRequest.builder()
                .originalFileName(originalFileName)
                .storeFileName(storeFileName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploadTime(LocalDateTime.now())
                .userName(userName)
                .build();

        File savedFile = fileRepository.save(fileUploadRequest.toEntity());

        return FileUploadResponse.fromEntity(savedFile);
    }

    @Override
    public Long deleteFile(Long fileId) throws IOException {
        // 삭제할 파일 확인
        System.out.println(fileId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        // DB 에서 파일 메타데이터 삭제
        fileRepository.deleteById(fileId);

        // 파일 시스템에서 파일 삭제
        Path fileToDelete = Path.of(file.getFilePath());
        Files.delete(fileToDelete);

        return file.getId();
    }


    // 동일한 이름 충돌 방지를 위해 random 값으로 서버 내부 관리용 파일명 제작
    public String createStoreFileName(String originalFileName){
        return UUID.randomUUID().toString() + "." + extractExt(originalFileName);
    }

    // 확장자 별도 추출
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    public String getFullPath(String filename) {
        return fileDir + filename;
    }
}
