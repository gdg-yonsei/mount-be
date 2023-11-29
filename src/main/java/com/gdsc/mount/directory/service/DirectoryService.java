package com.gdsc.mount.directory.service;

import com.gdsc.mount.directory.domain.Directory;
import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.repository.DirectoryRepository;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final static String ROOT = "Files-Upload";

    private final DirectoryRepository directoryRepository;
    // create
    public void createDirectory(DirectoryCreateRequest request) throws IOException {
        if (!directoryRepository.existsByPathIncludingDirectory(request.getPath())) {
            Path directoryPath = Paths.get(ROOT + request.getPath());
            Files.createDirectories(directoryPath);
            directoryRepository.save(new Directory(request.getPath(), request.getUsername()));
        }
    }

    // rename
    // delete
    public void deleteDirectory(String path) throws IOException {
        Path directoryPath = Paths.get(path);
        Files.walkFileTree(directoryPath, new SimpleFileVisitor<>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // move
}
