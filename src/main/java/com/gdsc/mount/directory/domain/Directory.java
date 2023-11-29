package com.gdsc.mount.directory.domain;

import com.gdsc.mount.common.domain.TimestampEntity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "directory")
@Getter
public class Directory extends TimestampEntity {
    @Id
    private String _id;

    @NotNull
    // add validation for path so that it will always end and start with "/"
    private String pathIncludingDirectory;

    @NotBlank
    private String username;

    public Directory(String pathIncludingDirectory, String username) {
        this.pathIncludingDirectory = pathIncludingDirectory;
        this.username = username;
    }

    public void renameDirectory(String newDirectoryName) {
        this.pathIncludingDirectory = newDirectoryName;
    }

}
