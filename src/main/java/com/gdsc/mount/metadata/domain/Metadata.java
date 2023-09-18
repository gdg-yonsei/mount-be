package com.gdsc.mount.metadata.domain;

import com.gdsc.mount.common.domain.Node;
import com.gdsc.mount.common.domain.NodeType;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;

@Document(collection = "metadata")
@Getter
public class Metadata extends Node {
    @Id
    private String _id;

    @NotNull
    @Field(name = "username")
    @Indexed(unique = true)
    private String username;

    @Field(name = "content_type")
    private String contentType;

    @Field(name = "size_in_bytes")
    private Long sizeInBytes;

    @Field(name = "download_uri")
    private String downloadUri;

    protected Metadata() {}

    public Metadata(String username, String name, String parentDirectoryId, String contentType, Long sizeInBytes, String downloadUri) {
        super(NodeType.METADATA, name, parentDirectoryId);
        this.username = username;
        this.contentType = contentType;
        this.sizeInBytes = sizeInBytes;
        this.downloadUri = downloadUri;
    }
}
