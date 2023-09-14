package gdsc.backend.controller;


import gdsc.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        storageService.store(file, userId);
        return ResponseEntity.ok().body("File uploaded successfully! -> filename = " + file.getOriginalFilename());
    }

    // 파일 다운로드
    @GetMapping("/downloadFile/{uuid}/{userId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("uuid") String uuid, @PathVariable("userId") String userId) {
        Resource resource = storageService.download(uuid, userId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // 파일 삭제
    @DeleteMapping("/deleteFile/{uuid}/{userId}")
    public ResponseEntity<String> deleteOne(@PathVariable("uuid") String uuid, @PathVariable("userId") String userId) {
        try {
            storageService.deleteOne(uuid, userId);
            return ResponseEntity.ok().body("File deleted successfully! -> filename = " + uuid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fail to delete file");
        }
    }

}
