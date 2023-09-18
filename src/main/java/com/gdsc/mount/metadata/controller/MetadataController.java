package com.gdsc.mount.metadata.controller;


import com.gdsc.mount.common.exception.ErrorResponse;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import com.gdsc.mount.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

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

    // upload file
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestBody @Valid CreateMetadataRequest request)
            throws Exception {
        String fileName = StringUtils.cleanPath(request.getName());
        String fileCode = metadataService.uploadFile(file, request);
        return ResponseEntity.status(201).body(fileCode);
    }

    // download file
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody @Valid DownloadFileRequest request) {
        Resource resource;
        try {
            resource = metadataService.downloadFile(request);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
        }

        return ResponseEntity.ok().body(resource);
    }

    // delete file
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId,
                                             @RequestBody @Valid DeleteFileRequest request) throws Exception{
        boolean success = metadataService.deleteFile(request);
        String body = success ? "success" : "fail";
        return ResponseEntity.status(204).body(body);
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

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleIOException(
            IOException exception
    ) {
        ErrorResponse response = ErrorResponse.of(
                exception,
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
