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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FolderService {

    private final FileFolderRepository fileFolderRepository;
    private final FileFolderManager fileFolderManager;

    public FolderCreateResponse createFolder(FolderCreateRequest folderCreateRequest) {

        String userName = folderCreateRequest.userName();
        Long parentId = folderCreateRequest.parentId();

        // 부모 폴더에 대한 유효성 검증
        fileFolderManager.validateParentFolder(parentId, userName);

        String folderName = FileFolderUtil.generateRandomFolderName();
        String folderLogicalPath = fileFolderManager.getFullLogicalPath(userName, folderName, parentId);
        log.debug("[createFolder] folderName: {}, folderPath: {}", folderName, folderLogicalPath);

        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더를 생성하지 않음 (DB 에 메타데이터만 저장)
        // 1. 생성 요청 폴더의 저장
        FileFolder savedFileFolder = saveFolderMetadataForCreateRequest(folderCreateRequest, folderName, folderLogicalPath, parentId, userName);

        // 2. 생성 대상 폴더 ID 를 부모 폴더의 childId 에 추가
        fileFolderManager.addChildIdIntoParentFolder(parentId, savedFileFolder.getId(), userName);

        return FolderCreateResponse.fromEntity(savedFileFolder);

    }

    public Long updateFolderName(Long folderId, FileFolderUpdateRequest request) {

        String userName = request.userName();
        String newFolderName = request.newFolderName();

        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getUserFolder(folderId, userName);

        // 사용자가 생성한 폴더의 범위 내에서 동일한 폴더 이름으로 이름 수정을 할 경우 예외
        fileFolderManager.checkDuplicateName(userName, newFolderName);
        log.debug("[updateFolderName] FileName: {}, NewFolderName : {}", fileFolder.getOriginalName(), newFolderName);

        // 가상 폴더 구조를 사용하고 있으므로 물리적 이름 업데이트는 필요 없음
        // 폴더의 메타데이터 업데이트
        fileFolder.updateOriginalName(newFolderName);
        fileFolderRepository.save(fileFolder);

        return fileFolder.getId();
    }

    public FolderInfoResponse getFolderMetadata(Long folderId, String userName) {
        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getUserFolder(folderId, userName);
        log.debug("[getFolderMetadata] FolderName: {}", fileFolder.getOriginalName());

        // 폴더의 메타데이터 조회
        List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(fileFolder.getChildIds());

        return FolderInfoResponse.fromEntity(fileFolder, childFileFolders);
    }

    public Long deleteFolder(Long folderId, String userName) {
        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getUserFolder(folderId, userName);
        log.debug("[deleteFolder] FolderName: {}", fileFolder.getOriginalName());

        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 삭제는 필요 없음
        // 1. 대상 폴더의 삭제
        fileFolderRepository.delete(fileFolder);

        // 2. 삭제 대상 폴더의 하위의 폴더와 파일 삭제
        deleteChildren(fileFolder);

        return fileFolder.getId();
    }

    public Long moveFolder(Long folderId, FileFolderMoveRequest request) {

        String userName = request.userName();
        Long newParentFolderId = request.newParentFolderId();

        // 폴더 확인 및 권한 검사
        FileFolder fileFolder = getUserFolder(folderId, userName);

        // 부모 폴더에 대한 유효성 검증
        fileFolderManager.validateParentFolder(newParentFolderId, userName);

        // 상위 폴더가 자신의 하위 폴더로 이동할 수 없음. 이 경우 예외처리.
        if (fileFolder.getChildIds().contains(newParentFolderId)) {
            throw new FileFolderNotAllowedException(ActionType.MOVE);
        }
        log.debug("[moveFolder] FolderName: {}, NewParentFolderId: {}", fileFolder.getOriginalName(), newParentFolderId);

        // 부모 폴더가 변경되었을 경우에만 이동으로 판단하여 이동 작업 수행
        // 가상 폴더 구조를 사용하고 있으므로 물리적 폴더 이동은 필요 없음
        if (fileFolder.getParentId() != newParentFolderId) {
            // 1. 대상 폴더의 이동
            updateMetadataWhenMoveFolder(fileFolder, newParentFolderId, userName);

            // 2. 이동 대상 폴더의 하위 폴더 및 파일 이동
            updateMetadataWhenMoveChildren(fileFolder, userName);
        }

        return fileFolder.getId();
    }

    // ====================================================================================================

    private FileFolder getUserFolder(Long fileId, String userName) {
        return fileFolderRepository.findByIdAndTypeAndUserName(fileId, FileFolderType.FOLDER, userName)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    private FileFolder saveFolderMetadataForCreateRequest(FolderCreateRequest folderCreateRequest, String folderName, String folderDir, Long parentId, String userName) {
        return fileFolderRepository.save(folderCreateRequest.toEntity(folderName, folderDir, parentId, userName));
    }

    private void deleteChildren(FileFolder fileFolder) {
        // 폴더를 삭제할 때 하위 폴더 및 파일 전체 삭제, 이를 DFS 방식으로 처리
        Deque<FileFolder> stack = new ArrayDeque<>();
        stack.push(fileFolder);

        while (!stack.isEmpty()) {
            FileFolder currentFolder = stack.pop();
            List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(currentFolder.getChildIds());
            for (FileFolder childFileFolder : childFileFolders) {

                // DB 에서 currentFolder 의 하위 폴더 및 파일 메타데이터 삭제
                fileFolderRepository.delete(childFileFolder);

                // 폴더일 경우에는 stack 에 추가
                if (childFileFolder.getFileFolderType() == FileFolderType.FOLDER) {
                    stack.push(childFileFolder);
                }

                // 파일일 경우에만 물리적 파일 삭제 (폴더일 경우 가상 폴더 구조를 사용하므로 물리적 폴더는 삭제 하지 않음)
                if (childFileFolder.getFileFolderType() == FileFolderType.FILE) {
                    fileFolderManager.deletePhysicalFile(childFileFolder.getStoredName());
                }
            }
        }
    }

    private void updateMetadataWhenMoveChildren(FileFolder fileFolder, String userName) {
        // 폴더를 이동할때에 하위의 모든 자식 폴더 및 파일들도 함께 이동, 이를 DFS 방식으로 처리
        Deque<FileFolder> stack = new ArrayDeque<>();
        stack.push(fileFolder);

        while (!stack.isEmpty()) {
            FileFolder currentFolder = stack.pop();
            List<FileFolder> childFileFolders = fileFolderRepository.findChildrenByChildIds(currentFolder.getChildIds());

            // currentFolder 의 하위 폴더 및 파일 이동
            for (FileFolder childFileFolder : childFileFolders) {

                // 파일과 폴더의 경우 모두, 메타데이터 업데이트 (path 업데이트만 수행)
                childFileFolder.updatePath(fileFolderManager.getFullLogicalPath(userName, childFileFolder.getOriginalName(), childFileFolder.getParentId()));
                fileFolderRepository.save(childFileFolder);

                // 폴더일 경우에만 stack 에 추가
                if (childFileFolder.getFileFolderType() == FileFolderType.FOLDER) {
                    stack.push(childFileFolder);
                }
            }

            // 가상 폴더 구조를 사용하므로 물리적 이동은 없음
        }
    }

    private void updateMetadataWhenMoveFolder(FileFolder currentFolder, Long newParentFolderId, String userName) {
        // 이동 대상 폴더의 경우, 메타데이터 수정 시 childId 목록 수정, parentId 수정, path 수정이 필요함

        // 부모 폴더의 childId 목록에서 자식 폴더 id 삭제
        fileFolderManager.removeChildIdFromParentFolder(currentFolder.getParentId(), currentFolder.getId(), userName);

        // 새 부모 폴더의 childId 목록에 자식 폴더 id 추가
        fileFolderManager.addChildIdIntoParentFolder(newParentFolderId, currentFolder.getId(), userName);

        // 폴더의 parentId 와 path 업데이트
        currentFolder.updateParentId(newParentFolderId);
        currentFolder.updatePath(fileFolderManager.getFullLogicalPath(userName, currentFolder.getOriginalName(), newParentFolderId));
        fileFolderRepository.save(currentFolder);
    }

}
