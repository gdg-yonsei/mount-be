package com.gdsc.mount.metadata.service;


import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.DownloadFileRequest;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import com.gdsc.mount.metadata.vo.CreateMetadataValues;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final MetadataRepository metadataRepository;

    public Metadata getMetadatabyId(String id) {
        return metadataRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No such metadata with given id."));
    }

    public List<MetadataResponse> getAllByPage(int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Metadata> metadata = metadataRepository.findAll(pr);
        if (metadata.getNumberOfElements() == 0) {
            metadata = Page.empty();
        }
        return metadata.stream()
                .map(MetadataResponse::of)
                .collect(Collectors.toList());
    }

    public void createMetadata(CreateMetadataRequest request, MultipartFile file, String fileCode) {
        Metadata metadata = new Metadata(new CreateMetadataValues(request, file, fileCode));
        metadataRepository.save(metadata);
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

    public Metadata findByPathIfOwner(String username, String path) throws IOException {
        Metadata metadata = findByPath(path);
        checkFileOwner(username, metadata);
        return metadata;
    }

    private void checkFileOwner(String username, Metadata metadata) throws IOException {
        if (!metadata.getUsername().equals(username)) throw new IOException("You are not the owner of this file.");
    }

    private Metadata findByPath(String path) {
        return metadataRepository.findByPath(path)
                .orElseThrow(() -> new NoSuchElementException("No metadata found with given path."));
    }

}
