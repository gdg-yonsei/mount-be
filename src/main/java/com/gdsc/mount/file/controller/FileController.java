package com.gdsc.mount.file.controller;

import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.service.DirectoryService;
import com.gdsc.mount.file.dto.FileDeleteRequest;
import com.gdsc.mount.file.dto.FileDownloadRequest;
import com.gdsc.mount.file.service.FileService;
import com.gdsc.mount.metadata.dto.MetadataCreateRequest;
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
    private final DirectoryService directoryService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("username") String username,
                                             @RequestParam("path") String path) throws Exception {
        String fileName = StringUtils.cleanPath(file.getName());
        MetadataCreateRequest request = new MetadataCreateRequest(fileName, username, path);
        directoryService.createDirectory(new DirectoryCreateRequest(path, username));
        String fileCode = fileService.uploadFile(file, request.getPath());
        metadataService.createMetadata(request, file, fileCode);
        return ResponseEntity.status(200).body(fileCode);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody @Valid FileDownloadRequest request) {
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
    public ResponseEntity<String> deleteFile(@RequestBody @Valid FileDeleteRequest request) throws Exception{
        String filePath = fileService.deleteFile(request);
        boolean success = metadataService.deleteFile(request, filePath);
        String body = success ? "success" : "fail";
        return ResponseEntity.status(204).body(body);
    }
}
