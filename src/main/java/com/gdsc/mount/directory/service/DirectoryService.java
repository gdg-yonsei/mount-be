package com.gdsc.mount.directory.service;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.gdsc.mount.directory.dto.DirectoryCreateRequest;
import com.gdsc.mount.directory.dto.DirectoryUpdateRequest;
import com.gdsc.mount.directory.vo.DirectoryCreateValues;
import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final static String ROOT = "Files-Upload";
    private final static int PAGE_SIZE = 15;

    private final MetadataRepository metadataRepository;

    public Metadata findDirectoryByPathIncludingDirectory(String pathIncludingDirectory) {
        return metadataRepository.findByPathWithFile(pathIncludingDirectory)
                .orElseThrow(() -> new IllegalArgumentException("No directory exists with given path: " + pathIncludingDirectory));
    }

    public List<Metadata> findAllByDirectory(String directoryPath, int page) {
        PageRequest pr = PageRequest.of(page, PAGE_SIZE);
        Page<Metadata> metadata = metadataRepository.findAllByPathWithoutFile(directoryPath, pr);
        if (metadata.getNumberOfElements() == 0) {
            throw new IllegalArgumentException("No folder or file exists with given path: " + directoryPath);
        }
        return metadata.toList();
    }

    // create
    public String createDirectory(DirectoryCreateRequest request) throws IOException {
        if (!metadataRepository.existsByPathWithFile(request.getPath())) {
            Path path = Paths.get(ROOT + request.getPath());
            Files.createDirectories(path);
        }

        String[] splitArray = request.getPath().split("/");
        String directoryPath = "/";
        for (int i = 1; i < splitArray.length; i++) {
            directoryPath += splitArray[i] + "/";
            if (!metadataRepository.existsByPathWithFile(directoryPath)) {
                metadataRepository.save(new Metadata(new DirectoryCreateValues(request.getUsername(), directoryPath)));
            }
        }
        return directoryPath;
    }

    // rename
    public String updateDirectoryName(DirectoryUpdateRequest request) throws IOException {
        checkOwner(request.getUsername(), request.getPathIncludingDirectory());
        int idx = nthLastIndexOf(2, "/", request.getPathIncludingDirectory());
        String pathForDirectory = request.getPathIncludingDirectory().substring(0, idx + 1);
        String newPathIncludingDirectory = pathForDirectory + request.getNewDirectoryName() +"/";

        if (metadataRepository.existsByPathWithFile(newPathIncludingDirectory)) {
            throw new IllegalArgumentException("Directory already exists: " + newPathIncludingDirectory);
        }
        Path oldDirectoryPath = Paths.get(ROOT + request.getPathIncludingDirectory());
        Path newDirectoryPath = Paths.get(ROOT + newPathIncludingDirectory);
        Files.move(oldDirectoryPath, newDirectoryPath, REPLACE_EXISTING);

        // optional여부는 이미 checked by if statement above
        Metadata metadata = metadataRepository.findByPathWithFile(request.getPathIncludingDirectory()).get();
        metadata.renameDirectory(newPathIncludingDirectory, pathForDirectory);
        metadataRepository.save(metadata);
        return newPathIncludingDirectory;
    }

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

    private void checkOwner(String username, String path) {
        Metadata metadata = findDirectoryByPathIncludingDirectory(path);
        if (!username.equals(metadata.getUsername())) {
            throw new IllegalArgumentException("You are not the owner of this directory");
        }
    }

    public static int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }
}
