package com.gdsc.mount.directory.controller;

import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.service.DirectoryService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/new")
    public ResponseEntity<String> createDirectory(@RequestBody @Valid DirectoryCreateRequest request) throws Exception {
        directoryService.createDirectory(request);
        return ResponseEntity.status(200).body("Directory created.");
    }
}
