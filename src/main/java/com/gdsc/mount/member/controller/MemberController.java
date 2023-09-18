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

}
