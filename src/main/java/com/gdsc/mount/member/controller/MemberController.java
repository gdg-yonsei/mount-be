package com.gdsc.mount.member.controller;

import com.gdsc.mount.common.exception.ErrorResponse;
import com.gdsc.mount.member.dto.CreateMemberRequest;
import com.gdsc.mount.member.dto.MemberResponse;
import com.gdsc.mount.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;


import java.util.NoSuchElementException;

@Controller
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;


    // get member
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable String memberId) {
        return ResponseEntity.ok().body(memberService.findMemberById(memberId));
    }

    // create member
    @PostMapping("/new")
    public ResponseEntity<MemberResponse> registerMember(@RequestBody CreateMemberRequest request) {
        MemberResponse response = memberService.createMember(request.getUsername());
        return ResponseEntity.status(201).body(response);
    }

    // delete member
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable String memberId) {
        memberService.deleteMemberById(memberId);
        return ResponseEntity.status(204).body("Successfully deleted.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoSuchElementFoundException(
            NoSuchElementException exception, WebRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                exception,
                "No such element exists.",
                HttpStatus.NOT_FOUND,
                request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation error. Check 'errors' field for details."
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(),
                    fieldError.getDefaultMessage());
        }
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            WebRequest request){
        ErrorResponse response = ErrorResponse.of(exception,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception exception,
            WebRequest request){
        ErrorResponse response = ErrorResponse.of(exception,
                "An unexpected error occurred.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


}
