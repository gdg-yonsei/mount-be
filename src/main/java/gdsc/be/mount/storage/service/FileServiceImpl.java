package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.File;
import gdsc.be.mount.storage.exception.FileDownloadNotAllowedException;
import gdsc.be.mount.storage.exception.FileNotFoundException;
import gdsc.be.mount.storage.exception.FileStorageExeption;
import gdsc.be.mount.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public FileUploadResponse uploadFile(MultipartFile file, String userName) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
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
        } catch (IOException ex) {
            throw new FileStorageExeption("파일 업로드 중 오류가 발생했습니다.", ex);
        }
    }

    @Override
    public Long deleteFile(Long fileId) {
        try {
            // 삭제할 파일 확인
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException("File not found"));

            // DB 에서 파일 메타데이터 삭제
            fileRepository.deleteById(fileId);

            // 파일 시스템에서 파일 삭제
            Path fileToDelete = Path.of(file.getFilePath());
            Files.delete(fileToDelete);

            return file.getId();
        } catch (IOException ex) {
            throw new FileStorageExeption("파일 삭제 중 오류가 발생했습니다.", ex);
        }
    }

    @Override
    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        try {
            // 다운로드 요청이 들어온 파일 확인 후, 해당 파일 없으면 예외
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException("File not found"));

            // 본인이 만든 파일인지 확인 후, 아니라면 예외
            if (!userName.equals(file.getUserName())) {
                throw new FileDownloadNotAllowedException("You are not allowed to download this file");
            }

            String originalFileName = file.getOriginalFileName();
            String saveFileName = file.getStoreFileName();
            log.info("saveFileName = {}", saveFileName);

            UrlResource resource = new UrlResource("file:" + getFullPath(saveFileName));
            log.info("URL Resource = {}", resource);

            // 다운로드 시 가독성 위해 최초 파일명 사용
            String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

            return new FileDownloadResponse(resource, contentDisposition);
        } catch (IOException ex) {
            throw new FileStorageExeption("파일 다운로드 중 오류가 발생했습니다.", ex);
        }
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
