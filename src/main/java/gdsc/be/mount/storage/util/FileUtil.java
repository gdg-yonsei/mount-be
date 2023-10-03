package gdsc.be.mount.storage.util;

public class FileUtil {
    public static final String DEFAULT_FILE_EXTENSION = "txt";

    public static String extractExt(String originalFilename) {
        // 확장자 별도 추출
        int pos = originalFilename.lastIndexOf(".");

        // 확장자가 없는 경우 기본 확장자 반환
        if (pos == -1 || pos == originalFilename.length() - 1) {
            return DEFAULT_FILE_EXTENSION;
        }

        return originalFilename.substring(pos + 1);
    }
}
