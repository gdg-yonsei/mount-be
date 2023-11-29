package com.gdsc.mount.metadata.domain;

import static com.gdsc.mount.directory.service.DirectoryService.nthLastIndexOf;

import com.gdsc.mount.common.domain.TimestampEntity;
import com.gdsc.mount.directory.vo.DirectoryCreateValues;
import com.gdsc.mount.metadata.vo.MetadataCreateValues;
import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "metadata")
@Getter
public class Metadata extends TimestampEntity {
    @Id
    private String _id;

    @NotBlank
    @Field(name = "name")
    @ValidName
    private String name;

    @NotBlank
    @Field(name = "path_with_file")
    @Indexed(unique = true)
    @ValidPath
    private String pathWithFile;

    @NotBlank
    @Field(name = "path_without_file")
    @Indexed
    @ValidPath
    private String pathWithoutFile;

    @NotBlank
    @Field(name = "username")
    @Indexed(unique = true)
    @ValidName
    private String username;

    @Field(name = "content_type")
    private String contentType;

    @Field(name = "size_in_bytes")
    private Long sizeInBytes;

    protected Metadata() {}

    public Metadata(MetadataCreateValues values) {
        this._id = values.getFileCode();
        this.username = values.getUsername();
        this.contentType = values.getFile().getContentType();
        this.sizeInBytes = values.getFile().getSize();
        this.name = values.getName();
        this.pathWithFile = values.getPath() + values.getName();
        this.pathWithoutFile = values.getPath();
    }

    public Metadata(DirectoryCreateValues values) {
        this.username = values.getUsername();
        this.pathWithFile = values.getPathWithDirectory();
        int idx = nthLastIndexOf(2, "/", values.getPathWithDirectory());
        this.pathWithoutFile = values.getPathWithDirectory().substring(0, idx+1);
    }

    public void renameDirectory(String newPathIncludingDirectory, String pathWithoutFile) {
        this.pathWithFile = newPathIncludingDirectory;
        this.pathWithoutFile = pathWithoutFile;
    }


}
