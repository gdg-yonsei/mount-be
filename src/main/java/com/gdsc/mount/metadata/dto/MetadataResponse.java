package com.gdsc.mount.metadata.dto;

import com.gdsc.mount.metadata.domain.Metadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class MetadataResponse {
    private String _id;
    private String name;
    private String username;
    private String contentType;
    private Long sizeInBytes;
    private Instant createdAt;
    private Instant updatedAt;

    protected MetadataResponse() {}

    public static MetadataResponse of(Metadata metadata) {
        return MetadataResponse.builder()
                ._id(metadata.get_id())
                .name(metadata.getName())
                .username(metadata.getUsername())
                .contentType(metadata.getContentType())
                .sizeInBytes(metadata.getSizeInBytes())
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .build();
    }
}
