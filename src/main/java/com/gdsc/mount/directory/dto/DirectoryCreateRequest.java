package com.gdsc.mount.directory.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectoryCreateRequest {

    @ValidPath
    @NotBlank
    private String path;

    @ValidName
    @NotBlank
    private String username;
}
