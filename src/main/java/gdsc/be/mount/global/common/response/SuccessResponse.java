package gdsc.be.mount.global.common.response;

import lombok.Getter;

/*
 * SuccessResponse
 *
 * {
 *    "success": true,      // API 호출 성공
 *    "data": { .. }        // API 호출 성공 시 반환되는 데이터
 * }
 *
 */

@Getter
public class SuccessResponse<T> extends CommonResponse {

    private final T data;

    private SuccessResponse(T data) {
        super(true);
        this.data = data;
    }

    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data);
    }
}