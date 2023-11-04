package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileFolderMoveRequest;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.exception.*;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static gdsc.be.mount.storage.util.FileFolderUtil.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileService {

    private final FileFolderRepository fileFolderRepository;
    private final FileFolderManager fileFolderManager;

    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest fileUploadRequest) {
        Long parentId = fileUploadRequest.parentId();
        String userName = fileUploadRequest.userName();

        // 파일 유효성 검사
        checkFileValidation(file);

        // 부모 폴더에 대한 유효성 검증
        fileFolderManager.validateParentFolder(parentId, userName);

        String originalFileName = file.getOriginalFilename(); // 사용자가 등록한 최초 파일명
        String storeFileName = generateStoreFileName(originalFileName); // 서버 내부에서 관리할 파일명
        String logicalFilePath = fileFolderManager.getFullLogicalPath(userName, storeFileName, parentId); // 파일의 논리적 경로

        log.debug("[uploadFile] originalFileName: {}, logicalFilePath: {}", originalFileName, logicalFilePath);

        try {
            // 1. 파일 시스템에서 물리적 파일 저장
            fileFolderManager.savePhysicalFile(file, storeFileName);

            try {
                // 2. DB 에 파일 메타데이터 저장
                FileFolder savedFileFolder = saveFileMetadataForUploadRequest(fileUploadRequest, originalFileName, storeFileName, logicalFilePath, file.getSize(), file.getContentType());

                // 3. 부모 폴더에 자식 폴더 id 추가
                fileFolderManager.addChildIdIntoParentFolder(parentId, savedFileFolder.getId(), userName);

                return FileUploadResponse.fromEntity(savedFileFolder);
            } catch (Exception dbException) {
                // 만약 DB에 파일 메타데이터 저장 중에 예외가 발생하면 물리적 파일 삭제 후 예외 다시 던지기
                fileFolderManager.deletePhysicalFile(storeFileName);
                throw dbException;
            }
        } catch (IOException ex) {
            throw new FileFolderUploadException();
        }
    }

    public Long deleteFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileToDelete = getUserFile(fileId, userName);
        log.debug("[deleteFile] FileName: {}", fileToDelete.getOriginalName());

        // 1. DB 에서 파일 메타데이터 삭제
        fileFolderRepository.deleteById(fileId);

        // 2. 파일 시스템에서 물리적 파일 삭제
        fileFolderManager.deletePhysicalFile(fileToDelete.getStoredName());

        return fileToDelete.getId();
    }

    public FileDownloadResponse downloadFile(Long fileId, String userName) {
        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getUserFile(fileId, userName);

        String originalFileName = fileFolder.getOriginalName();
        String storedFileName = fileFolder.getStoredName();

        try {

            UrlResource resource = fileFolderManager.getResource(storedFileName);

            log.debug("[downloadFile] saveFileName: {}, URL Resource: {}", storedFileName, resource);

            // 다운로드 시 가독성 위해 최초 파일명 사용
            String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginalFileName + "\"";

            return new FileDownloadResponse(resource, contentDisposition);
        } catch (IOException ex){
            throw new FileFolderDownloadExpcetion();
        }
    }

    public Long moveFile(Long fileId, FileFolderMoveRequest request){

        String userName = request.userName();
        Long newParentFolderId = request.newParentFolderId();

        // 파일 확인 및 권한 검사
        FileFolder fileFolder = getUserFile(fileId, userName);

        // 부모 폴더에 대한 유효성 검증
        fileFolderManager.validateParentFolder(newParentFolderId, userName);


        log.debug("[moveFile] fileId: {}, newParentFolderId: {}", fileId, newParentFolderId);

        // 부모 폴더가 변경되었을 경우에만 이동으로 판단
        if (fileFolder.getParentId() != newParentFolderId) {
            moveFileToNewParentFolder(fileFolder, newParentFolderId, userName);
        }

        return fileFolder.getId();
    }

    // ====================================================================================================

    private FileFolder getUserFile(Long fileId, String userName) {
        return fileFolderRepository.findByIdAndTypeAndUserName(fileId, FileFolderType.FILE, userName)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    private FileFolder saveFileMetadataForUploadRequest(FileUploadRequest fileUploadRequest, String originalFileName, String storeFileName, String logicalFilePath, long fileSize, String fileType) {
        return fileFolderRepository.save(fileUploadRequest.toEntity(originalFileName, storeFileName, logicalFilePath, fileSize, fileType));
    }

    private void moveFileToNewParentFolder(FileFolder fileFolder, Long newParentFolderId, String userName) {
        // 1. 부모 폴더의 childId 목록에서 자식 폴더 id 삭제
        fileFolderManager.removeChildIdFromParentFolder(fileFolder.getParentId(), fileFolder.getId(), userName);

        // 2. 새 부모 폴더의 childId 목록에 자식 폴더 id 추가
        fileFolderManager.addChildIdIntoParentFolder(newParentFolderId, fileFolder.getId(), userName);

        // 3. 폴더의 parentId 와 path 업데이트
        fileFolder.updateParentId(newParentFolderId);
        fileFolder.updatePath(fileFolderManager.getFullLogicalPath(userName, fileFolder.getStoredName(), newParentFolderId));
        fileFolderRepository.save(fileFolder);

        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 이동은 필요 없음
    }
}
