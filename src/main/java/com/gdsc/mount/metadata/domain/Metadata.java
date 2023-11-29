package com.gdsc.mount.metadata.domain;

import com.gdsc.mount.common.domain.TimestampEntity;
import com.gdsc.mount.metadata.vo.CreateMetadataValues;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Document(collection = "metadata")
@Getter
public class Metadata extends TimestampEntity {
    @Id
    private String _id;

    @NotNull
    @Field(name = "name")
    private String name;

    @NotNull
    @Field(name = "path_with_file")
    private String pathWithFile;

    @NotNull
    @Field(name = "at_root")
    private boolean atRoot;

    @NotNull
    @Field(name = "username")
    @Indexed(unique = true)
    private String username;

    @Field(name = "content_type")
    private String contentType;

    @Field(name = "size_in_bytes")
    private Long sizeInBytes;

    protected Metadata() {}

    public Metadata(String _id, String name, String pathWithFile, boolean atRoot, String username, MultipartFile file) {
        this._id = _id;
        this.username = username;
        this.contentType = file.getContentType();
        this.sizeInBytes = file.getSize();
        this.name = name;
        this.pathWithFile = pathWithFile;
        this.atRoot = atRoot;
    }

    public Metadata(CreateMetadataValues values) {
        this._id = values.getFileCode();
        this.username = values.getUsername();
        this.contentType = values.getFile().getContentType();
        this.sizeInBytes = values.getFile().getSize();
        this.name = values.getName();
        this.pathWithFile = values.getPath() + values.getName();
        this.atRoot = values.isAtRoot();
    }


}
