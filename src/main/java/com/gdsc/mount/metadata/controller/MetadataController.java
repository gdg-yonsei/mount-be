package com.gdsc.mount.metadata.controller;


import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import com.gdsc.mount.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class MetadataController {
    private final MetadataService metadataService;

    // get file
    @GetMapping("/{fileId}")
    public ResponseEntity<MetadataResponse> findFileById(@PathVariable String fileId) {
        MetadataResponse response = MetadataResponse.of(metadataService.getMetadatabyId(fileId));
        return ResponseEntity.status(200).body(response);
    }

    // get all
    @GetMapping("/all")
    public ResponseEntity<List<MetadataResponse>> findFilesByPage(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.status(200).body(metadataService.getAllByPage(page, size));
    }

    // delete file
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId,
                                             @RequestBody @Valid DeleteFileRequest request) throws Exception{
        boolean success = metadataService.deleteFile(request);
        String body = success ? "success" : "fail";
        return ResponseEntity.status(204).body(body);
    }

}
