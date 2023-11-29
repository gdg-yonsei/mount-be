package com.gdsc.mount.metadata.service;


import com.gdsc.mount.metadata.domain.Metadata;
import com.gdsc.mount.metadata.dto.CreateMetadataRequest;
import com.gdsc.mount.metadata.dto.DeleteFileRequest;
import com.gdsc.mount.metadata.dto.MetadataResponse;
import com.gdsc.mount.metadata.repository.MetadataRepository;
import com.gdsc.mount.metadata.vo.CreateMetadataValues;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public boolean deleteFile(DeleteFileRequest request, String filePath) throws IOException {
        Path file = Path.of(filePath);
        Metadata metadata = findByPathIfOwner(request.getUsername(),file.toString());
        metadataRepository.deleteById(metadata.get_id());
        return metadataRepository.existsByPath(file.toString());
    }

    public Metadata findByPathIfOwner(String username, String path) throws IOException {
        Metadata metadata = findByPath(path);
        checkFileOwner(username, metadata);
        return metadata;
    }

    private void checkFileOwner(String username, Metadata metadata) throws NoSuchFileException {
        if (!metadata.getUsername().equals(username)) throw new NoSuchFileException("No metadata of yours found with given path.");
    }

    private Metadata findByPath(String path) {
        return metadataRepository.findByPath(path)
                .orElseThrow(() -> new NoSuchElementException("No metadata found with given path."));
    }

}
