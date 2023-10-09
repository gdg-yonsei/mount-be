package gdsc.be.mount.storage.service;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import gdsc.be.mount.storage.exception.FileFolderDeletionException;
import gdsc.be.mount.storage.exception.FileFolderNameDuplicateException;
import gdsc.be.mount.storage.exception.FileFolderNotFoundException;
import gdsc.be.mount.storage.repository.FileFolderRepository;
import gdsc.be.mount.storage.util.FileFolderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 물리적인 파일 과업 관련 공통 로직 처리
 */
@Component
@RequiredArgsConstructor
public class FileFolderManager {

    @Value("${upload.path}")
    public String uploadPath;

    private final FileFolderRepository fileFolderRepository;

    public void savePhysicalFile(MultipartFile file, String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        file.transferTo(Files.createFile(path));
    }

    public void deletePhysicalFile(String storeFileName) {
        // 물리적 파일 삭제
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new FileFolderDeletionException();
        }
    }

    public UrlResource getResource(String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path filePath = Paths.get(uploadPath, storeFileName);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw new FileFolderNotFoundException();
        }

        return resource;
    }

    public void checkDuplicateName(String userName, String fileFolderName) {
        if (fileFolderRepository.existsByUserNameAndOriginalName(userName, fileFolderName)) {
            throw new FileFolderNameDuplicateException();
        }
    }

    public void validateParentFolder(Long fileId, String userName) {
        fileFolderRepository.findByIdAndTypeAndUserName(fileId, FileFolderType.FOLDER, userName)
                .orElseThrow(FileFolderNotFoundException::new);
    }

    public void addChildIdIntoParentFolder(Long parentId, Long childId, String userName) {
        // 부모 폴더는 본인이 제작한 폴더여야 함
        FileFolder parentFileFolder = fileFolderRepository.findByIdAndTypeAndUserName(parentId, FileFolderType.FOLDER, userName)
                .orElseThrow(FileFolderNotFoundException::new);
        parentFileFolder.addChildId(childId);
        fileFolderRepository.save(parentFileFolder);
    }

    public void removeChildIdFromParentFolder(Long parentId, Long childId, String userName) {
        // 부모 폴더는 본인이 제작한 폴더여야 함
        FileFolder parentFileFolder = fileFolderRepository.findByIdAndTypeAndUserName(parentId, FileFolderType.FOLDER, userName)
                .orElseThrow(FileFolderNotFoundException::new);
        parentFileFolder.removeChildId(childId);
        fileFolderRepository.save(parentFileFolder);
    }

    private String getParentFolderLogicalPath(Long parentId, String userName) {
        // 부모 폴더는 본인이 제작한 폴더여야 함
        FileFolder parentFileFolder = fileFolderRepository.findByIdAndTypeAndUserName(parentId, FileFolderType.FOLDER, userName)
                .orElseThrow(FileFolderNotFoundException::new);
        return parentFileFolder.getPath();
    }

    public String getFullLogicalPath(String userName, String storeFileName, Long parentId) {

        StringBuilder pathBuilder = new StringBuilder();

        if (parentId != null) {
            pathBuilder.append(getParentFolderLogicalPath(parentId, userName));
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

}

