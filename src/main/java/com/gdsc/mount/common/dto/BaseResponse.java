package com.gdsc.mount.common.dto;

import lombok.Builder;
import lombok.Getter;

import com.gdsc.mount.common.exception.ResultCode;

@Getter
@Builder
public class BaseResponse {
    private int resultCode = ResultCode.SUCCESS.getCode();
    private String resultMessage = ResultCode.SUCCESS.getMessage();

    protected BaseResponse() {}

    public BaseResponse(int resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }
}
