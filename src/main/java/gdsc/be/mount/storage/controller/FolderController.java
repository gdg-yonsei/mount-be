package gdsc.be.mount.storage.controller;

import gdsc.be.mount.global.common.response.SuccessResponse;
import gdsc.be.mount.storage.dto.request.FileFolderUpdateRequest;
import gdsc.be.mount.storage.dto.request.FolderCreateRequest;
import gdsc.be.mount.storage.dto.response.FolderCreateResponse;
import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
import gdsc.be.mount.storage.service.FolderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/folders")
@Slf4j
@Validated
public class FolderController {

    private final FolderService folderService;

    /**
     * 폴더 생성 기능
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<FolderCreateResponse>> createFolder(
            @RequestBody @Valid FolderCreateRequest folderCreateRequest
    ) {
        FolderCreateResponse data = folderService.createFolder(folderCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(data));
    }

    /**
     * 폴더 이름 변경
     */
    @PatchMapping("/{folderId}")
    public ResponseEntity<SuccessResponse<Long>> updateFolderName(
            @PathVariable Long folderId,
            @RequestBody @Valid FileFolderUpdateRequest request
    ) {
        Long data = folderService.updateFolderName(folderId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

    /**
     * 특정 폴더에 대한 요청 시 폴더에 포함된 파일 및 폴더의 메타데이터 목록을 반환
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<SuccessResponse<FolderInfoResponse>> getFolderMetadata(
            @PathVariable Long folderId,
            @RequestParam("user") @NotBlank String userName
    ) {
        FolderInfoResponse data = folderService.getFolderMetadata(folderId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

    /**
     * 폴더 삭제 기능
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<SuccessResponse<Long>> deleteFolder(
            @PathVariable Long folderId,
            @RequestParam("user") @NotBlank String userName
    ) {
        Long data = folderService.deleteFolder(folderId, userName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(data));
    }

}
