package gdsc.backend.controller;


import gdsc.backend.exception.StorageException;
import gdsc.backend.exception.UnauthorizedAccessException;
import gdsc.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

// API 일경우 RestController
@Controller
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    // home.html 띄우기
    @RequestMapping("/uploadFile")
    public String uploadFile() {
        return "home";
    }

    // 파일 업로드
    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) throws IllegalStateException, IOException {
        try {
            storageService.store(file, userId);
            return ResponseEntity.ok().body("File uploaded successfully! -> filename = " + file.getOriginalFilename());
        } catch (StorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fail to upload file" + file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail to upload file" + file.getOriginalFilename());
        }
    }

    // 파일 다운로드
    @GetMapping("/downloadFile/{fileId}/{userId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId, @PathVariable("userId") String userId) {
        try {
            Resource resource = storageService.download(fileId, userId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (StorageException e) { // 파일 메타데이터가 없는 경우
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (UnauthorizedAccessException e) { // 권한이 없을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (FileNotFoundException e) { // 파일이 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

    }

    // 파일 삭제
    @DeleteMapping("/deleteFile/{fileId}/{userId}")
    public ResponseEntity<String> deleteOne(@PathVariable("fileId") Long fileId, @PathVariable("userId") String userId) {
        try {
            storageService.deleteOne(fileId, userId);
            return ResponseEntity.ok().body("File deleted successfully! -> fileId = " + fileId);
        } catch (StorageException e) { // 파일 메타데이터가 없는 경우
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (UnauthorizedAccessException e) { // 권한이 없을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (FileNotFoundException e) { // 파일이 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
