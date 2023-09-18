package com.gdsc.mount.directory.service;

import com.gdsc.mount.directory.domain.Directory;
import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final DirectoryRepository directoryRepository;

    public void createDirectory(DirectoryCreateRequest request) {
        Directory directory;
        if (request.getParentDirectoryId() != null) {
            Directory parentDirectory = findParentDirectory(request.getParentDirectoryId());
            directory = new Directory(request.getName(), parentDirectory, parentDirectory.getPath() + "/" + request.getName(), false);
            parentDirectory.addDirectory(directory);
        } else {
            directory = new Directory(request.getName(), null, "/" + request.getName(), true);
        }
        directoryRepository.save(directory);
    }

    public Directory findParentDirectory(String parentDirectoryId) {
        return directoryRepository.findById(parentDirectoryId)
                .orElseThrow(() -> new NoSuchElementException("No directory found with id: " + parentDirectoryId));
    }
}
