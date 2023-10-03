package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileFolderUpdateRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FolderService {

    private final FileFolderRepository fileFolderRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public FolderCreateResponse createFolder(FolderCreateRequest folderCreateRequest) {

        String userName = folderCreateRequest.userName();
        Long parentId = folderCreateRequest.parentId();

        // 만약 부모의 폴더의 주인이 자신이 아니라면 예외 발생
        checkIfParentIsYours(parentId, userName);

        // 만약 parentId 가 폴더가 아니라면 예외 발생
        checkIfParentIsFolder(parentId);

        String folderName = generateRandomFolderName();
        String folderLogicalPath = getFullLogicalPath(userName, folderName, parentId);

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

        // 수정하려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderUpdateNotAllowedException();
        }

        // 사용자가 생성한 폴더의 범위 내에서 동일한 폴더 이름으로 이름 수정을 할 경우 예외
        checkDuplicateFolderName(userName, newFolderName);

        String originalFolderName = fileFolder.getOriginalName();

        log.debug("[updateFolderName] FileName: {}, NewFolderName : {}", originalFolderName, newFolderName);

        // 1. DB 에서 폴더 이름 수정
        fileFolder.updateOriginalName(newFolderName);
        fileFolderRepository.save(fileFolder);

        // 2. 파일 시스템에서 폴더 이름 업데이트 -> 가상 폴더 구조를 사용하고 있으므로 물리적 이름 업데이트는 필요 없음

        return fileFolder.getId();
    }

    public FolderInfoResponse getFolderMetadata(Long folderId, String userName) {

        FileFolder fileFolder = fileFolderRepository.findById(folderId)
                .orElseThrow(FileFolderNotFoundException::new);

        // 정보를 얻으려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderUpdateNotAllowedException();
        }

        if(!fileFolder.getUserName().equals(userName)){
            throw new FileFolderDownloadNotAllowedException();
        }
        List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(fileFolder.getChildIds());

        return FolderInfoResponse.fromEntity(fileFolder, childFileFolders);
    }


    // ====================================================================================================

    private String extractExt(String originalFilename) {
        // 확장자 별도 추출
        int pos = originalFilename.lastIndexOf(".");

        // 확장자가 없는 경우 빈 문자열 반환
        if (pos == -1 || pos == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(pos + 1);
    }

    private String getFullLogicalPath(String userName, String storeFileName, Long parentId) {

        StringBuilder pathBuilder = new StringBuilder();

        if (parentId != null) {
            pathBuilder.append(getParentFolderLogicalPath(parentId));
        } else {
            // 최상위 위치면 사용자 이름으로 시작
            pathBuilder.append(userName).append("/");
        }

        if(extractExt(storeFileName).isEmpty()){
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

    private void addChildIdIntoParentFolder(Long parentId, Long childId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        parentFileFolder.addChildId(childId);
        fileFolderRepository.save(parentFileFolder);
    }

    UrlResource getResource(String path) throws IOException {
        Path filePath = Path.of(path);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw new FileFolderNotFoundException();
        }

        return resource;
    }

    private static String generateRandomFolderName() {
        // 랜덤한 UUID를 사용하여 폴더 이름 생성
        return UUID.randomUUID().toString().substring(0, 5);
    }

    private FileFolder saveFileFolderMetadataToDB(FolderCreateRequest folderCreateRequest, String folderName, String folderDir, Long parentId, String userName) {
        return fileFolderRepository.save(folderCreateRequest.toEntity(folderName, folderDir, parentId, userName));
    }

    /**
     * 파일 및 폴더 권한 검사 관련 메서드
     */
    private FileFolder getFileFolderFromDatabase(Long fileId) {
        return fileFolderRepository.findById(fileId)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    private void checkOwnership(String userName, String owner, boolean isForUpload, boolean isForUpdate) {
        if (!userName.equals(owner)) {

            if(isForUpload){
                throw new FileFolderUploadNotAllowedException();
            }else if(isForUpdate){
                throw new FileFolderUpdateNotAllowedException();
            }

        }
    }

    private void checkIfParentIsYours(Long parentId, String userName) {
        if(parentId != null){
            FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
            checkOwnership(userName, parentFileFolder.getUserName(), true, false);
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

    private FileFolder getFileFolderForUpdateAfterCheck(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(), false, true);
        return fileFolder;
    }

    private void checkDuplicateFolderName(String userName, String folderName) {
        if (fileFolderRepository.existsByUserNameAndOriginalName(userName, folderName)) {
            throw new FileFolderNameDuplicateException();
        }
    }

}