package gdsc.be.mount.storage.controller;

import gdsc.be.mount.global.common.response.SuccessResponse;
import gdsc.be.mount.storage.dto.response.FileDownloadResponse;
import gdsc.be.mount.storage.dto.response.FileUploadResponse;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
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

    /**
     * 폴더 이름 변경
     * @param folderId 폴더 id
     * @param userName 사용자 이름
     * @param newFolderName 새로운 폴더 이름
     */
    @PutMapping("/folders/{folderId}")
    public ResponseEntity<SuccessResponse<Long>> updateFolderName(
            @PathVariable Long folderId,
            @RequestParam("user") @NotBlank String userName,
            @RequestParam("new") @NotBlank String newFolderName) {
        Long data = fileFolderService.updateFolderName(folderId, userName, newFolderName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

    /**
     * 특정 폴더에 대한 요청 시 폴더에 포함된 파일 및 폴더의 메타데이터 목록을 반환
     * @param folderId 폴더 id
     *                 null 일 경우 최상위 폴더의 메타데이터 목록을 반환
     *                 null 이 아닐 경우 해당 폴더의 메타데이터 목록을 반환
     * @param userName 사용자 이름
     */
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<SuccessResponse<FolderInfoResponse>> getFolderMetadata(
            @PathVariable @Nullable Long folderId,
            @RequestParam("user") @NotBlank String userName) {
        FolderInfoResponse data = fileFolderService.getFolderMetadata(folderId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

}
