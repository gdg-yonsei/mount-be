package gdsc.be.mount.storage.controller;

import gdsc.be.mount.global.common.response.SuccessResponse;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.service.FileFolderService;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
@Validated
public class FileFolderController {

    private final FileFolderService fileFolderService;

    /**
     * 파일 업로드
     * @param file
     * @param userName
     * @param parentId 부모 폴더 id
     * @return ResponseEntity<SuccessResponse<FileUploadResponse>>
     */
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user") @NotBlank String userName,
            @RequestParam @Nullable Long parentId) {

        FileUploadResponse data = fileFolderService.uploadFile(file, userName, parentId);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(data));
    }

    /**
     * 파일 삭제 기능
     * @param fileId
     * @param userName
     * @return ResponseEntity<SuccessResponse<Long>>
     */
    @DeleteMapping(value = "/files/{fileId}")
    public ResponseEntity<SuccessResponse<Long>> deleteFile(
            @PathVariable Long fileId,
            @RequestParam("user") @NotBlank String userName) {

        Long deletedFileId = fileFolderService.deleteFile(fileId, userName);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(SuccessResponse.of(deletedFileId));
    }

    /**
     * 파일 다운로드 기능
     * @param fileId
     * @param userName
     * @return ResponseEntity<UrlResource>
     */
    @GetMapping(value = "/files/{fileId}/download")
    public ResponseEntity<UrlResource> downloadFile(
            @PathVariable Long fileId,
            @RequestParam("user") @NotBlank String userName) {

        FileDownloadResponse content = fileFolderService.downloadFile(fileId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, content.contentDisposition())
                .body(content.urlResource());
    }

    /**
     * 폴더 생성
     * @param userName 사용자 이름
     * @param parentId 부모 폴더 id
     * @return ResponseEntity<SuccessResponse<FolderCreateResponse>>
     */
    @PostMapping("/folders")
    public ResponseEntity<SuccessResponse<FolderCreateResponse>> createFolder(
            @RequestParam("user") @NotBlank String userName,
            @RequestParam @Nullable Long parentId) {
        FolderCreateResponse data = fileFolderService.createFolder(userName, parentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(data));
    }

}
