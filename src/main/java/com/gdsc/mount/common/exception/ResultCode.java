package com.gdsc.mount.exception;

public enum NotFoundResultCode {
    MEMBER_NOT_FOUND(600, "MEMBER_NOT_FOUND"),
    METADATA_NOT_FOUND(601, "METADATA_NOT_FOUND");

    private final int code;
    private final String message;

    private NotFoundResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
