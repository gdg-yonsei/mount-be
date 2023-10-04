package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.ActionType;
import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileFolderUpdateRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.exception.*;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import gdsc.be.mount.storage.util.FileFolderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

        // 만약 parentId 가 폴더가 아니라면 예외 발생
        checkIfParentIsFolder(parentId);

        // 만약 부모의 폴더의 주인이 자신이 아니라면 예외 발생
        checkParentFolderOwnershipForUpload(parentId, userName);

        String folderName = FileFolderUtil.generateRandomFolderName();
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
        FileFolder fileFolder = getFileFolderForUpdateAfterCheckOwnership(folderId, userName);

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

        // 정보를 얻으려는 대상이 자신이 생성한 폴더인지 확인
        checkOwnership(userName, fileFolder.getUserName(), ActionType.READ);

        List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(fileFolder.getChildIds());

        return FolderInfoResponse.fromEntity(fileFolder, childFileFolders);
    }

    public Long deleteFolder(Long folderId, String userName) {
        FileFolder fileFolder = fileFolderRepository.findById(folderId)
                .orElseThrow(FileFolderNotFoundException::new);

        // 삭제하려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderDeletionException();
        }

        // 삭제하려는 대상이 자신이 생성한 폴더인지 확인
        checkOwnership(userName, fileFolder.getUserName(), ActionType.DELETE);

        log.debug("[deleteFolder] FolderName: {}", fileFolder.getOriginalName());

        // 1. DB에서 해당 폴더의 메타데이터 삭제
        fileFolderRepository.delete(fileFolder);

        // 2. 하위의 폴더와 파일 삭제 시 DFS 방식으로 처리
        deleteChildFileFolder(fileFolder);

        return fileFolder.getId();
    }


    // ====================================================================================================

    private String getFullLogicalPath(String userName, String storeFileName, Long parentId) {

        StringBuilder pathBuilder = new StringBuilder();

        if (parentId != null) {
            pathBuilder.append(getParentFolderLogicalPath(parentId));
        } else {
            // 최상위 위치면 사용자 이름으로 시작
            pathBuilder.append(userName).append("/");
        }

        if(FileFolderUtil.extractExt(storeFileName).isEmpty()){
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

    private FileFolder saveFileFolderMetadataToDB(FolderCreateRequest folderCreateRequest, String folderName, String folderDir, Long parentId, String userName) {
        return fileFolderRepository.save(folderCreateRequest.toEntity(folderName, folderDir, parentId, userName));
    }

    private void deleteChildFileFolder(FileFolder fileFolder) {
        Deque<FileFolder> stack = new ArrayDeque<>();
        stack.push(fileFolder);

        while (!stack.isEmpty()) {
            FileFolder currentFolder = stack.pop();

            if (currentFolder.getFileFolderType() == FileFolderType.FOLDER) {
                List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(currentFolder.getChildIds());

                // DB 에서 하위 폴더 및 파일들의 메타 데이터 삭제
                fileFolderRepository.deleteAll(childFileFolders);

                for (FileFolder childFileFolder : childFileFolders) {
                    stack.push(childFileFolder);

                    // 파일일 경우에만 물리적 파일 삭제 (폴더일 경우 가상 폴더 구조를 사용하므로 물리적 폴더는 삭제 하지 않음)
                    if (childFileFolder.getFileFolderType() == FileFolderType.FILE) {
                        Path path = Paths.get(uploadPath, childFileFolder.getStoredName());
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new FileFolderDeletionException();
                        }
                    }
                }
            }
        }
    }

    /**
     * 검증 관련 메서드
     */
    private FileFolder getFileFolderFromDatabase(Long fileId) {
        return fileFolderRepository.findById(fileId)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    private void checkOwnership(String userName, String owner, ActionType actionType) {
        if (!userName.equals(owner)) {
            switch (actionType) {
                case UPLOAD -> throw new FileFolderUploadNotAllowedException();
                case UPDATE -> throw new FileFolderUpdateNotAllowedException();
                case READ -> throw new FileFolderReadNotAllowedException();
                default -> {
                }
            }
        }
    }

    private void checkParentFolderOwnershipForUpload(Long parentId, String userName) {
        if(parentId != null){
            FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
            checkOwnership(userName, parentFileFolder.getUserName(), ActionType.UPLOAD);
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

    private FileFolder getFileFolderForUpdateAfterCheckOwnership(Long fileId, String userName) {
        FileFolder fileFolder = getFileFolderFromDatabase(fileId);
        checkOwnership(userName, fileFolder.getUserName(), ActionType.UPDATE);
        return fileFolder;
    }

    private void checkDuplicateFolderName(String userName, String folderName) {
        if (fileFolderRepository.existsByUserNameAndOriginalName(userName, folderName)) {
            throw new FileFolderNameDuplicateException();
        }
    }

}
