package com.gdsc.mount.directory.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DirectoryDeleteRequest {

    @ValidName
    @NotBlank
    private String username;

    @ValidPath
    @NotBlank
    private String pathWithDirectory;

    @ValidName
    @NotBlank
    private String directoryName;
}
