package gdsc.backend.service;

import gdsc.backend.domain.FileMetaData;
import gdsc.backend.domain.Folder;
import gdsc.backend.dto.CreateFolderRequest;
import gdsc.backend.exception.FolderNotFoundException;
import gdsc.backend.exception.StorageException;
import gdsc.backend.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    @Transactional
    public String createFolder(CreateFolderRequest request) throws FolderNotFoundException, StorageException {
        // [Error] StorageException : 해당 폴더 이름이 이미 존재할 경우
        Optional<Folder> existSameFolderName = folderRepository.findByNameAndParentId(request.getName(), request.getParentId());
        if (existSameFolderName.isPresent()) {
            throw new StorageException("Folder name already exists.");
        }

        // [Error] FolderNotFoundException : 해당 부모 폴더가 없을 경우
        Folder parentFolder = null;
        if (request.getParentId() != null) {
            parentFolder = folderRepository.findById(request.getParentId())
                    .orElseThrow(() -> new FolderNotFoundException("Parent folder not found."));
        }

        // [Success] 폴더 생성
        Folder folder = new Folder(request.getName(), request.getUserId(), parentFolder);
        folderRepository.save(folder);
        return folder.getName();
    }

    public List<Folder> getAllSubFolders(Long folderId) throws FolderNotFoundException {
        Optional<Folder> folder = folderRepository.findById(folderId);

        // [Error] FolderNotFoundException : 해당 폴더가 없을 경우
        if (folder.isEmpty()) {
            throw new FolderNotFoundException("Folder not found.");
        }

        // [Success] 해당 폴더의 하위 폴더 목록 반환
        return folder.map(Folder::getChild).orElse(null);
    }

    public List<FileMetaData> getAllFilesInFolder(Long folderId) throws FolderNotFoundException {
        Optional<Folder> folder = folderRepository.findById(folderId);

        // [Error] FolderNotFoundException : 해당 폴더가 없을 경우
        if (folder.isEmpty()) {
            throw new FolderNotFoundException("Folder not found.");
        }

        // [Success] 해당 폴더의 하위 파일 목록 반환
        return folder.map(Folder::getFiles).orElse(null);
    }


}
