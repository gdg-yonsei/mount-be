package com.gdsc.mount.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public static final String TRACE = "trace";

    private final int status;
    private final String message;
    private String stackTrace;
    private List<ValidationError> errors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(int status, String message, String stackTrace) {
        this.status = status;
        this.message = message;
        this.stackTrace = stackTrace;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ValidationError {
        private final String field;
        private final String message;
    }

    public void addValidationError(String field, String message){
        if(Objects.isNull(errors)){
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }

    public static ErrorResponse of(
            Exception exception,
            String message,
            HttpStatus httpStatus,
            WebRequest request
    ) {
        ErrorResponse errorResponse;

        if(isTraceOn(request)){
            errorResponse = new ErrorResponse(httpStatus.value(), message, ExceptionUtils.getStackTrace(exception));
        }
        errorResponse = new ErrorResponse(httpStatus.value(), message);
        return errorResponse;
    }

    private static boolean isTraceOn(WebRequest request) {
        String [] value = request.getParameterValues(TRACE);
        return Objects.nonNull(value)
                && value.length > 0
                && value[0].contentEquals("true");
    }

}
