package gdsc.be.mount.global.common.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_ERROR                  (500, "C001", "Internal server error"),
    INVALID_PARAMETER               (400, "C002", "Invalid parameter"),
    NOT_FOUND                       (404, "C003", "Not Found"),
    METHOD_NOT_ALLOWED              (405, "C004", "Method not allowed"),
    INVALID_INPUT_VALUE             (400, "C005", "Invalid input fileFolderType"),
    INVALID_TYPE_VALUE              (400, "C006", "Invalid fileFolderType value"),
    BAD_CREDENTIALS                 (400, "C007", "Bad credentials"),
    DATABASE_CONSTRAINT_VIOLATION   (400, "C008", "Database constraint violation"),
    REFERENCE_INTEGRITY_VIOLATION   (400, "C009", "Reference integrity violation"),
    DATA_SIZE_VIOLATION             (400, "C010", "FileFolder size exceeds limit"),
    CONFLICT                        (409, "C011", "Conflict occurred"),

    // FileFolder
    FILE_NOT_FOUND                  (404, "F001", "FileFolder is not found"),
    FILE_DOWNLOAD_NOT_ALLOWED       (404, "F002", "You are not allowed to download this file"),
    FILE_DELETE_NOT_ALLOWED         (404, "F003", "You are not allowed to delete this file"),
    FILE_UPLOAD_FAILED              (500, "F004", "FileFolder upload failed"),
    FILE_DOWNLOAD_FAILED            (500, "F005", "FileFolder download failed"),
    FILE_DELETE_FAILED              (500, "F006", "FileFolder delete failed"),
    FILE_NOT_ALLOWED                (404, "F007", "FileFolder is not allowed"),
    FILE_SIZE_EXCEEDS_LIMIT         (400, "F008", "FileFolder size exceeds limit"),
    FILE_NOT_ALLOWED_EXTENSION      (400, "F009", "FileFolder is not allowed extension"),
    FILE_EMPTY                      (400, "F010", "FileFolder is empty"),

    FOLDER_CREATE_FAILED            (500, "F011", "Folder create failed");


    private final int status;
    private final String code;
    private final String message;

}