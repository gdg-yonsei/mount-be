package com.gdsc.mount.common.exception;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ServiceException extends Exception {
    private final int resultCode;
    private final String resultMessage;

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode.getCode();
        this.resultMessage = resultCode.getMessage();
    }
}
