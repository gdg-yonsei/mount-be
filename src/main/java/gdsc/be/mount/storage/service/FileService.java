package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.exception.*;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import gdsc.be.mount.storage.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileService {

    private final FileFolderRepository fileFolderRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) {

        validate(file); // 파일 유효성 검사

        Long parentId = fileUploadRequest.parentId();
        String userName = fileUploadRequest.userName();

        // 만약 부모의 폴더의 주인이 자신이 아니라면 예외 발생
        checkIfParentIsYours(parentId, userName);

        // 만약 parentId 가 폴더가 아니라면 예외 발생
        checkIfParentIsFolder(parentId);

        String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
        String storeFileName = createStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명
        String logicalFilePath = getFullLogicalPath(userName, storeFileName, parentId); // 파일의 논리적 경로
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
            throw new FileFolderUploadException();
        }
    }

    public Long deleteFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForDeletionAfterCheck(fileId, userName);

        // 삭제하려는 대상이 파일인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FOLDER){
            throw new FileFolderDeleteNotAllowedException();
        }

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
            throw new FileFolderDeletionException();
        }
    }

    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForDownloadAfterCheck(fileId, userName);

        // 다운로드 하려는 대상이 파일인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FOLDER){
            throw new FileFolderDownloadNotAllowedException();
        }

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
        } catch (IOException ex){
            throw new FileFolderDownloadExpcetion();
        }
    }



    // ====================================================================================================

    /*
    파일 저장 관련 메서드
     */

    private void validate(MultipartFile file) {
        // 파일이 존재하는지 확인
        if (file == null) {
            throw new FileEmptyException();
        }

        // 파일명이 비어있는지 확인
        if (StringUtils.isEmpty(file.getOriginalFilename())) {
            throw new FileEmptyException();
        }

        // 파일 크기가 0인지 확인
        if (file.getSize() == 0) {
            throw new FileEmptyException();
        }
    }

    private String createStoreFileName(String originalFileName){
        // 원본 파일명에서 확장자 추출
        String ext = FileUtil.extractExt(originalFileName);

        // 확장자가 없는 경우 기본 확장자를 사용 (예. txt 로 설정)
        if (ext.isEmpty()) {
            ext = "txt";
        }

        return UUID.randomUUID().toString().substring(0, 5) + "." + ext;
    }

    private String getFullLogicalPath(String userName, String storeFileName, Long parentId) {

        StringBuilder pathBuilder = new StringBuilder();

        if (parentId != null) {
            pathBuilder.append(getParentFolderLogicalPath(parentId));
        } else {
            // 최상위 위치면 사용자 이름으로 시작
            pathBuilder.append(userName).append("/");
        }

        if(FileUtil.extractExt(storeFileName).isEmpty()){
            // 폴더는 끝에 / 가 붙고, 파일은 / 가 붙지 않음
            storeFileName += "/";
        }
        pathBuilder.append(storeFileName);

        return pathBuilder.toString();
    }

    private String getParentFolderLogicalPath(Long parentId) {
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

    private UrlResource getResource(String path) throws IOException {
        Path filePath = Path.of(path);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw new FileFolderNotFoundException();
        }

        return resource;
    }

    /**
     * 파일 및 폴더 권한 검사 관련 메서드
     */
    private FileFolder getFileFolderFromDatabase(Long fileId) {
        return fileFolderRepository.findById(fileId)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    private void checkOwnership(String userName, String owner, boolean isForUpload, boolean isForDownload, boolean isForUpdate) {
        if (!userName.equals(owner)) {

            if(isForUpload){
                throw new FileFolderUploadNotAllowedException();
            }else if(isForDownload){
                throw new FileFolderDownloadNotAllowedException();
            }else if(isForUpdate){
                throw new FileFolderUpdateNotAllowedException();
            }else{
                throw new FileFolderDeleteNotAllowedException();
            }

        }
    }

    private void checkIfParentIsYours(Long parentId, String userName) {
        if(parentId != null){
            FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
            checkOwnership(userName, parentFileFolder.getUserName(), true, false, false);
        }
    }

    private void checkIfParentIsFolder(Long parentId) {
        if(parentId != null){
            FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
            if(parentFileFolder.getFileFolderType() == FileFolderType.FILE){
                throw new FileFolderUploadException();
            }
        }
    }

    private FileFolder getFileFolderForDownloadAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(),false, true, false);
        return fileFolder;
    }

    private FileFolder getFileFolderForDeletionAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(),false, false, false);
        return fileFolder;
    }
}
