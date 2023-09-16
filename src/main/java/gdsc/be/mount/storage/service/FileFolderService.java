package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.exception.*;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileFolderService {

    private final FileFolderRepository fileFolderRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public FileUploadResponse uploadFile(MultipartFile file, String userName, Long parentId) {

        validate(file); // 파일 유효성 검사

        String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
        String storeFileName = createStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명

        log.debug("[uploadFile] originalFileName: {}, storeFileName: {}", originalFileName, storeFileName);

        try {
            // 1. 파일 시스템에서 물리적 파일 저장
            String filePath = getFullPath(storeFileName, parentId);
            System.out.println("😃" + filePath);
            savePhysicalFile(file, filePath);

            // 2. DB 에 파일 메타데이터 저장
            FileFolder savedFileFolder = saveFileMetadataToDB(originalFileName, storeFileName, filePath, file.getSize(), file.getContentType(), userName);

            return FileUploadResponse.fromEntity(savedFileFolder);
        } catch (IOException ex) {
            throw FileUploadException.EXCEPTION;
        }
    }

    public Long deleteFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileForDeletion(fileId, userName);

        log.debug("[deleteFile] FileName: {}", fileFolder.getOriginalName());

        try {
            // 1. DB 에서 파일 메타데이터 삭제
            deleteFileMetadata(fileId);

            // 2. 파일 시스템에서 물리적 파일 삭제
            deletePhysicalFile(fileFolder.getPath());

            return fileFolder.getId();
        } catch (IOException ex) {
            throw FileDeletionException.EXCEPTION;
        }
    }

    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        try {
            // 파일 확인 및 권한 검사
            FileFolder fileFolder = getFileForDownload(fileId, userName);

            String originalFileName = fileFolder.getOriginalName();
            String saveFileName = fileFolder.getStoredName();
            String filePath = fileFolder.getPath();

            UrlResource resource = getResource(filePath);

            log.debug("[downloadFile] saveFileName: {}, URL Resource: {}", saveFileName, resource);

            // 다운로드 시 가독성 위해 최초 파일명 사용
            String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

            return new FileDownloadResponse(resource, contentDisposition);
        } catch (MalformedURLException ex) {
            // URL 생성 오류
            throw FileDownloadExpcetion.EXCEPTION;
        } catch (IOException ex) {
            throw FileDownloadExpcetion.EXCEPTION;
        }
    }

    public FolderCreateResponse createFolder(String userName, Long parentId) {

        String folderName = generateRandomFolderName();
        String folderPath = getFullPath(folderName, parentId);

        log.debug("[createFolder] folderName: {}, folderPath: {}", folderName, folderPath);

        try {
            // 1. 파일 시스템에서 물리적 폴더 생성
            savePhysicalFolder(folderPath);

            // 2. DB 에 폴더 메타데이터 저장
            FileFolder savedFileFolder = saveFolderMetadataToDB(folderName, folderPath, parentId, userName);

            return FolderCreateResponse.fromEntity(savedFileFolder);

        } catch (IOException e) {
            throw FolderCreateException.EXCEPTION;
        }
    }

    // ====================================================================================================

    /*
    파일 저장 관련 메서드
     */

    private void validate(MultipartFile file) {
        // 파일이 존재하는지 확인
        if (file == null) {
            throw FileUploadException.EXCEPTION;
        }

        // 파일명이 비어있는지 확인
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw FileUploadException.EXCEPTION;
        }

        // 파일 크기가 0인지 확인
        if (file.getSize() == 0) {
            throw FileUploadException.EXCEPTION;
        }
    }

    private String createStoreFileName(String originalFileName){
        // 원본 파일명에서 확장자 추출
        String ext = extractExt(originalFileName);

        // 확장자가 없는 경우 기본 확장자를 사용 (예. txt 로 설정)
        if (ext.isEmpty()) {
            ext = "txt";
        }

        return UUID.randomUUID().toString() + "." + ext;
    }

    private String extractExt(String originalFilename) {
        // 확장자 별도 추출
        int pos = originalFilename.lastIndexOf(".");

        // 확장자가 없는 경우 빈 문자열 반환
        if (pos == -1 || pos == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(pos + 1);
    }

    private String getFullPath(String storeFileName, Long parentId) {
        String uploadPath = this.uploadPath;
        if (parentId != null) {
            uploadPath += getParentFolderOriginalName(parentId) + "/";
        }
        return uploadPath + storeFileName;
    }

    private String getParentFolderOriginalName(Long parentId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        return parentFileFolder.getOriginalName();
    }

    private void savePhysicalFile(MultipartFile file, String filePath) throws IOException {
        file.transferTo(Files.createFile(Path.of(filePath)));
    }

    private FileFolder saveFileMetadataToDB(String originalFileName, String storeFileName, String filePath, long fileSize, String fileType, String userName) {
        FileUploadRequest fileUploadRequest =
                FileUploadRequest.builder()
                        .fileFolderType(FileFolderType.FILE)
                        .parentId(null)
                        .originalName(originalFileName)
                        .storedName(storeFileName)
                        .path(filePath)
                        .size(fileSize)
                        .contentType(fileType)
                        .uploadTime(LocalDateTime.now())
                        .userName(userName)
                        .build();

        return fileFolderRepository.save(fileUploadRequest.toEntity());
    }

    /*
    파일 삭제 관련 메서드
     */

    private void deleteFileMetadata(Long fileId) {
        fileFolderRepository.deleteById(fileId);
    }

    private void deletePhysicalFile(String filePath) throws IOException {
        Path fileToDelete = Path.of(filePath);
        Files.delete(fileToDelete);
    }

    /*
    파일 다운로드 관련 메서드
     */

    private UrlResource getResource(String path) throws IOException {
        Path filePath = Path.of(path);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw FileNotFoundException.EXCEPTION;
        }

        return resource;
    }

    /*
     파일 확인 및 권한 검사 관련 메서드
     */

    private FileFolder getFileFromDatabase(Long fileId) {
        return fileFolderRepository.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.EXCEPTION);
    }

    private FileFolder getFileWithOwnershipCheck(Long fileId, String userName, boolean isForDownload) {
        // DB에서 해당 파일 메타데이터를 가져옴
        FileFolder fileFolder = getFileFromDatabase(fileId);

        // 본인이 만든 파일인지 확인 후, 아니라면 예외를 던짐
        if (!userName.equals(fileFolder.getUserName())) {
            if (isForDownload) {
                throw FileDownloadNotAllowedException.EXCEPTION;
            } else {
                throw FileDeleteNotAllowedException.EXCEPTION;
            }
        }

        return fileFolder;
    }

    private FileFolder getFileForDeletion(Long fileId, String userName) {
        return getFileWithOwnershipCheck(fileId, userName, false);
    }

    private FileFolder getFileForDownload(Long fileId, String userName) {
        return getFileWithOwnershipCheck(fileId, userName, true);
    }


    /*
    폴더 생성 관련 메서드
     */

    private void savePhysicalFolder(String folderPath) throws IOException {
        Files.createDirectory(Paths.get(folderPath));
    }

    private static String generateRandomFolderName() {
        // 랜덤한 UUID를 사용하여 폴더 이름 생성
        return UUID.randomUUID().toString();
    }

    private FileFolder saveFolderMetadataToDB(String folderName, String folderDir, Long parentId, String userName) {
        FolderCreateRequest folderCreateRequest
                = FolderCreateRequest.builder()
                    .fileFolderType(FileFolderType.FOLDER)
                    .parentId(parentId)
                    .childId(null)
                    .originalName(folderName) // 추후에 폴더명 변경 기능 추가
                    .storedName(folderName)
                    .path(folderDir)
                    .uploadTime(LocalDateTime.now())
                    .userName(userName)
                    .build();
        return fileFolderRepository.save(folderCreateRequest.toEntity());
    }


}
