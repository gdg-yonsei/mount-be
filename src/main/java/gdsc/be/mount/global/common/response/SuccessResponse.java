package gdsc.be.mount.global.common.response;

import lombok.Getter;

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