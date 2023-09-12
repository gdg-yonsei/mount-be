package com.gdsc.mount.metadata.dto;

import com.gdsc.mount.metadata.domain.Metadata;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MetadataResponse {
    private String _id;
    private String fileName;
    private String username;
    private String contentType;
    private Long sizeInBytes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected MetadataResponse() {}
    public MetadataResponse(String _id, String fileName, String username, String contentType, Long sizeInBytes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this._id = _id;
        this.fileName = fileName;
        this.username = username;
        this.contentType = contentType;
        this.sizeInBytes = sizeInBytes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MetadataResponse of(Metadata metadata) {
        return MetadataResponse.builder()
                ._id(metadata.get_id())
                .fileName(metadata.getFileName())
                .username(metadata.getUsername())
                .contentType(metadata.getContentType())
                .sizeInBytes(metadata.getSizeInBytes())
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .build();
    }
}
