package com.gdsc.mount.metadata.service;

import com.gdsc.mount.common.domain.NodeType;
import com.gdsc.mount.directory.domain.Directory;
import com.gdsc.mount.directory.service.DirectoryService;
import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final MetadataRepository metadataRepository;
    private final DirectoryService directoryService;

    public Metadata getMetadatabyId(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No such metadata with given id."));
    }

    public List<MetadataResponse> getAllByPage(int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Metadata> fileInfos = metadataRepository.findAll(pr);
        if (fileInfos.getNumberOfElements() == 0) {
            fileInfos = Page.empty();
        }
        return fileInfos.stream()
                .map(MetadataResponse::of)
                .collect(Collectors.toList());
    }

    public String uploadFile(MultipartFile file, CreateMetadataRequest request) throws Exception {
        Path uploadPath = Paths.get(request.getPath());
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new Exception("Failed to create directory.");
            }
        }
        String fileCode = RandomStringUtils.randomAlphanumeric(10);
        Directory parentDirectory = directoryService.findParentDirectoryByPath(request.getPath());


        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileCode + "-" + request.getPath());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            metadataRepository.save(new Metadata(fileCode, request.getName(), parentDirectory, request.getPath(), request.isAtRoot(), request.getUsername(), file, "/download/" + fileCode));
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + request.getName());
        }
        return fileCode;
    }

    public Resource downloadFile(DownloadFileRequest request) throws IOException {

        Path foundFile = null;
        Metadata metadata = findByPath(request.getPath());
        checkFileOwner(request.getUsername(), metadata);

        String pathWithoutTarget = request.getPath().substring(0, request.getPath().lastIndexOf("/"));
        Path path = Paths.get(pathWithoutTarget);

        for (Path file : Files.list(path).collect(Collectors.toList())) {
            if (file.getFileName().toString().startsWith(metadata.get_id())) {
                foundFile = file;
            }
        }

        if (foundFile != null) return new UrlResource(foundFile.toUri());
        return null;
    }

    public boolean deleteFile(DeleteFileRequest request) throws IOException {
        Metadata metadata = findByPath(request.getPath());
        String pathWithoutTarget = request.getPath().substring(0, request.getPath().lastIndexOf("/"));
        checkFileOwner(request.getUsername(), metadata);
        File file = FileUtils.getFile(pathWithoutTarget + metadata.get_id());
        boolean success = FileUtils.deleteQuietly(file);
        metadataRepository.deleteById(metadata.get_id());
        return success;
    }


    private void checkFileOwner(String username, Metadata metadata) throws IOException {
        if (!metadata.getUsername().equals(username)) throw new IOException("You are not the owner of this file.");
    }

    public Metadata findByPath(String path) {
        return metadataRepository.findByPath(path)
                .orElseThrow(() -> new NoSuchElementException("No metadata found with given path."));
    }

}
