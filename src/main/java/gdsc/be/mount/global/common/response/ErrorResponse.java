package gdsc.be.mount.global.common.response;

import gdsc.be.mount.global.common.Enum.ErrorCode;
import lombok.Getter;

/*
 * ErrorResponse
 *
 * {
 *    "success": false,     // API 호출 실패
 *    "code": "F001",       // 내부 ErrorCode
 *    "message": null,      // 내부 ErrorCode에 대한 메시지
 * }
 *
 */

@Getter
public class ErrorResponse extends CommonResponse{

    private final String code;
    private final String message;

    private ErrorResponse(ErrorCode code){
        super(false);
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code);
    }
}
