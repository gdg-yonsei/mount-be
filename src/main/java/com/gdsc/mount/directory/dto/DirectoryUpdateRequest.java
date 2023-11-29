package com.gdsc.mount.directory.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectoryUpdateRequest {
    @ValidPath
    @NotBlank
    private final String pathIncludingDirectory;

    @ValidName
    @NotBlank
    private final String newDirectoryName;

    @ValidName
    @NotBlank
    private final String username;
}
