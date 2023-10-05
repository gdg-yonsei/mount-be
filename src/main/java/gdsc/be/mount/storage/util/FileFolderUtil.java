package gdsc.be.mount.storage.util;

import gdsc.be.mount.storage.Enum.ActionType;
import gdsc.be.mount.storage.exception.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileFolderUtil {

    @Value("${upload.path}")
    private static String uploadPath;

    public static final String DEFAULT_FILE_EXTENSION = "txt";

    public static void checkOwnership(String userName, String owner, ActionType actionType) {
        if (!userName.equals(owner)) {
            switch (actionType) {
                case UPLOAD -> throw new FileFolderUploadNotAllowedException();
                case DOWNLOAD -> throw new FileFolderDownloadNotAllowedException();
                case UPDATE -> throw new FileFolderUpdateNotAllowedException();
                case DELETE -> throw new FileFolderDeleteNotAllowedException();
                case READ -> throw new FileFolderReadNotAllowedException();
                default -> {
                }
            }
        }
    }

    public static void checkFileValidation(MultipartFile file) {
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

    public static void savePhysicalFile(MultipartFile file, String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        file.transferTo(Files.createFile(path));
    }

    public static void deletePhysicalFile(String storeFileName) {
        // 물리적 파일 삭제
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path path = Paths.get(uploadPath, storeFileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new FileFolderDeletionException();
        }
    }

    public static UrlResource getResource(String storeFileName) throws IOException {
        // 가상 폴더 구조이므로 가상 경로가 아닌 물리적인 실제 경로를 사용
        Path filePath = Paths.get(uploadPath, storeFileName);
        UrlResource resource = new UrlResource(filePath.toUri());

        // 물리적인 파일이 존재하지 않으면 예외
        if (!resource.exists()) {
            throw new FileFolderNotFoundException();
        }

        return resource;
    }

    public static boolean isFolder(String originalFilename) {
        // 확장자 별도 추출
        int pos = originalFilename.lastIndexOf(".");

        // 확장자가 없는 경우 기본 확장자 반환
        if (pos == -1 || pos == originalFilename.length() - 1) {
            return true;
        }

        return false;
    }

    public static String extractExt(String originalFilename) {
        // 확장자 별도 추출
        int pos = originalFilename.lastIndexOf(".");

        // 확장자가 없는 경우 기본 확장자 반환
        if (pos == -1 || pos == originalFilename.length() - 1) {
            return DEFAULT_FILE_EXTENSION;
        }

        return originalFilename.substring(pos + 1);
    }

    public static String generateStoreFileName(String originalFileName){
        // 원본 파일명에서 확장자 추출
        String ext = FileFolderUtil.extractExt(originalFileName);

        // 확장자가 없는 경우 기본 확장자를 사용 (예. txt 로 설정)
        if (ext.isEmpty()) {
            ext = "txt";
        }

        return UUID.randomUUID().toString().substring(0, 5) + "." + ext;
    }

    public static String generateRandomFolderName() {
        // 랜덤한 UUID를 사용하여 폴더 이름 생성
        return UUID.randomUUID().toString().substring(0, 5);
    }

}
