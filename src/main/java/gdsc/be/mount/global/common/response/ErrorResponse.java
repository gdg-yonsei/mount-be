package gdsc.be.mount.global.common.response;

import lombok.Getter;

@Getter
public class ErrorResponse<T> extends CommonResponse{

    private final T error;

    private ErrorResponse(T error){
        super(false);
        this.error = error;
    }

    public static <T> ErrorResponse<T> of(T error) {
        return new ErrorResponse<>(error);
    }
}
