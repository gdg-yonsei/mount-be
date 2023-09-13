package com.gdsc.mount.metadata.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Document(collection = "metadata")
@Getter
public class Metadata {
    @Id
    private String _id;

    @NotNull
    @Field(name = "file_name")
    private String fileName;

    @NotNull
    @Field(name = "username")
    private String username;

    @Field(name = "content_type")
    private String contentType;

    @Field(name = "size_in_bytes")
    private Long sizeInBytes;

    @Field(name = "download_uri")
    private String downloadUri;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    protected Metadata() {}

    public Metadata(String fileId, String fileName, String username, String contentType, Long sizeInBytes, String downloadUri) {
        this._id = fileId;
        this.fileName = fileName;
        this.username = username;
        this.contentType = contentType;
        this.sizeInBytes = sizeInBytes;
        this.downloadUri = downloadUri;
    }
}
