package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.dto.request.FileFolderUpdateRequest;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileFolderService {

    private final FileFolderRepository fileFolderRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) {

        validate(file); // 파일 유효성 검사

        Long parentId = fileUploadRequest.parentId();

        String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
        String storeFileName = createStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명
        String logicalFilePath = getFullLogicalPath(storeFileName, parentId); // 파일의 논리적 경로
        String physicalFilePath = uploadPath + storeFileName; // 파일의 물리적 경로

        log.debug("[uploadFile] originalFileName: {}, logicalFilePath: {}", originalFileName, logicalFilePath);

        try {
            // 1. 파일 시스템에서 물리적 파일 저장
            savePhysicalFile(file, physicalFilePath);

            try {
                // 2. DB 에 파일 메타데이터 저장
                FileFolder savedFileFolder = saveFileMetadataToDB(fileUploadRequest, originalFileName, storeFileName, logicalFilePath, file.getSize(), file.getContentType());

                // 3. 부모 폴더에 자식 폴더 id 추가
                if(parentId != null){
                    addChildIdIntoParentFolder(parentId, savedFileFolder.getId());
                }

                return FileUploadResponse.fromEntity(savedFileFolder);
            } catch (Exception dbException) {
                // 만약 DB에 파일 메타데이터 저장 중에 예외가 발생하면 물리적 파일 삭제 후 예외 다시 던지기
                deletePhysicalFile(physicalFilePath);
                throw dbException;
            }
        } catch (IOException ex) {
            throw FileUploadException.EXCEPTION;
        }
    }

    public Long deleteFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForDeletionAfterCheck(fileId, userName);

        String deletedFileName = fileFolder.getOriginalName();
        String physicalFilePath = uploadPath + fileFolder.getStoredName(); // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        log.debug("[deleteFile] FileName: {}", deletedFileName);

        try {
            // 1. DB 에서 파일 메타데이터 삭제
            deleteFileMetadata(fileId);

            // 2. 파일 시스템에서 물리적 파일 삭제
            deletePhysicalFile(physicalFilePath);

            return fileFolder.getId();
        } catch (IOException ex) {
            throw FileDeletionException.EXCEPTION;
        }
    }

    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForDownloadAfterCheck(fileId, userName);

        String originalFileName = fileFolder.getOriginalName();
        String saveFileName = fileFolder.getStoredName();
        String physicalFilePath = uploadPath + saveFileName; // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용

        try {

            UrlResource resource = getResource(physicalFilePath);

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

    public FolderCreateResponse createFolder(FolderCreateRequest folderCreateRequest) {

        String userName = folderCreateRequest.userName();
        Long parentId = folderCreateRequest.parentId();

        String folderName = generateRandomFolderName();
        String folderLogicalPath = getFullLogicalPath(folderName, parentId);

        log.debug("[createFolder] folderName: {}, folderPath: {}", folderName, folderLogicalPath);

        // 1. 파일 시스템에서 물리적 폴더 생성 -> 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 생성은 필요 없음

        // 2. DB 에 폴더 메타데이터 저장
        FileFolder savedFileFolder = saveFileFolderMetadataToDB(folderCreateRequest, folderName, folderLogicalPath, parentId, userName);

        // 3. 부모 폴더에 자식 폴더 id 추가
        if (parentId != null) {
            addChildIdIntoParentFolder(parentId, savedFileFolder.getId());
        }

        return FolderCreateResponse.fromEntity(savedFileFolder);

    }

    public Long updateFolderName(Long folderId, FileFolderUpdateRequest request) {

        String userName = request.userName();
        String newFolderName = request.newFolderName();

        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForUpdateAfterCheck(folderId, userName);

        // 이미 존재하는 이름으로 변경할 경우 오류
        checkDuplicateFolderName(newFolderName, fileFolder.getParentId());

        String originalFolderName = fileFolder.getOriginalName();

        log.debug("[updateFolderName] FileName: {}, NewFolderName : {}", originalFolderName, newFolderName);

        // 1. DB 에서 폴더 이름 수정
        fileFolder.updateOriginalName(newFolderName);
        fileFolderRepository.save(fileFolder);

        // 2. 파일 시스템에서 폴더 이름 업데이트 -> 가상 폴더 구조를 사용하고 있으므로 물리적 이름 업데이트는 필요 없음

        return fileFolder.getId();
    }

    public FolderInfoResponse getFolderMetadata(Long folderId, String userName) {

        // 추후 수정 : DB Connection 한 번에 처리하도록 두 개의 서브 쿼리를 결합 (UNION)

        FileFolder folder = fileFolderRepository.findById(folderId).orElseThrow(
                () -> FileNotFoundException.EXCEPTION
        );
        if(!folder.getUserName().equals(userName)){
            throw FileDownloadNotAllowedException.EXCEPTION;
        }
        List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(folder.getChildIds());

        return FolderInfoResponse.fromEntity(folder, childFileFolders);
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

        return UUID.randomUUID().toString().substring(0, 5) + "." + ext;
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

    private String getFullLogicalPath(String storeFileName, Long parentId) {

        StringBuilder pathBuilder = new StringBuilder();

        if (parentId != null) {
            pathBuilder.append(getParentFolderOriginalName(parentId));
        }

        if(extractExt(storeFileName).isEmpty()){
            // 폴더는 끝에 / 가 붙고, 파일은 / 가 붙지 않음
            storeFileName += "/";
        }
        pathBuilder.append(storeFileName);

        return pathBuilder.toString();
    }

    private String getParentFolderOriginalName(Long parentId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        return parentFileFolder.getPath();
    }

    private void savePhysicalFile(MultipartFile file, String filePath) throws IOException {
        file.transferTo(Files.createFile(Path.of(filePath)));
    }

    private FileFolder saveFileMetadataToDB(FileUploadRequest fileUploadRequest, String originalFileName, String storeFileName, String logicalFilePath, long fileSize, String fileType) {
        return fileFolderRepository.save(fileUploadRequest.toEntity(originalFileName, storeFileName, logicalFilePath, fileSize, fileType));
    }

    private void addChildIdIntoParentFolder(Long parentId, Long childId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        parentFileFolder.addChildId(childId);
        fileFolderRepository.save(parentFileFolder);
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

    UrlResource getResource(String path) throws IOException {
        Path filePath = Path.of(path);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw FileNotFoundException.EXCEPTION;
        }

        return resource;
    }

    /*
    폴더 생성 관련 메서드
     */

    private static String generateRandomFolderName() {
        // 랜덤한 UUID를 사용하여 폴더 이름 생성
        return UUID.randomUUID().toString().substring(0, 5);
    }

    private FileFolder saveFileFolderMetadataToDB(FolderCreateRequest folderCreateRequest, String folderName, String folderDir, Long parentId, String userName) {
        return fileFolderRepository.save(folderCreateRequest.toEntity(folderName, folderDir, parentId, userName));
    }

    /*
     파일 및 폴더 권한 검사 관련 메서드
     */
    private FileFolder getFileFolderFromDatabase(Long fileId) {
        return fileFolderRepository.findById(fileId)
                .orElseThrow(() -> FileNotFoundException.EXCEPTION);
    }

    private void checkOwnership(String userName, String owner, boolean isForDownload, boolean isForUpdate) {
        if (!userName.equals(owner)) {

            if(isForDownload){
                throw FileDownloadNotAllowedException.EXCEPTION;
            }else if(isForUpdate){
                throw FileUpdateNotAllowedException.EXCEPTION;
            }else{
                throw FileDeleteNotAllowedException.EXCEPTION;
            }

        }
    }

    private FileFolder getFileFolderForDownloadAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(),true, false);
        return fileFolder;
    }

    private FileFolder getFileFolderForDeletionAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(),false, false);
        return fileFolder;
    }

    private FileFolder getFileFolderForUpdateAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(), false, true);
        return fileFolder;
    }

    private void checkDuplicateFolderName(String folderName, Long parentId) {
        if (fileFolderRepository.existsByOriginalNameAndParentId(folderName, parentId)) {
            throw FolderNameDuplicateException.EXCEPTION;
        }
    }

}
