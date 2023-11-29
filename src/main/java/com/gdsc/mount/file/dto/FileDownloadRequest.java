package com.gdsc.mount.file.dto;

import com.gdsc.mount.validation.annotation.ValidName;
import com.gdsc.mount.validation.annotation.ValidPath;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDownloadRequest {

    @NotBlank
    @ValidName
    private String username;

    @NotNull
    @ValidPath
    private String path;

    @NotBlank
    @ValidName
    private String fileName;

    protected FileDownloadRequest() {}

}
