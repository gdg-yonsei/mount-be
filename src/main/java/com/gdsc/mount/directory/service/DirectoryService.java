package com.gdsc.mount.directory.service;

import com.gdsc.mount.directory.domain.Directory;
import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.repository.DirectoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final DirectoryRepository directoryRepository;

    public void createDirectory(DirectoryCreateRequest request) {
        Directory directory;
        if (request.getPath() == null || request.getPath().isEmpty() || request.getPath().equals("/")) {
            directory = new Directory(request.getName(), null, "/" + request.getName(), true);
        } else {
            Directory parentDirectory = findParentDirectoryByPath(request.getPath());
            directory = new Directory(request.getName(), parentDirectory, parentDirectory.getPath() + "/" + request.getName(), false);
            parentDirectory.addDirectory(directory);
        }
        directoryRepository.save(directory);
    }

    public Directory findDirectoryById(String directoryId) {
        return directoryRepository.findById(directoryId)
                .orElseThrow(() -> new NoSuchElementException("No directory found with id: " + directoryId));
    }

    public Directory findParentDirectoryByPath(String path) {
        String[] pathParts = path.split("/");
        String parentName = pathParts[pathParts.length - 2];
        return findByPath(path, parentName);
    }

    public Directory findDirectoryByPath(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        return findByPath(path, fileName);
    }

    public Directory findByPath(String path, String target) {
        List<Directory> directoryCandidates = directoryRepository.findAllByName(target);
        if (directoryCandidates.isEmpty()) {
            throw new NoSuchElementException("No directory found with given name.");
        } else if (directoryCandidates.size() == 1) {
            return directoryCandidates.get(0);
        } else {
            for (Directory directoryCandidate : directoryCandidates) {
                if (directoryCandidate.getPath().equals(path)) {
                    return directoryCandidate;
                }
            }
            throw new NoSuchElementException("No directory found with given name.");
        }
    }
}
