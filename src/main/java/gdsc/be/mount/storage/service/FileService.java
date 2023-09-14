package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.File;
import gdsc.be.mount.storage.exception.*;
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
public class FileService {

    private final FileRepository fileRepository;

    @Value("${upload.path}")
    private String fileDir;

    public FileUploadResponse uploadFile(MultipartFile file, String userName) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
            }

            String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
            String storeFileName = createStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명

            // 1. 파일 시스템에서 물리적 파일 저장
            String filePath = getFullPath(storeFileName);
            saveFileToDisk(file, filePath);

            // 2. DB 에 파일 메타데이터 저장
            File savedFile = saveFileMetadataToDB(originalFileName, storeFileName, filePath, file.getSize(), file.getContentType(), userName);

            return FileUploadResponse.fromEntity(savedFile);
        } catch (IOException ex) {
            throw FileUploadException.EXCEPTION;
        }
    }

    public Long deleteFile(Long fileId, String userName) {
        try {
            // 파일 확인 및 권한 검사
            File file = getFileForDeletion(fileId, userName);

            // 1. DB 에서 파일 메타데이터 삭제
            deleteFileMetadata(fileId);

            // 2. 파일 시스템에서 물리적 파일 삭제
            deletePhysicalFile(file.getFilePath());

            return file.getId();
        } catch (IOException ex) {
            throw FileDeletionException.EXCEPTION;
        }
    }

    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        try {

            // 파일 확인 및 권한 검사
            File file = getFileForDownload(fileId, userName);

            String originalFileName = file.getOriginalFileName();
            String saveFileName = file.getStoreFileName();

            UrlResource resource = getResource(saveFileName);

            log.debug("Saved File Name = {} / URL Resource = {}", saveFileName, resource);

            // 다운로드 시 가독성 위해 최초 파일명 사용
            String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

            return new FileDownloadResponse(resource, contentDisposition);
        } catch (IOException ex) {
            throw FileDownloadExpcetion.EXCEPTION;
        }
    }


    // 동일한 이름 충돌 방지를 위해 random 값으로 서버 내부 관리용 파일명 제작
    private String createStoreFileName(String originalFileName){
        return UUID.randomUUID().toString() + "." + extractExt(originalFileName);
    }

    // 확장자 별도 추출
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private String getFullPath(String filename) {
        return fileDir + filename;
    }

    private void saveFileToDisk(MultipartFile file, String filePath) throws IOException {
        file.transferTo(Files.createFile(Path.of(filePath)));
    }

    private File saveFileMetadataToDB(String originalFileName, String storeFileName, String filePath, long fileSize, String fileType, String userName) {
        FileUploadRequest fileUploadRequest =
                FileUploadRequest.builder()
                        .originalFileName(originalFileName)
                        .storeFileName(storeFileName)
                        .filePath(filePath)
                        .fileSize(fileSize)
                        .fileType(fileType)
                        .uploadTime(LocalDateTime.now())
                        .userName(userName)
                        .build();

        return fileRepository.save(fileUploadRequest.toEntity());
    }

    private File getFileForDeletion(Long fileId, String userName) {
        // DB 에서 해당 파일 메타데이터가 없으면 예외
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.EXCEPTION);

        // 본인이 만든 파일인지 확인 후, 아니라면 예외
        if (!userName.equals(file.getUserName())) {
            throw FileDeleteNotAllowedException.EXCEPTION;
        }

        return file;
    }

    private void deleteFileMetadata(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    private void deletePhysicalFile(String filePath) throws IOException {
        Path fileToDelete = Path.of(filePath);
        Files.delete(fileToDelete);
    }

    private File getFileForDownload(Long fileId, String userName) {
        // DB 에서 해당 파일 메타데이터가 없으면 예외
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.EXCEPTION);

        // 본인이 만든 파일인지 확인 후, 아니라면 예외
        if (!userName.equals(file.getUserName())) {
            throw FileDownloadNotAllowedException.EXCEPTION;
        }

        return file;
    }


    private UrlResource getResource(String saveFileName) throws IOException {
        Path filePath = Path.of(getFullPath(saveFileName));
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw FileNotFoundException.EXCEPTION;
        }

        return resource;
    }

}
