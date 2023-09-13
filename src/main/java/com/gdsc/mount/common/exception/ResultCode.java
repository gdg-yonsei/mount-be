package com.gdsc.mount.common.exception;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, ResultMessage.SUCCESS),
    SUCCESS_CREATED(201, ResultMessage.SUCCESS_CREATED),
    INTERNAL_SERVER_ERROR(500, ResultMessage.INTERNAL_SERVER_ERROR),
    MEMBER_NOT_FOUND(600, ResultMessage.MEMBER_NOT_FOUND),
    METADATA_NOT_FOUND(601, ResultMessage.METADATA_NOT_FOUND),
    EXISTING_USERNAME(602, ResultMessage.EXISTING_USERNAME),
    UNAUTHORIZED_DOWNLOAD_ATTEMPT(603, ResultMessage.UNAUTHORIZED_DOWNLOAD_ATTEMPT);

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public interface ResultMessage {
        String SUCCESS = "Successfully processed.";
        String MEMBER_NOT_FOUND = "No member exists with given id.";
        String METADATA_NOT_FOUND = "No metadata exists with given id.";
        String EXISTING_USERNAME = "Member with given username already exists.";
        String INTERNAL_SERVER_ERROR = "Internal server error.";
        String UNAUTHORIZED_DOWNLOAD_ATTEMPT = "You can only download the file you uploaded.";
        String SUCCESS_CREATED = "Successfully created.";
    }

}
