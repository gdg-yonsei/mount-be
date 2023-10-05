package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.ActionType;
import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.dto.request.FileFolderMoveRequest;
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

        // 부모 폴더에 대한 유효성 검증
        checkParentFolderValidation(parentId, userName);

        String folderName = FileFolderUtil.generateRandomFolderName();
        String folderLogicalPath = getFullLogicalPath(userName, folderName, parentId);
        log.debug("[createFolder] folderName: {}, folderPath: {}", folderName, folderLogicalPath);

        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더를 생성하지 않음 (DB 에 메타데이터만 저장)
        // 1. 생성 요청 폴더의 저장
        FileFolder savedFileFolder = saveFileFolderMetadataToDB(folderCreateRequest, folderName, folderLogicalPath, parentId, userName);

        // 2. 생성 대상 폴더 ID 를 부모 폴더의 childId 에 추가
        if (parentId != null) {
            addChildIdIntoParentFolder(parentId, savedFileFolder.getId());
        }

        return FolderCreateResponse.fromEntity(savedFileFolder);

    }

    public Long updateFolderName(Long folderId, FileFolderUpdateRequest request) {

        String userName = request.userName();
        String newFolderName = request.newFolderName();

        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForUpdateAfterCheckValidation(folderId, userName);

        // 사용자가 생성한 폴더의 범위 내에서 동일한 폴더 이름으로 이름 수정을 할 경우 예외
        checkDuplicateFolderName(userName, newFolderName);
        log.debug("[updateFolderName] FileName: {}, NewFolderName : {}", fileFolder.getOriginalName(), newFolderName);

        // 가상 폴더 구조를 사용하고 있으므로 물리적 이름 업데이트는 필요 없음
        // 폴더의 메타데이터 업데이트
        fileFolder.updateOriginalName(newFolderName);
        fileFolderRepository.save(fileFolder);

        return fileFolder.getId();
    }

    public FolderInfoResponse getFolderMetadata(Long folderId, String userName) {

        FileFolder fileFolder = fileFolderRepository.findById(folderId)
                .orElseThrow(FileFolderNotFoundException::new);

        // 폴더 확인 및 권한 검사
        checkFolderForRead(fileFolder, userName);
        log.debug("[getFolderMetadata] FolderName: {}", fileFolder.getOriginalName());

        // 폴더의 메타데이터 조회
        List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(fileFolder.getChildIds());

        return FolderInfoResponse.fromEntity(fileFolder, childFileFolders);
    }

    public Long deleteFolder(Long folderId, String userName) {

        FileFolder fileFolder = fileFolderRepository.findById(folderId)
                .orElseThrow(FileFolderNotFoundException::new);

        // 폴더 확인 및 권한 검사
        checkFolderForDeletion(fileFolder, userName);
        log.debug("[deleteFolder] FolderName: {}", fileFolder.getOriginalName());

        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 삭제는 필요 없음
        // 1. 삭제 대상 폴더 삭제
        fileFolderRepository.delete(fileFolder);

        // 2. 하위의 폴더와 파일 삭제
        deleteChildFolder(fileFolder);

        return fileFolder.getId();
    }

    public Long moveFolder(Long folderId, FileFolderMoveRequest request) {

        String userName = request.userName();
        Long newParentFolderId = request.newParentFolderId();

        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getFileFolderForUpdateAfterCheckValidation(folderId, userName);

        // 부모 폴더에 대한 유효성 검증
        checkParentFolderValidation(newParentFolderId, userName);

        // 상위 폴더가 자신의 하위 폴더로 이동할 수 없음. 이 경우 예외처리.
        if (fileFolder.getChildIds().contains(newParentFolderId)) {
            throw new FileFolderMoveNotAllowedException();
        }

        // 부모 폴더가 변경되었을 경우에만 이동으로 판단하여 이동 작업 수행
        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 이동은 필요 없음
        if (fileFolder.getParentId() != newParentFolderId) {
            // 1. 대상 폴더의 이동
            updateMetadataForMoveFolder(fileFolder, newParentFolderId, userName);

            // 2. 이동 대상 폴더의 하위 폴더 및 파일 이동
            updateMetadataForMoveChildren(fileFolder, userName);
        }

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

        if(FileFolderUtil.isFolder(storeFileName)){
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

    private void removeChildIdFromParentFolder(Long parentId, Long childId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        parentFileFolder.removeChildId(childId);
        fileFolderRepository.save(parentFileFolder);
    }

    private FileFolder saveFileFolderMetadataToDB(FolderCreateRequest folderCreateRequest, String folderName, String folderDir, Long parentId, String userName) {
        return fileFolderRepository.save(folderCreateRequest.toEntity(folderName, folderDir, parentId, userName));
    }

    private void deleteChildFolder(FileFolder fileFolder) {
        // 폴더를 삭제할 때 하위 폴더 및 파일 전체 삭제, 이를 DFS 방식으로 처리
        Deque<FileFolder> stack = new ArrayDeque<>();
        stack.push(fileFolder);

        while (!stack.isEmpty()) {
            FileFolder currentFolder = stack.pop();

            // 1. currentFolder 의 메타데이터 삭제
            fileFolderRepository.delete(currentFolder);

            // 2. currentFolder 의 하위 폴더 및 파일 삭제
            List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(currentFolder.getChildIds());
            for (FileFolder childFileFolder : childFileFolders) {
                stack.push(childFileFolder);

                // DB 에서 currentFolder 의 하위 폴더 및 파일 메타데이터 삭제
                fileFolderRepository.delete(childFileFolder);

                // 폴더일 경우에는 childId 에 해당되는 객체를 stack 에 추가
                if (childFileFolder.getFileFolderType() == FileFolderType.FOLDER) {
                    fileFolderRepository.findChildrenByChildIds(childFileFolder.getChildIds())
                            .forEach(stack::push);
                }

                // 파일일 경우에만 물리적 파일 삭제 (폴더일 경우 가상 폴더 구조를 사용하므로 물리적 폴더는 삭제 하지 않음)
                if (childFileFolder.getFileFolderType() == FileFolderType.FILE) {
                    deletePhysicalFile(childFileFolder);
                }
            }
        }
    }

    private void deletePhysicalFile(FileFolder fileFolder) {
        // 물리적 파일 삭제
        Path path = Paths.get(uploadPath, fileFolder.getStoredName());
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new FileFolderDeletionException();
        }
    }

    private void updateMetadataForMoveChildren(FileFolder fileFolder, String userName) {
        // 폴더를 이동할때에 하위의 모든 자식 폴더 및 파일들도 함께 이동, 이를 DFS 방식으로 처리
        Deque<FileFolder> stack = new ArrayDeque<>();
        stack.push(fileFolder);

        while (!stack.isEmpty()) {
            FileFolder currentFolder = stack.pop();
            List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(currentFolder.getChildIds());

            // currentFolder 의 하위 폴더 및 파일 이동
            for (FileFolder childFileFolder : childFileFolders) {
                stack.push(childFileFolder);

                // DB 에서 currentFolder 의 하위 폴더 및 파일 메타데이터 업데이트 (path 업데이트만 수행)
                childFileFolder.updatePath(getFullLogicalPath(userName, childFileFolder.getOriginalName(), childFileFolder.getParentId()));
                fileFolderRepository.save(childFileFolder);

                // 폴더일 경우에는 childId 에 해당되는 객체를 stack 에 추가
                if (childFileFolder.getFileFolderType() == FileFolderType.FOLDER) {
                    fileFolderRepository.findChildrenByChildIds(childFileFolder.getChildIds())
                            .forEach(stack::push);
                }
            }

            // 가상 폴더 구조를 사용하므로 물리적 이동은 없음
        }
    }

    private void updateMetadataForMoveFolder(FileFolder currentFolder, Long newParentFolderId, String userName) {
        // 이동 대상 폴더의 경우, 메타데이터 수정 시 childId 목록 수정, parentId 수정, path 수정이 필요함

        // 부모 폴더의 childId 목록에서 자식 폴더 id 삭제
        if (currentFolder.getParentId() != null) {
            removeChildIdFromParentFolder(currentFolder.getParentId(), currentFolder.getId());
        }
        // 새 부모 폴더의 childId 목록에 자식 폴더 id 추가
        if (newParentFolderId != null) {
            addChildIdIntoParentFolder(newParentFolderId, currentFolder.getId());
        }
        // 폴더의 parentId 와 path 업데이트
        currentFolder.updateParentId(newParentFolderId);
        currentFolder.updatePath(getFullLogicalPath(userName, currentFolder.getOriginalName(), newParentFolderId));
        fileFolderRepository.save(currentFolder);
    }

    /**
     * 검증 관련 메서드
     */

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

    private void checkParentFolderValidation(Long parentId, String userName) {
        if(parentId != null){
            // 만약 parentId 가 폴더가 아니라면 예외 발생
            checkIfParentIsFolder(parentId);
            // 만약 부모의 폴더의 주인이 자신이 아니라면 예외 발생
            checkParentFolderOwnershipForUpload(parentId, userName);
        }
    }

    private void checkIfParentIsFolder(Long parentId) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        if(parentFileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderUploadException();
        }
    }

    private void checkParentFolderOwnershipForUpload(Long parentId, String userName) {
        FileFolder parentFileFolder = fileFolderRepository.findById(parentId).orElseThrow();
        checkOwnership(userName, parentFileFolder.getUserName(), ActionType.UPLOAD);
    }

    private FileFolder getFileFolderForUpdateAfterCheckValidation(Long fileId, String userName) {
        FileFolder fileFolder = fileFolderRepository.findById(fileId)
                .orElseThrow(FileFolderNotFoundException::new);

        // 수정하려는 대상이 자신이 생성한 폴더인지 확인
        checkOwnership(userName, fileFolder.getUserName(), ActionType.UPDATE);

        // 수정하려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderUpdateNotAllowedException();
        }
        return fileFolder;
    }

    private void checkDuplicateFolderName(String userName, String folderName) {
        if (fileFolderRepository.existsByUserNameAndOriginalName(userName, folderName)) {
            throw new FileFolderNameDuplicateException();
        }
    }

    private void checkFolderForRead(FileFolder fileFolder, String userName) {
        // 정보를 얻으려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderUpdateNotAllowedException();
        }

        // 정보를 얻으려는 대상이 자신이 생성한 폴더인지 확인
        checkOwnership(userName, fileFolder.getUserName(), ActionType.READ);
    }

    private void checkFolderForDeletion(FileFolder fileFolder, String userName) {
        // 삭제하려는 대상이 폴더인지 확인
        if(fileFolder.getFileFolderType() == FileFolderType.FILE){
            throw new FileFolderDeletionException();
        }

        // 삭제하려는 대상이 자신이 생성한 폴더인지 확인
        checkOwnership(userName, fileFolder.getUserName(), ActionType.DELETE);
    }

}
