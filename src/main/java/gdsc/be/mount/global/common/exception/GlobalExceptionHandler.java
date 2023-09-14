package gdsc.be.mount.global.common.exception;
import gdsc.be.mount.global.common.Enum.ErrorCode;
import gdsc.be.mount.global.common.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import static gdsc.be.mount.global.common.Enum.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 지원하지 않는 HTTP 요청 메서드에 대한 예외 처리
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Unsupported HTTP request method: {} / {}", e.getMethod(), e.getMessage());
        final ErrorResponse response = ErrorResponse.of(METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 지정하지 않은 API URI 요청이 들어왔을 경우에 대한 예외처리
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNotFoundExceptoin(NoHandlerFoundException e) {
        log.error("NoHandlerFoundException Occurred: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 최상위 BusinessException 에 대한 예외처리
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(errorCode.getStatus()));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error("Exception Occurred: {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(INTERNAL_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}