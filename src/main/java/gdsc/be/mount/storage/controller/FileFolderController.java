package gdsc.be.mount.storage.controller;

import gdsc.be.mount.global.common.response.SuccessResponse;
import gdsc.be.mount.storage.dto.request.FileFolderUpdateRequest;
import gdsc.be.mount.storage.dto.request.FileUploadRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
import gdsc.be.mount.storage.service.FileFolderService;
import jakarta.validation.Valid;
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
     * 파일 업로드 기능
     */
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse<FileUploadResponse>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") @Valid FileUploadRequest fileUploadRequest
    ) {

        FileUploadResponse data = fileFolderService.uploadFile(file, fileUploadRequest);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(data));
    }

    /**
     * 파일 삭제 기능
     */
    @DeleteMapping(value = "/files/{fileId}")
    public ResponseEntity<SuccessResponse<Long>> deleteFile(
            @PathVariable Long fileId,
            @RequestParam("user") @NotBlank String userName
    ) {
        Long deletedFileId = fileFolderService.deleteFile(fileId, userName);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(SuccessResponse.of(deletedFileId));
    }

    /**
     * 파일 다운로드 기능
     */
    @GetMapping(value = "/files/{fileId}/download")
    public ResponseEntity<UrlResource> downloadFile(
            @PathVariable Long fileId,
            @RequestParam("user") @NotBlank String userName
    ) {
        FileDownloadResponse content = fileFolderService.downloadFile(fileId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, content.contentDisposition())
                .body(content.urlResource());
    }


    /**
     * 폴더 생성 기능
     */
    @PostMapping("/folders")
    public ResponseEntity<SuccessResponse<FolderCreateResponse>> createFolder(
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        FolderCreateResponse data = fileFolderService.createFolder(folderCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(data));
    }


    /**
     * 폴더 이름 변경
     */
    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<SuccessResponse<Long>> updateFolderName(
            @PathVariable Long folderId,
            @RequestBody @Valid FileFolderUpdateRequest request
    ) {
        Long data = fileFolderService.updateFolderName(folderId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

    /**
     * 특정 폴더에 대한 요청 시 폴더에 포함된 파일 및 폴더의 메타데이터 목록을 반환
     */
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<SuccessResponse<FolderInfoResponse>> getFolderMetadata(
            @PathVariable Long folderId,
            @RequestParam("user") @NotBlank String userName
    ) {
        FolderInfoResponse data = fileFolderService.getFolderMetadata(folderId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

}
