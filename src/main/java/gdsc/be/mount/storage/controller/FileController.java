package gdsc.be.mount.storage.controller;

import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse uploadFile(HttpServletRequest request,
                             @RequestParam("user") String userName,
                             @RequestParam("file") MultipartFile file) throws IOException {
        log.info("multipartFile = {}", file);
        return fileService.uploadFile(file, userName);
    }
}
