package gdsc.be.mount.storage.util;

import java.util.UUID;

public class FileFolderUtil {
    public static final String DEFAULT_FILE_EXTENSION = "txt";

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
