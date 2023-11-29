package com.gdsc.mount.file.controller;

import com.gdsc.mount.file.service.FileService;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.service.MetadataService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final MetadataService metadataService;
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("username") String username,
                                             @RequestParam("path") String path,
                                             @RequestParam("atRoot") boolean atRoot) throws Exception {
        String fileName = StringUtils.cleanPath(file.getName());
        CreateMetadataRequest request = new CreateMetadataRequest(fileName, username, path, atRoot);
        String fileCode = fileService.uploadFile(file);
        metadataService.createMetadata(request, file, fileCode);
        return ResponseEntity.status(200).body(fileCode);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody @Valid DownloadFileRequest request) {
        Resource resource;
        try {
            resource = fileService.downloadFile(request);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
        }

        return ResponseEntity.ok().body(resource);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestBody @Valid DeleteFileRequest request) throws Exception{
        String filePath = fileService.deleteFile(request.getPath());
        boolean success = metadataService.deleteFile(request, filePath);
        String body = success ? "success" : "fail";
        return ResponseEntity.status(204).body(body);
    }
}
