package com.gdsc.mount.directory.controller;

import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.dto.DirectoryUpdateRequest;
import com.gdsc.mount.directory.service.DirectoryService;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    @PostMapping("/new")
    public ResponseEntity<String> createDirectory(@RequestBody @Valid DirectoryCreateRequest request) throws Exception {
        String directoryPath = directoryService.createDirectory(request);
        return ResponseEntity.status(200).body("Directory created: " + directoryPath);
    }

    @PutMapping("/rename")
    public ResponseEntity<String> renameDirectory(@RequestBody @Valid DirectoryUpdateRequest request) throws Exception {
        String directoryPath = directoryService.updateDirectoryName(request);
        return ResponseEntity.status(200).body("Directory renamed: "+directoryPath);
    }

    @GetMapping("/contents")
    public ResponseEntity<List<MetadataResponse>> viewDirectoryContents(@RequestParam String path, @RequestParam int page) throws Exception {
        List<MetadataResponse> metadataResponses = directoryService.findAllByDirectory(path, page)
                .stream()
                .map(MetadataResponse::of)
                .collect(Collectors.toList());
        return ResponseEntity.status(200)
                .body(metadataResponses);
    }

}
