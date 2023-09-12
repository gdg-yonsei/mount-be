package gdsc.be.mount.storage.controller;

import gdsc.be.mount.global.common.response.SuccessResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final FileService fileService;

    /**
     * 파일 업로드
     * @param request
     * @param userName
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<FileUploadResponse>> uploadFile(HttpServletRequest request,
                                                                         @RequestParam("user") String userName,
                                                                         @RequestParam("file") MultipartFile file) throws IOException {
        log.info("File = {}", file);
        FileUploadResponse data = fileService.uploadFile(file, userName);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(data));
    }

    /**
     * 파일 삭제 기능
     * @param fileId
     * @return
     * @throws IOException
     */
    @DeleteMapping(value = "/{fileId}")
    public ResponseEntity<SuccessResponse<Long>> deleteFile(@PathVariable Long fileId) throws IOException {

        Long deletedFileId = fileService.deleteFile(fileId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(SuccessResponse.of(deletedFileId));
    }

}
