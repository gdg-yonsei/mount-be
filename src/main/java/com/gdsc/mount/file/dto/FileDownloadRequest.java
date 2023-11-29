package com.gdsc.mount.file.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDownloadRequest {

    @NotBlank
    private String username;

    @NotNull
    private String path;

    @NotBlank
    private String fileName;

    protected FileDownloadRequest() {}

}
