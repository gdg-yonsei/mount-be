package gdsc.backend.controller;

import gdsc.backend.domain.FileMetaData;
import gdsc.backend.domain.Folder;
import gdsc.backend.dto.CreateFolderRequest;
import gdsc.backend.dto.GetFileMetaDataResponse;
import gdsc.backend.dto.GetFolderResponse;
import gdsc.backend.exception.FolderNotFoundException;
import gdsc.backend.exception.StorageException;
import gdsc.backend.service.FolderService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    // 폴더 생성
    @PostMapping("/api/folder")
    public ResponseEntity<String> createFolder(@RequestBody @Valid CreateFolderRequest request) {
        try {
            String folderName = folderService.createFolder(request);
            return ResponseEntity.ok().body("Folder created successfully! -> folder name = " + folderName);
        } catch (StorageException e) { // 같은 이름의 폴더가 이미 존재할 경우
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Folder already exists!");
        } catch (FolderNotFoundException e) { // 부모 폴더가 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parent folder not found!");
        }
    }

    // 특정 폴더 ID로 하위 폴더 목록 반환
    @GetMapping("/api/{folderId}/folders")
    public ResponseEntity<List<GetFolderResponse>> getFoldersInFolder(
            @PathVariable("folderId") Long folderId) {
        try {
            List<Folder> folders = folderService.getAllSubFolders(folderId);
            List<GetFolderResponse> response = folders.stream()
                    .map(f -> new GetFolderResponse(f.getName(), f.getUserId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body(response);
        } catch (FolderNotFoundException e) { // 해당 폴더가 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // 특정 폴더 ID로 하위 파일 목록 반환
    @GetMapping("/api/{folderId}/files")
    public ResponseEntity<List<GetFileMetaDataResponse>> getFilesInFolder(
            @PathVariable("folderId") Long folderId) {
        try {
            List<FileMetaData> files = folderService.getAllFilesInFolder(folderId);
            List<GetFileMetaDataResponse> response = files.stream()
                    .map(f -> new GetFileMetaDataResponse(f.getSaveFileName(), f.getUserId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok().body(response);
        } catch (FolderNotFoundException e) { // 해당 폴더가 없을 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
